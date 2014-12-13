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
    private long mOldestPostDate;
    private boolean mOnlineEnabled;
    private boolean mShowCacheWhileLoadingOnline;

    public RSSListLoader(Context context, String blogId, String tag, boolean append, long oldestPostDate, boolean onlineEnabled, boolean showCacheWhileLoadingOnline) {
        super(context);
        mBlogId = blogId;
        mTag = tag;
        mAppend = append;
        mOldestPostDate = oldestPostDate;
        mOnlineEnabled = onlineEnabled;
        mShowCacheWhileLoadingOnline = showCacheWhileLoadingOnline;
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

        SharedPreferences prefs = getContext().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        String version = prefs.getString(Preferences.App.Keys.RSS_VERSION, Preferences.App.Default.RSS_VERSION);

        if (!mShowCacheWhileLoadingOnline && mOnlineEnabled) {
            List<RSSItem> list = tryGetFullLoad();
            if (list == null) {
                list = getListOnly();
            }

            int result;
            String newVersion = prefs.getString(Preferences.App.Keys.RSS_VERSION, Preferences.App.Default.RSS_VERSION);
            if (newVersion.equals(version)) {
                result = RSSResult.RESULT_REDUNDANT;
            } else {
                result = RSSResult.RESULT_OK;
            }

            return new RSSResult(version, result, list, mAppend);
        } else {
            List<RSSItem> list = getListOnly();
            if (list.size() == 0) {
                list = tryGetFullLoad();
            }

            int result;
            if (mShowCacheWhileLoadingOnline) {
                result = RSSResult.RESULT_REDO_ONLINE;
            } else {
                result = RSSResult.RESULT_OK;
            }
            return new RSSResult(version, result, list, mAppend);
        }
    }

    private List<RSSItem> tryGetFullLoad() {

        // Get an initial list
        List<RSSItem> list = downloadParseSaveGetList(mAppend, mOldestPostDate);

        SharedPreferences prefs = getContext().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        int loadLimit = prefs.getInt(Preferences.Keys.LOAD_LIMIT, Preferences.Default.LOAD_LIMIT);

        RSSDatabase database = new RSSDatabase(getContext());

        while (list.size() < loadLimit) {

            // Get date of the oldest post with the tag
            // we're interested in. List is a list of up
            // to loadLimit items with the tag we're
            // interested in that are in the database.
            // If list size is 0, that means there are
            // no posts with such tags in the database.
            // Therefore, we can safely tell
            // downloadParseSaveGetList() to look for
            // posts older than the oldest post in the database.

            long updatedOldestPostDate;
            if (list.size() > 0) {
                updatedOldestPostDate=list.get(list.size() - 1).date;
            } else {
                updatedOldestPostDate = database.getOldestPostDate(null);
            }
            List<RSSItem> appendList = downloadParseSaveGetList(true, updatedOldestPostDate);
            if (appendList.size() == 0) {
                break;
            } else {
                list.addAll(appendList);
            }
        }

        if (list.size() > loadLimit) {
            list = list.subList(0, loadLimit);
        }

        database.close();
        return list;
    }

    /**
     * Gets a list of RSSItems from the internet
     * @return a list of RSSItems.
     */
    private List<RSSItem> downloadParseSaveGetList(boolean append, long prevOldestPostDate) {
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
                int loadLimit = prefs.getInt(Preferences.Keys.LOAD_LIMIT, Preferences.Default.LOAD_LIMIT);

                RSSDatabase database = new RSSDatabase(getContext());
                parser.parseAndSave(mBlogId, lastFeedVersion, append);

                // Display list
                List<RSSItem> list;
                if (append) {
                    list = database.getItems(getFilterTags(), prevOldestPostDate, loadLimit);
                } else {
                    list = database.getItems(getFilterTags(), loadLimit);
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
        SharedPreferences prefs = getContext().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        int loadLimit = prefs.getInt(Preferences.Keys.LOAD_LIMIT, Preferences.Default.LOAD_LIMIT);
        RSSDatabase database = new RSSDatabase(getContext());
        List<RSSItem> list = database.getItems(getFilterTags(), loadLimit);
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
