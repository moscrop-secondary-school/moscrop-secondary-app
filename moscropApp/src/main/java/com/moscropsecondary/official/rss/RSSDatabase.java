package com.moscropsecondary.official.rss;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.moscropsecondary.official.util.Preferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivon on 24/10/14.
 */
public class RSSDatabase extends SQLiteOpenHelper {

    private SQLiteDatabase mDB;
    private Context mContext;

    private static final String _ID                 = "_id";
    private static final String COLUMN_DATE         = "date";
    private static final String COLUMN_TITLE        = "title";
    private static final String COLUMN_CONTENT      = "content";
    private static final String COLUMN_PREVIEW      = "preview";
    private static final String COLUMN_TAGS         = "tags";
    private static final String COLUMN_URL          = "url";
    private static final String COLUMN_METADATA     = "metadata";

    private static final String NAME = "rssfeeds";
    private static final String NAME_FTS = "rssfeeds_fts";
    private static final int VERSION = 1;

    public RSSDatabase(Context context) {
        super(context, NAME, null, VERSION);
        mContext = context;
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " INTEGER, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_PREVIEW + " TEXT, " +
                COLUMN_TAGS + " TEXT, " +
                COLUMN_URL + " TEXT, " +
                COLUMN_METADATA + " TEXT" + ")");

        db.execSQL("CREATE VIRTUAL TABLE " + NAME_FTS + " USING fts3 (" +
                _ID + ", " +
                COLUMN_DATE + ", " +
                COLUMN_TITLE + ", " +
                COLUMN_CONTENT + ", " +
                COLUMN_PREVIEW + ", " +
                COLUMN_TAGS + ", " +
                COLUMN_URL + ", " +
                COLUMN_METADATA + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // N/A
    }

    /**
     * Get the time of the oldest post in the database
     *
     * @param filterTags
     *          Apply filters to get the time of
     *          the oldest post that matches these filters
     * @return  the time in milliseconds
     */
    public long getOldestPostDate(String[] filterTags) {
        String[] columns = new String[] { COLUMN_DATE };
        String selection = null;
        if (filterTags != null && filterTags.length >= 1) {
            selection = "";
            for (int i=0; i<filterTags.length; i++) {
                String tag = filterTags[i];
                tag = "\'%" + tag + "%\'";
                selection += COLUMN_TAGS + " LIKE " + tag;
                if (i < filterTags.length-1) {
                    selection += " OR ";
                }
            }
        }
        String orderBy = COLUMN_DATE + " asc";
        String limit = "1";

        Cursor c = mDB.query(NAME_FTS, columns, selection, null, null, null, orderBy, limit);
        long oldestPostDate = System.currentTimeMillis();
        if (c.getCount() >= 1) {
            c.moveToPosition(0);
            oldestPostDate = c.getLong(c.getColumnIndex(COLUMN_DATE));
        }
        return oldestPostDate;
    }

