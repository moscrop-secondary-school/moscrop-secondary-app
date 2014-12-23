package com.moscropsecondary.official.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.util.DateUtil;
import com.moscropsecondary.official.util.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ivon on 9/7/14.
 */
public class EventListAdapter extends BaseAdapter {

    public static class Day {
        public final int dayNumber;
        public final List<GCalEvent> events;

        public Day(int dayNumber) {
            this.dayNumber = dayNumber;
            this.events = new ArrayList<GCalEvent>();
        }
    }

    private Context mContext;
    private List<Day> mDays = new ArrayList<Day>();
    private HashMap<Integer, Integer> mDayMap = new HashMap<>();

    public EventListAdapter(Context context, List<GCalEvent> events) {
        mContext = context;
        addAll(events);
    }

    public void addAll(List<GCalEvent> events) {
        for (GCalEvent event : events) {
            add(event);
        }
    }

    public void add(GCalEvent event) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(event.startTime);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        // Set starting date to be date of first event
        int dayNumber = DateUtil.daysFromMillis(cal.getTimeInMillis());

        // Convert event end to dayNumber
        // Subtract 1 to prevent events ending at 0:00:00.000 (midnight) from counting as being on that day
        int eventEndDayNumber = DateUtil.daysFromMillis(event.endTime-1);

        // Make sure to include every day within the span of the event
        while (dayNumber <= eventEndDayNumber) {

            Day day;

            // Check if a Day object already exists for this date
            Integer position = mDayMap.get(dayNumber);

            if (position == null) {     // Position will be null if this date has not been added

                // Day object doesn't exist yet, create a new Day and add it to list and HashMap
                day = new Day(dayNumber);
                mDayMap.put(dayNumber, mDays.size());
                mDays.add(day);

            } else {                    // Else, the day exists
                // Retrieve existing Day object from list
                day = mDays.get(position);
            }

            // Add this event to Day's event list
            day.events.add(event);

            dayNumber++;
        }
    }

    public void clear() {
        mDays.clear();
        mDayMap.clear();
    }

    public int getPositionNearestToDay(int day) {
        while (day < getItem(mDays.size()-1).dayNumber) {
            Integer position = mDayMap.get(day);
            if (position != null) {
                return position;
            }
            day++;
        }
        return getItem(mDays.size()-1).dayNumber;
    }

    @Override
    public int getCount() {
        return mDays.size();
    }

    @Override
    public Day getItem(int position) {
        return mDays.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).dayNumber;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.day_list_item, null);
        }

        Day day = mDays.get(position);

        TextView dayNumberText = (TextView) view.findViewById(R.id.day_number);
        TextView dayMonthText = (TextView) view.findViewById(R.id.day_month);
        LinearLayout dayEventsGroup = (LinearLayout) view.findViewById(R.id.day_events);

        long dayMillis = DateUtil.millisFromDays(day.dayNumber);
        Date date = new Date(dayMillis);

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM yyyy");

        dayNumberText.setText(sdfDay.format(date));
        dayMonthText.setText(sdfMonth.format(date));
        dayEventsGroup.removeAllViews();

        for (GCalEvent event : day.events) {
            View eventView = inflater.inflate(R.layout.event_list_item, null);

            TextView titleText = (TextView) eventView.findViewById(R.id.event_title);
            titleText.setText(event.title);

            TextView subtitleText = (TextView) eventView.findViewById(R.id.event_subtitle);
            String duration = DateUtil.formatEventDuration(event);
            subtitleText.setText(duration);

            dayEventsGroup.addView(eventView);
        }

        return view;
    }
}
