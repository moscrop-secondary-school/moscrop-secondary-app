package com.ivon.moscropsecondary.list;

import android.os.Message;

import com.ivon.moscropsecondary.ui.RSSFragment;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by ivon on 01/07/14.
 */
public class FeedDownloader implements Runnable {

    WeakReference<RSSFragment> mWeakReference;
    String mUri;
    int mLoadConfig;

    public FeedDownloader(RSSFragment fragment, String uri, int loadConfig) {
        mWeakReference = new WeakReference(fragment);
        mUri = uri;
        mLoadConfig = loadConfig;
    }



    @Override
    public void run() {

        if (Thread.currentThread().isInterrupted()) {
            finish();
            return;
        }
        if(!obtainAndSend(LoadHandler.START_LOAD, null))
            return;

        RSSReader reader = new RSSReader();
        RSSFeed feed = null;

        reader.setCallbacks(mWeakReference.get());

        if (Thread.currentThread().isInterrupted()) {
            finish();
            return;
        }
        try {
            feed = reader.load(mUri, mLoadConfig);
        } catch (RSSReaderException e) {
            e.printStackTrace();
            return;
        } finally {
            reader.close();
        }

            /*
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            */

        if (Thread.currentThread().isInterrupted()) {
            finish();
            return;
        }
        if(feed == null) {
            obtainAndSend(LoadHandler.INVALID_FEED, null);
            return;
        }

        if (Thread.currentThread().isInterrupted()) {
            finish();
            return;
        }
        if(!obtainAndSend(LoadHandler.CLEAR_ADAPTER, null))
            return;

        // Get a list of items from the feed
        List<RSSItem> mRSSItems = feed.getItems();

        for(RSSItem item : mRSSItems) {
            // Before adding another item check if we've been interrupted
            if (Thread.currentThread().isInterrupted()) {
                finish();
                return;
            }
            if(item != null) {
                if(!obtainAndSend(LoadHandler.ADD_ITEM, item))
                    return;
            }
        }

        finish();
    }

    private void finish() {
        obtainAndSend(LoadHandler.FINISH_LOAD, null);
    }

    private boolean obtainAndSend(int what, Object obj) {
        if(mWeakReference.get() != null) {
            LoadHandler handler = mWeakReference.get().mHandler;
            Message msg = handler.obtainMessage(what, obj);
            handler.sendMessage(msg);
            return true;
        } else {
            return false;
        }
    }
}
