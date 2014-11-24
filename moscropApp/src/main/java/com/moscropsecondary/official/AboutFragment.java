package com.moscropsecondary.official;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.adapter.AboutAdapter;
import com.moscropsecondary.official.model.SettingsItem;
import com.moscropsecondary.official.util.Dialogs;
import com.moscropsecondary.official.util.Util;

public class AboutFragment extends ListFragment
{
    // ID Keys
    private final int CREDITS_PEOPLE = 0;


    // App Preferences
    private SharedPreferences mPrefs;

    // List Adapter
    private AboutAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
//        mPrefs = getActivity().getSharedPreferences(Util.PREFS_NAME, 0);
        addSettings();
        setListAdapter(mAdapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // Reinitialize our preferences if they somehow became null
//        if(mPrefs == null)
//            mPrefs = getActivity().getSharedPreferences(Util.PREFS_NAME, 0);

        // We have a custom divider so let's disable this
        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        // Apply custom selector
        getListView().setSelector(R.drawable.selector_transparent_lgray);
        getListView().setDrawSelectorOnTop(false);

    }

    private void addSettings()
    {
        mAdapter = new AboutAdapter(getActivity());

        mAdapter.addHeader(getString(R.string.about));
        mAdapter.addItem(new SettingsItem.Builder()
                .type(AboutAdapter.TYPE_TEXT)
                .title(getString(R.string.people))
                .description(getString(R.string.people_description))
                .id(CREDITS_PEOPLE)
                .build());
    }
}