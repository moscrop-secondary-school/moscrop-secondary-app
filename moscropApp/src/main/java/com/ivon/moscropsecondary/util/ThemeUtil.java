package com.ivon.moscropsecondary.util;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.MainActivity;
import com.ivon.moscropsecondary.MoscropBaseActivity;

public final class ThemeUtil {
	private ThemeUtil() {};

	private static int[] THEMES = new int[] {
		R.style.CardLight,
		R.style.CardBlack,
		R.style.CardTBlack,
	};

	public static int getSelectTheme() {
		int theme = MainActivity.getPreferences().getInt("theme", 0);
		return (theme >= 0 && theme < THEMES.length) ? theme : 0;
	}

	public static void setTheme(MoscropBaseActivity activity) {
		activity.mTheme = getSelectTheme();
		activity.setTheme(THEMES[activity.mTheme]);
	}

	public static void reloadTheme(MoscropBaseActivity activity) {
		int theme = getSelectTheme();
		if (theme != activity.mTheme)
			activity.recreate();
	}

	public static int getThemeColor(Context context, int id) {
		Theme theme = context.getTheme();
		TypedArray a = theme.obtainStyledAttributes(new int[] {id});
		int result = a.getColor(0, 0);
		a.recycle();
		return result;
	}
}
