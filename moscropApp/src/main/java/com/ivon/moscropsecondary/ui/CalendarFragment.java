package com.ivon.moscropsecondary.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.util.Logger;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CalendarFragment extends Fragment {

    public static final String MOSCROP_CALENDAR_JSON_URL = "http://www.google.com/calendar/feeds/moscroppanthers@gmail.com/public/full?alt=json&max-results=10&orderby=starttime&sortorder=descending";

    private int mPosition;
    private View mContentView;

    private ExtendedCalendarView mCalendarView;
    
    public static CalendarFragment newInstance(int position) {
    	CalendarFragment fragment = new CalendarFragment();
        fragment.mPosition = position;
    	return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);
    	
    	mContentView = inflater.inflate(R.layout.fragment_events, container, false);

        insertDays();

        mCalendarView = (ExtendedCalendarView) mContentView.findViewById(R.id.calendar);
        mCalendarView.setGesture(ExtendedCalendarView.LEFT_RIGHT_GESTURE);

        // JSON Testing stuff
        new Thread(new Runnable() {
            @Override
            public void run() {
                doJsonStuff();
            }
        }).start();

    	return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
    }

    private void insertDays() {

        /*
        // Delete all rows and replace with updated data
        getActivity().getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);
        */

        // temporary for testing
        Cursor c = getActivity().getContentResolver().query(CalendarProvider.CONTENT_URI, null, null, null, null);
        int count = c.getCount();
        c.close();
        if(count > 0) {
            return;
        }

        // Insert updated data
        ContentValues values = new ContentValues();
        values.put(CalendarProvider.COLOR, Event.COLOR_RED);
        values.put(CalendarProvider.DESCRIPTION, "Eat moon cakes :D");
        values.put(CalendarProvider.LOCATION, "Home");
        values.put(CalendarProvider.EVENT, "Mid-Autumn Festival");

        Calendar cal = Calendar.getInstance();

        cal.set(2014, Calendar.SEPTEMBER, 6, 8, 0);
        values.put(CalendarProvider.START, cal.getTimeInMillis());
        values.put(CalendarProvider.START_DAY, getJulianDayFromCalendar(cal));

        cal.set(2014, Calendar.SEPTEMBER, 8, 20, 5);
        values.put(CalendarProvider.END, cal.getTimeInMillis());
        values.put(CalendarProvider.END_DAY, getJulianDayFromCalendar(cal));

        getActivity().getContentResolver().insert(CalendarProvider.CONTENT_URI, values);
    }

    private int getJulianDayFromCalendar(Calendar calendar) {
        TimeZone tz = TimeZone.getDefault();
        return Time.getJulianDay(calendar.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(calendar.getTimeInMillis())));
    }

    private void doJsonStuff() {

        JSONObject main = getJsonObject(MOSCROP_CALENDAR_JSON_URL);
        if(main == null) {
            return;
        }

        try {
            JSONObject feed = main.getJSONObject("feed");
            JSONObject updated = feed.getJSONObject("updated");
            String time = updated.getString("$t");
            Logger.log("Updated time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getJsonObject(String url) {

        JSONObject resultObj = null;

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        InputStream inputStream = null;
        String resultStr = null;
        try {

            // Make sure status is OK
            HttpResponse response = httpclient.execute(httpGet);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HttpStatus.SC_OK) {
                Logger.log("Status code", status.getStatusCode());
                Logger.log("Reason", status.getReasonPhrase());
                return null;
            }

            // Read response
            HttpEntity entity = response.getEntity();
            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            // Build input stream into response string
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
            resultStr = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if(inputStream != null) {
                    inputStream.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        try {
            resultObj = new JSONObject(resultStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultObj;
    }
}
