package com.moscropsecondary.official.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.moscropsecondary.official.MainActivity;
import com.moscropsecondary.official.R;
import com.moscropsecondary.official.util.DateUtil;
import com.moscropsecondary.official.util.Logger;
import com.moscropsecondary.official.util.Preferences;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment
        implements AdapterView.OnItemClickListener {

    public static final String MOSCROP_CALENDAR_ID = "moscropsecondaryschool@gmail.com";
    public static final String MOSCROP_CALENDAR_JSON_URL = "http://www.google.com/calendar/feeds/moscropsecondaryschool@gmail.com/public/full?alt=json&max-results=1000&orderby=starttime&sortorder=descending&singleevents=true";

    private static final String KEY_POSITION = "position";
    private int mPosition;
    private View mContentView;
    private Date mSelectedDate;

    private CaldroidFragment mCaldroid;
    private ListView mListView;
    private TextView mHeaderView;
    private TextView mFooterView;

    private int mYear = -1;
    private int mMonth = -1;     // java.util.Calendar months. One less than actual month.

    private List<GCalEvent> mEvents = new ArrayList<GCalEvent>();
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
        mAdapter = new EventListAdapter(getActivity(), mEvents);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        mHeaderView = new TextView(getActivity());
        mHeaderView.setTextSize(20);
        mListView.addHeaderView(mHeaderView, null, false);
        mFooterView = new TextView(getActivity());
        mFooterView.setText("No events planned for today");

        mCaldroid = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar today = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, today.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, today.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
        mCaldroid.setArguments(args);
        mCaldroid.setCaldroidListener(mCaldroidListener);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.calendar_frame, mCaldroid).commit();

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
    }

    private void downloadCalendar() {

        // Check if provider is empty
        CalendarDatabase db = new CalendarDatabase(getActivity());
        int count = db.getCount();
        db.close();

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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCaldroidListener.onChangeMonth(mMonth + 1, mYear);
                    mCaldroidListener.onSelectDate(Calendar.getInstance().getTime(), null);
                }
            });
        }
    }

    private void loadEventsIntoCaldroid() {
        CalendarDatabase db = new CalendarDatabase(getActivity());
        List<GCalEvent> events = db.getEventsForMonth(mYear, mMonth);
        db.close();

        for (GCalEvent event : events) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(event.startTime);                                           // Set cal to event start time
            while (cal.getTimeInMillis() < event.endTime-1) {                               // Loop through days within event range
                Date date = cal.getTime();
                mCaldroid.setBackgroundResourceForDate(R.color.primary_dark_green, date);   // Set background color of this day
                cal.setTimeInMillis(cal.getTimeInMillis() + 24*60*60*1000);                 // Increment calendar by a day
            }
        }
    }

    final CaldroidListener mCaldroidListener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            if (date != null) {
                // Only update if there isn't an existing selected date or
                // if the new date differs from the existing selected date
                if (mSelectedDate == null || !date.equals(mSelectedDate)) {
                    mSelectedDate = date;
                    updateEventsList(date);
                }
            } else {
                Logger.log("Selected day is null");
            }
        }

        @Override
        public void onChangeMonth(final int month, final int year) {
            Logger.log("Change month: " + month + ", " + year);
            mMonth = month-1;
            mYear = year;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mYear == year && mMonth == month-1) {
                        loadEventsIntoCaldroid();
                        mCaldroid.refreshView();
                    }
                }
            }, 1000);
        }
    };

    private void updateEventsList(Date date) {

        Logger.log("Updating events list");
        mEvents.clear();

        CalendarDatabase db = new CalendarDatabase(getActivity());
        List<GCalEvent> events = db.getEventsForDay(date);
        for (GCalEvent event : events) {
            mEvents.add(event);
        }

        mListView.removeFooterView(mFooterView);
        if (mEvents.size() == 0) {
            mListView.addFooterView(mFooterView, null, false);
        }

        if(mHeaderView != null) {
            String dateStr;
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                dateStr = String.format("%s %d", DateUtil.getMonthName(cal.get(Calendar.MONTH), true), cal.get(Calendar.DAY_OF_MONTH));
            } else {
                dateStr = String.format("%s %d, %d", DateUtil.getMonthName(cal.get(Calendar.MONTH), true), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR));
            }
            mHeaderView.setText(dateStr);
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (view != mHeaderView && view != mFooterView) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.event_dialog, null);

            GCalEvent event = mEvents.get(position - 1);  // -1 to account for header view
            String title = event.title;
            String duration = DateUtil.formatEventDuration(event);
            String description = event.description;
            String location = event.location;

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
}
