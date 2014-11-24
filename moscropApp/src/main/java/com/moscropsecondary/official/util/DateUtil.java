package com.moscropsecondary.official.util;

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
/*
    public static String formatEventDuration(Event event) {

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
    }*/
}
