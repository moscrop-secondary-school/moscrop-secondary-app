package com.ivon.moscropsecondary;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
public class SettingsFragment extends PreferenceFragment {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference prefTheme = findPreference("theme");
        prefTheme.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().recreate();
                return true;
            }
        });
    }
}