    /**
     * Save a list of RSSItems to the database
     */
    public void save(List<RSSItem> items) {
        mDB.beginTransaction();
        try {
            for (RSSItem item : items) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_DATE, item.date);
                values.put(COLUMN_TITLE, item.title);
                values.put(COLUMN_CONTENT, item.content);
                values.put(COLUMN_PREVIEW, item.preview);
                String tags = "";
                for (int i = 0; i < item.tags.length; i++) {
                    tags += item.tags[i];
                    if (!(i == item.tags.length - 1)) {
                        tags += ",";
                    }
                }
                values.put(COLUMN_TAGS, tags);
                values.put(COLUMN_URL, item.url);
                values.put(COLUMN_METADATA, item.metadata);

                mDB.insert(NAME_FTS, null, values);
            }
            mDB.setTransactionSuccessful();
        } finally {
            mDB.endTransaction();
        }
    }

    /**
     * Get a count of how many RSSItems are in the database
     */
    public int getCount() {
        String[] columns = new String[] { _ID };
        Cursor c = mDB.query(NAME_FTS, columns, null, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    /**
     * Retrieve a list of RSSItems for a given query
     *
     * @param filterTags
     *          Apply filters to only search in
     *          posts that match these filters
     */
    public List<RSSItem> search(String[] filterTags, String query) {
        String selection = NAME_FTS + " MATCH ? COLLATE NOCASE";
        if (filterTags != null && filterTags.length >= 1) {
            selection += " AND (";
            for (int i=0; i<filterTags.length; i++) {
                String tag = filterTags[i];
                tag = "\'%" + tag + "%\'";
                selection += COLUMN_TAGS + " LIKE " + tag;
                if (i < filterTags.length-1) {
                    selection += " OR ";
                }
            }
            selection += ")";
        }
        String[] selectionArgs = new String[] { appendWildcard(query) };
        String orderBy = COLUMN_DATE + " desc";

        List<RSSItem> items = new ArrayList<RSSItem>();

        Cursor c = mDB.query(NAME_FTS, null, selection, selectionArgs, null, null, orderBy);
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            long duration = c.getLong(c.getColumnIndex(COLUMN_DATE));
            String title = c.getString(c.getColumnIndex(COLUMN_TITLE));
            String content = c.getString(c.getColumnIndex(COLUMN_CONTENT));
            String preview = c.getString(c.getColumnIndex(COLUMN_PREVIEW));
            String[] tags = c.getString(c.getColumnIndex(COLUMN_TAGS)).split(",");
            String url = c.getString(c.getColumnIndex(COLUMN_URL));
            String metadata = c.getString(c.getColumnIndex(COLUMN_METADATA));
            RSSItem item = new RSSItem(duration, title, content, preview, tags, url, metadata);
            items.add(item);
        }
        c.close();

        return items;

    }

    private String appendWildcard(String query) {
        if (TextUtils.isEmpty(query)) return query;

        final StringBuilder builder = new StringBuilder();
        final String[] splits = TextUtils.split(query, " ");

        for (String split : splits)
            builder.append(split).append("*").append(" ");

        return builder.toString().trim();
    }

    /**
     * Get all items stored in the database
     * @return list of RSSItems
     */
    public List<RSSItem> getItems() {
        return getItems(null, Preferences.Default.LOAD_LIMIT);
    }

    /**
     * Get all items stored in the database
     * with a certain tag
     *
     * @param filterTags
     *          tag to filter by
     * @return  list of RSSItems with certain tag
     */
    public List<RSSItem> getItems(String[] filterTags, int loadLimit) {
        return getItems(filterTags, System.currentTimeMillis(), loadLimit);
    }

    /**
     * Get all items stored in the database
     * older than a specified date
     *
     * @param filterTags
     *          tag to filter by
     * @param dateMax
     *          only return posts older than this date
     * @param loadLimit
     *          only return this many results
     * @return  list of RSSItems that satisfy the given criteria
     */
    public List<RSSItem> getItems(String[] filterTags, long dateMax, int loadLimit) {
        String selection = COLUMN_DATE + " < " + dateMax;
        if (filterTags != null && filterTags.length >= 1) {
            selection += " AND (";
            for (int i=0; i<filterTags.length; i++) {
                String tag = filterTags[i];
                tag = "\'%" + tag + "%\'";
                selection += COLUMN_TAGS + " LIKE " + tag;
                if (i < filterTags.length-1) {
                    selection += " OR ";
                }
            }
            selection += ")";
        }
        String orderBy = COLUMN_DATE + " desc";
        String limit = String.valueOf(loadLimit);

        List<RSSItem> items = new ArrayList<RSSItem>();

        Cursor c = mDB.query(NAME_FTS, null, selection, null, null, null, orderBy, limit);
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            long duration = c.getLong(c.getColumnIndex(COLUMN_DATE));
            String title = c.getString(c.getColumnIndex(COLUMN_TITLE));
            String content = c.getString(c.getColumnIndex(COLUMN_CONTENT));
            String preview = c.getString(c.getColumnIndex(COLUMN_PREVIEW));
            String[] tags = c.getString(c.getColumnIndex(COLUMN_TAGS)).split(",");
            String url = c.getString(c.getColumnIndex(COLUMN_URL));
            String metadata = c.getString(c.getColumnIndex(COLUMN_METADATA));
            RSSItem item = new RSSItem(duration, title, content, preview, tags, url, metadata);
            items.add(item);
        }

        return items;
    }

    /**
     * Delete all posts in the database
     *
     * @return  number of posts deleted
     */
    public int deleteAll() {
        return mDB.delete(NAME_FTS, null, null);
    }

    /**
     * Delete all posts dated after a specified time
     *
     * @param time
     *          only delete posts after this time
     * @return  number of posts deleted
     */
    public int deleteIfPublishedAfter(long time) {
        return mDB.delete(NAME_FTS, COLUMN_DATE + ">=?", new String[] { String.valueOf(time) });
    }
}
