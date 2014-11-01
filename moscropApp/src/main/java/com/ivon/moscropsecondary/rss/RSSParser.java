package com.ivon.moscropsecondary.rss;

import android.content.Context;
import android.content.SharedPreferences;

import com.ivon.moscropsecondary.util.DateUtil;
import com.ivon.moscropsecondary.util.JsonUtil;
import com.ivon.moscropsecondary.util.Logger;
import com.ivon.moscropsecondary.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ivon on 20/10/14.
 */
public class RSSParser {

    public static final String NEWSLETTER_TAG = "newsletter";
    public static final String STUDENT_SUBS_TAG = "studentsubs";

    private Context mContext;
    private RSSTagCriteria[] mCriteria;

    public RSSParser(Context context) throws JSONException, IOException {
        mContext = context;
        mCriteria = RSSTagCriteria.getCriteriaList(context);
    }
    private String getUpdatedTimeFromJsonObject(JSONObject jsonObject) throws JSONException {
        JSONObject feed = jsonObject.getJSONObject("feed");
        JSONObject updated = feed.getJSONObject("updated");
        return updated.getString("$t");
    }

    private List<RSSItem> getEntriesListFromJsonObject(JSONObject jsonObject) throws JSONException {
        JSONObject feed = jsonObject.getJSONObject("feed");
        JSONObject updated = feed.getJSONObject("updated");
        JSONArray entry = feed.getJSONArray("entry");
        JSONObject entryObjects[] = JsonUtil.extractJsonArray(entry);
        if (entryObjects == null) {
            return null;
        }

        List<RSSItem> items = new ArrayList<RSSItem>();
        for (JSONObject entryObject : entryObjects) {
            RSSItem item = entryObjectToRssItem(entryObject);
            if(item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private RSSItem entryObjectToRssItem(JSONObject entryObject) throws JSONException {

        String dateStr = entryObject.getJSONObject("published").getString("$t");
        long date = DateUtil.parseRCF339Date(dateStr).getTime();
        String title = entryObject.getJSONObject("title").getString("$t");
        String content = entryObject.getJSONObject("content").getString("$t");

        JSONObject[] links = JsonUtil.extractJsonArray(entryObject.getJSONArray("link"));
        String url = "";
        for (JSONObject link : links) {
            String type = link.getString("rel");
            if (type.equals("alternate")) {
                url = link.getString("href");
                break;
            }
        }

        String[] tags = extractTags(entryObject, title);

        return new RSSItem(date, title, content, tags, url);
    }

    private String[] extractTags(JSONObject entryObject, String title) {

        // Get a list of categories
        List<String> categories = new ArrayList<String>();
        try {
            JSONObject[] categoryObjects = JsonUtil.extractJsonArray(entryObject.getJSONArray("category"));
            for (JSONObject o : categoryObjects) {
                categories.add(o.getString("term"));
            }
        } catch (JSONException e) {
            Logger.error("RSSParser.extractTags(): caught JSONException while parsing categories for " + title, e);
        }

        // Get a list of authors
        List<String> authors = new ArrayList<String>();
        try {
            JSONObject[] categoryObjects = JsonUtil.extractJsonArray(entryObject.getJSONArray("author"));
            for (JSONObject o : categoryObjects) {
                authors.add(o.getJSONObject("name").getString("$t"));
            }
        } catch (JSONException e) {
            Logger.error("RSSParser.extractTags(): caught JSONException while parsing authors for " + title, e);
        }

        // Check if it matches criteria
        ArrayList<String> tags = new ArrayList<String>();
        for (RSSTagCriteria criteria : mCriteria) {
            if ((criteria.category != null && categories.contains(criteria.category)) || (criteria.author != null && authors.contains(criteria.author))) {
                tags.add(criteria.name);
            }
        }

        // Convert list to array
        String[] tagsArray = new String[tags.size()];
        for (int i=0; i<tagsArray.length; i++) {
            tagsArray[i] = tags.get(i);
        }

        return tagsArray;
    }

    private RSSFeed getRssFeed(Context context, String url) throws JSONException {
        JSONObject jsonObject = JsonUtil.getJsonObjectFromUrl(context, url);
        if (jsonObject != null) {
            String timestamp = getUpdatedTimeFromJsonObject(jsonObject);
            List<RSSItem> items = getEntriesListFromJsonObject(jsonObject);
            return new RSSFeed(timestamp, items);
        } else {
            return null;
        }
    }

    private String getFeedUrlFromId(String blogId) {
        return "http://" + blogId + ".blogspot.ca/feeds/posts/default?alt=json&max-results=1000";
    }

    private void saveUpdateInfo(String gcalVersion) {
        SharedPreferences.Editor prefs = mContext.getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS).edit();
        prefs.putLong(Preferences.App.Keys.RSS_LAST_UPDATED, System.currentTimeMillis());
        prefs.putString(Preferences.App.Keys.RSS_VERSION, gcalVersion);
        prefs.apply();
    }

    /**
     * Download, parse, and store data from a Blogger RSS feed. This method
     * will load all data from the whole calendar. Unlike parseAndSave(String, long, String),
     * this method does not check for RSS version and will disregard any previously
     * saved data. This method will delete all previously saved data and replace it
     * with freshly downloaded data. Because this takes a long time
     * and is often unnecessary, it is only recommended to use this method
     * when loading for the first time. Afterwards it is recommeneded to keep
     * track of updates and use parseAndSave(String, long, String).
     *
     * @param blogId
     *      ID of the Blogger RSS feed
     */
    public void parseAndSaveAll(String blogId) {

        Logger.log("Processing all");

        // Get the list of events from the URL
        RSSFeed feed = null;
        try {
            String url = getFeedUrlFromId(blogId);
            feed = getRssFeed(mContext, url);
        } catch (JSONException e) {
            Logger.error("RSSParser.parseAndSave()", e);
        }

        if (feed != null) {
            saveUpdateInfo(feed.version);
            RSSDatabase database = new RSSDatabase(mContext);
            database.deleteAll();
            database.save(feed.items);
        }
    }

    /**
     * Selectively download, parse, and store data from a Blogger RSS feed
     *
     * @param blogId
     *      ID of the Blogger RSS feed
     * @param publishedMin
     *      Matches published-min query parameter of Google Calendar API.
     *      Only process RSS items with publish dates after this param.
     *      Given in milliseconds.
     * @param lastFeedVersion
     *      "Updated" string from the last processed Blogger RSS feed. Used to version
     *      Blogger RSS feed and determine if it is necessary to go through with processing.
     *      After all, if it's the same version, no need to do all that work again!
     *      Usually of the format "2014-09-09T12:21:08.000Z"
     */
    public void parseAndSave(String blogId, long publishedMin, String lastFeedVersion) {

        Logger.log("Processing selectively");

        RSSFeed feed = null;
        try {
            String url = getFeedUrlFromId(blogId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String publishedMinStr = sdf.format(new Date(publishedMin));
            url = url + "&published-min=" + publishedMinStr;

            feed = getRssFeed(mContext, url);
        } catch (JSONException e) {
            Logger.error("RSSParser.parseAndSave()", e);
        }

        if (feed != null) {

            // We just updated, so update records
            // with current time and the version we
            // just downloaded regardless of whether
            // updating the database was needed

            saveUpdateInfo(feed.version);

            String newFeedVersion = feed.version;
            if (!newFeedVersion.equals(lastFeedVersion)) {

                // There has been changes! We must update!
                //
                // The maintainer of the calendar probably
                // won't make changes to events that have
                // already past. Therefore we only need to
                // update events that begin after the last
                // update time.
                //
                // Begin by deleting those events

                RSSDatabase database = new RSSDatabase(mContext);
                int deleted = database.deleteIfPublishedAfter(publishedMin);

                if (deleted != feed.items.size()) {
                    Logger.warn("Processing calendar events: deleted "
                                    + deleted + " events from database, but only inserting "
                                    + feed.items.size() + " new events."
                    );
                }

                // Our list of events will already only consist
                // of events that begin after startMin, so no
                // overlapping will occur. We can save normally.

                database.save(feed.items);

            } else {
                Logger.log("Existing version is already up to date.");
            }
        }
    }
}
