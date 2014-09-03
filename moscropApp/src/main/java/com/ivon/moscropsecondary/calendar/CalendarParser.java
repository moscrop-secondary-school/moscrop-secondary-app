package com.ivon.moscropsecondary.calendar;

import com.ivon.moscropsecondary.util.JsonUtil;
import com.ivon.moscropsecondary.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivon on 9/2/14.
 */
public class CalendarParser {

    public static class GCalEvent {
        public final String title;
        public final String content;
        public final String where;
        public final String startTimeRCF;
        public final String endTimeRCF;

        public GCalEvent(String title, String content, String where, String startTimeRCF, String endTimeRCF) {
            this.title = title;
            this.content = content;
            this.where = where;
            this.startTimeRCF = startTimeRCF;
            this.endTimeRCF = endTimeRCF;
        }
    }

    public static class CalendarFeed {
        public final String timestamp;
        public final List<GCalEvent> events;

        public CalendarFeed(String timestamp, List<GCalEvent> events) {
            this.timestamp = timestamp;
            this.events = events;
        }
    }

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
            Logger.log("Entry JSONArray is null");
            return null;
        } else {
            Logger.log("Entry JSONArray is NOT null");
        }

        List<GCalEvent> events = new ArrayList<GCalEvent>();
        Logger.log("Converting entryObjects[] with " + entryObjects.length + " objects to list of GCalEvents");
        for (JSONObject entryObject : entryObjects) {
            Logger.log("Object number " + (events.size() + 1) + ": entering for loop");
            GCalEvent event = entryArrayJsonObjectToEvent(entryObject);
            Logger.log("Parsed JSONObject to GCalEvent");
            if(event != null) {
                Logger.log("Event is not null, adding it to list");
                events.add(event);
            } else {
                Logger.log("Event is null, NOT adding it to list");
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

    public static CalendarFeed getCalendarFeed(String url) throws JSONException {
        JSONObject jsonObject = JsonUtil.getJsonObjectFromUrl(url);
        Logger.log("Retrieved JSONObject");
        String timestamp = getUpdatedTimeFromJsonObject(jsonObject);
        List<GCalEvent> events = getEventsListFromJsonObject(jsonObject);

        return new CalendarFeed(timestamp, events);
    }


}
