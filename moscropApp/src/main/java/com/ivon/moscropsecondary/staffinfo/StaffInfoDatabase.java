package com.ivon.moscropsecondary.staffinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ivon.moscropsecondary.util.Logger;
import com.ivon.moscropsecondary.util.Preferences;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivon on 9/23/14.
 */
public class StaffInfoDatabase extends SQLiteOpenHelper {

    private SQLiteDatabase mDB;
    private Context mContext;

    private static final String DB_NAME = "staff_info";
    private static final String TABLE_NAME = "staff_info";
    private static final int VERSION = 1;

    public StaffInfoDatabase(Context context) {
        super(context, DB_NAME, null, VERSION);
        mContext = context;
        Logger.log("getting writable database");
        mDB = getDatabase();
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDatabase() {

        Logger.log("createDatabase()");

            // By calling this method and empty database will be created into the default system path
            // of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                copyDatabase();
            } catch (IOException e) {
                Logger.error("Error creating staff info database", e);
            }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean databaseExists(){

        SharedPreferences prefs = mContext.getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        String existing_db_version = prefs.getString(Preferences.App.Keys.STAFF_DB_VERSION, Preferences.App.Default.STAFF_DB_VERSION);
        if (existing_db_version.equals(Preferences.App.Default.STAFF_DB_VERSION)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDatabase() throws IOException{
        Logger.log("copying database");
        // Open your local db as the input stream
        InputStream input = mContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = mContext.getDatabasePath(DB_NAME).toString();

        // Open the empty db as the output stream
        OutputStream output = new FileOutputStream(outFileName);

        // Transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer))>0) {
            output.write(buffer, 0, length);
        }

        // Close the streams
        output.flush();
        output.close();
        input.close();

        SharedPreferences.Editor prefs = mContext.getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS).edit();
        prefs.putString(Preferences.App.Keys.STAFF_DB_VERSION, "some version name");
        prefs.apply();

        Logger.log("Done copying");
    }

    public SQLiteDatabase getDatabase() {

        if(!databaseExists()) {
            createDatabase();
        }

        String path = mContext.getDatabasePath(DB_NAME).toString();
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.log("Staff info database oncreate, but we are doing nothing here");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing here yet, although we might need to use this method
    }

    @Override
    public synchronized void close() {
        if (mDB != null) {
            mDB.close();
        }
        super.close();
    }

    /******************** Helper methods ********************/

    private List<StaffInfoModel> getListFromCursor(Cursor c) {
        List<StaffInfoModel> list = new ArrayList<StaffInfoModel>();
        c.moveToPosition(-1);
        while(c.moveToNext()) {

            // Extract the values from database
            String prefix = c.getString(c.getColumnIndex("name_prefix"));
            String firstName = c.getString(c.getColumnIndex("name_first"));
            String lastName = c.getString(c.getColumnIndex("name_last"));
            String email = c.getString(c.getColumnIndex("email"));
            String subject = c.getString(c.getColumnIndex("subject"));
            String room = c.getString(c.getColumnIndex("room"));
            String site = c.getString(c.getColumnIndex("sites"));
            boolean isDH = c.getInt(c.getColumnIndex("is_department_head"))==1;
            int teacherID = c.getInt(c.getColumnIndex("teacher_id"));

            // Process name
            String firstInitial = firstName.substring(0, 1).toUpperCase();
            String name = String.format("%s. %s. %s", prefix, firstInitial, lastName);

            // Split subjects
            String[] subjects = subject.split("\\s*,\\s*");

            // Split rooms
            // TODO temporary solution is to those pesky "todo" tags in the database
            int[] rooms;
            if (!room.contains("TODO")) {
                String[] roomStrs = room.split("\\s*,\\s*");
                rooms = new int[roomStrs.length];
                for (int i = 0; i < roomStrs.length; i++) {
                    try {
                        rooms[i] = Integer.parseInt(roomStrs[i]);
                    } catch (NumberFormatException e) {
                        Logger.error("Parsing room number from String to int", e);
                    }
                }
            } else {
                rooms = new int[] { 3, 1, 4 };
            }

            // Create and add
            StaffInfoModel model = new StaffInfoModel(name, email, subjects, rooms, site, isDH, teacherID);
            list.add(model);
            Logger.log("Added " + name + " to list with room array of " + rooms.length + " length");
        }
        c.close();
        return list;
    }

    public List<StaffInfoModel> getList(String query) {

        String selection;
        if (query == null) {
            selection = null;
        } else {

            query = "\'%" + query + "%\'";

            selection = "name_first" + " LIKE " + query
                    + " OR " + "name_last" + " LIKE " + query
                    + " OR " + "subject" + " LIKE " + query
                    + " OR " + "room" + " LIKE " + query;
            //selection = "staff_info MATCH " + query;
        }

        Cursor c = mDB.query(TABLE_NAME, null, selection, null, null, null, "name_last asc");
        List<StaffInfoModel> list = getListFromCursor(c);

        return list;

    }

}
