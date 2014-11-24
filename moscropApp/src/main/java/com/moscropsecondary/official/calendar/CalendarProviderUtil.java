package com.moscropsecondary.official.calendar;

/**
 * Util used to handle interactions with
 * ExtendedCalendarView's content provider.
 *
 * Created by ivon on 9/16/14.
 */
public class CalendarProviderUtil {

    /*public static int deleteAll(Context context) {
        Logger.log("Deleting all");
        return context.getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);
    }

    public static int deleteAfterTime(Context context, long time) {
        Logger.log("Deleting after " + time);
        return context.getContentResolver().delete(CalendarProvider.CONTENT_URI, CalendarProvider.END + ">=?", new String[] {String.valueOf(time)});
    }

    public static void saveEventsToProvider(Context context, List<GCalEvent> events) {
        Logger.log("Saving events");
        ContentValues[] valueArray = new ContentValues[events.size()];
        int i = 0;
        for (GCalEvent event : events) {

            ContentValues values = new ContentValues();
            values.put(CalendarProvider.COLOR, Event.COLOR_BLUE);
            values.put(CalendarProvider.DESCRIPTION, event.content);
            values.put(CalendarProvider.LOCATION, event.where);
            values.put(CalendarProvider.EVENT, event.title);

            Calendar cal = new GregorianCalendar();
            Date date = DateUtil.parseRCF339Date(event.startTimeRCF);
            cal.setTime(date);

            values.put(CalendarProvider.START, cal.getTimeInMillis());
            values.put(CalendarProvider.START_DAY, DateUtil.getJulianDayFromCalendar(cal));

            date = DateUtil.parseRCF339Date(event.endTimeRCF);
            cal.setTime(date);

            values.put(CalendarProvider.END, cal.getTimeInMillis());
            values.put(CalendarProvider.END_DAY, DateUtil.getJulianDayFromCalendar(cal));

            valueArray[i++] = values;
        }
        context.getContentResolver().bulkInsert(CalendarProvider.CONTENT_URI, valueArray);
    }*/
}
