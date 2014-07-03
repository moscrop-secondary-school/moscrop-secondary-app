package com.ivon.moscropsecondary.list;

import android.os.Message;

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

    LoadHandler mHandler;
    String mUri;
    int mLoadConfig;
    WeakReference<RSSReader.RSSReaderCallbacks> mWeakReference;

    public FeedDownloader(LoadHandler handler, String uri, int loadConfig, RSSReader.RSSReaderCallbacks callbacks) {
        mHandler = handler;
        mUri = uri;
        mLoadConfig = loadConfig;
        mWeakReference = new WeakReference<RSSReader.RSSReaderCallbacks>(callbacks);
    }

    @Override
    public void run() {

        /**
         * TODO git commit -m 'Fix memory leak caused by loading feed'
         *
         * 1. Remove RSSFragment fragment = mWeakReference.get();
         *
         * 2. Call mWeakReference.get() each time you want to use
         * the fragment. Don't be lazy! Check for null each time!
         *
         * 3. Add a WeakReference for the RSSCallbacks passed to
         * RSSReader, so that doesn't hold on to the fragment.
         *
         * 4. ???
         *
         * 5. Profit!
         */

        //RSSFragment fragment = mWeakReference.get();

        if(mHandler == null)
            return;

        Message startMsg = mHandler.obtainMessage(LoadHandler.START_LOAD);
        mHandler.sendMessage(startMsg);

        RSSReader reader = new RSSReader();
        RSSFeed feed = null;

        reader.setCallbacks(mWeakReference);

        try {
            feed = reader.load(mUri, mLoadConfig);
        } catch (RSSReaderException e) {
            e.printStackTrace();
        }
        reader.close();

            /*
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            */

        if(feed == null) {
            Message invalidFeedMsg = mHandler.obtainMessage(LoadHandler.INVALID_FEED);
            mHandler.sendMessage(invalidFeedMsg);
            return;
        }

        Message clearAdapterMsg = mHandler.obtainMessage(LoadHandler.CLEAR_ADAPTER);
        mHandler.sendMessage(clearAdapterMsg);

        List<RSSItem> mRSSItems = feed.getItems();

        for(RSSItem item : mRSSItems) {
            if(item != null) {
                Message addItemMsg = mHandler.obtainMessage(LoadHandler.ADD_ITEM, item);
                mHandler.sendMessage(addItemMsg);
            }
        }

        Message finishMsg = mHandler.obtainMessage(LoadHandler.FINISH_LOAD);
        mHandler.sendMessage(finishMsg);
    }
}
