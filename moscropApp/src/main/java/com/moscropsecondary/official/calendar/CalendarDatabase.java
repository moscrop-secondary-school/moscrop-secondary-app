package com.moscropsecondary.official.calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Util used to handle interactions with
 * ExtendedCalendarView's content provider.
 *
 * Created by ivon on 9/16/14.
 */
public class CalendarDatabase extends SQLiteOpenHelper {

    private SQLiteDatabase mDB;
    private Context mContext;

    private static final String _ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_START = "start";
    private static final String COLUMN_END = "end";

    private static final String NAME = "calendar";
    private static final int VERSION = 1;

    public CalendarDatabase(Context context) {
        super(context, NAME, null, VERSION);
        mContext = context;
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_START + " INTEGER, " +
                COLUMN_END + " INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // N/A
    }

    public int deleteAll() {
        return mDB.delete(NAME, null, null);
    }

    public int deleteAfterTime(long time) {
        return mDB.delete(NAME, COLUMN_END + ">=?", new String[]{String.valueOf(time)});
    }

    public void saveEventsToProvider(List<GCalEvent> events) {
        mDB.beginTransaction();
        try {
            for (GCalEvent event : events) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TITLE, event.title);
                values.put(COLUMN_DESCRIPTION, event.description);
                values.put(COLUMN_LOCATION, event.location);
                values.put(COLUMN_START, event.startTime);
                values.put(COLUMN_END, event.endTime);
                mDB.insert(NAME, null, values);
            }
            mDB.setTransactionSuccessful();
        } finally {
            mDB.endTransaction();
        }
    }

    private GCalEvent fromCursor(Cursor c) {
        String title = c.getString(c.getColumnIndex(COLUMN_TITLE));
        String description = c.getString(c.getColumnIndex(COLUMN_DESCRIPTION));
        String location = c.getString(c.getColumnIndex(COLUMN_LOCATION));
        long startTime = c.getLong(c.getColumnIndex(COLUMN_START));
        long endTime = c.getLong(c.getColumnIndex(COLUMN_END));
        return new GCalEvent(title, description, location, startTime, endTime);
    }

    public List<GCalEvent> getAllEvents() {
        Cursor c = mDB.query(NAME, null, null, null, null, null, null);
        List<GCalEvent> events = new ArrayList<GCalEvent>();
        c.moveToFirst();
        while (c.moveToNext()) {
            events.add(fromCursor(c));
        }
        c.close();

        return events;
    }

    public List<GCalEvent> getEventsForDuration(long lowerBound, long upperBound) {
        // Consider 3 cases:
        // 1. Date contains start of an event
        // 2. Date contains the end of an event
        // 3. Date is contained within a multi-day event

        String selection = "(" + COLUMN_START + ">=" + lowerBound + " AND " + COLUMN_START + "<"  + upperBound + ")"
                + " OR " + "(" + COLUMN_END   + ">"  + lowerBound + " AND " + COLUMN_END   + "<=" + upperBound + ")"
                + " OR " + "(" + COLUMN_START + "<"  + lowerBound + " AND " + COLUMN_END   + ">"  + upperBound + ")";

        String orderBy = COLUMN_START + " ASC";

        Cursor c = mDB.query(NAME, null, selection, null, null, null, orderBy);
        List<GCalEvent> events = new ArrayList<GCalEvent>();
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            events.add(fromCursor(c));
        }
        c.close();
        return events;
    }

    public List<GCalEvent> getEventsForMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long lowerBound = cal.getTimeInMillis();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.setTimeInMillis(cal.getTimeInMillis() + 24*60*60*1000);
        long upperBound = cal.getTimeInMillis();

        return getEventsForDuration(lowerBound, upperBound);
    }

    public List<GCalEvent> getEventsForDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long lowerBound = cal.getTimeInMillis();
        cal.setTimeInMillis(cal.getTimeInMillis() + 24*60*60*1000);
        long upperBound = cal.getTimeInMillis();

        return getEventsForDuration(lowerBound, upperBound);

    }

    public int getCount() {
        Cursor c = mDB.query(NAME, null, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }
}
