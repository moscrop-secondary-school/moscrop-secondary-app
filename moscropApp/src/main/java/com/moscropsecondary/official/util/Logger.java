package com.moscropsecondary.official.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Logger {
	
	public static final boolean DEBUG = true;
	public static final boolean SPAM = false;
	public static final String TAG = "com.moscropsecondary.official";
	
	public static void error(String message) {
		Log.e(TAG, message);
	}

    public static void error(String message, Exception e) {
        Log.e(TAG, message, e);
    }

	public static void warn(String message) {
		Log.w(TAG, message);
	}
	
	public static void warn(String name, int value) {
		Log.w(TAG, name + " = " + value);
	}
	
	public static void log(String message) {
		if(DEBUG) Log.i(TAG, message);
	}
	
	public static void log(String name, String value) {
		if(DEBUG) Log.i(TAG, name + " = " + value);
	}
	
	public static void log(String name, int value) {
		if(DEBUG) Log.i(TAG, name + " = " + value);
	}
	
	public static void spam(String message) {
		if(SPAM) log(message);
	}
	
	public static void spam(String name, String value) {
		if(SPAM) log(name, value);
	}
	
	public static void spam(String name, int value) {
		if(SPAM) log(name, value);
	}
	
	public static void toast(Context context, String message) {
		if(DEBUG) Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
