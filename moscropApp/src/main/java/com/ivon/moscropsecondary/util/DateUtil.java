package com.ivon.moscropsecondary.util;

import android.text.format.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by ivon on 9/16/14.
 */
public class DateUtil {

    public static int getJulianDayFromCalendar(Calendar calendar) {
        TimeZone tz = TimeZone.getDefault();
        return Time.getJulianDay(calendar.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(calendar.getTimeInMillis())));
    }

    public static Date parseRCF339Date(String dateStr) {
        try {
            if (dateStr.endsWith("Z")) {         // End in Z means no time zone
                SimpleDateFormat noTimeZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                return noTimeZoneFormat.parse(dateStr);
            } else {
                if(dateStr.length() >= 28) {     // Proper RCF 3339 format with time zone
                    SimpleDateFormat withTimeZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
                    return withTimeZoneFormat.parse(dateStr);
                } else {                        // Format uncertain, only take common substring
                    SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String substring = dateStr.substring(0, 10);
                    return shortDateFormat.parse(substring);
                }
            }
        } catch (ParseException e) {
            Logger.error("DateUtil.parseRCF3339Date() with dateStr = " + dateStr, e);
        }
        return null;
    }

    public static String getMonthName(int month, boolean shortened) {
        if (shortened) {
            switch (month) {
                case Calendar.JANUARY:
                    return "Jan";
                case Calendar.FEBRUARY:
                    return "Feb";
                case Calendar.MARCH:
                    return "Mar";
                case Calendar.APRIL:
                    return "Apr";
                case Calendar.MAY:
                    return "May";
                case Calendar.JUNE:
                    return "Jun";
                case Calendar.JULY:
                    return "Jul";
                case Calendar.AUGUST:
                    return "Aug";
                case Calendar.SEPTEMBER:
                    return "Sep";
                case Calendar.OCTOBER:
                    return "Oct";
                case Calendar.NOVEMBER:
                    return "Nov";
                case Calendar.DECEMBER:
                    return "Dec";
            }
        } else {
            switch (month) {
                case Calendar.JANUARY:
                    return "January";
                case Calendar.FEBRUARY:
                    return "February";
                case Calendar.MARCH:
                    return "March";
                case Calendar.APRIL:
                    return "April";
                case Calendar.MAY:
                    return "May";
                case Calendar.JUNE:
                    return "June";
                case Calendar.JULY:
                    return "July";
                case Calendar.AUGUST:
                    return "August";
                case Calendar.SEPTEMBER:
                    return "September";
                case Calendar.OCTOBER:
                    return "October";
                case Calendar.NOVEMBER:
                    return "November";
                case Calendar.DECEMBER:
                    return "December";
            }
        }
        return "";
    }
}
