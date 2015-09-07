package com.moscrop.official.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.moscrop.official.BuildConfig;

public class Logger {

	public static final boolean SPAM = false;
	public static final String TAG = "moscrop";

	private static String getLogTag() {
		Throwable t = new Throwable();
		if (t.getStackTrace().length > 2) {
			StackTraceElement methodCaller = t.getStackTrace()[2];
			return methodCaller.getClassName();
		} else {
			return TAG;
		}
	}

	public static void error(String message) {
		Log.e(getLogTag(), message);
	}

	public static void error(String message, Exception e) {
		Log.e(getLogTag(), message, e);
	}

	public static void warn(String message) {
		Log.w(getLogTag(), message);
	}

	public static void warn(String name, int value) {
		Log.w(getLogTag(), name + " = " + value);
	}

	public static void log(String message) {
		if(BuildConfig.DEBUG) Log.i(getLogTag(), message);
	}

	public static void log(String name, String value) {
		if(BuildConfig.DEBUG) Log.i(getLogTag(), name + " = " + value);
	}

	public static void log(String name, int value) {
		if(BuildConfig.DEBUG) Log.i(getLogTag(), name + " = " + value);
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
		if(BuildConfig.DEBUG) Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
