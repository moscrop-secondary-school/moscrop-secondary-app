package com.moscrop.official.util;

import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ivon on 9/2/14.
 */
public class JsonUtil {

    public static JSONObject getJsonObjectFromUrl(Context context, String url) throws JSONException {

        JSONObject resultObj = null;

        if (Util.isConnected(context)) {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            InputStream inputStream = null;
            String resultStr = null;
            try {

                // Make sure status is OK
                HttpResponse response = httpclient.execute(httpGet);
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    Logger.log("Status code", status.getStatusCode());
                    Logger.log("Reason", status.getReasonPhrase());
                    return null;
                }

                // Read response
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();

                // Log downloaded data size
                inputStream = countBytes(inputStream);

                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                // Build input stream into response string
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                resultStr = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            resultObj = new JSONObject(resultStr);
        }

        return resultObj;
    }

    private static byte[] toByteArray(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toByteArray();
    }

    private static InputStream countBytes(InputStream is) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            int count = 0;
            while ((len = is.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
                count++;
            }
            baos.flush();

            InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

            byte[] bytes = toByteArray(is1);
            Logger.log("Downloaded " + bytes.length + " bytes of data");


            return is2;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return is;
    }

    public static JSONObject getJsonObjectFromFile(File file) throws JSONException, IOException {
        String s = Util.readFile(file);
        return new JSONObject(s);
    }

    public static JSONObject[] extractJsonArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray != null) {
            JSONObject objects[] = new JSONObject[jsonArray.length()];
            for (int i=0; i<jsonArray.length(); i++) {
                objects[i] = jsonArray.getJSONObject(i);
            }
            return objects;
        } else {
            return null;
        }
    }
}
