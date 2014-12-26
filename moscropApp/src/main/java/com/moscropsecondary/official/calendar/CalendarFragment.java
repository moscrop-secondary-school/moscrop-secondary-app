package com.moscropsecondary.official.calendar;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moscropsecondary.official.MainActivity;
import com.moscropsecondary.official.R;
import com.moscropsecondary.official.ToolbarActivity;
import com.moscropsecondary.official.util.DateUtil;
import com.moscropsecondary.official.util.Preferences;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment
        implements AbsListView.OnScrollListener {

    public static final String MOSCROP_CALENDAR_ID = "moscropsecondaryschool@gmail.com";
    public static final String MOSCROP_CALENDAR_JSON_URL = "http://www.google.com/calendar/feeds/moscropsecondaryschool@gmail.com/public/full?alt=json&max-results=1000&orderby=starttime&sortorder=descending&singleevents=true";

    private static final String KEY_POSITION = "position";
    private int mPosition;
    private View mContentView;

    private View mCaldroidFrame;
    private CaldroidFragment mCaldroid;
    private ListView mListView;
    private View mToolbarTitle;

    private boolean mCalendarIsShowing = false;
    private boolean mSearchViewExpanded = false;

    private int mYear = -1;
    private int mMonth = -1;     // java.util.Calendar months. One less than actual month.

    private EventListAdapter mAdapter;
    
    public static CalendarFragment newInstance(int position) {
    	CalendarFragment fragment = new CalendarFragment();
        fragment.mPosition = position;
        Calendar cal = Calendar.getInstance();
        fragment.mYear = cal.get(Calendar.YEAR);
        fragment.mMonth = cal.get(Calendar.MONTH);
    	return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);
    	
    	mContentView = inflater.inflate(R.layout.fragment_events, container, false);

        //insertDays();

        mListView = (ListView) mContentView.findViewById(R.id.daily_events_list);
        mAdapter = new EventListAdapter(getActivity(), new ArrayList<GCalEvent>());
        mListView.setAdapter(mAdapter);
        //mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);

        mCaldroid = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar today = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, today.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, today.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.caldroid_bg_color, typedValue, true);
        int caldroidBgColor = typedValue.data;
        theme.resolveAttribute(R.attr.caldroid_month_text_color, typedValue, true);
        int monthTextColor = typedValue.data;
        theme.resolveAttribute(R.attr.caldroid_week_text_color, typedValue, true);
        int weekTextColor = typedValue.data;
        theme.resolveAttribute(R.attr.caldroid_normal_day_text_color, typedValue, true);
        int normalDayTextColor = typedValue.data;
        theme.resolveAttribute(R.attr.caldroid_disable_day_text_color, typedValue, true);
        int disableDayTextColor = typedValue.data;
        theme.resolveAttribute(R.attr.caldroid_event_indicator_color, typedValue, true);
        int eventIndicatorColor = typedValue.data;

        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[] { R.attr.caldroid_prev_arrow_resource, R.attr.caldroid_next_arrow_resource });
        int prevArrowResource = a.getResourceId(0, 0);
        int nextArrowResource = a.getResourceId(1, 0);
        a.recycle();

        args.putBoolean(CaldroidFragment.SHOW_TITLE_BAR, false);
        args.putInt(CaldroidFragment.BACKGROUND_COLOR, caldroidBgColor);
        args.putInt(CaldroidFragment.PREV_ARROW_RESOURCE, prevArrowResource);
        args.putInt(CaldroidFragment.NEXT_ARROW_RESOURCE, nextArrowResource);
        args.putInt(CaldroidFragment.MONTH_TEXT_COLOR, monthTextColor);
        args.putInt(CaldroidFragment.WEEK_TEXT_COLOR, weekTextColor);
        args.putInt(CaldroidFragment.NORMAL_DAY_TEXT_COLOR, normalDayTextColor);
        args.putInt(CaldroidFragment.DISABLE_DAY_TEXT_COLOR, disableDayTextColor);
        args.putInt(CaldroidFragment.EVENT_INDICATOR_COLOR, eventIndicatorColor);

        mCaldroid.setArguments(args);
        mCaldroid.setCaldroidListener(mCaldroidListener);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.calendar_frame, mCaldroid).commit();

        mCaldroidFrame = mContentView.findViewById(R.id.calendar_frame);
        mCaldroidFrame.setVisibility(View.GONE);

        if(savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(KEY_POSITION, mPosition);
        }

        // Refresh calendar
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadCalendar();
            }
        }).start();

    	return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        addTitleWithArrow();
    }

    @Override
    public void onPause() {
        super.onPause();
        Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
        toolbar.removeView(mToolbarTitle);
    }

    @Override
    public void onStop() {
        super.onStop();
        mSearchViewExpanded = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_events, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint("Search events");

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchViewExpanded = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchViewExpanded = false;
                addTitleWithArrow();
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void addTitleWithArrow() {
        if (!mSearchViewExpanded) {
            Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
            toolbar.removeView(mToolbarTitle);
            mToolbarTitle = LayoutInflater.from(getActivity()).inflate(R.layout.spinner_actionbar_title, toolbar, false);
            setToolbarTitle("Events");
            mToolbarTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCalendarIsShowing) {
                        hideCalendar();
                    } else {
                        showCalendar();
                    }
                }
            });
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(mToolbarTitle, lp);
        }
    }

    private String getTitleStringFromDate(int year, int month) {
        String title = DateUtil.getMonthName(month-1, false);

        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.YEAR) != year) {
            title = title + " " + year;
        }

        return title;
    }

    private void setToolbarTitle(String subtitle) {
        if (mToolbarTitle != null) {
            ((TextView) mToolbarTitle.findViewById(android.R.id.text1)).setText(subtitle);
        }
    }

    private void downloadCalendar() {

        // Check if provider is empty
        CalendarDatabase db = new CalendarDatabase(getActivity());
        int count = db.getCount();

        SharedPreferences prefs = getActivity().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        long lastUpdateMillis = prefs.getLong(Preferences.App.Keys.GCAL_LAST_UPDATED, Preferences.App.Default.GCAL_LAST_UPDATED);
        String lastGcalVersion = prefs.getString(Preferences.App.Keys.GCAL_VERSION, Preferences.App.Default.GCAL_VERSION);

        if((count == 0)
                || (lastUpdateMillis == Preferences.App.Default.GCAL_LAST_UPDATED)
                || (lastGcalVersion.equals(Preferences.App.Default.GCAL_VERSION))
                ) {

            // Provider is empty
            // Or, if last update info is missing, to be safe,
            // we will reload everything. Make sure data is up to date.
            CalendarParser.parseAndSaveAll(getActivity(), MOSCROP_CALENDAR_ID);

        } else {

            // Everything good to go! Functioning normally.

            // If last update time is in the future for some reason,
            // assume last update time is now so we can recheck everything
            // between now and the last updated time, which is somehow
            // in the future. Probably aliens. (Actually, very likely due to timezones)
            if(lastUpdateMillis > System.currentTimeMillis()) {
                lastUpdateMillis = System.currentTimeMillis();
            }

            CalendarParser.parseAndSave(getActivity(), MOSCROP_CALENDAR_ID, lastUpdateMillis, lastGcalVersion);
        }

        // Update UI when done loading
        if (getActivity() != null) {
            final List<GCalEvent> events = db.getAllEvents();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.clear();
                    mAdapter.addAll(events);
                    mAdapter.notifyDataSetChanged();
                    scrollTo(System.currentTimeMillis());

                    //mCaldroidListener.onChangeMonth(mMonth + 1, mYear);
                    //mCaldroidListener.onSelectDate(Calendar.getInstance().getTime(), null);
                }
            });
        }

        db.close();
    }

    private void scrollTo(long millis) {
        int dayNumber = DateUtil.daysFromMillis(millis);
        int position = mAdapter.getPositionNearestToDay(dayNumber);
        if (position != -1) {
            mListView.setSelection(position);
        }
    }

    private void loadEventsIntoCaldroid() {
        if (getActivity() != null) {
            CalendarDatabase db = new CalendarDatabase(getActivity());
            List<GCalEvent> events = db.getEventsForMonth(mYear, mMonth);
            db.close();

            for (GCalEvent event : events) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(event.startTime);                                               // Set cal to event start time
                while (cal.getTimeInMillis() < event.endTime - 1) {                                 // Loop through days within event range
                    Date date = cal.getTime();
                    mCaldroid.setHasEventsForDate(true, date);                                      // Set hasEvents flag of this day
                    cal.setTimeInMillis(cal.getTimeInMillis() + 24 * 60 * 60 * 1000);               // Increment calendar by a day
                }
            }
        }
    }

    final CaldroidListener mCaldroidListener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            scrollTo(date.getTime());
            hideCalendar();
        }

        @Override
        public void onChangeMonth(final int month, final int year) {
            if (mCalendarIsShowing) {
                setToolbarTitle(getTitleStringFromDate(year, month));
            }
        }
    };

    private void showCalendar() {

        mCalendarIsShowing = true;

        setToolbarTitle(getTitleStringFromDate(mYear, mMonth+1));
        ((TextView) mToolbarTitle.findViewById(android.R.id.text1)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner_triangle_up, 0);

        Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        mCaldroidFrame.startAnimation(slideIn);

        mCaldroidFrame.setVisibility(View.VISIBLE);
    }

    private void hideCalendar() {

        mCalendarIsShowing = false;

        setToolbarTitle("Events");
        ((TextView) mToolbarTitle.findViewById(android.R.id.text1)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner_triangle, 0);

        Animation slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        mCaldroidFrame.startAnimation(slideOut);

        mCaldroidFrame.setVisibility(View.GONE);
    }

    public void doSearch(String query) {
        Toast.makeText(getActivity(), "Events: " + query, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Do nothing
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mAdapter != null && mAdapter.getCount() > 0) {
            EventListAdapter.Day day = mAdapter.getItem(firstVisibleItem);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(DateUtil.millisFromDays(day.dayNumber));
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            if (month != mMonth || year != mYear) {
                mMonth = month;
                mYear = year;
                mCaldroid.moveToDate(cal.getTime());
            }
        }
    }
}
