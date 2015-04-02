package com.moscrop.official.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moscrop.official.R;
import com.moscrop.official.util.DateUtil;

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
        addToEnd(events);
    }

    public void addToEnd(List<GCalEvent> events) {
        for (GCalEvent event : events) {
            add(event, false);
        }
    }

    public void addToFront(List<GCalEvent> events) {
        for (int i=events.size()-1; i>=0; i--) {
            GCalEvent event = events.get(i);
            add(event, true);
        }
    }

    /**
     * Add an event to the list.
     *
     * This method will add the event to
     * the corresponding day in the list,
     * or add a new day into the list if
     * there are no events yet for that day.
     *
     * @param event
     *          Event to add to the list
     * @param addToFront
     *          If the event day is not in the list yet,
     *          true will add the event day to the front of the list
     *          and false will append it to the end of the list
     */
    public void add(GCalEvent event, boolean addToFront) {

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
                if (addToFront) {
                    mDays.add(0, day);
                } else {
                    mDays.add(day);
                }

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

    /**
     * Get the list position of the nearest day
     * after the specified day that contains an event
     *
     * @param day
     *          Specified day
     * @return  position in list
     */
    public int getPositionNearestToDay(int day) {
        if (mDays.size() > 0) {
            while (day < getItem(mDays.size() - 1).dayNumber) {
                Integer position = mDayMap.get(day);
                if (position != null) {
                    return position;
                }
                day++;
            }
            return mDays.size() - 1;
        } else {
            return -1;
        }
    }

    /**
     * Get number of days that contain events
     */
    @Override
    public int getCount() {
        return mDays.size();
    }

    /**
     * Get the Day object in that position
     *
     * NOTE: does not return GCalEvent objects,
     * although the Day object contains a list
     * of GCalEvents.
     */
    @Override
    public Day getItem(int position) {
        return mDays.get(position);
    }

    /**
     * Gets the day number for the day in that position
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).dayNumber;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        for (final GCalEvent event : day.events) {
            View eventView = inflater.inflate(R.layout.event_list_item, null);

            TextView titleText = (TextView) eventView.findViewById(R.id.event_title);
            titleText.setText(event.title);

            TextView subtitleText = (TextView) eventView.findViewById(R.id.event_subtitle);
            String duration = DateUtil.formatEventDuration(event);
            subtitleText.setText(duration);

            eventView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEventDialog(inflater, event);
                }
            });

            dayEventsGroup.addView(eventView);
        }

        return view;
    }

    private void showEventDialog(LayoutInflater inflater, GCalEvent event) {

        View dialogView = inflater.inflate(R.layout.event_dialog, null);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
