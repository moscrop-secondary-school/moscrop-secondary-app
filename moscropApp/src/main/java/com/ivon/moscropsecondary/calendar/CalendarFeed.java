package com.ivon.moscropsecondary.calendar;

import java.util.List;

/**
 * Created by ivon on 9/16/14.
 */
public class CalendarFeed {
    public final String version;
    public final List<GCalEvent> events;

    public CalendarFeed(String version, List<GCalEvent> events) {
        this.version = version;
        this.events = events;
    }
}
