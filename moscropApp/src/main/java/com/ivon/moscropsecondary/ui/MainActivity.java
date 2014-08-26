package com.ivon.moscropsecondary.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.util.Logger;

public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
	protected RSSFragment mNewsFragment;
	protected RSSFragment mEmailFragment;
	protected RSSFragment mStudentSubsFragment;
	protected CalendarFragment mEventsFragment;
	protected TeachersFragment mTeachersFragment;
    
	//protected static int currentFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Logger.log("oncreate activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, boolean fromSavedInstanceState) {

        if(!fromSavedInstanceState) {
            // determine which fragment to load
            Fragment mNextFragment = null;
            switch (position) {

                case 0:
                    if (mNewsFragment == null)
                        mNewsFragment = RSSFragment.newInstance(0, RSSFragment.FEED_ALL);
                    mNextFragment = mNewsFragment;
                    break;
                case 1:
                    if (mEmailFragment == null)
                        mEmailFragment = RSSFragment.newInstance(1, RSSFragment.FEED_NEWSLETTERS);
                    mNextFragment = mEmailFragment;
                    break;
                case 2:
                    if (mStudentSubsFragment == null)
                        mStudentSubsFragment = RSSFragment.newInstance(2, RSSFragment.FEED_SUBS);
                    mNextFragment = mStudentSubsFragment;
                    break;
                case 3:
                    if (mEventsFragment == null) mEventsFragment = CalendarFragment.newInstance(3);
                    mNextFragment = mEventsFragment;
                    break;
                case 4:
                    if (mTeachersFragment == null)
                        mTeachersFragment = TeachersFragment.newInstance(4);
                    mNextFragment = mTeachersFragment;
                    break;
            }

            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, mNextFragment)
                    .commit();
        }
    }

    public void onSectionAttached(int position) {
        mTitle = getResources().getStringArray(R.array.navigation_items)[position];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
    	// If drawr toggle was selected
    	
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}