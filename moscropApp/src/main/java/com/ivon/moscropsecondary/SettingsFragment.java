package com.ivon.moscropsecondary;

import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.ivon.moscropsecondary.rss.RSSTagCriteria;
import com.ivon.moscropsecondary.util.Preferences;
import com.ivon.moscropsecondary.util.ThemesUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference theme = findPreference(Preferences.Keys.THEME);
        theme.setOnPreferenceChangeListener(this);

        MultiSelectListPreference tagChooser = (MultiSelectListPreference) findPreference(Preferences.Keys.TAGS);
        String[] tagNamesArray;
        try {
            tagNamesArray = RSSTagCriteria.getTagNames(getActivity());
        } catch (IOException e) {
            tagNamesArray = new String[] {};
        } catch (JSONException e) {
            tagNamesArray = new String[] {};
        }
        Set<String> existingSelectedValuesSet = tagChooser.getValues();
        String[] existingSelectedValues = existingSelectedValuesSet.toArray(new String[existingSelectedValuesSet.size()]);

        List<String> newSelectedValues = new ArrayList<String>();
        for (String value : existingSelectedValues) {
            if (Arrays.asList(tagNamesArray).contains(value)) {
                newSelectedValues.add(value);
            }
        }

        Set<String> newSelectedValuesSet = new HashSet<String>(newSelectedValues);

        tagChooser.setEntries(tagNamesArray);
        tagChooser.setEntryValues(tagNamesArray);
        tagChooser.setValues(newSelectedValuesSet);
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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }
}
