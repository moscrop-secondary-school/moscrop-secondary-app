package com.moscrop.official.rss;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;

import com.moscrop.official.util.Preferences;
import com.moscrop.official.util.Util;

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
    private String mSearchQuery;
    private boolean mAppend;
    private long mOldestPostDate;
    private boolean mOnlineEnabled;
    private boolean mShowCacheWhileLoadingOnline;

    public RSSListLoader(Context context, String blogId, String tag, String searchQuery, boolean append, long oldestPostDate, boolean onlineEnabled, boolean showCacheWhileLoadingOnline) {
        super(context);
        mBlogId = blogId;
        mTag = tag;
        mSearchQuery = searchQuery;
        mAppend = append;
        mOldestPostDate = oldestPostDate;
        mOnlineEnabled = onlineEnabled;
        mShowCacheWhileLoadingOnline = showCacheWhileLoadingOnline;
    }

    @Override
    public RSSResult loadInBackground() {

        // Retrieve information about the previously cached version
        SharedPreferences prefs = getContext().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        String version = prefs.getString(Preferences.App.Keys.RSS_VERSION, Preferences.App.Default.RSS_VERSION);

        if (mSearchQuery != null) {

            // If this is a search request, we will
            // simply use the search() method of RSSDatabase

            RSSDatabase db = new RSSDatabase(getContext());
            List<RSSItem> items = db.search(getFilterTags(), mSearchQuery);
            db.close();

            int result = RSSResult.RESULT_OK;
            return new RSSResult(version, result, items, false);

        } else {

            // Otherwise, we must handle the request based on the other flags

            if (!mShowCacheWhileLoadingOnline && mOnlineEnabled) {

                // Load online first, then offline if needed

                List<RSSItem> list = tryGetFullLoad();
                if (list == null) {
                    list = getListOnly();
                }

                int result;
                String newVersion = prefs.getString(Preferences.App.Keys.RSS_VERSION, Preferences.App.Default.RSS_VERSION);

                if (!mAppend && newVersion.equals(version)) {
                    result = RSSResult.RESULT_REDUNDANT;
                } else {
                    result = RSSResult.RESULT_OK;
                }

                return new RSSResult(version, result, list, mAppend);

            } else {

                // Load offline first, then online if needed

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
    }

    private List<RSSItem> tryGetFullLoad() {

        // Get an initial list
        List<RSSItem> list = downloadParseSaveGetList(mAppend, mOldestPostDate);

        if (list == null) {
            return null;
        }

        SharedPreferences prefs = getContext().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        int loadLimit = prefs.getInt(Preferences.Keys.LOAD_LIMIT, Preferences.Default.LOAD_LIMIT);

        RSSDatabase database = new RSSDatabase(getContext());
        int prevCycleDatabaseCount = database.getCount();

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
            if (database.getCount() <= prevCycleDatabaseCount) {
                // If this "page" added no new items to the database,
                // we now know that there are no more posts in Blogger.
                // Therefore, we can stop trying and give up.
                break;
            } else {
                list.addAll(appendList);
            }

            prevCycleDatabaseCount = database.getCount();
        }

        if (list.size() > loadLimit) {
            list = list.subList(0, loadLimit);
        }

        database.close();
        return list;
    }

    /**
     * Gets a list of RSSItems from the internet
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
            } catch (JSONException | IOException e) {
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
        switch (mTag) {
            case "All":
                try {
                    tags = RSSTagCriteria.getTagNames(getContext());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "Subscribed":
                try {
                    tags = RSSTagCriteria.getSubscribedTags(getContext());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                tags = new String[]{mTag};
                break;
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
