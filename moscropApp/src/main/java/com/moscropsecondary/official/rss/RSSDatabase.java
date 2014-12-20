package com.moscropsecondary.official.rss;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final int VERSION = 1;

    public RSSDatabase(Context context) {
        super(context, NAME, null, VERSION);
        mContext = context;
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_DATE + " INTEGER, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_PREVIEW + " TEXT, " +
                COLUMN_TAGS + " TEXT, " +
                COLUMN_URL + " TEXT, " +
                COLUMN_METADATA + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // N/A
    }

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

        Cursor c = mDB.query(NAME, columns, selection, null, null, null, orderBy, limit);
        long oldestPostDate = System.currentTimeMillis();
        if (c.getCount() >= 1) {
            c.moveToPosition(0);
            oldestPostDate = c.getLong(c.getColumnIndex(COLUMN_DATE));
        }
        return oldestPostDate;
    }

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

                mDB.insert(NAME, null, values);
            }
            mDB.setTransactionSuccessful();
        } finally {
            mDB.endTransaction();
        }
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

        Cursor c = mDB.query(NAME, null, selection, null, null, null, orderBy, limit);
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

    public int deleteAll() {
        return mDB.delete(NAME, null, null);
    }

    public int deleteIfPublishedAfter(long time) {
        return mDB.delete(NAME, COLUMN_DATE + ">=?", new String[] { String.valueOf(time) });
    }
}
