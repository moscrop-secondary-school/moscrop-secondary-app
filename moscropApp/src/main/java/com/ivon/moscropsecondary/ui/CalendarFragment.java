package com.ivon.moscropsecondary.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivon.moscropsecondary.R;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CalendarFragment extends Fragment {

    private int mPosition;
    private View mContentView;

    private ExtendedCalendarView mCalendarView;
    
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

        insertDays();

        mCalendarView = (ExtendedCalendarView) mContentView.findViewById(R.id.calendar);
        mCalendarView.setGesture(ExtendedCalendarView.LEFT_RIGHT_GESTURE);

    	return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
    }

    private void insertDays() {

        /*
        // Delete all rows and replace with updated data
        getActivity().getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);
        */

        // temporary for testing
        Cursor c = getActivity().getContentResolver().query(CalendarProvider.CONTENT_URI, null, null, null, null);
        int count = c.getCount();
        c.close();
        if(count > 0) {
            return;
        }

        // Insert updated data
        ContentValues values = new ContentValues();
        values.put(CalendarProvider.COLOR, Event.COLOR_RED);
        values.put(CalendarProvider.DESCRIPTION, "Eat moon cakes :D");
        values.put(CalendarProvider.LOCATION, "Home");
        values.put(CalendarProvider.EVENT, "Mid-Autumn Festival");

        Calendar cal = Calendar.getInstance();

        cal.set(2014, Calendar.SEPTEMBER, 6, 8, 0);
        values.put(CalendarProvider.START, cal.getTimeInMillis());
        values.put(CalendarProvider.START_DAY, getJulianDayFromCalendar(cal));

        cal.set(2014, Calendar.SEPTEMBER, 8, 20, 5);
        values.put(CalendarProvider.END, cal.getTimeInMillis());
        values.put(CalendarProvider.END_DAY, getJulianDayFromCalendar(cal));

        getActivity().getContentResolver().insert(CalendarProvider.CONTENT_URI, values);
    }

    private int getJulianDayFromCalendar(Calendar calendar) {
        TimeZone tz = TimeZone.getDefault();
        return Time.getJulianDay(calendar.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(calendar.getTimeInMillis())));
    }
}
