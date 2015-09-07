package com.moscrop.official.rss;

import android.content.Context;
import android.content.SharedPreferences;

import com.moscrop.official.util.JsonUtil;
import com.moscrop.official.util.Preferences;
import com.moscrop.official.util.Util;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Owner on 9/6/2015.
 */
public class ParseCacheTracker  {

    /**
     * If the user asks for a post again within
     * this time, retrieve from cache.
     */
    private static final long ONLINE_CACHE_AGE_THRESHOLD = 10*60*1000;  // 10 minutes

    public static ParseQuery.CachePolicy getCachePolicy(Context context, String id) {

        // If offline, retrieve from cache
        if (!Util.isConnected(context)) {
            return ParseQuery.CachePolicy.CACHE_ONLY;
        }

        try {

            SharedPreferences prefs = context.getSharedPreferences(Preferences.ParseCacheTracker.NAME, Context.MODE_MULTI_PROCESS);
            String cacheListStr = prefs.getString(Preferences.ParseCacheTracker.Keys.PARSE_CACHE_TRACKER, Preferences.ParseCacheTracker.Default.PARSE_CACHE_TRACKER);
            JSONObject jsonObject = new JSONObject(cacheListStr);
            JSONArray jsonArray = jsonObject.getJSONArray("cacheList");
            JSONObject[] cacheArray = JsonUtil.extractJsonArray(jsonArray);

            long now = System.currentTimeMillis();
            long limit = now -  ONLINE_CACHE_AGE_THRESHOLD;

            for (int i=cacheArray.length-1; i>=0; i--) {
                JSONObject cache = cacheArray[i];
                if (cache.getLong("timestamp") > limit) {
                    if (cache.getString("id").equals(id)) {
                        // We found it. There exists a cache
                        // of this post that is relatively new.
                        return ParseQuery.CachePolicy.CACHE_ONLY;
                    }
                } else {
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // We are online, and there is no cached post we can use
        return ParseQuery.CachePolicy.NETWORK_ONLY;
    }

    public static void addCache(Context context, long timestamp, String id) throws JSONException {

        /**
         * The list of caches is sorted by timestamp in ascending order.
         * E.g. the first post is the oldest, and the last post is the newest.
         */

        SharedPreferences prefs = context.getSharedPreferences(Preferences.ParseCacheTracker.NAME, Context.MODE_MULTI_PROCESS);
        String cacheListStr = prefs.getString(Preferences.ParseCacheTracker.Keys.PARSE_CACHE_TRACKER, Preferences.ParseCacheTracker.Default.PARSE_CACHE_TRACKER);
        JSONObject jsonObject = new JSONObject(cacheListStr);
        JSONArray jsonArray = jsonObject.getJSONArray("cacheList");
        JSONObject[] cacheArray = JsonUtil.extractJsonArray(jsonArray);
        List<JSONObject> cacheList = new ArrayList<>(Arrays.asList(cacheArray));

        // Check if the item is already cached
        int alreadyStoredInPosition = -1;
        for (int i=0; i<cacheList.size(); i++) {
            JSONObject cache = cacheList.get(i);
            if (cache.getString("id").equals(id)) {
                // Already cached. Simply update timestamp.
                cache.put("timestamp", timestamp);
                alreadyStoredInPosition = i;
            }
        }

        if (alreadyStoredInPosition != -1) {
            JSONObject cache = cacheList.remove(alreadyStoredInPosition);
            cacheList.add(cache);
        } else {
            // If item is not yet cached, add a new cache entry
            JSONObject cache = new JSONObject(String.format("{timestamp:%d,id:%s}", timestamp, id));
            cacheList.add(cache);

            // Save maximum of 1000 caches
            // Remove the oldest caches first
            while (cacheList.size() > 1000) {   // TODO Get number of posts to cache from preferences
                JSONObject deletedCache = cacheList.remove(0);    // Delete the tracker reference
                deleteCache(deletedCache);
            }
        }

        // Write new cache list into JSONObject
        jsonArray = new JSONArray();
        for (JSONObject cache : cacheList) {
            jsonArray.put(cache);
        }
        jsonObject.put("cacheList", jsonArray);

        // Save JSONObject to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Preferences.ParseCacheTracker.Keys.PARSE_CACHE_TRACKER, jsonObject.toString());
        editor.apply();
    }

    private static void deleteCache(JSONObject deletedCache) throws JSONException {
        String id = deletedCache.getString("id");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("BlogPosts");
        query.selectKeys(Arrays.asList("content"));
        query.whereEqualTo("objectId", id);
        query.clearCachedResult();
    }
}
