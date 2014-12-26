package com.moscropsecondary.official.calendar;

import java.util.List;

/**
 * Created by ivon on 9/16/14.
 */
public class CalendarResult {

    public static final int RESULT_OK           = 0;
    public static final int RESULT_REDUNDANT    = 1;
    public static final int RESULT_REDO_ONLINE  = 2;
    public static final int RESULT_FAIL         = 3;

    public final String version;
    public final int resultCode;
    public final List<GCalEvent> events;
    public final int mode;

    public CalendarResult(String version, int resultCode, List<GCalEvent> events, int mode) {
        this.version = version;
        this.resultCode = resultCode;
        this.events = events;
        this.mode = mode;
    }
}
