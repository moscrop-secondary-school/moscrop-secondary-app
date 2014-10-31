package com.ivon.moscropsecondary.rss;

import android.content.Context;

import com.ivon.moscropsecondary.util.DateUtil;
import com.ivon.moscropsecondary.util.JsonUtil;
import com.ivon.moscropsecondary.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivon on 20/10/14.
 */
public class RSSParser {

    public static class RSSTagCriteria {

        public final String name;
        public final String author;
        public final String category;

        public RSSTagCriteria(String name, String author, String category) {
            this.name = name;
            this.author = (author.equals("@null")) ? null : author;
            this.category = (category.equals("@null")) ? null : category;
        }
    }

    public static final String NEWSLETTER_TAG = "newsletter";
    public static final String STUDENT_SUBS_TAG = "studentsubs";

    private RSSTagCriteria[] mCriteria;

    public RSSParser(String taglist_json) throws JSONException {
        this(new JSONObject(taglist_json));
    }

    public RSSParser(JSONObject taglistJsonObject) throws JSONException {
        JSONObject[] criteriaArray = JsonUtil.extractJsonArray(taglistJsonObject.getJSONArray("tags"));
        mCriteria = new RSSTagCriteria[criteriaArray.length];
        for (int i=0; i<criteriaArray.length; i++) {
            mCriteria[i] = new RSSTagCriteria(
                    criteriaArray[i].getString("name"),
                    criteriaArray[i].getString("id_author"),
                    criteriaArray[i].getString("id_category")
            );
        }
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

    // TODO All the tagging magic happens here.
    private String[] extractTags(JSONObject entryObject, String title) {

        // Get a list of categories
        List<String> categories = new ArrayList<String>();
        try {
            JSONObject[] categoryObjects = JsonUtil.extractJsonArray(entryObject.getJSONArray("category"));
            for (JSONObject o : categoryObjects) {
                categories.add(o.getString("term"));
            }
        } catch (JSONException e) {
            Logger.error("RSSParser.extractTags(): caught JSONException while parsing categories ", e);
        }

        // Get a list of authors
        List<String> authors = new ArrayList<String>();
        try {
            JSONObject[] categoryObjects = JsonUtil.extractJsonArray(entryObject.getJSONArray("author"));
            for (JSONObject o : categoryObjects) {
                authors.add(o.getJSONObject("name").getString("$t"));
            }
        } catch (JSONException e) {
            Logger.error("RSSParser.extractTags(): caught JSONException while parsing authors ", e);
        }

        // Temporary outputs
        for (String s : categories) {
            Logger.log("Title: " + title + " Category: " + s);
        }
        for (String s : authors) {
            Logger.log("Title: " + title + " Authors: " + s);
        }

        ArrayList<String> tags = new ArrayList<String>();
        for (RSSTagCriteria criteria : mCriteria) {
            Logger.log("Title: " + title + " Checking: " + criteria.name);
            if ((criteria.category != null && categories.contains(criteria.category)) || (criteria.author != null && authors.contains(criteria.author))) {
                tags.add(criteria.name);
            }
        }

        String[] tagsArray = new String[tags.size()];
        for (int i=0; i<tagsArray.length; i++) {
            tagsArray[i] = tags.get(i);
        }

        for (String tag : tagsArray) {
            Logger.log("Title: " + title + " Tags: " + tag);
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
        return "http://" + blogId + ".blogspot.ca/feeds/posts/default?alt=json";
    }

    public void parseAndSaveAll(Context context, String blogId) {

        // TODO move this override somewhere else
        blogId = "moscropschool";

        // Get the list of events from the URL
        RSSFeed feed = null;
        try {
            String url = getFeedUrlFromId(blogId);
            feed = getRssFeed(context, url);
        } catch (JSONException e) {
            Logger.error("RSSParser.parseAndSave()", e);
        }

        if (feed != null) {
            // TODO saveUpdateInfo(context, feed.version);
            RSSDatabase database = new RSSDatabase(context);
            database.deleteAll();
            database.save(feed.items);
        }
    }
}
