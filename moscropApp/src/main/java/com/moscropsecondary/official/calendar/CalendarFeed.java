package com.moscropsecondary.official.calendar;

import java.util.List;

/**
 * Created by ivon on 12/25/14.
 */
public class CalendarFeed {

    public final String version;
    public final List<GCalEvent> events;

    public CalendarFeed(String version, List<GCalEvent> events) {
        this.version = version;
        this.events = events;
    }
}