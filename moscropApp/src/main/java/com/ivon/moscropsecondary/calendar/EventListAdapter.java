package com.ivon.moscropsecondary.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.tyczj.extendedcalendarview.Event;

import java.util.List;

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

        TextView titleText = (TextView) view.findViewById(R.id.event_title);
        Event event = mEvents.get(position);
        titleText.setText(event.getTitle());

        return view;
    }
}
