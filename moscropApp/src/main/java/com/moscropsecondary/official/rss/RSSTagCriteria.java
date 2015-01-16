package com.moscropsecondary.official.rss;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moscropsecondary.official.util.JsonUtil;
import com.moscropsecondary.official.util.Logger;
import com.moscropsecondary.official.util.Preferences;
import com.moscropsecondary.official.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by ivon on 10/31/14.
 */
public class RSSTagCriteria {

    public static final String TAG_LIST_JSON = "taglist.json";
    public static final String TAG_LIST_URL = "http://pastebin.com/raw.php?i=dMePcZ9e";

    public static final String NO_IMAGE = "no image";

    public final String name;
    public final String author;
    public final String category;
    public final String imageUrl;

    public RSSTagCriteria(String name, String author, String category, String imageUrl) {
        this.name = name;
        this.author = (author.equals("@null")) ? null : author;
        this.category = (category.equals("@null")) ? null : category;
        this.imageUrl = (imageUrl.equals("@null")) ? NO_IMAGE : imageUrl;
    }

    public static File getTagListFile(Context context) {
        return new File(context.getFilesDir(), TAG_LIST_JSON);
    }

    public static JSONObject getTagListJsonObject(Context context) throws JSONException, IOException {

        File file = getTagListFile(context);
        if (!file.exists()) {
            RSSTagCriteria.copyTagListFromAssetsToStorage(context);
        }

        return JsonUtil.getJsonObjectFromFile(file);
    }

    public static RSSTagCriteria[] getCriteriaList(Context context) throws JSONException, IOException {
        JSONObject tagListJsonObject = getTagListJsonObject(context);
        JSONObject[] criteriaJSONArray = JsonUtil.extractJsonArray(tagListJsonObject.getJSONArray("tags"));
        RSSTagCriteria[] criteriaList = new RSSTagCriteria[criteriaJSONArray.length];
        for (int i=0; i<criteriaJSONArray.length; i++) {
            criteriaList[i] = new RSSTagCriteria(
                    criteriaJSONArray[i].getString("name"),
                    criteriaJSONArray[i].getString("id_author"),
                    criteriaJSONArray[i].getString("id_category"),
                    criteriaJSONArray[i].getString("icon_img")
            );
        }
        return criteriaList;
    }

    public static String[] getTagNames(Context context) throws IOException, JSONException {
        JSONObject tagListJsonObject = getTagListJsonObject(context);
        JSONObject[] criteriaJSONArray = JsonUtil.extractJsonArray(tagListJsonObject.getJSONArray("tags"));

        List<String> names = new ArrayList<String>();
        for (JSONObject criteriaObject : criteriaJSONArray) {
            if (!criteriaObject.getString("name").equals("Student Bulletin")) {
                names.add(criteriaObject.getString("name"));
            }
        }

        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.equals("Official")) {
                    return Integer.MIN_VALUE;
                } else if (s2.equals("Official")) {
                    return Integer.MAX_VALUE;
                } else {
                    return s1.compareToIgnoreCase(s2);
                }
            }
        });

        String[] allTags = new String[names.size()];
        for (int i=0; i<allTags.length; i++) {
            allTags[i] = names.get(i);
        }

        return allTags;
    }

    public static String[] getSubscribedTags(Context context) throws IOException, JSONException {

        // Get a list of recognized tags
        String[] tagNamesArray = getTagNames(context);

        // Get a list of tags the user subscribed to
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> existingSelectedValuesSet = prefs.getStringSet(Preferences.Keys.TAGS, Preferences.Default.TAGS);
        String[] existingSelectedValues = existingSelectedValuesSet.toArray(new String[existingSelectedValuesSet.size()]);

        List<String> validatedSelectedValues = new ArrayList<String>();
        for (String value : existingSelectedValues) {
            if (Arrays.asList(tagNamesArray).contains(value)) {
                validatedSelectedValues.add(value);
            }
        }

        Collections.sort(validatedSelectedValues, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.equals("Official")) {
                    return Integer.MIN_VALUE;
                } else if (s2.equals("Official")) {
                    return Integer.MAX_VALUE;
                } else {
                    return s1.compareToIgnoreCase(s2);
                }
            }
        });

        String[] subscribedTags = new String[validatedSelectedValues.size()];
        for (int i=0; i<subscribedTags.length; i++) {
            subscribedTags[i] = validatedSelectedValues.get(i);
        }

        return subscribedTags;
    }

    public static void copyTagListFromAssetsToStorage(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open(TAG_LIST_JSON);
        File file = new File(context.getFilesDir(), TAG_LIST_JSON);
        OutputStream outputStream = new FileOutputStream(file);
        Util.copy(inputStream, outputStream);
    }

    public static void downloadTagListToStorage(Context context) throws IOException {
        if (Util.isConnected(context)) {

            // Create HttpGet
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(TAG_LIST_URL);

            // Make sure status is OK
            HttpResponse response = httpclient.execute(httpGet);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HttpStatus.SC_OK) {
                Logger.log("Status code", status.getStatusCode());
                Logger.log("Reason", status.getReasonPhrase());
            }

            // Read response to InputStream
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();

            // Create OutputStream
            File file = new File(context.getFilesDir(), TAG_LIST_JSON);
            OutputStream outputStream = new FileOutputStream(file);

            // Copy data from HTTP InputStream to file OutputStream
            Util.copy(inputStream, outputStream);

            try {
                // Check if it is a newer version
                SharedPreferences prefs = context.getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
                String oldVersion = prefs.getString(Preferences.App.Keys.TAG_LIST_VERSION, Preferences.App.Default.TAG_LIST_VERSION);
                JSONObject tagListObject = getTagListJsonObject(context);
                String newVersion = tagListObject.getString("updated");
                if (!newVersion.equals(oldVersion)) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Preferences.App.Keys.RSS_VERSION, "update required");
                    editor.putString(Preferences.App.Keys.TAG_LIST_VERSION, newVersion);
                    editor.commit();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
