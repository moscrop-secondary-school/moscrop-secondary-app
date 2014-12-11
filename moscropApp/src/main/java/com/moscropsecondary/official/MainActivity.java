package com.moscropsecondary.official;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.moscropsecondary.official.calendar.CalendarFragment;
import com.moscropsecondary.official.rss.RSSFragment;
import com.moscropsecondary.official.staffinfo.StaffInfoFragment;
import com.moscropsecondary.official.util.Logger;
import com.moscropsecondary.official.util.Preferences;
import com.moscropsecondary.official.util.ThemesUtil;

public class MainActivity extends ToolbarActivity
        implements NavigationDrawerBase.NavigationDrawerCallbacks, ThemesUtil.ThemeChangedListener {

    private DrawerLayout mDrawerLayout;
    protected RSSFragment mNewsFragment;
    protected RSSFragment mEmailFragment;
    //protected RSSFragment mStudentSubsFragment;
    protected CalendarFragment mEventsFragment;
    protected StaffInfoFragment mTeachersFragment;

    protected static int mCurrentFragment;

    private CharSequence mTitle;

    private boolean mThemeRequiresUpdate = false;

    public interface BackPressListener {
        public abstract boolean onBackPressed();
    }

    private BackPressListener mBackPressListener;

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int theme = ThemesUtil.getThemeResFromPreference(this);
        setTheme(theme);
        mThemeRequiresUpdate = false;   // We just set the latest theme
        ThemesUtil.registerThemeChangedListener(this);

        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.ic_ab_drawer);

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mTitle = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);

        //setUpToolbarSpinner();
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            switch (mCurrentFragment) {
                case NavigationDrawerFragment.NEWS:
                    mNewsFragment.doSearch(query);
                    break;
                case NavigationDrawerFragment.EMAIL:
                    mEmailFragment.doSearch(query);
                    break;
                case NavigationDrawerFragment.EVENTS:
                    mEventsFragment.doSearch(query);
                    break;
                case NavigationDrawerFragment.TEACHERS:
                    mTeachersFragment.doSearch(query);
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mThemeRequiresUpdate) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, boolean fromSavedInstanceState) {

        if(!fromSavedInstanceState) {
            // determine which fragment to load
            Fragment mNextFragment = null;
            switch (position) {

                case NavigationDrawerFragment.NEWS:
                    if (mNewsFragment == null) {
                        SharedPreferences prefs = getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
                        String lastTag = prefs.getString(Preferences.App.Keys.RSS_LAST_TAG, Preferences.App.Default.RSS_LAST_TAG);
                        mNewsFragment = RSSFragment.newInstance(0, RSSFragment.FEED_NEWS, lastTag);
                    }
                    mNextFragment = mNewsFragment;
                    break;
                case NavigationDrawerFragment.EMAIL:
                    if (mEmailFragment == null) mEmailFragment = RSSFragment.newInstance(NavigationDrawerFragment.EMAIL, RSSFragment.FEED_NEWS, "Student Bulletin");
                    mNextFragment = mEmailFragment;
                    break;
                case NavigationDrawerFragment.EVENTS:
                    if (mEventsFragment == null) mEventsFragment = CalendarFragment.newInstance(NavigationDrawerFragment.EVENTS);
                    mNextFragment = mEventsFragment;
                    break;
                case NavigationDrawerFragment.TEACHERS:
                    if (mTeachersFragment == null)
                        mTeachersFragment = StaffInfoFragment.newInstance(NavigationDrawerFragment.TEACHERS);
                    mNextFragment = mTeachersFragment;
                    break;
                case NavigationDrawerFragment.SETTINGS:
                    Intent settingsIntent = new Intent(this, GenericActivity.class);
                    settingsIntent.putExtra(GenericActivity.TYPE_KEY, GenericActivity.TYPE_SETTINGS);
                    startActivity(settingsIntent);
                    return;
                case NavigationDrawerFragment.ABOUT:
                    Intent aboutIntent = new Intent(this, GenericActivity.class);
                    aboutIntent.putExtra(GenericActivity.TYPE_KEY, GenericActivity.TYPE_ABOUT);
                    startActivity(aboutIntent);
                    return;
                case NavigationDrawerFragment.CONTACT:
                    Intent contactIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.dev_email), null));
                    contactIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_subject));
                    contactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(contactIntent, getString(R.string.send_email)));
                    return;
            }

            // update the main content by replacing fragments
            Logger.log("Choosing fragment: " + position);
            if (mNextFragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, mNextFragment)
                        .commit();
            }
            mCurrentFragment = position;
        }
    }

    public void onSectionAttached(int position) {
        if (position == -1) {
            mTitle = "";
        } else {
            mTitle = getResources().getStringArray(R.array.navigation_items)[position];
        }
        getSupportActionBar().setTitle(mTitle);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_staff, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // If drawr toggle was selected

        int id = item.getItemId();

        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(Gravity.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                mDrawerLayout.closeDrawer(Gravity.START);
                return true;
            }
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    public void onThemeChanged() {
        mThemeRequiresUpdate = true;
    }
}