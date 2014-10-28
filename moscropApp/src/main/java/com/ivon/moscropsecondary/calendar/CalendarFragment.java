package com.ivon.moscropsecondary.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.MainActivity;
import com.ivon.moscropsecondary.util.DateUtil;
import com.ivon.moscropsecondary.util.Logger;
import com.ivon.moscropsecondary.util.Preferences;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment
        implements ExtendedCalendarView.OnDaySelectListener, AdapterView.OnItemClickListener {

    public static final String MOSCROP_CALENDAR_ID = "moscropsecondaryschool@gmail.com";
    public static final String MOSCROP_CALENDAR_JSON_URL = "http://www.google.com/calendar/feeds/moscropsecondaryschool@gmail.com/public/full?alt=json&max-results=1000&orderby=starttime&sortorder=descending&singleevents=true";

    private static final String KEY_POSITION = "position";
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
        mListView.setOnItemClickListener(this);

        mHeaderView = new TextView(getActivity());
        mHeaderView.setTextSize(20);
        mListView.addHeaderView(mHeaderView);
        mFooterView = new TextView(getActivity());
        mFooterView.setText("No events planned for today");

        mCalendarView = (ExtendedCalendarView) mContentView.findViewById(R.id.calendar);
        mCalendarView.setGesture(ExtendedCalendarView.LEFT_RIGHT_GESTURE);
        mCalendarView.setOnDaySelectListener(this);

        if(savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(KEY_POSITION, mPosition);
        }

        // Refresh calendar
        new Thread(new Runnable() {
            @Override
            public void run() {
                doJsonStuff();
            }
        }).start();

    	return mContentView;
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
                date = String.format("%s %d", DateUtil.getMonthName(day.getMonth(), true), day.getDay());
            } else {
                date = String.format("%s %d, %d", DateUtil.getMonthName(day.getMonth(), true), day.getDay(), day.getYear());
            }
            mHeaderView.setText(date);
        }

        mAdapter.notifyDataSetChanged();
    }

    private void doJsonStuff() {

        // Check if provider is empty
        Cursor c = getActivity().getContentResolver().query(CalendarProvider.CONTENT_URI, null, null, null, null);
        int count = c.getCount();
        c.close();

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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCalendarView.refreshCalendar();
                Logger.log("done loading");
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.event_dialog, null);

        Event event = mEvents.get(position-1);  // -1 to account for header view
        String title = event.getTitle();
        String duration = DateUtil.formatEventDuration(event);
        String description = event.getDescription();
        String location = event.getLocation();

        if (duration != null && !duration.equals("")) {
            View durationGroup = dialogView.findViewById(R.id.view_event_duration_group);
            durationGroup.setVisibility(View.VISIBLE);
            TextView durationText = (TextView) dialogView.findViewById(R.id.view_event_duration);
            durationText.setText(duration);
        } else {
            View durationGroup = dialogView.findViewById(R.id.view_event_duration_group);
            durationGroup.setVisibility(View.GONE);
        }
        
        if (description != null && !description.equals("")) {
            View descriptionGroup = dialogView.findViewById(R.id.view_event_description_group);
            descriptionGroup.setVisibility(View.VISIBLE);
            TextView descriptionText = (TextView) dialogView.findViewById(R.id.view_event_description);
            descriptionText.setText(description);
        } else {
            View descriptionGroup = dialogView.findViewById(R.id.view_event_description_group);
            descriptionGroup.setVisibility(View.GONE);
        }
        
        if (location != null && !location.equals("")) {
            View locationGroup = dialogView.findViewById(R.id.view_event_location_group);
            locationGroup.setVisibility(View.VISIBLE);
            TextView locationText = (TextView) dialogView.findViewById(R.id.view_event_location);
            locationText.setText(location);
        } else {
            View locationGroup = dialogView.findViewById(R.id.view_event_location_group);
            locationGroup.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setView(dialogView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
