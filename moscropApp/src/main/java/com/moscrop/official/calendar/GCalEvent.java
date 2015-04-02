package com.moscrop.official.calendar;

/**
 * Created by ivon on 9/16/14.
 */
public class GCalEvent {
    public final String title;
    public final String description;
    public final String location;
    public final long startTime;
    public final long endTime;

    public GCalEvent(String title, String description, String location, long startTime, long endTime) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}