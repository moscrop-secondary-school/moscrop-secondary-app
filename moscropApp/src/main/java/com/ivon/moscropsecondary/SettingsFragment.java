package com.ivon.moscropsecondary;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.ivon.moscropsecondary.util.Preferences;
import com.ivon.moscropsecondary.util.ThemesUtil;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference theme = findPreference(Preferences.Keys.THEME);
        theme.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int titleRes = preference.getTitleRes();
        if(titleRes == R.string.theme_title) {
            ThemesUtil.notifyThemeChanged();
            Toast.makeText(getActivity(), "Theme will change when you exit settings", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
