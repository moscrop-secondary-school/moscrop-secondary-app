package com.ivon.moscropsecondary.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.calendar.CalendarParser;
import com.ivon.moscropsecondary.calendar.EventListAdapter;
import com.ivon.moscropsecondary.util.Logger;
import com.ivon.moscropsecondary.util.Preferences;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment implements ExtendedCalendarView.OnDaySelectListener {

    public static final String MOSCROP_CALENDAR_ID = "moscroppanthers@gmail.com";
    public static final String MOSCROP_CALENDAR_JSON_URL = "http://www.google.com/calendar/feeds/moscroppanthers@gmail.com/public/full?alt=json&max-results=1000&orderby=starttime&sortorder=descending&singleevents=true";

    private int mPosition;
    private View mContentView;
    private Day mSelectedDay;

    private ExtendedCalendarView mCalendarView;
    private ListView mListView;
    private TextView mHeaderView;
    private TextView mFooterView;

    private List<Event> mEvents = new ArrayList<Event>();
    private EventListAdapter mAdapter;
    
    public static CalendarFragment newInstance(int position) {
    	CalendarFragment fragment = new CalendarFragment();
        fragment.mPosition = position;
    	return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);
    	
    	mContentView = inflater.inflate(R.layout.fragment_events, container, false);

        //insertDays();

        mListView = (ListView) mContentView.findViewById(R.id.daily_events_list);
        mAdapter = new EventListAdapter(getActivity(), mEvents);
        mListView.setAdapter(mAdapter);

        mHeaderView = new TextView(getActivity());
        mHeaderView.setTextSize(20);
        mListView.addHeaderView(mHeaderView);
        mFooterView = new TextView(getActivity());
        mFooterView.setText("No events planned for today");

        mCalendarView = (ExtendedCalendarView) mContentView.findViewById(R.id.calendar);
        mCalendarView.setGesture(ExtendedCalendarView.LEFT_RIGHT_GESTURE);
        mCalendarView.setOnDaySelectListener(this);

        // JSON Testing stuff
        new Thread(new Runnable() {
            @Override
            public void run() {
                doJsonStuff();
            }
        }).start();

    	return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
    }

    @Override
    public void onDaySelected(Day day) {
        if (day != null) {
            Logger.log("Selected day " + day.getDay());
            // Check if day is valid (>0) and is not the same as the already selected day
            // If the already selected day is null then nothing has been set yet, proceed
            if (mSelectedDay == null
                    || (day.getDay() > 0 &&
                        !(day.getDay() == mSelectedDay.getDay() && day.getMonth() == mSelectedDay.getMonth() && day.getYear() == mSelectedDay.getYear())
                        )
                    ) {
                mSelectedDay = day;
                updateEventsList(day);
            }
        } else {
           Logger.log("Selected day is null");
        }
    }

    private void updateEventsList(Day day) {

        Logger.log("Updating events list");
        mEvents.clear();

        List<Event> dayEvents = day.getEvents();
        for (Event event : dayEvents) {
            mEvents.add(event);
        }

        mListView.removeFooterView(mFooterView);
        if (mEvents.size() == 0) {
            mListView.addFooterView(mFooterView);
        }

        if(mHeaderView != null) {
            String date;

            if (day.getYear() == Calendar.getInstance().get(Calendar.YEAR)) {
                date = String.format("%s %d", getMonthName(day.getMonth(), true), day.getDay());
            } else {
                date = String.format("%s %d, %d", getMonthName(day.getMonth(), true), day.getDay(), day.getYear());
            }
            mHeaderView.setText(date);
        }

        mAdapter.notifyDataSetChanged();
    }

    private String getMonthName(int month, boolean shortened) {
        if (shortened) {
            switch (month) {
                case Calendar.JANUARY:
                    return "Jan";
                case Calendar.FEBRUARY:
                    return "Feb";
                case Calendar.MARCH:
                    return "Mar";
                case Calendar.APRIL:
                    return "Apr";
                case Calendar.MAY:
                    return "May";
                case Calendar.JUNE:
                    return "Jun";
                case Calendar.JULY:
                    return "Jul";
                case Calendar.AUGUST:
                    return "Aug";
                case Calendar.SEPTEMBER:
                    return "Sep";
                case Calendar.OCTOBER:
                    return "Oct";
                case Calendar.NOVEMBER:
                    return "Nov";
                case Calendar.DECEMBER:
                    return "Dec";
            }
        } else {
            switch (month) {
                case Calendar.JANUARY:
                    return "January";
                case Calendar.FEBRUARY:
                    return "February";
                case Calendar.MARCH:
                    return "March";
                case Calendar.APRIL:
                    return "April";
                case Calendar.MAY:
                    return "May";
                case Calendar.JUNE:
                    return "June";
                case Calendar.JULY:
                    return "July";
                case Calendar.AUGUST:
                    return "August";
                case Calendar.SEPTEMBER:
                    return "September";
                case Calendar.OCTOBER:
                    return "October";
                case Calendar.NOVEMBER:
                    return "November";
                case Calendar.DECEMBER:
                    return "December";
            }
        }
        return "";
    }

    private void doJsonStuff() {

        // Check if provider is empty
        Cursor c = getActivity().getContentResolver().query(CalendarProvider.CONTENT_URI, null, null, null, null);
        int count = c.getCount();
        c.close();

        SharedPreferences prefs = getActivity().getSharedPreferences(Preferences.Calendar.NAME, Context.MODE_MULTI_PROCESS);
        long lastUpdateMillis = prefs.getLong(Preferences.Calendar.Keys.LAST_UPDATED, Preferences.Calendar.Default.LAST_UPDATED);
        String lastGcalVersion = prefs.getString(Preferences.Calendar.Keys.GCAL_VERSION, Preferences.Calendar.Default.GCAL_VERSION);

        if((count == 0)
                || (lastUpdateMillis == Preferences.Calendar.Default.LAST_UPDATED)
                || (lastGcalVersion.equals(Preferences.Calendar.Default.GCAL_VERSION))
                ) {

            // Provider is empty
            // Or, if last update info is missing, to be safe,
            // we will reload everything. Make sure data is up to date.
            CalendarParser.processAll(getActivity(), MOSCROP_CALENDAR_ID);

        } else {

            // Everything good to go! Functioning normally.

            // If last update time is in the future for some reason,
            // assume last update time is now so we can recheck everything
            // between now and the last updated time, which is somehow
            // in the future. Probably aliens. (Actually, very likely due to timezones)
            if(lastUpdateMillis > System.currentTimeMillis()) {
                lastUpdateMillis = System.currentTimeMillis();
            }

            CalendarParser.process(getActivity(), MOSCROP_CALENDAR_ID, lastUpdateMillis, lastGcalVersion);
        }

        // Update UI when done loading
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCalendarView.refreshCalendar();
                Logger.log("done loading");
            }
        });
    }
}
