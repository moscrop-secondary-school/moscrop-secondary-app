package com.ivon.moscropsecondary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.ivon.moscropsecondary.calendar.CalendarFragment;
import com.ivon.moscropsecondary.rss.RSSFragment;
import com.ivon.moscropsecondary.staffinfo.StaffInfoFragment;

public class MainActivity extends ToolbarActivity
        implements NavigationDrawerBase.NavigationDrawerCallbacks {

    private DrawerLayout mDrawerLayout;
	protected RSSFragment mNewsFragment;
	protected RSSFragment mEmailFragment;
	protected RSSFragment mStudentSubsFragment;
	protected CalendarFragment mEventsFragment;
	protected StaffInfoFragment mTeachersFragment;
    
	//protected static int currentFragment;

    private CharSequence mTitle;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.ic_ab_drawer);

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mTitle = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);
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

                case 0:
                    if (mNewsFragment == null)
                        mNewsFragment = RSSFragment.newInstance(0, RSSFragment.FEED_NEWS);
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
                        mTeachersFragment = StaffInfoFragment.newInstance(4);
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
        } else if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}