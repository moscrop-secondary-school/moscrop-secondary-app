package com.moscropsecondary.official;        //TODO fix error

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.moscropsecondary.official.adapter.AboutAdapter;
import com.moscropsecondary.official.model.SettingsItem;
import com.moscropsecondary.official.util.Preferences;
import com.moscropsecondary.official.util.ThemesUtil;
import com.moscropsecondary.official.util.Dialogs;

public class AboutFragment extends ListFragment
        implements ThemesUtil.ThemeChangedListener {
//        PreferenceFragment
//        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    // ID Keys
    private final int CREDITS_PEOPLE = 0;

    private static final String KEY_POSITION = "position";
    private int mPosition;
    private View mContentView;
    // App Preferences
    private SharedPreferences mPrefs;

    // List Adapter
    private AboutAdapter mAdapter;

    private boolean mThemeRequiresUpdate = false;

    public static AboutFragment newInstance(int position) {
        AboutFragment fragment = new AboutFragment();
        fragment.mPosition = position;
        fragment.addSettings();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        int theme = ThemesUtil.getThemeResFromPreference(this);
        setTheme(theme);
        mThemeRequiresUpdate = false;   // We just set the latest theme
        ThemesUtil.registerThemeChangedListener(this);
    }
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

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        SettingsItem mSetting = mAdapter.getItem(position);

        switch (mSetting.getID()) {
            case CREDITS_PEOPLE:
                Dialogs.getCreditsPeopleDialog(getActivity()).show();
                break;
        }
    }
}