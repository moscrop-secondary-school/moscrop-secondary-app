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

    private static final String NEWSLETTER_TAG = "newsletter";
    private static final String STUDENT_SUBS_TAG = "studentsubs";

    private static String getUpdatedTimeFromJsonObject(JSONObject jsonObject) throws JSONException {
        JSONObject feed = jsonObject.getJSONObject("feed");
        JSONObject updated = feed.getJSONObject("updated");
        return updated.getString("$t");
    }

    private static List<RSSItem> getEntriesListFromJsonObject(JSONObject jsonObject) throws JSONException {
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

    private static RSSItem entryObjectToRssItem(JSONObject entryObject) throws JSONException {

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

        String[] tags = extractTags(entryObject, url);

        return new RSSItem(date, title, content, tags, url);
    }

    private static String[] extractTags(JSONObject entryObject, String url) throws JSONException {
        if (url.contains("moscropnewsletters.blogspot.ca")) {
            return new String[] { NEWSLETTER_TAG };
        } else if (url.contains("moscropstudents.blogspot.ca")) {
            return new String[] { STUDENT_SUBS_TAG };
        } else {
            String[] tags;
            try {
                JSONObject[] categories = JsonUtil.extractJsonArray(entryObject.getJSONArray("category"));
                 tags = new String[categories.length];
                for (int i = 0; i < categories.length; i++) {
                    tags[i] = categories[i].getString("term");
                }
            } catch (JSONException e) {
                Logger.error("RSSParser.extractTags(): caught JSONException ", e);
                tags = new String[] { "" };
            }
            return tags;
        }
    }

    private static RSSFeed getRssFeed(Context context, String url) throws JSONException {
        JSONObject jsonObject = JsonUtil.getJsonObjectFromUrl(context, url);
        if (jsonObject != null) {
            String timestamp = getUpdatedTimeFromJsonObject(jsonObject);
            List<RSSItem> items = getEntriesListFromJsonObject(jsonObject);
            return new RSSFeed(timestamp, items);
        } else {
            return null;
        }
    }

    private static String getFeedUrlFromId(String blogId) {
        return "http://" + blogId + ".blogspot.ca/feeds/posts/default?alt=json";
    }

    public static void parseAndSaveAll(Context context, String blogId) {

        // Get the list of events from the URL
        RSSFeed feed = null;
        try {
            String url = getFeedUrlFromId(blogId);
            feed = getRssFeed(context, url);
        } catch (JSONException e) {
            Logger.error("RSSParser.parseAndSave()", e);
        }

        if (feed != null) {
            Logger.log("parseAndSaveAll(): feed is not null!");
            // TODO saveUpdateInfo(context, feed.version);
            RSSDatabase database = new RSSDatabase(context);
            database.deleteAll();
            database.save(feed.items);
        } else {
            Logger.log("parseAndSaveAll(): feed is null!!!!!!!");
        }
    }
}
