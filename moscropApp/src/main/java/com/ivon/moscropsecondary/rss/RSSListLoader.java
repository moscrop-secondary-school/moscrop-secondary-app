package com.ivon.moscropsecondary.rss;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.ivon.moscropsecondary.util.Preferences;
import com.ivon.moscropsecondary.util.Util;
import com.tyczj.extendedcalendarview.CalendarProvider;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by ivon on 24/10/14.
 */
public class RSSListLoader extends AsyncTaskLoader<List<RSSItem>> {

    private List<RSSItem> mList;
    private String mBlogId;
    private String mTag;
    private boolean mOnlineEnabled;

    public RSSListLoader(Context context, String blogId, String tag, boolean onlineEnabled) {
        super(context);
        mBlogId = blogId;
        mTag = tag;
        mOnlineEnabled = onlineEnabled;
    }

    @Override
    public List<RSSItem> loadInBackground() {
/*        if (mOnlineEnable && isConnected()) {
            Logger.log("RSSListLoader Loading from " + mBlogId + " and " + mTag);
            RSSParser.parseAndSaveAll(getContext(), mBlogId, mTag);
        }
        RSSDatabase database = new RSSDatabase(getContext());
        return database.getItems(mTag);
        */

        if (mOnlineEnabled) {
            List<RSSItem> list = downloadParseSaveGetList();
            if (list == null) {
                list = getListOnly();
            }
            return list;
        } else {
            List<RSSItem> list = getListOnly();
            if (list.size() == 0) {
                list = downloadParseSaveGetList();
            }
            return list;
        }
    }

    private List<RSSItem> downloadParseSaveGetList() {
        if (Util.isConnected(getContext())) {

            // Try to update the tag list before updating feed
            try {
                RSSTagCriteria.downloadTagListToStorage(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Try to create a parser object
            RSSParser parser = null;
            try {
                parser = new RSSParser(getContext());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Download and parse the feed
            if (parser != null) {

                // Check if database is empty
                Cursor c = getContext().getContentResolver().query(CalendarProvider.CONTENT_URI, null, null, null, null);
                int count = c.getCount();
                c.close();

                SharedPreferences prefs = getContext().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
                long lastUpdateMillis = prefs.getLong(Preferences.App.Keys.RSS_LAST_UPDATED, Preferences.App.Default.RSS_LAST_UPDATED);
                String lastFeedVersion = prefs.getString(Preferences.App.Keys.RSS_VERSION, Preferences.App.Default.RSS_VERSION);

                if((count == 0)
                        || (lastUpdateMillis == Preferences.App.Default.RSS_LAST_UPDATED)
                        || (lastFeedVersion.equals(Preferences.App.Default.RSS_VERSION))
                        ) {

                    // Provider is empty
                    // Or, if last update info is missing, to be safe,
                    // we will reload everything. Make sure data is up to date.
                    parser.parseAndSaveAll(mBlogId);

                } else {

                    // Everything good to go! Functioning normally.

                    // If last update time is in the future for some reason,
                    // assume last update time is now so we can recheck everything
                    // between now and the last updated time, which is somehow
                    // in the future. Probably aliens. (Actually, very likely due to timezones)
                    if(lastUpdateMillis > System.currentTimeMillis()) {
                        lastUpdateMillis = System.currentTimeMillis();
                    }

                    parser.parseAndSave(mBlogId, lastUpdateMillis, lastFeedVersion);

                }

                // Display list
                RSSDatabase database = new RSSDatabase(getContext());
                List<RSSItem> list = database.getItems(getFilterTags());
                database.close();
                return list;

            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    private List<RSSItem> getListOnly() {
        RSSDatabase database = new RSSDatabase(getContext());
        List<RSSItem> list = database.getItems(getFilterTags());
        database.close();
        return list;
    }

    private String[] getFilterTags() {
        String[] tags = null;
        if (mTag.equals("All")) {
            try {
                tags = RSSTagCriteria.getTagNames(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (mTag.equals("Subscribed")) {
            try {
                tags = RSSTagCriteria.getSubscribedTags(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            tags = new String[] { mTag };
        }
        return tags;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(List<RSSItem> list) {

        mList = list;

        if(isStarted()) {
            super.deliverResult(list);
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if(mList != null) {
            deliverResult(mList);
        }
        if(takeContentChanged() || mList == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        mList = null;
    }
}
