package com.moscrop.official.staffinfo;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivon on 04/04/15.
 */
public class StaffInfoDatabase extends SQLiteOpenHelper {

    private static StaffInfoDatabase mInstance;
    private Context mContext;

    private static final String _ID = "_id";
    private static final String COLUMN_NAME_PREFIX  = "name_prefix";
    private static final String COLUMN_FIRST_NAME   = "first_name";
    private static final String COLUMN_LAST_NAME    = "last_name";
    private static final String COLUMN_ROOMS        = "rooms";
    private static final String COLUMN_DEPARTMENT   = "department";
    private static final String COLUMN_EMAIL        = "email";
    private static final String COLUMN_SITES        = "sites";

    private static final String NAME = "staff_info";
    private static final String NAME_FTS = "staff_info_fts";
    private static final int VERSION = 2015040501;

    private StaffInfoDatabase(Context context) {
        super(context, NAME, null, VERSION);
        mContext = context;
    }

    public static synchronized StaffInfoDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StaffInfoDatabase(context);
        }
        if (mInstance.getCount() == 0) {
            mInstance.populateDatabaseFromCsv();
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME_PREFIX  + " TEXT" + ", " +
                COLUMN_FIRST_NAME   + " TEXT" + ", " +
                COLUMN_LAST_NAME    + " TEXT" + ", " +
                COLUMN_ROOMS        + " TEXT" + ", " +
                COLUMN_DEPARTMENT   + " TEXT" + ", " +
                COLUMN_EMAIL        + " TEXT" + ", " +
                COLUMN_SITES        + " TEXT" + ""   +
                ")");

        db.execSQL("CREATE VIRTUAL TABLE " + NAME_FTS + " USING fts3 (" +
                _ID + ", " +
                COLUMN_NAME_PREFIX  + ", " +
                COLUMN_FIRST_NAME   + ", " +
                COLUMN_LAST_NAME    + ", " +
                COLUMN_ROOMS        + ", " +
                COLUMN_DEPARTMENT   + ", " +
                COLUMN_EMAIL        + ", " +
                COLUMN_SITES        + "" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NAME_FTS);
        onCreate(db);
    }

    private void populateDatabaseFromCsv() {
        AssetManager assetManager = mContext.getAssets();
        try {

            InputStream is = assetManager.open("staff_info.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            getWritableDatabase().beginTransaction();

            String line = reader.readLine();    // remove the first row of headers
            while ((line = reader.readLine()) != null) {
                String[] array = line.split(",");

                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_PREFIX,  array[0]);
                values.put(COLUMN_FIRST_NAME,   array[1]);
                values.put(COLUMN_LAST_NAME,    array[2]);
                values.put(COLUMN_ROOMS,        array[3]);
                values.put(COLUMN_DEPARTMENT,   array[4]);
                values.put(COLUMN_EMAIL,        array[5]);
                values.put(COLUMN_SITES,        array[6]);
                getWritableDatabase().insert(NAME_FTS, null, values);
            }

            getWritableDatabase().setTransactionSuccessful();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    public List<StaffInfoModel> getList() {
        Cursor c = getReadableDatabase().query(NAME_FTS, null, null, null, null, null, null);
        List<StaffInfoModel> list = new ArrayList<StaffInfoModel>();
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();

        return list;
    }

    public List<StaffInfoModel> search(String query) {

        String selection = NAME_FTS + " MATCH ? COLLATE NOCASE";
        String[] selectionArgs = new String[] { appendWildcard(query) };

        Cursor c = getReadableDatabase().query(NAME_FTS, null, selection, selectionArgs, null, null, null);
        List<StaffInfoModel> list = new ArrayList<StaffInfoModel>();
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();

        return list;
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

    private StaffInfoModel fromCursor(Cursor c) {
        String namePrefix = c.getString(c.getColumnIndex(COLUMN_NAME_PREFIX));
        String firstName = c.getString(c.getColumnIndex(COLUMN_FIRST_NAME));
        String lastName = c.getString(c.getColumnIndex(COLUMN_LAST_NAME));
        String rooms = c.getString(c.getColumnIndex(COLUMN_ROOMS));
        String department = c.getString(c.getColumnIndex(COLUMN_DEPARTMENT));
        String email = c.getString(c.getColumnIndex(COLUMN_EMAIL));
        String sites = c.getString(c.getColumnIndex(COLUMN_SITES));

        return new StaffInfoModel(namePrefix, firstName, lastName,
                StaffInfoModel.roomsStringToArray(rooms),
                department, email,
                StaffInfoModel.sitesStringToArray(sites)
        );
    }

    public int getCount() {
        Cursor c = getReadableDatabase().query(NAME_FTS, null, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }
}
