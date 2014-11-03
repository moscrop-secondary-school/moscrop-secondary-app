package com.moscropsecondary.official.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by ivon on 10/31/14.
 */
public class Util {

    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer))>0) {
            output.write(buffer, 0, length);
        }
    }

    public static String readFile(File file) throws IOException {
        if (file == null) {
            return "";
        }

        InputStream inputSteam = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputSteam));
        StringBuilder sb = new StringBuilder();

        String line = "";

        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        reader.close();
        return sb.toString();
    }

    public static boolean isConnected(Context context) {
        if(context == null)
            return false;

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
