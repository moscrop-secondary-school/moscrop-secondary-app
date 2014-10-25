package com.ivon.moscropsecondary.rss;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.AsyncTaskLoader;

import com.ivon.moscropsecondary.util.Logger;

import java.util.List;

/**
 * Created by ivon on 24/10/14.
 */
public class RSSListLoader2 extends AsyncTaskLoader<List<RSSItem>> {

    private List<RSSItem> mList;
    private String mBlogId;
    private String mTag;

    public RSSListLoader2(Context context, String blogId, String tag) {
        super(context);
        mBlogId = blogId;
        mTag = tag;
    }

    @Override
    public List<RSSItem> loadInBackground() {
        if (isConnected()) {
            Logger.log("RSSListLoader2 Loading from " + mBlogId + " and " + mTag);
            RSSParser.parseAndSaveAll(getContext(), mBlogId, mTag);
            RSSDatabase database = new RSSDatabase(getContext());
            return database.getItems();
        } else {
            return null;
        }
    }

    private boolean isConnected() {
        if(getContext() == null)
            return false;

        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
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
