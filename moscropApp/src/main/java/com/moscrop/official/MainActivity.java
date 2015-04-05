package com.moscrop.official;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.moscrop.official.calendar.CalendarFragment;
import com.moscrop.official.rss.RSSFragment;
import com.moscrop.official.staffinfo.StaffInfoFragment;
import com.moscrop.official.util.Logger;
import com.moscrop.official.util.Preferences;
import com.moscrop.official.util.ThemesUtil;

public class MainActivity extends ToolbarActivity
        implements NavigationDrawerBase.NavigationDrawerCallbacks, ThemesUtil.ThemeChangedListener {

    private DrawerLayout mDrawerLayout;
    protected RSSFragment mNewsFragment;
    protected RSSFragment mEmailFragment;
    protected CalendarFragment mEventsFragment;
    protected StaffInfoFragment mTeachersFragment;

    protected static int mCurrentFragment = -1;

    private CharSequence mTitle;

    private boolean mThemeRequiresUpdate = false;

    public interface BackPressListener {
        public abstract boolean onBackPressed();
    }

    private BackPressListener mBackPressListener;

    public interface CustomTitleFragment {
        public abstract void removeCustomTitle();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int theme = ThemesUtil.getThemeResFromPreference(this, ThemesUtil.THEME_TYPE_DRAWER);
        setTheme(theme);
        mThemeRequiresUpdate = false;   // We just set the latest theme
        ThemesUtil.registerThemeChangedListener(this);

        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.ic_drawer);
        getSupportActionBar().setTitle("");

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mTitle = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(mDrawerLayout, R.id.navigation_drawer_container);

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
            mCurrentFragment = -1;
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCurrentFragment = -1;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, boolean fromSavedInstanceState) {

        Logger.log("From saved instance state: " + fromSavedInstanceState);

        if (position == mCurrentFragment) {
            Logger.log("Desired position (" + position + ") is the same as mCurrentFragment (" + mCurrentFragment + ")");
            return;
        }

        if(!fromSavedInstanceState) {
            // determine which fragment to load
            Fragment mNextFragment = null;
            String mNextTag = "";
            switch (position) {
                case NavigationDrawerFragment.NEWS:
                    if (mNewsFragment == null) {
                        SharedPreferences prefs = getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
                        String lastTag = prefs.getString(Preferences.App.Keys.RSS_LAST_TAG, Preferences.App.Default.RSS_LAST_TAG);
                        mNewsFragment = RSSFragment.newInstance(0, RSSFragment.FEED_NEWS, lastTag);
                    }
                    mNextFragment = mNewsFragment;
                    mNextTag = "mNewsFragment";
                    break;
                case NavigationDrawerFragment.EMAIL:
                    if (mEmailFragment == null) mEmailFragment = RSSFragment.newInstance(NavigationDrawerFragment.EMAIL, RSSFragment.FEED_NEWS, "Student Bulletin");
                    mNextFragment = mEmailFragment;
                    mNextTag = "mEmailFragment";
                    break;
                case NavigationDrawerFragment.EVENTS:
                    if (mEventsFragment == null) mEventsFragment = CalendarFragment.newInstance(NavigationDrawerFragment.EVENTS);
                    mNextFragment = mEventsFragment;
                    mNextTag = "mEventsFragment";
                    break;
                case NavigationDrawerFragment.TEACHERS:
                    if (mTeachersFragment == null) mTeachersFragment = StaffInfoFragment.newInstance(NavigationDrawerFragment.TEACHERS);
                    mNextFragment = mTeachersFragment;
                    mNextTag = "mTeachersFragment";
                    break;
                case NavigationDrawerFragment.SETTINGS:
                    Intent settingsIntent = new Intent(this, GenericActivity.class);
                    settingsIntent.putExtra(GenericActivity.TYPE_KEY, GenericActivity.TYPE_SETTINGS);
                    delayedStartActivity(settingsIntent);
                    return;
                case NavigationDrawerFragment.ABOUT:
                    Intent aboutIntent = new Intent(this, GenericActivity.class);
                    aboutIntent.putExtra(GenericActivity.TYPE_KEY, GenericActivity.TYPE_ABOUT);
                    delayedStartActivity(aboutIntent);
                    return;
                case NavigationDrawerFragment.CONTACT:
                    Intent contactIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.dev_email), null));
                    contactIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_subject));
                    contactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    delayedStartActivity(Intent.createChooser(contactIntent, getString(R.string.send_email)));
                    return;
            }

            // update the main content by replacing fragments
            Logger.log("Choosing fragment: " + position);
            if (mNextFragment != null) {

                removeCustomTitleFromOldFragment();

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.drawer_fragment_enter, R.anim.drawer_fragment_exit, R.anim.drawer_fragment_enter, R.anim.drawer_fragment_exit)
                        .replace(R.id.content_frame, mNextFragment, mNextTag)
                        .commit();
            }
            mCurrentFragment = position;

        } else {

            // Position will never be SETTINGS, ABOUT, or CONTACT
            // when from savedInstanceState because those link to other activities.

            // Fragment will already be loaded, but we must obtain a reference to it
            switch (position) {
                case NavigationDrawerFragment.NEWS:
                    mNewsFragment = (RSSFragment) getSupportFragmentManager().findFragmentByTag("mNewsFragment");
                    break;
                case NavigationDrawerFragment.EMAIL:
                    mEmailFragment = (RSSFragment) getSupportFragmentManager().findFragmentByTag("mEmailFragment");
                    break;
                case NavigationDrawerFragment.EVENTS:
                    mEventsFragment = (CalendarFragment) getSupportFragmentManager().findFragmentByTag("mEventsFragment");
                    break;
                case NavigationDrawerFragment.TEACHERS:
                    mTeachersFragment = (StaffInfoFragment) getSupportFragmentManager().findFragmentByTag("mTeachersFragment");
                    break;
            }
        }
    }

    private static final int STANDARD_DRAWER_CLOSE_DELAY    = 350;  // milliseconds
    private static final int DRAWER_CLOSE_APPROX_DURATION   = 300;  // milliseconds

    private void delayedStartActivity(final Intent intent) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, DRAWER_CLOSE_APPROX_DURATION);
    }

    @Override
    public int getNavigationDrawerCloseDelay(int position) {
        switch (position) {

            case NavigationDrawerFragment.NEWS:
            case NavigationDrawerFragment.EMAIL:
            case NavigationDrawerFragment.TEACHERS:
                return STANDARD_DRAWER_CLOSE_DELAY;

            case NavigationDrawerFragment.EVENTS:
                return 500;

            case NavigationDrawerFragment.SETTINGS:
            case NavigationDrawerFragment.ABOUT:
            case NavigationDrawerFragment.CONTACT:
                return 0;

            default:
                return STANDARD_DRAWER_CLOSE_DELAY;
        }
    }

    private void removeCustomTitleFromOldFragment() {
        Fragment fragment = null;
        switch (mCurrentFragment) {
            case NavigationDrawerFragment.NEWS:
                fragment = mNewsFragment;
                break;
            case NavigationDrawerFragment.EMAIL:
                fragment = mEmailFragment;
                break;
            case NavigationDrawerFragment.EVENTS:
                fragment = mEventsFragment;
                break;
            case NavigationDrawerFragment.TEACHERS:
                fragment = mTeachersFragment;
                break;
        }

        if (fragment != null) {
            if (fragment instanceof CustomTitleFragment) {
                ((CustomTitleFragment) fragment).removeCustomTitle();
            }
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