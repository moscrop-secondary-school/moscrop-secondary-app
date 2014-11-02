package com.ivon.moscropsecondary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ivon.moscropsecondary.calendar.CalendarFragment;
import com.ivon.moscropsecondary.rss.RSSFragment;
import com.ivon.moscropsecondary.staffinfo.StaffInfoFragment;
import com.ivon.moscropsecondary.util.Logger;
import com.ivon.moscropsecondary.util.ThemesUtil;

public class MainActivity extends ToolbarActivity
        implements NavigationDrawerBase.NavigationDrawerCallbacks, ThemesUtil.ThemeChangedListener {

    private DrawerLayout mDrawerLayout;
    protected RSSFragment mNewsFragment;
    protected RSSFragment mEmailFragment;
    protected RSSFragment mStudentSubsFragment;
    protected CalendarFragment mEventsFragment;
    protected StaffInfoFragment mTeachersFragment;

    //protected static int currentFragment;

    private CharSequence mTitle;

    private boolean mThemeRequiresUpdate = false;

    public interface BackPressListener {
        public abstract boolean onBackPressed();
    }

    private BackPressListener mBackPressListener;

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
                    if (mNewsFragment == null)
                        mNewsFragment = RSSFragment.newInstance(0, RSSFragment.FEED_NEWS, "Autopost");
                    mNextFragment = mNewsFragment;
                    break;
                case NavigationDrawerFragment.EVENTS:
                    if (mEventsFragment == null) mEventsFragment = CalendarFragment.newInstance(1);
                    mNextFragment = mEventsFragment;
                    break;
                case NavigationDrawerFragment.TEACHERS:
                    if (mTeachersFragment == null)
                        mTeachersFragment = StaffInfoFragment.newInstance(2);
                    mNextFragment = mTeachersFragment;
                    break;
                case NavigationDrawerFragment.SETTINGS:
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    return;
                case NavigationDrawerFragment.ABOUT:
                    Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
                    return;
                case NavigationDrawerFragment.CONTACT:
                    Intent contactIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.email), null));
                    contactIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_subject));
                    contactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(contactIntent, getString(R.string.send_email)));
                    return;
            }

            // update the main content by replacing fragments
            Logger.log("Choosing fragment: " + position);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, mNextFragment)
                    .commit();
        }
    }

    public void onSectionAttached(int position) {
        mTitle = getResources().getStringArray(R.array.navigation_items)[position];
        Logger.log("onSectionAttached: " + position + ", " + mTitle);
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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