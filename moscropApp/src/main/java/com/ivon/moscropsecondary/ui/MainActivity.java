package com.ivon.moscropsecondary.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.list.EmailCardProcessor;
import com.ivon.moscropsecondary.list.HtmlCardProcessor;
import com.ivon.moscropsecondary.util.Logger;

public class MainActivity extends ActionBarActivity implements OnItemClickListener {
	
    protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;
    
    protected String[] mFragmentTitles;
    
	protected RSSFragment mNewsFragment;
	protected RSSFragment mEmailFragment;
	protected RSSFragment mStudentSubsFragment;
	protected CalendarFragment mEventsFragment;
	protected TeachersFragment mTeachersFragment;
    
	protected static int currentFragment;

	protected void initializeNavigationDrawer() {
        mFragmentTitles = getResources().getStringArray(R.array.navigation_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mFragmentTitles));
        mDrawerList.setOnItemClickListener(this);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        mDrawerToggle = new DrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
	}
    
	protected void initializeFragments() {
		mNewsFragment = (RSSFragment) getSupportFragmentManager().findFragmentByTag("newsFragment");
		mEmailFragment = (RSSFragment) getSupportFragmentManager().findFragmentByTag("emailFragment");
		mStudentSubsFragment = (RSSFragment) getSupportFragmentManager().findFragmentByTag("studentSubsFragment");
        mEventsFragment = (CalendarFragment) getSupportFragmentManager().findFragmentByTag("eventsFragment");
        mTeachersFragment = (TeachersFragment) getSupportFragmentManager().findFragmentByTag("teachersFragment");
        selectItem(currentFragment);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Logger.log("oncreate activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeNavigationDrawer();
        initializeFragments();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
    	// If drawr toggle was selected
    	if (mDrawerToggle.onOptionsItemSelected(item)) {
    		return true;
    	}
    	
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    public class DrawerToggle extends ActionBarDrawerToggle {

		public DrawerToggle(Activity activity, DrawerLayout drawerLayout,
				int drawerImageRes, int openDrawerContentDescRes,
				int closeDrawerContentDescRes) {
			super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes,
					closeDrawerContentDescRes);
		}
    	
        public void onDrawerClosed(View view) {
            supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        public void onDrawerOpened(View drawerView) {
            supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }
    
    protected void selectItem(final int position) {
    	String tag;
    	Fragment fragment;
    	
    	switch(position) {
    	case 1:
    		if(mEmailFragment == null) mEmailFragment = RSSFragment.newInstance(RSSFragment.BLOGGER_NEWSLETTER_URL, new EmailCardProcessor(0xffaa66cc));
    		fragment = mEmailFragment;
    		tag = "emailFragment";
    		break;
    	case 2:
    		if(mStudentSubsFragment == null) mStudentSubsFragment = RSSFragment.newInstance(RSSFragment.BLOGGER_SUBS_URL, new HtmlCardProcessor(0xffcc0000));
    		fragment = mStudentSubsFragment;
    		tag = "studentSubsFragment";
    		break;
    	case 3:
    		if(mEventsFragment == null) mEventsFragment = CalendarFragment.newInstance();
    		fragment = mEventsFragment;
    		tag = "eventsFragment";
    		break;
    	case 4:
    		if(mTeachersFragment == null) mTeachersFragment = TeachersFragment.newInstance();
    		fragment = mTeachersFragment;
    		tag = "teacherFragment";
    		break;
    	default:
    		if(mNewsFragment == null) mNewsFragment = RSSFragment.newInstance(RSSFragment.BLOGGER_NEWS_URL, new HtmlCardProcessor(0xff33b5e5));
            fragment = mNewsFragment;
            tag = "newsFragment";
    		break;
    	}
    	loadFragment(position, fragment, tag);
    }
    
    protected void loadFragment(final int position, Fragment fragment, String tag) {
			
		currentFragment = position;
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();
			
	    new Handler().postDelayed(new Runnable() {
	        @Override
	        public void run() {
	            mDrawerLayout.closeDrawer(mDrawerList);
	        }           
	    }, 150);
        mDrawerList.setItemChecked(position, true);
        setTitle(mFragmentTitles[position]);
    }
}