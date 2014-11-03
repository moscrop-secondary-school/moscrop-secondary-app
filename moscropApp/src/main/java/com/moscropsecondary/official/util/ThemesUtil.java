package com.moscropsecondary.official.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.moscropsecondary.official.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivon on 27/10/14.
 */
public class ThemesUtil {

    /** Themes Section */

    public static final int THEME_LIGHT         = 0;
    public static final int THEME_BLACK         = 1;
    public static final int THEME_TRANSPARENT   = 2;

    public static int getThemeResFromPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String s = prefs.getString(Preferences.Keys.THEME, Preferences.Default.THEME);
        try {
            int i = Integer.parseInt(s);

            switch (i) {
                case THEME_LIGHT:
                    return R.style.Theme_Light;
                case THEME_BLACK:
                    return R.style.Theme_Black;
                case THEME_TRANSPARENT:
                    return R.style.Theme_Transparent;
                default:
                    return R.style.Theme_Light;
            }
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return R.style.Theme_Light;
        }
    }

    public interface ThemeChangedListener {
        public abstract void onThemeChanged();
    }

    private static List<ThemeChangedListener> mListeners = new ArrayList<ThemeChangedListener>();

    public static void registerThemeChangedListener(ThemeChangedListener listener) {
        mListeners.add(listener);
    }

    public static void unregisterThemeChangedListeners(ThemeChangedListener listener) {
        mListeners.remove(listener);
    }

    public static void notifyThemeChanged() {
        for(ThemeChangedListener l : mListeners) {
            if(l != null) {
                l.onThemeChanged();
            }
        }
    }
}
