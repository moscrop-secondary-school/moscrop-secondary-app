package com.ivon.moscropsecondary.rss;

import android.content.Context;

import com.ivon.moscropsecondary.util.Logger;
import com.ivon.moscropsecondary.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
