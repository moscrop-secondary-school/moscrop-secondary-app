package com.ivon.moscropsecondary.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.calendar.CalendarParser;
import com.ivon.moscropsecondary.calendar.CalendarParser.GCalEvent;
import com.ivon.moscropsecondary.calendar.EventListAdapter;
import com.ivon.moscropsecondary.util.Logger;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CalendarFragment extends Fragment implements ExtendedCalendarView.OnDaySelectListener {

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
            String date = String.format("%s %d", getMonthName(day.getMonth(), true), day.getDay());
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

    /*
    private void insertDays() {

        // temporary for testing
        Cursor c = getActivity().getContentResolver().query(CalendarProvider.CONTENT_URI, null, null, null, null);
        int count = c.getCount();
        c.close();
        if(count > 0) {
            return;
        }

        // Insert updated data
        ContentValues values = new ContentValues();
        values.put(CalendarProvider.COLOR, Event.DEFAULT_EVENT_ICON);
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
    */

    private int getJulianDayFromCalendar(Calendar calendar) {
        TimeZone tz = TimeZone.getDefault();
        return Time.getJulianDay(calendar.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(calendar.getTimeInMillis())));
    }

    private void doJsonStuff() {

        // temporary for testing
        Cursor c = getActivity().getContentResolver().query(CalendarProvider.CONTENT_URI, null, null, null, null);
        int count = c.getCount();
        c.close();
        if(count > 0) {
            return;
        }

        Logger.log("Trying to receive events list");
        try {
            List<GCalEvent> events = CalendarParser.getCalendarFeed(MOSCROP_CALENDAR_JSON_URL).events;
            Logger.log("Received events list");

            if (events != null) {
                Logger.log("Events list is not null");
                ContentValues[] valueArray = new ContentValues[events.size()];
                int i = 0;
                for (GCalEvent event : events) {
                    Logger.log("doJsonStuff: iterating through lists; Event number " + (i+1));

                    ContentValues values = new ContentValues();
                    values.put(CalendarProvider.COLOR, Event.DEFAULT_EVENT_ICON);
                    values.put(CalendarProvider.DESCRIPTION, event.content);
                    values.put(CalendarProvider.LOCATION, event.where);
                    values.put(CalendarProvider.EVENT, event.title);

                    Calendar cal = new GregorianCalendar();
                    Date date = parseRCF339Date(event.startTimeRCF);
                    cal.setTime(date);

                    values.put(CalendarProvider.START, cal.getTimeInMillis());
                    values.put(CalendarProvider.START_DAY, getJulianDayFromCalendar(cal));

                    date = parseRCF339Date(event.endTimeRCF);
                    cal.setTime(date);

                    values.put(CalendarProvider.END, cal.getTimeInMillis());
                    values.put(CalendarProvider.END_DAY, getJulianDayFromCalendar(cal));

                    valueArray[i++] = values;
                    Logger.log("Adding contentvalue to array");
                }
                getActivity().getContentResolver().bulkInsert(CalendarProvider.CONTENT_URI, valueArray);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCalendarView.refreshCalendar();
                        Logger.log("done loading");
                    }
                });
            }
        } catch (JSONException e) {
            Logger.error("CalendarFragment.doJsonStuff()", e);
        }
    }

    private Date parseRCF339Date(String dateStr) {
        try {
            if (dateStr.endsWith("Z")) {         // End in Z means no time zone
                SimpleDateFormat noTimeZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                return noTimeZoneFormat.parse(dateStr);
            } else {
                if(dateStr.length() >= 28) {     // Proper RCF 3339 format with time zone
                    SimpleDateFormat withTimeZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
                    return withTimeZoneFormat.parse(dateStr);
                } else {                        // Format uncertain, only take common substring
                    SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String substring = dateStr.substring(0, 10);
                    return shortDateFormat.parse(substring);
                }
            }
        } catch (ParseException e) {
            Logger.error("CalendarFragment.parseRCF3339Date() with dateStr = " + dateStr, e);
        }
        return null;
    }
}
