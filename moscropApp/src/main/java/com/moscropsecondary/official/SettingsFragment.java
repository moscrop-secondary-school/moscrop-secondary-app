package com.moscropsecondary.official;

import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.moscropsecondary.official.rss.RSSTagCriteria;
import com.moscropsecondary.official.util.Logger;
import com.moscropsecondary.official.util.Preferences;
import com.moscropsecondary.official.util.ThemesUtil;
import com.moscropsecondary.official.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private MultiSelectListPreference mTagChooser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference theme = findPreference(Preferences.Keys.THEME);
        theme.setOnPreferenceChangeListener(this);

        mTagChooser = (MultiSelectListPreference) findPreference(Preferences.Keys.TAGS);
        updateTagsList();

        Preference refresh = findPreference("refresh_tag_list");
        refresh.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int titleRes = preference.getTitleRes();
        if(titleRes == R.string.theme_selector_title) {
            ThemesUtil.notifyThemeChanged();
            Toast.makeText(getActivity(), "Theme will change when you exit settings", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        int titleRes = preference.getTitleRes();
        if (titleRes == R.string.refresh_tag_list_title) {
            Logger.log("Refresh clicked");
            refreshTagsFromServer();
            return true;
        }
        return false;
    }

    private void refreshTagsFromServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Util.isConnected(getActivity())) {
                    try {
                        RSSTagCriteria.downloadTagListToStorage(getActivity());
                        showResult(true);
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                showResult(false);
            }

            private void showResult(final boolean success) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                getActivity(),
                                success ? "Categories successfully updated" : "Categories update failed",
                                Toast.LENGTH_SHORT
                        ).show();
                        updateTagsList();
                    }
                });
            }
        });
        thread.start();
    }

    private void updateTagsList() {
        String[] tagNamesArray;
        try {
            tagNamesArray = RSSTagCriteria.getTagNames(getActivity());
        } catch (IOException e) {
            tagNamesArray = new String[] {};
        } catch (JSONException e) {
            tagNamesArray = new String[] {};
        }
        Set<String> existingSelectedValuesSet = mTagChooser.getValues();
        String[] existingSelectedValues = existingSelectedValuesSet.toArray(new String[existingSelectedValuesSet.size()]);

        List<String> newSelectedValues = new ArrayList<String>();
        for (String value : existingSelectedValues) {
            if (Arrays.asList(tagNamesArray).contains(value)) {
                newSelectedValues.add(value);
            }
        }

        Set<String> newSelectedValuesSet = new HashSet<String>(newSelectedValues);

        mTagChooser.setEntries(tagNamesArray);
        mTagChooser.setEntryValues(tagNamesArray);
        mTagChooser.setValues(newSelectedValuesSet);
    }
}
