package com.moscrop.official;

import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.moscrop.official.rss.ParseCategoryHelper;
import com.moscrop.official.util.Preferences;
import com.moscrop.official.util.ThemesUtil;

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

    public interface SubscriptionListChangedListener {
        public abstract void onSubscriptionListChanged();
    }

    private static List<SubscriptionListChangedListener> mListeners = new ArrayList<SubscriptionListChangedListener>();

    public static void registerSubscriptionListChangedListener(SubscriptionListChangedListener listener) {
        mListeners.add(listener);
    }

    public static void unregisterSubscriptionListChangedListeners(SubscriptionListChangedListener listener) {
        mListeners.remove(listener);
    }

    private static void notifySubscriptionListChanged() {
        for(SubscriptionListChangedListener l : mListeners) {
            if(l != null) {
                l.onSubscriptionListChanged();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference theme = findPreference(Preferences.Keys.THEME);
        theme.setOnPreferenceChangeListener(this);

        mTagChooser = (MultiSelectListPreference) findPreference(Preferences.Keys.TAGS);
        updateTagsList();
        mTagChooser.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int titleRes = preference.getTitleRes();
        if(titleRes == R.string.theme_selector_title) {
            ThemesUtil.notifyThemeChanged();
            Toast.makeText(getActivity(), "Theme will change when you exit settings", Toast.LENGTH_SHORT).show();
        } else if (titleRes == R.string.tag_chooser_title) {
            notifySubscriptionListChanged();
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        /*int titleRes = preference.getTitleRes();
        if (titleRes == R.string.refresh_tag_list_title) {
            Logger.log("Refresh clicked");
            refreshTagsFromServer();
            return true;
        }*/
        return false;
    }

    private void updateTagsList() {
        String[] tagNamesArray;
        try {
            tagNamesArray = ParseCategoryHelper.getAllTagNames(getActivity());
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
