package com.moscropsecondary.official.rss;

import android.content.Context;
import android.content.SharedPreferences;

import com.moscropsecondary.official.util.DateUtil;
import com.moscropsecondary.official.util.JsonUtil;
import com.moscropsecondary.official.util.Logger;
import com.moscropsecondary.official.util.Preferences;

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
            //Logger.error("RSSParser.extractTags(): caught JSONException while parsing categories for " + title, e);
        }

        // Get a list of authors
        List<String> authors = new ArrayList<String>();
        try {
            JSONObject[] categoryObjects = JsonUtil.extractJsonArray(entryObject.getJSONArray("author"));
            for (JSONObject o : categoryObjects) {
                authors.add(o.getJSONObject("name").getString("$t"));
            }
        } catch (JSONException e) {
            //Logger.error("RSSParser.extractTags(): caught JSONException while parsing authors for " + title, e);
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
        return "http://" + blogId + ".blogspot.ca/feeds/posts/default?alt=json&max-results=25";
    }

    private void saveUpdateInfo(String gcalVersion) {
        SharedPreferences.Editor prefs = mContext.getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS).edit();
        prefs.putLong(Preferences.App.Keys.RSS_LAST_UPDATED, System.currentTimeMillis());
        prefs.putString(Preferences.App.Keys.RSS_VERSION, gcalVersion);
        prefs.apply();
    }

    public void parseAndSave(String blogId, String lastFeedVersion, boolean append) {

        if (!append) {

            // Get the list of events from the URL
            RSSFeed feed = null;
            try {
                String url = getFeedUrlFromId(blogId);
                feed = getRssFeed(mContext, url);
            } catch (JSONException e) {
                //Logger.error("RSSParser.parseAndSave()", e);
            }

            if (feed != null) {

                // We just updated, so update records
                // with current time and the version we
                // just downloaded regardless of whether
                // updating the database was needed

                //saveUpdateInfo(feed.version);

                String newFeedVersion = feed.version;
                if (!newFeedVersion.equals(lastFeedVersion)) {

                    // New version. All our cache is invalid.
                    // Exterminate! Exterminate the cache!
                    // Of course, then replace with new data.

                    RSSDatabase database = new RSSDatabase(mContext);
                    database.deleteAll();
                    database.save(feed.items);
                    database.close();
                }
            }

        } else {

            RSSFeed feed = null;
            RSSDatabase database = new RSSDatabase(mContext);
            try {
                String url = getFeedUrlFromId(blogId);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                String publishedMaxStr = sdf.format(new Date(database.getOldestPostDate(null)));
                url = url + "&published-max=" + publishedMaxStr;
                feed = getRssFeed(mContext, url);
            } catch (JSONException e) {
                //Logger.error("RSSParser.parseAndSave()", e);
            }

            if (feed != null) {
                // We just updated, so update records
                // with current time and the version we
                // just downloaded regardless of whether
                // updating the database was needed

                //saveUpdateInfo(feed.version);


                // New version. All our cache is invalid.
                // Exterminate! Exterminate the cache!
                // Of course, then replace with new data.

                database.save(feed.items);
            }
            database.close();
        }

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
            //Logger.error("RSSParser.parseAndSave()", e);
        }

        if (feed != null) {
            saveUpdateInfo(feed.version);
            RSSDatabase database = new RSSDatabase(mContext);
            database.deleteAll();
            database.save(feed.items);
            database.close();
        }
    }
}
