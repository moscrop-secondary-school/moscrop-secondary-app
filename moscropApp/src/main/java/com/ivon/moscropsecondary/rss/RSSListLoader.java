package com.ivon.moscropsecondary.rss;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.AsyncTaskLoader;

import com.ivon.moscropsecondary.util.JsonUtil;

import org.json.JSONException;

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
        if (isConnected()) {
            /*String resultStr = "";
            try {
                File file = new File("/sdcard/taglist.json");
                BufferedReader reader = new BufferedReader(new FileReader("/sdcard/taglist.json"));
                StringBuilder sb = new StringBuilder();

                // Build input stream into response string
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                resultStr = sb.toString();
            } catch (IOException e) {

            }*/
            RSSParser parser = null;
            try {
                parser = new RSSParser(JsonUtil.getJsonObjectFromUrl(getContext(), "http://pastebin.com/raw.php?i=dMePcZ9e"));
            } catch (JSONException e) {

            }
            if (parser != null) {
                parser.parseAndSaveAll(getContext(), mBlogId);
                RSSDatabase database = new RSSDatabase(getContext());
                List<RSSItem> list = database.getItems(mTag);
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
        List<RSSItem> list = database.getItems(mTag);
        database.close();
        return list;
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
