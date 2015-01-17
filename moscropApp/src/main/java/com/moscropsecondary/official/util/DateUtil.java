package com.moscropsecondary.official.util;

import android.text.format.Time;

import com.moscropsecondary.official.calendar.GCalEvent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by ivon on 9/16/14.
 */
public class DateUtil {

    public static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    public static int getJulianDayFromCalendar(Calendar calendar) {
        TimeZone tz = TimeZone.getDefault();
        return Time.getJulianDay(calendar.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(calendar.getTimeInMillis())));
    }

    /**
     * Use this one for generic dates
     * @param dateStr
     * @return
     */
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

    /**
     * Use this one for Google Calendar dates
     * @param dateStr
     * @param dateOnly
     * @return
     */
    public static Date parseRCF339Date(String dateStr, boolean dateOnly) {
        try {
            if (dateStr.endsWith("Z")) {         // End in Z means no time zone
                SimpleDateFormat noTimeZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                return noTimeZoneFormat.parse(dateStr);
            } else {
                if(!dateOnly) {     // Proper RCF 3339 format with time zone
                    SimpleDateFormat withTimeZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ");
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

    public static String formatEventDuration(GCalEvent event) {

        String duration;

        long startMillis = event.startTime;
        long endMillis = event.endTime;

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

                // All day event. Self explanatory.
                duration = "All day";

            } else {

                // Complete days events, but span multiple days

                String startStr;
                String endStr;

                DateFormat dfNoYear = new SimpleDateFormat("MMM dd", Locale.getDefault());
                DateFormat dfWithYear = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                // Subtract 1 to prevent events ending at 12AM the day
                // after its "real" end date from being counted as an
                // event on that next day
                endDate.setTime(endDate.getTime() - 1);

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

            // Events that have specific hour/minute start/ends.

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

        return duration;
    }

    /**
     * Return millis since epoch in UTC time given days since epoch in the default (current) time zone
     */
    public static long millisFromDays(int day) {
        long millis = day * DAY_MILLIS;
        long offset = TimeZone.getDefault().getOffset(millis);
        return millis - offset;
    }

    /**
     * Returns days since epoch in the default (current) time zone given number of millis since epoch in UTC time
     */
    public static int daysFromMillis(long millis) {
        long offset = TimeZone.getDefault().getOffset(millis);
        millis += offset;
        return (int) (millis / DAY_MILLIS);
    }

    public static String getRelativeTime(long time) {

        String timestamp = "";

        long nowMillis = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(nowMillis);

        long postMillis = time;
        Calendar post = Calendar.getInstance();
        post.setTimeInMillis(postMillis);

        long diffMillis = nowMillis - postMillis;

        if (diffMillis < 60*60*1000) {
            long minAgo = diffMillis / (60*1000);
            timestamp = minAgo + " minutes ago";
        } else if (diffMillis < 24*60*60*1000) {
            long hoursAgo = diffMillis / (60*60*1000);
            timestamp = hoursAgo + " hours ago";
        } else if (post.get(Calendar.DAY_OF_MONTH) == calOneDayAgo(now)) {
            timestamp = "Yesterday";
        } else {
            long daysBetween = daysBetween(post, now);
            if (daysBetween <= 7) {
                timestamp = daysBetween + " days ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
                timestamp = sdf.format(new Date(postMillis));
            }
        }

        return timestamp;
    }

    private static int calOneDayAgo(Calendar cal) {     // Helper method for getRelativeTime()
        cal.setTimeInMillis(cal.getTimeInMillis() - (24*60*60*1000));
        int date = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTimeInMillis(cal.getTimeInMillis() + (24*60*60*1000));
        return date;
    }

    /**
     * Calculates the number of days between two Calendar dates.
     * @param cal1
     *          Calendar date that occurs first
     * @param cal2
     *          Calendar object that occurs second
     * @return  Number of days between cal1 and cal2
     */
    private static long daysBetween(Calendar cal1, Calendar cal2) {     // Helper method for getRelativeTime()
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(cal1.getTimeInMillis());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long millis1 = cal.getTimeInMillis();

        cal.setTimeInMillis(cal2.getTimeInMillis());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long millis2 = cal.getTimeInMillis();

        long numDays = (millis2 - millis1) / (24*60*60*1000);
        return numDays;
    }

}
