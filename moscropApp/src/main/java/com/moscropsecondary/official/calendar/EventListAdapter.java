package com.moscropsecondary.official.calendar;

/**
 * Created by ivon on 9/7/14.
 */
public class EventListAdapter /*extends ArrayAdapter<Event> */{

    /*List<Event> mEvents = null;

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
        String duration = DateUtil.formatEventDuration(event);
        subtitleText.setText(duration);

        return view;
    }*/
}
