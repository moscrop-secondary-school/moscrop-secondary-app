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

    // Stuff

    public List<String> listAllByLastName() {
        Cursor c = mDB.query(TABLE_NAME, new String[] {"email"}, null, null, null, null, "name_last asc");
        List<String> list = new ArrayList<String>();
        c.moveToPosition(-1);
        while(c.moveToNext()) {
            String s = c.getString(c.getColumnIndex("email"));
            list.add(s);
        }
        c.close();
        return list;
    }
}
