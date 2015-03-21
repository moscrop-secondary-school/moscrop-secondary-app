package com.moscropsecondary.official.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.moscropsecondary.official.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivon on 27/10/14.
 */
public class ThemesUtil {

    /** Themes Section */

    public static final int THEME_LIGHT         = 0;
    public static final int THEME_DARK          = 1;
    public static final int THEME_BLACK         = 2;
    public static final int THEME_TRANSPARENT   = 3;
    
    public static final int THEME_TYPE_NORMAL   = 0;
    public static final int THEME_TYPE_DETAIL   = 1;
    public static final int THEME_TYPE_DRAWER   = 2;

    public static int getThemeResFromPreference(Context context, int themeType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String s = prefs.getString(Preferences.Keys.THEME, Preferences.Default.THEME);
        try {
            int i = Integer.parseInt(s);

            switch(themeType) {

                case THEME_TYPE_DETAIL:
                {
                    switch (i) {
                        case THEME_LIGHT:
                            return R.style.Theme_Light_Detail;
                        case THEME_DARK:
                            return R.style.Theme_Dark_Detail;
                        case THEME_BLACK:
                            return R.style.Theme_Black_Detail;
                        case THEME_TRANSPARENT:
                            return R.style.Theme_Transparent_Detail;
                        default:
                            return R.style.Theme_Light_Detail;
                    }
                }
                
                case THEME_TYPE_DRAWER: 
                {
                    switch (i) {
                        case THEME_LIGHT:
                            return R.style.Theme_Light_Drawer;
                        case THEME_DARK:
                            return R.style.Theme_Dark_Drawer;
                        case THEME_BLACK:
                            return R.style.Theme_Black_Drawer;
                        case THEME_TRANSPARENT:
                            return R.style.Theme_Transparent_Drawer;
                        default:
                            return R.style.Theme_Light_Drawer;
                    }
                }

                default:    // default to THEME_TYPE_NORMAL
                {
                    switch (i) {
                        case THEME_LIGHT:
                            return R.style.Theme_Light;
                        case THEME_DARK:
                            return R.style.Theme_Dark;
                        case THEME_BLACK:
                            return R.style.Theme_Black;
                        case THEME_TRANSPARENT:
                            return R.style.Theme_Transparent;
                        default:
                            return R.style.Theme_Light;
                    }
                }
            }
            
            
        } catch(NumberFormatException e) {
            e.printStackTrace();
            switch(themeType) {
                case THEME_TYPE_DETAIL:
                    return R.style.Theme_Light_Detail;
                case THEME_TYPE_DRAWER:
                    return R.style.Theme_Light_Drawer;
                default:
                    return R.style.Theme_Light;
            }
        }
    }

    public static boolean isDarkTheme(Context context) {
        int theme = getThemeResFromPreference(context, THEME_TYPE_NORMAL);

        switch (theme) {
            case R.style.Theme_Light:
                return false;
            case R.style.Theme_Dark:
            case R.style.Theme_Black:
            case R.style.Theme_Transparent:
                return true;
            default:
                return false;
        }
    }

    public static int getThemePrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.status, typedValue, true);
        return typedValue.data;
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
