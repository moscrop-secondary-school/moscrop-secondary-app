package com.ivon.moscropsecondary.list;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.AsyncTaskLoader;

import com.ivon.moscropsecondary.list.RSSAdapter.RSSAdapterItem;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivon on 24/08/14.
 */
public class RSSListLoader extends AsyncTaskLoader<List<RSSAdapterItem>> implements RSSReader.RSSReaderCallbacks {

    public static final int CONFIG_ONLINE_PRIORITY = 0;
    public static final int CONFIG_CACHED_PRIORITY = 1;
    public static final int CONFIG_ONLINE_ONLY = 2;
    public static final int CONFIG_CACHED_ONLY = 3;

    private List<RSSAdapterItem> mList;
    private String mUri;
    private int mLoadConfig;

    public RSSListLoader(Context context, String uri, int loadConfig) {
        super(context);
        mUri = uri;
        mLoadConfig = loadConfig;
    }

    private int getTypeFromLink(String link) {
        if(link.contains("moscropschool")) {
            return CardUtil.TYPE_NEWS_CARD;
        } else if(link.contains("moscropnewsletters")) {
            return CardUtil.TYPE_EMAIL_CARD;
        } else if(link.contains("moscropstudents")) {
            return CardUtil.TYPE_SUBS_CARD;
        } else {
            return -1;
        }
    }

    /* Runs on a worker thread */
    @Override
    public List<RSSAdapterItem> loadInBackground() {

        RSSReader reader = new RSSReader();
        RSSFeed feed = null;

        reader.setCallbacks(this);

        try {
            feed = reader.load(mUri, mLoadConfig);
        } catch (RSSReaderException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }

        if(feed != null) {
            // Get a list of items from the feed
            List<RSSItem> items = feed.getItems();
            List<RSSAdapterItem> list = new ArrayList<RSSAdapterItem>();

            for (RSSItem item : items) {
                if (item != null) {
                    String link = item.getLink().toString();
                    int type = getTypeFromLink(link);
                    RSSAdapterItem adapterItem = new RSSAdapter.RSSAdapterItem(item, CardUtil.getCardProcessor(type));
                    list.add(adapterItem);
                }
            }
            return list;
        } else {
            return null;
        }
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(List<RSSAdapterItem> list) {

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
    public void onCanceled(List<RSSAdapterItem> mList) {
        // No need to close lists here
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        mList = null;
    }

    @Override
    public boolean onRequestNetworkState() {
        if(getContext() == null)
            return false;

        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    public File onRequestCacheFile(String uri) {
        if(getContext() == null) {
            return null;
        }

        File fileDir = getContext().getCacheDir();
        String condensedUri = uri.replaceAll("\\W+","");
        String fileName = condensedUri + "_cache.xml";
        File file = new File(fileDir, fileName);
        return file;
    }
}
