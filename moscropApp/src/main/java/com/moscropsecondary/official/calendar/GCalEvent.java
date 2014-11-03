package com.moscropsecondary.official.calendar;

/**
 * Created by ivon on 9/16/14.
 */
public class GCalEvent {
    public final String title;
    public final String content;
    public final String where;
    public final String startTimeRCF;
    public final String endTimeRCF;

    public GCalEvent(String title, String content, String where, String startTimeRCF, String endTimeRCF) {
        this.title = title;
        this.content = content;
        this.where = where;
        this.startTimeRCF = startTimeRCF;
        this.endTimeRCF = endTimeRCF;
    }
}