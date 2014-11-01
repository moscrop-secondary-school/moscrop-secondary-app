package com.ivon.moscropsecondary.rss;

import android.content.Context;

import com.ivon.moscropsecondary.util.JsonUtil;
import com.ivon.moscropsecondary.util.Logger;
import com.ivon.moscropsecondary.util.Util;

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

/**
 * Created by ivon on 10/31/14.
 */
public class RSSTagCriteria {

    public static final String TAG_LIST_JSON = "taglist.json";
    public static final String TAG_LIST_URL = "http://pastebin.com/raw.php?i=dMePcZ9e";

    public final String name;
    public final String author;
    public final String category;

    public RSSTagCriteria(String name, String author, String category) {
        this.name = name;
        this.author = (author.equals("@null")) ? null : author;
        this.category = (category.equals("@null")) ? null : category;
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
                    criteriaJSONArray[i].getString("id_category")
            );
        }
        return criteriaList;
    }

    public static String[] getTagNames(Context context) throws IOException, JSONException {
        JSONObject tagListJsonObject = getTagListJsonObject(context);
        JSONObject[] criteriaJSONArray = JsonUtil.extractJsonArray(tagListJsonObject.getJSONArray("tags"));
        String[] names = new String[criteriaJSONArray.length];
        for (int i=0; i<criteriaJSONArray.length; i++) {
            names[i] = criteriaJSONArray[i].getString("name");
        }
        return names;
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
        }
    }
}
