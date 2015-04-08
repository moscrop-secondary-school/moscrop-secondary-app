package com.moscrop.official.calendar;

import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moscrop.official.MainActivity;
import com.moscrop.official.R;
import com.moscrop.official.ToolbarActivity;
import com.moscrop.official.util.DateUtil;
import com.moscrop.official.util.Logger;
import com.moscrop.official.util.Preferences;
import com.moscrop.official.util.Util;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment
        implements AbsListView.OnScrollListener, MainActivity.CustomTitleFragment {

    public static final String MOSCROP_CALENDAR_ID = "moscroppanthers@gmail.com";

    private static final String KEY_POSITION = "position";
    private int mPosition;
    private View mContentView;

    private View mCaldroidFrame;
    private CaldroidFragment mCaldroid;
    private SwipeRefreshLayout mSwipeLayout;
    private ListView mListView;
    private View mToolbarTitle;
    private View mToolbarShadow;

    private static final TimeInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private static final TimeInterpolator mDecelerateInterpolator = new DecelerateInterpolator();

    private boolean mCalendarIsShowing = false;
    private boolean mSearchViewExpanded = false;
    private boolean mCustomTitleAdded = false;

    private int mYear = -1;
    private int mMonth = -1;     // java.util.Calendar months. One less than actual month.
    private long mLowerBound;
    private long mUpperBound;
    private static final long ONE_MONTH = 2592000000L;
    private boolean mScrolling = false;

    private static final int FRONT  = 0;
    private static final int END    = 1;

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

        // Setup swipe refresh layout
        mSwipeLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.fragment_event_swipe_container);
        mSwipeLayout.setEnabled(false);

        // Set up listview with custom calendar adapter
        mListView = (ListView) mContentView.findViewById(R.id.daily_events_list);
        mAdapter = new EventListAdapter(getActivity(), new ArrayList<GCalEvent>());
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);

        // Set up Caldroid and pass arguments in bundle
        mCaldroid = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar today = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, today.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, today.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

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

        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]{R.attr.caldroid_prev_arrow_resource, R.attr.caldroid_next_arrow_resource});
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

        // Add CaldroidFragment as a fragment-within-a-fragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.calendar_frame, mCaldroid, "CaldroidFragment").commit();

        // Initialize shadows for top sheet (Caldroid)
        mCaldroidFrame = mContentView.findViewById(R.id.calendar_frame);
        mToolbarShadow = getActivity().findViewById(R.id.toolbar_shadow);

        // Retrieve saved position
        if(savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(KEY_POSITION, mPosition);
        }

        // Add listener to perform calculations for animation
        // once view measurements have been calculated
        ViewTreeObserver observer = mCaldroidFrame.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {

                // Remove listener now that we no longer need it
                mCaldroidFrame.getViewTreeObserver().removeOnPreDrawListener(this);

                // Move Caldroid slide-in to be out of sight
                int height = mCaldroidFrame.getHeight();
                mCaldroidFrame.setTranslationY(-height);
                return true;
            }
        });

        // Refresh calendar asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadCalendar(true);
            }
        }).start();

        // Set custom toolbar title view
        addTitleWithArrow();

        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mCustomTitleAdded) {
            addTitleWithArrow();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mSearchViewExpanded = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeCustomTitle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((MainActivity) getActivity()).getToolbar().setElevation(Util.convertDpToPixel(4, getActivity()));
        } else {
            mToolbarShadow.setTranslationY(0);
        }

        mCalendarIsShowing = false;
    }

    @Override
    public void removeCustomTitle() {
        if (getActivity() != null) {
            Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
            if (toolbar != null) {
                toolbar.removeView(mToolbarTitle);
                mCustomTitleAdded = false;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mPosition);
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

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CalendarDatabase db = CalendarDatabase.getInstance(getActivity());
                        final List<GCalEvent> events = db.getAllEvents();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.clear();
                                mAdapter.addToEnd(events);
                                mAdapter.setShowSearchResultsMode(false);
                                mAdapter.notifyDataSetChanged();
                                scrollTo(System.currentTimeMillis());

                                loadEventsIntoCaldroid(events);
                                // TODO workaround for caldroidListener not working
                                mCaldroid.setCaldroidListener(mCaldroidListener);
                            }
                        });
                    }
                }).start();

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void addTitleWithArrow() {
        if (!mSearchViewExpanded) {
            Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
            toolbar.removeView(mToolbarTitle);
            mToolbarTitle = LayoutInflater.from(getActivity()).inflate(R.layout.toolbar_title_with_arrow, toolbar, false);
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

            mCustomTitleAdded = true;
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

    private void setToolbarTitle(String title) {
        if (mToolbarTitle != null) {
            ((TextView) mToolbarTitle.findViewById(android.R.id.text1)).setText(title);
        }
    }

    /**
     * Download the calendar from Google Calendar
     * Will only download if there is a newer version
     * or if there is no record of a previous offline cache.
     *
     * @param showCacheWhileLoading
     *          Set true to show events already in offline database
     *          while new events are being downloaded from internet
     */
    private void loadCalendar(boolean showCacheWhileLoading) {

        // Check if database is empty
        CalendarDatabase db = CalendarDatabase.getInstance(getActivity());
        int count = db.getCount();

        // Get information about current cached version and last update time
        SharedPreferences prefs = getActivity().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        long lastUpdateMillis = prefs.getLong(Preferences.App.Keys.GCAL_LAST_UPDATED, Preferences.App.Default.GCAL_LAST_UPDATED);
        String lastGcalVersion = prefs.getString(Preferences.App.Keys.GCAL_VERSION, Preferences.App.Default.GCAL_VERSION);

        // Immediately display calendar loaded from already-offline database
        if (showCacheWhileLoading) {
            if (getActivity() != null) {

                // TODO only show events from within a month on initial load

                /*Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.add(Calendar.MONTH, -1);
                mLowerBound = cal.getTimeInMillis();
                cal.add(Calendar.MONTH, 2);
                mUpperBound = cal.getTimeInMillis();
                final List<GCalEvent> events = db.getEventsForDuration(mLowerBound, mUpperBound);*/

                // TODO temporary load implementation that simply loads all posts
                final List<GCalEvent> events = db.getAllEvents();

                // Load all events from the database query
                // into the listview and CaldroidFragment.
                // Then scroll to the nearest event after "today"
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        mAdapter.addToEnd(events);
                        mAdapter.setShowSearchResultsMode(false);
                        mAdapter.notifyDataSetChanged();
                        scrollTo(System.currentTimeMillis());

                        loadEventsIntoCaldroid(events);
                    }
                });
            }
        }

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

            // Only delete calendar entries in the database starting AFTER lastUpdateMillis
            // and replace those with possibly updated entries pulled from Google Calendar
            CalendarParser.parseAndSave(getActivity(), MOSCROP_CALENDAR_ID, lastUpdateMillis, lastGcalVersion);
        }

        // Save new update/GCalVersion info
        String newGcalVersion = prefs.getString(Preferences.App.Keys.GCAL_VERSION, Preferences.App.Default.GCAL_VERSION);

        // Update UI when done loading
        if (showCacheWhileLoading && !newGcalVersion.equals(lastGcalVersion)) {
            if (getActivity() != null) {

                // TODO only show events from within a month on initial load

                /*Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.add(Calendar.MONTH, -1);
                mLowerBound = cal.getTimeInMillis();
                cal.add(Calendar.MONTH, 2);
                mUpperBound = cal.getTimeInMillis();
                final List<GCalEvent> events = db.getEventsForDuration(mLowerBound, mUpperBound);*/

                // TODO temporary load implementation that simply loads all posts
                final List<GCalEvent> events = db.getAllEvents();

                // Load all events from the database query
                // into the listview and CaldroidFragment.
                // Then scroll to the nearest event after "today"
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        mAdapter.addToEnd(events);
                        mAdapter.setShowSearchResultsMode(false);
                        mAdapter.notifyDataSetChanged();
                        scrollTo(System.currentTimeMillis());

                        loadEventsIntoCaldroid(events);
                    }
                });
            }
        }

        db.close();
    }

    // TODO used for future "append" loading where the initial load
    // TODO only loads events within a set duration, and all other
    // TODO events will be loaded once the user scrolls to the end of the list
    private void loadMoreCalendar(boolean addToEnd) {

        /*Logger.log("-------------------------");

        Logger.log("Loading more calendar from database");

        CalendarDatabase db = new CalendarDatabase(getActivity());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd @ kk:mm:ss.SSS");

        if (addToEnd) {
            Logger.log("Loading more from end of list");
            long newUpperBound = mUpperBound + ONE_MONTH;
            List<GCalEvent> events = db.getEventsForDuration(mUpperBound, newUpperBound);
            Logger.log("Loading from " + mUpperBound + " to " + newUpperBound);
            Logger.log("Also known as " + sdf.format(new Date(mUpperBound)) + " to " + sdf.format(new Date(newUpperBound)));
            Logger.log("Query returned " + events.size() + " items");
            mUpperBound = newUpperBound;
            mAdapter.addToEnd(events);
            mAdapter.notifyDataSetChanged();
            loadEventsIntoCaldroid(events);
        } else {
            Logger.log("Loading more from front of list");
            long newLowerBound = mLowerBound - ONE_MONTH;
            List<GCalEvent> events = db.getEventsForDuration(newLowerBound, mLowerBound);
            Logger.log("Loading from " + newLowerBound + " to " + mLowerBound);
            Logger.log("Also known as " + sdf.format(new Date(newLowerBound)) + " to " + sdf.format(new Date(mLowerBound)));
            Logger.log("Query returned " + events.size() + " items");
            mLowerBound = newLowerBound;
            mAdapter.addToFront(events);
            mAdapter.notifyDataSetChanged();
            loadEventsIntoCaldroid(events);
        }

        db.close();

        Logger.log("-------------------------");*/

    }

    /**
     * Gets list position of the nearest event beginning
     * after the given time "millis"
     *
     * Scrolls to that position without animation
     *
     * @param millis
     *          Time to scroll the list to
     */
    private void scrollTo(long millis) {
        int dayNumber = DateUtil.daysFromMillis(millis);
        int position = mAdapter.getPositionNearestToDay(dayNumber);
        if (position != -1) {
            Logger.log("Scrolling to position: " + position);
            mListView.setSelection(position);
        }
    }

    /**
     * Load a list of events into Caldroid so it knows
     * under which dates to add event indicators to
     *
     * @param events
     *          List of events
     */
    private void loadEventsIntoCaldroid(List<GCalEvent> events) {
        /*if (getActivity() != null) {
            CalendarDatabase db = new CalendarDatabase(getActivity());
            List<GCalEvent> events = db.getEventsForMonth(mYear, mMonth);
            db.close();*/

            Calendar cal = Calendar.getInstance();
            for (GCalEvent event : events) {
                cal.setTimeInMillis(event.startTime);                                       // Set cal to event start time
                while (cal.getTimeInMillis() < event.endTime - 1) {                         // Loop through days within event range
                    Date date = cal.getTime();
                    mCaldroid.setHasEventsForDate(true, date);                              // Set hasEvents flag of this day
                    cal.setTimeInMillis(cal.getTimeInMillis() + 24 * 60 * 60 * 1000);       // Increment calendar by a day
                }
            }
        //}
    }

    final CaldroidListener mCaldroidListener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            scrollTo(date.getTime());
            hideCalendar();     // Hide calendar after selecting a day to jump to
        }

        @Override
        public void onChangeMonth(final int month, final int year) {

            setToolbarTitle(getTitleStringFromDate(year, month));

            // TODO in current implementation there is no need
            // TODO for upper and lower bounds, and they are not set.

            // TODO this section is only needed once append-loading is added
            /*
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, 1);
            if (cal.getTimeInMillis() < mLowerBound) {
                Logger.log("calendar time less than lower bound");
                loadMoreCalendar(false);
            } else {
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                if (cal.getTimeInMillis() > mUpperBound) {
                    Logger.log("calendar time greater than upper bound");
                    loadMoreCalendar(true);
                }
            }
            */
        }
    };

    private static final long CALDROID_ANIM_DURATION = 400;

    /**
     * Expand the calendar top sheet containing the CaldroidFragment.
     * Animates it in and updates the toolbar title to show the month.
     */
    private void showCalendar() {

        // Set status boolean
        mCalendarIsShowing = true;

        // Change toolbar title from "Events" to the month shown in the calendar
        // Flip the arrow
        //setToolbarTitle(getTitleStringFromDate(mYear, mMonth+1));
        ((TextView) mToolbarTitle.findViewById(android.R.id.text1)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner_triangle_up, 0);

        // Animate calendar entrance
        mCaldroidFrame.animate().setDuration(CALDROID_ANIM_DURATION)
                .translationY(0)
                .setInterpolator(mDecelerateInterpolator);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // In lollipop, remove toolbar elevation to give the appearance
            // that it is "transferred" to the CaldroidFragment
            ((MainActivity) getActivity()).getToolbar().setElevation(0);
        } else {
            // In pre-lollipop there is no elevation attribute,
            // so animate the replica shadow along with the CaldroidFragment
            int height = mCaldroidFrame.getHeight();
            mToolbarShadow.animate().setDuration(CALDROID_ANIM_DURATION)
                    .translationY(height)
                    .setInterpolator(mDecelerateInterpolator);
        }
    }

    private void hideCalendar() {

        // Set status boolean
        mCalendarIsShowing = false;

        // Change toolbar title back to "Events"
        // Flip the arrow
        //setToolbarTitle("Events");
        ((TextView) mToolbarTitle.findViewById(android.R.id.text1)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner_triangle, 0);

        // Animate calendar exit
        // In lollipop and higher, "transfer" elevation back to toolbar
        // at the end of the exit animation. In pre-lollipop, simply
        // animate the replica shadow up along with the CaldroidFragment.
        int height = mCaldroidFrame.getHeight();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCaldroidFrame.animate().setDuration(CALDROID_ANIM_DURATION)
                    .translationY(-height)
                    .setInterpolator(mAccelerateInterpolator)
                    .withEndAction(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void run() {
                            ((MainActivity) getActivity()).getToolbar().setElevation(Util.convertDpToPixel(4, getActivity()));
                        }
                    });
        } else {
            mCaldroidFrame.animate().setDuration(CALDROID_ANIM_DURATION)
                    .translationY(-height)
                    .setInterpolator(mAccelerateInterpolator);

            mToolbarShadow.animate().setDuration(CALDROID_ANIM_DURATION)
                    .translationY(0)
                    .setInterpolator(mAccelerateInterpolator);
        }
    }

    /**
     * Perform full-text search (FTS) for specified query
     *
     * @param query
     *          String to search for
     */
    public void doSearch(final String query) {

        // TODO debug toast, remove before release
        Toast.makeText(getActivity(), "Events: " + query, Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Perform FTS query
                CalendarDatabase db = CalendarDatabase.getInstance(getActivity());
                final List<GCalEvent> events = db.search(query);

                // Load resulting list into ListView
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        mAdapter.addToEnd(events);
                        mAdapter.setShowSearchResultsMode(true);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
            if (mCalendarIsShowing) {
                hideCalendar();
            }
            mScrolling = true;
        } else {
            mScrolling = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mAdapter != null && mAdapter.getCount() > 0) {

            // TODO move the month-change to occur when expanding calendar

            // Change the calendar month (out of view) to the
            // month of the first visible event in the list
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

            // TODO mechanism for controlling when to append-load at when top/end of list is reached
            // TODO disabled for now

            /*if (mScrolling) {
                int loadMore = -1;
                if (mListView != null && mListView.getChildAt(mListView.getChildCount() - 1) != null
                        && mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1
                        && mListView.getChildAt(mListView.getChildCount() - 1).getBottom() <= mListView.getHeight()) {
                    //Logger.log("Reached the end of agenda");
                    loadMore = END;
                } else if (mListView != null && mListView.getChildAt(0) != null
                        && mListView.getFirstVisiblePosition() == 0
                        && mListView.getChildAt(0).getTop() >= 0) {
                    //Logger.log("Reached the top of agenda");
                    loadMore = FRONT;
                }

                switch (loadMore) {
                    case FRONT:
                        //Logger.log("Processing FRONT case");
                        loadMoreCalendar(false);
                        mScrolling = false;
                        break;
                    case END:
                        //Logger.log("Processing END case");
                        loadMoreCalendar(true);
                        mScrolling = false;
                        break;
                }
            }*/
        }
    }
}
