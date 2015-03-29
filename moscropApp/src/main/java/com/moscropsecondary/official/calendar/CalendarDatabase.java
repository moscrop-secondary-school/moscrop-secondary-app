package com.moscropsecondary.official.calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

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
    private static final String NAME_FTS = "calendar_fts";
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

        db.execSQL("CREATE VIRTUAL TABLE " + NAME_FTS + " USING fts3 (" +
                _ID + ", " +
                COLUMN_TITLE + ", " +
                COLUMN_DESCRIPTION + ", " +
                COLUMN_LOCATION + ", " +
                COLUMN_START + ", " +
                COLUMN_END + "" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // N/A
    }

    public int deleteAll() {
        return mDB.delete(NAME_FTS, null, null);
    }

    public int deleteAfterTime(long time) {
        return mDB.delete(NAME_FTS, COLUMN_END + ">=?", new String[]{String.valueOf(time)});
    }

    /**
     * Save a list of GCalEvents to the database
     * @param events
     */
    public void saveEventsToDatabase(List<GCalEvent> events) {
        mDB.beginTransaction();
        try {
            for (GCalEvent event : events) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TITLE, event.title);
                values.put(COLUMN_DESCRIPTION, event.description);
                values.put(COLUMN_LOCATION, event.location);
                values.put(COLUMN_START, event.startTime);
                values.put(COLUMN_END, event.endTime);
                mDB.insert(NAME_FTS, null, values);
            }
            mDB.setTransactionSuccessful();
        } finally {
            mDB.endTransaction();
        }
    }

    /**
     * Create a new GCalEvent using data
     * from the cursor at its current position
     */
    private GCalEvent fromCursor(Cursor c) {
        String title = c.getString(c.getColumnIndex(COLUMN_TITLE));
        String description = c.getString(c.getColumnIndex(COLUMN_DESCRIPTION));
        String location = c.getString(c.getColumnIndex(COLUMN_LOCATION));
        long startTime = c.getLong(c.getColumnIndex(COLUMN_START));
        long endTime = c.getLong(c.getColumnIndex(COLUMN_END));
        return new GCalEvent(title, description, location, startTime, endTime);
    }

    /**
     * Retrieve a list of GCalEvents for a given query
     */
    public List<GCalEvent> search(String query) {

        // TODO add a limit to events returned from search
        // TODO to prevent an extremely long list as repetitive
        // TODO events such as 'Spring Break' pile up throughout the years

        /*Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        long lowerBound = cal.getTimeInMillis();

        cal.add(Calendar.YEAR, 2);
        long upperBound = cal.getTimeInMillis();

        String selection = NAME_FTS + " MATCH ? COLLATE NOCASE AND " + COLUMN_END + " > ? AND " + COLUMN_START + " < ?";
        String[] selectionArgs = new String[] { appendWildcard(query), String.valueOf(lowerBound), String.valueOf(upperBound) };*/

        // TODO possibly instead of limiting by time difference,
        // TODO limit instead by number of results returned

        // TODO Old, non-limiting implementation

        String selection = NAME_FTS + " MATCH ? COLLATE NOCASE";
        String[] selectionArgs = new String[] { appendWildcard(query) };

        Cursor c = mDB.query(NAME_FTS, null, selection, selectionArgs, null, null, null);
        List<GCalEvent> events = new ArrayList<GCalEvent>();
        c.moveToFirst();
        while (c.moveToNext()) {
            events.add(fromCursor(c));
        }
        c.close();

        return events;
    }

    /**
     * Helper method used to prepare the query for a full-text search
     */
    private String appendWildcard(String query) {
        if (TextUtils.isEmpty(query)) return query;

        final StringBuilder builder = new StringBuilder();
        final String[] splits = TextUtils.split(query, " ");

        for (String split : splits)
            builder.append(split).append("*").append(" ");

        return builder.toString().trim();
    }

    /**
     * Retrieve a list of all events stored in the database
     */
    public List<GCalEvent> getAllEvents() {
        Cursor c = mDB.query(NAME_FTS, null, null, null, null, null, null);
        List<GCalEvent> events = new ArrayList<GCalEvent>();
        c.moveToFirst();
        while (c.moveToNext()) {
            events.add(fromCursor(c));
        }
        c.close();

        return events;
    }

    /**
     * Retrieve a list of events between the specified bounds
     */
    public List<GCalEvent> getEventsForDuration(long lowerBound, long upperBound) {
        // Consider 3 cases:
        // 1. Date contains start of an event
        // 2. Date contains the end of an event
        // 3. Date is contained within a multi-day event

        String selection = "(" + COLUMN_START + ">=" + lowerBound + " AND " + COLUMN_START + "<"  + upperBound + ")"
                + " OR " + "(" + COLUMN_END   + ">"  + lowerBound + " AND " + COLUMN_END   + "<=" + upperBound + ")"
                + " OR " + "(" + COLUMN_START + "<"  + lowerBound + " AND " + COLUMN_END   + ">"  + upperBound + ")";

        String orderBy = COLUMN_START + " ASC";

        Cursor c = mDB.query(NAME_FTS, null, selection, null, null, null, orderBy);
        List<GCalEvent> events = new ArrayList<GCalEvent>();
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            events.add(fromCursor(c));
        }
        c.close();
        return events;
    }

    /**
     * Retrieve a list of events for a specified month
     */
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

    /**
     * Retrieve a list of events for a specified day
     */
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

    /**
     * Get a count of how many events are in the database
     */
    public int getCount() {
        Cursor c = mDB.query(NAME_FTS, null, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }
}
