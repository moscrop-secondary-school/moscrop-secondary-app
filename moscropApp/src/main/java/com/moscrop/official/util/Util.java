package com.moscrop.official.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

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

    public static final int CONNECTION_TYPE_DATA = 0;
    public static final int CONNECTION_TYPE_WIFI = 1;
    public static final int CONNECTION_TYPE_NONE = 2;

    public static boolean isConnected(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(Preferences.Keys.LOAD_ON_WIFI_ONLY, Preferences.Default.LOAD_ON_WIFI_ONLY)) {
            return getConnectionType(context) == CONNECTION_TYPE_WIFI;
        } else {
            return getConnectionType(context) != CONNECTION_TYPE_NONE;
        }
    }

    public static int getConnectionType(Context context) {

        if(context == null)
            return CONNECTION_TYPE_NONE;

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return CONNECTION_TYPE_WIFI;
            } else {
                return CONNECTION_TYPE_DATA;
            }
        } else {
            return CONNECTION_TYPE_NONE;
        }
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context)
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);

        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context)
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);

        return dp;
    }
    public static Uri resToUri(Context context, int resId)
    {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
    }

    public static String getApacheLicense()
    {
        return getApacheLicense(null);
    }

    public static String getApacheLicense(String header)
    {
        StringBuilder builder = new StringBuilder();

        if(header != null)
            builder.append(header);
        builder.append("Licensed under the Apache License, Version 2.0 (the \"License\");");
        builder.append("you may not use this file except in compliance with the License.");
        builder.append("You may obtain a copy of the License at\n\n");
        builder.append("   http://www.apache.org/licenses/LICENSE-2.0\n\n");
        builder.append("Unless required by applicable law or agreed to in writing, software");
        builder.append("distributed under the License is distributed on an \"AS IS\" BASIS,");
        builder.append("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        builder.append("See the License for the specific language governing permissions and");
        builder.append("limitations under the License.");

        return builder.toString();
    }

    public static String getMITLicense()
    {
        return getMITLicense(null);
    }

    public static String getMITLicense(String header)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("The MIT License (MIT)\n\n");
        if(header != null)
            builder.append(header);
        builder.append("Permission is hereby granted, free of charge, to any person obtaining a copy");
        builder.append("of this software and associated documentation files (the \"Software\"), to deal");
        builder.append("in the Software without restriction, including without limitation the rights");
        builder.append("to use, copy, modify, merge, publish, distribute, sublicense, and/or sell");
        builder.append("copies of the Software, and to permit persons to whom the Software is");
        builder.append("furnished to do so, subject to the following conditions:\n\n");
        builder.append("The above copyright notice and this permission notice shall be included in all");
        builder.append("copies or substantial portions of the Software.\n\n");
        builder.append("THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR");
        builder.append("IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,");
        builder.append("FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE");
        builder.append("AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER");
        builder.append("LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,");
        builder.append("OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE");
        builder.append("SOFTWARE.");

        return builder.toString();
    }
}
