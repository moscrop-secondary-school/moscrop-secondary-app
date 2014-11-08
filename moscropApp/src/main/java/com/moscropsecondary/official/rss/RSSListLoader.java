package com.moscropsecondary.official.rss;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;

import com.moscropsecondary.official.util.Preferences;
import com.moscropsecondary.official.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by ivon on 24/10/14.
 */
public class RSSListLoader extends AsyncTaskLoader<RSSResult> {

    private RSSResult mResult;
    private String mBlogId;
    private String mTag;
    private boolean mAppend;
    private boolean mOnlineEnabled;

    public RSSListLoader(Context context, String blogId, String tag, boolean append, boolean onlineEnabled) {
        super(context);
        mBlogId = blogId;
        mTag = tag;
        mAppend = append;
        mOnlineEnabled = onlineEnabled;
    }

    @Override
    public RSSResult loadInBackground() {
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
            return new RSSResult(list, mAppend);
        } else {
            List<RSSItem> list = getListOnly();
            if (list.size() == 0) {
                list = downloadParseSaveGetList();
            }
            return new RSSResult(list, mAppend);
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

                SharedPreferences prefs = getContext().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
                String lastFeedVersion = prefs.getString(Preferences.App.Keys.RSS_VERSION, Preferences.App.Default.RSS_VERSION);

                RSSDatabase database = new RSSDatabase(getContext());
                long prevOldestPostDate = database.getOldestPostDate();

                parser.parseAndSave(mBlogId, lastFeedVersion, mAppend);

                // Display list
                List<RSSItem> list;
                if (mAppend) {
                    list = database.getItems(getFilterTags(), prevOldestPostDate);
                } else {
                    list = database.getItems(getFilterTags());
                }
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
    public void deliverResult(RSSResult result) {

        mResult = result;

        if(isStarted()) {
            super.deliverResult(result);
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
        if(mResult != null) {
            deliverResult(mResult);
        }
        if(takeContentChanged() || mResult == null) {
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

        mResult = null;
    }
}
