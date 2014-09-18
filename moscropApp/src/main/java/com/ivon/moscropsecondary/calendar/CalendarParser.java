package com.ivon.moscropsecondary.calendar;

import android.content.Context;
import android.content.SharedPreferences;

import com.ivon.moscropsecondary.util.JsonUtil;
import com.ivon.moscropsecondary.util.Logger;
import com.ivon.moscropsecondary.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Helper class that performs Google
 * Calendar specific JSON parsing.
 *
 * Created by ivon on 9/2/14.
 */
public class CalendarParser {

    private static String getUpdatedTimeFromJsonObject(JSONObject jsonObject) throws JSONException {
        JSONObject feed = jsonObject.getJSONObject("feed");
        JSONObject updated = feed.getJSONObject("updated");
        return updated.getString("$t");
    }

    private static List<GCalEvent> getEventsListFromJsonObject(JSONObject jsonObject) throws JSONException {
        JSONObject feed = jsonObject.getJSONObject("feed");
        JSONObject updated = feed.getJSONObject("updated");
        JSONArray entry = feed.getJSONArray("entry");
        JSONObject entryObjects[] = JsonUtil.extractJsonArray(entry);
        if (entryObjects == null) {
            return null;
        }

        List<GCalEvent> events = new ArrayList<GCalEvent>();
        for (JSONObject entryObject : entryObjects) {
            GCalEvent event = entryArrayJsonObjectToEvent(entryObject);
            if(event != null) {
                events.add(event);
            }
        }
        return events;
    }

    private static GCalEvent entryArrayJsonObjectToEvent(JSONObject entryObject) throws JSONException {
        String title = entryObject.getJSONObject("title").getString("$t");
        String content = entryObject.getJSONObject("content").getString("$t");
        String where = entryObject.getJSONArray("gd$where").getJSONObject(0).getString("valueString");

        JSONObject when = entryObject.getJSONArray("gd$when").getJSONObject(0);
        String startTimeRCF = when.getString("startTime");
        String endTimeRCF = when.getString("endTime");

        return new GCalEvent(title, content, where, startTimeRCF, endTimeRCF);
    }

    public static CalendarFeed getCalendarFeed(Context context, String url) throws JSONException {
        JSONObject jsonObject = JsonUtil.getJsonObjectFromUrl(context, url);
        if (jsonObject != null) {
            String timestamp = getUpdatedTimeFromJsonObject(jsonObject);
            List<GCalEvent> events = getEventsListFromJsonObject(jsonObject);
            return new CalendarFeed(timestamp, events);
        } else {
            return null;
        }
    }

    private static String getCalendarUrlFromId(String id) {
        return "http://www.google.com/calendar/feeds/" + id + "/public/full?alt=json&max-results=1000&orderby=starttime&sortorder=descending&singleevents=true";
    }

    private static void saveUpdateInfo(Context context, String gcalVersion) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(Preferences.Calendar.NAME, Context.MODE_MULTI_PROCESS).edit();
        prefs.putLong(Preferences.Calendar.Keys.LAST_UPDATED, System.currentTimeMillis());
        prefs.putString(Preferences.Calendar.Keys.GCAL_VERSION, gcalVersion);
        prefs.commit();
    }

    /**
     * Download, parse, and store data from a Google Calendar feed. This method
     * will load all data from the whole calendar. Unlike process(String, long, String),
     * this method does not check for Gcal version and will disregard any previously
     * saved data. This method will delete all previously saved data and replace it
     * with freshly downloaded data. Because this takes a long time
     * and is often unnecessary, it is only recommended to use this method
     * when loading for the first time. Afterwards it is recommeneded to keep
     * track of updates and use process(String, long, String).
     *
     * @param id
     *      ID of the Google Calendar
     */
    public static void processAll(Context context, String id) {

        Logger.log("Processing all");

        // Get the list of events from the URL
        CalendarFeed feed = null;
        try {
            String url = getCalendarUrlFromId(id);
            feed = getCalendarFeed(context, url);
        } catch (JSONException e) {
            Logger.error("CalendarParser.processAll()", e);
        }

        if (feed != null) {
            saveUpdateInfo(context, feed.version);
            CalendarProviderUtil.deleteAll(context);
            CalendarProviderUtil.saveEventsToProvider(context, feed.events);
        }
    }

    /**
     * Selectively download, parse, and store data from a Google Calendar feed
     *
     * @param id
     *      ID of the Google Calendar
     * @param startMin
     *      Matches startMin query param of Google Calendar API.
     *      Only process calendar events with start dates after this param.
     *      Given in milliseconds.
     * @param lastGcalVersion
     *      "Updated" string from the last processed Google Calendar feed. Used to version
     *      Google Calendar feed and determine if it is necessary to go through with processing.
     *      After all, if it's the same version, no need to do all that work again!
     *      Usually of the format "2014-09-09T12:21:08.000Z"
     */
    public static void process(Context context, String id, long startMin, String lastGcalVersion) {

        Logger.log("Processing selectively");

        CalendarFeed feed = null;
        try {
            String url = getCalendarUrlFromId(id);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String startMinStr = sdf.format(new Date(startMin));
            url = url + "&start-min=" + startMinStr;

            feed = getCalendarFeed(context, url);
        } catch (JSONException e) {
            Logger.error("CalendarParser.process()", e);
        }

        if (feed != null) {

            // We just updated, so update records
            // with current time and the version we
            // just downloaded regardless of whether
            // updating the database was needed

            saveUpdateInfo(context, feed.version);

            String newGcalVersion = feed.version;
            if (!newGcalVersion.equals(lastGcalVersion)) {

                // There has been changes! We must update!
                //
                // The maintainer of the calendar probably
                // won't make changes to events that have
                // already past. Therefore we only need to
                // update events that begin after the last
                // update time.
                //
                // Begin by deleting those events

                int deleted = CalendarProviderUtil.deleteAfterTime(context, startMin);

                if (deleted != feed.events.size()) {
                    Logger.warn("Processing calendar events: deleted "
                            + deleted + " events from database, but only inserting "
                            + feed.events.size() + " new events."
                    );
                }

                // Our list of events will already only consist
                // of events that begin after startMin, so no
                // overlapping will occur. We can save normally.

                CalendarProviderUtil.saveEventsToProvider(context, feed.events);

            } else {
                Logger.log("Existing version is already up to date.");
            }
        }
    }


}
