package com.ivon.moscropsecondary.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.tyczj.extendedcalendarview.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ivon on 9/7/14.
 */
public class EventListAdapter extends ArrayAdapter<Event> {

    List<Event> mEvents = null;

    public EventListAdapter(Context context, List<Event> events) {
        super(context, android.R.layout.simple_list_item_1, events);
        mEvents = events;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.event_list_item, null);
        }

        Event event = mEvents.get(position);

        TextView titleText = (TextView) view.findViewById(R.id.event_title);
        titleText.setText(event.getTitle());

        TextView subtitleText = (TextView) view.findViewById(R.id.event_subtitle);

        /*String startTime = event.getStartDate("yyyy-MM-dd HH:mm:ss.SSS");
        String endTime = event.getEndDate("yyyy-MM-dd HH:mm:ss.SSS");*/
        String duration;

        long startMillis = event.getStartMillis();
        long endMillis = event.getEndMillis();

        Date startDate = new Date(startMillis);
        Date endDate = new Date(endMillis);

        Calendar startCal = Calendar.getInstance();
        int currentYear = startCal.get(Calendar.YEAR);  // hijacking startCal to get current year
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        final long millisInADay = 24 * 60 * 60 * 1000;
        long durationMillis = endMillis - startMillis;


        // If duration is a multiple of a day
        if ((startCal.get(Calendar.HOUR_OF_DAY) == 0)
                && (startCal.get(Calendar.MINUTE) == 0)
                && (startCal.get(Calendar.SECOND) == 0)
                && (startCal.get(Calendar.MILLISECOND) == 0)
                && (endCal.get(Calendar.HOUR_OF_DAY) == 0)
                && (endCal.get(Calendar.MINUTE) == 0)
                && (endCal.get(Calendar.SECOND) == 0)
                && (endCal.get(Calendar.MILLISECOND) == 0)
                ) {

            if (durationMillis == millisInADay) {
                duration = "All day";
            } else {
                String startStr;
                String endStr;

                DateFormat dfNoYear = new SimpleDateFormat("MMM dd", Locale.getDefault());
                DateFormat dfWithYear = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                if (startCal.get(Calendar.YEAR) == currentYear) {
                    startStr = dfNoYear.format(startDate);
                } else {
                    startStr = dfWithYear.format(startDate);
                }

                if (endCal.get(Calendar.YEAR) == currentYear) {
                    endStr = dfNoYear.format(endDate);
                } else {
                    endStr = dfWithYear.format(endDate);
                }

                duration = startStr + " - " +  endStr;
            }

        } else {
            String startStr;
            String endStr;

            if ((startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR))
                    && (startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH))
                    && (startCal.get(Calendar.DAY_OF_MONTH) == endCal.get(Calendar.DAY_OF_MONTH))
                    ) {

                DateFormat df = new SimpleDateFormat("h:mm a");
                startStr = df.format(startDate);
                endStr = df.format(endDate);

            } else {
                DateFormat dfNoYear = new SimpleDateFormat("MMM dd, h:mm a");
                DateFormat dfWithYear = new SimpleDateFormat("MMM dd, yyyy, h:mm a");

                if (startCal.get(Calendar.YEAR) == currentYear) {
                    startStr = dfNoYear.format(startDate);
                } else {
                    startStr = dfWithYear.format(startDate);
                }

                if (endCal.get(Calendar.YEAR) == currentYear) {
                    endStr = dfNoYear.format(endDate);
                } else {
                    endStr = dfWithYear.format(endDate);
                }
            }

            duration = startStr + " - " +  endStr;
        }

        subtitleText.setText(duration);

        return view;
    }
}
