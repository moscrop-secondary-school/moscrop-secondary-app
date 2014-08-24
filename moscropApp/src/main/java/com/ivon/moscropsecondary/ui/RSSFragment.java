package com.ivon.moscropsecondary.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.list.FeedDownloader;
import com.ivon.moscropsecondary.list.LoadHandler;
import com.ivon.moscropsecondary.list.RSSAdapter;
import com.ivon.moscropsecondary.list.RSSAdapter.RSSAdapterItem;
import com.ivon.moscropsecondary.util.Logger;

import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReader.RSSReaderCallbacks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RSSFragment extends Fragment
        implements AdapterView.OnItemClickListener, OnRefreshListener, RSSReaderCallbacks {


	public static final String MOSCROP_CHEMISTRY_URL = "http://moscropchemistry.wordpress.com/feed/";
	public static final String REDDIT_URL = "http://www.reddit.com/r/aww/.rss";
	public static final String HUGO_BARA_URL = "http://gplus-to-rss.appspot.com/rss/+HugoBarra";
	public static final String MOSCROP_PAGE_URL = "http://gplus-to-rss.appspot.com/rss/108865428316172309900";
	public static final String TEST_EMAIL_URL = "http://emails2rss.appspot.com/rss?id=1af3a2260113d04b2c0b99e0f751921b9365";
	public static final String BLOGGER_URL = "http://moscropschool.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_NEWS_URL = "http://moscropschool.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_SUBS_URL = "http://moscropstudents.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_NEWSLETTER_URL = "http://moscropnewsletters.blogspot.ca/feeds/posts/default?alt=rss";


    public static final String FEED_ALL = "http://www.feedcombine.com/rss/2122/moscrop-feeds-all.xml";
    public static final String FEED_NEWS = "http://moscropschool.blogspot.ca/feeds/posts/default?alt=rss";
    public static final String FEED_NEWSLETTERS = "http://moscropnewsletters.blogspot.ca/feeds/posts/default?alt=rss";
    public static final String FEED_SUBS = "http://moscropstudents.blogspot.ca/feeds/posts/default?alt=rss";

    private static final String KEY_URL = "url";
	
	private String mURL = FEED_ALL;

    private int mPosition;

    public SwipeRefreshLayout mSwipeLayout = null;
    public ListView mListView = null;
    public RSSAdapter mAdapter = null;
    public List<RSSAdapterItem> mItems = new ArrayList<RSSAdapterItem>();

    public LoadHandler mHandler = new LoadHandler(this);

	/**
	 * Create and return a new instance of RSSFragment with given parameters
	 * 
	 * @param feed URL of the RSS feed to load and display
	 * @return New instance of RSSFragment
	 */
	public static RSSFragment newInstance(int position, String feed) {
		RSSFragment fragment = new RSSFragment();
        fragment.mPosition = position;
		fragment.mURL = feed;
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);

    	View mContentView = inflater.inflate(R.layout.fragment_rsslist, container, false);
        mContentView.setBackgroundColor(0xffe4e4e4);

        if(savedInstanceState != null) {
            mURL = savedInstanceState.getString(KEY_URL, mURL);
        }

    	mSwipeLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.rlf_swipe);
        mSwipeLayout.setOnRefreshListener(this);
        //mSwipeLayout.setEnabled(false); // TODO: Temporarily disabled

        // Uncomment to set colors for loading bar of SwipeRefreshLayout
        /*swipeLayout.setColorScheme(
        		android.R.color.holo_blue_dark, 
                R.color.background_holo_light, 
                android.R.color.holo_blue_dark, 
                R.color.background_holo_light);*/

        mListView = (ListView) mContentView.findViewById(R.id.rlf_list);

        // Set the adapter for the recycler view
        mAdapter = new RSSAdapter(getActivity(), mItems);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

    	loadFeed(false, RSSReader.CONFIG_CACHED_PRIORITY);
    	
    	return mContentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_URL, mURL);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(mPosition);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interruptAll();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	MenuItem refresh = menu.findItem(R.id.action_refresh);
        if(refresh != null) {
            refresh.setVisible(true);
        }
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh) {
            onRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onRequestNetworkState() {

        // TODO fix getActivity() checking
        if(getActivity() == null)
            return false;

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected =
                activeNetwork != null &&
                        activeNetwork.isConnected();
        return isConnected;
    }

    @Override
    public File onRequestCacheFile(String uri) {

        // TODO fix getActivity() checking
        if(getActivity() == null) {
            return null;
        }

        File fileDir = getActivity().getCacheDir();
        String condensedUri = uri.replaceAll("\\W+","");
        String fileName = condensedUri + "_cache.xml";
        File file = new File(fileDir, fileName);
        return file;
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		RSSItem r = mItems.get(position).item;
        String title = ((TextView) view.findViewById(R.id.rlc_title)).getText().toString();

		Intent intent = new Intent(getActivity(), NewsDisplayActivity.class);

		intent.putExtra(NewsDisplayActivity.EXTRA_URL, r.getLink().toString());
		intent.putExtra(NewsDisplayActivity.EXTRA_CONTENT, r.getDescription());
		intent.putExtra(NewsDisplayActivity.EXTRA_TITLE, title);

        getActivity().startActivity(intent);
	}

	/**
	 * Perform a refresh of the feed.
	 * This method will create and start
	 * an AsyncTask to download and parse
	 * a RSS feed and load it to a ListView
	 * 
	 * @param force Refresh even if feed is not null when true. Only refresh when feed is null if false.
	 */
	private void loadFeed(boolean force, int loadConfig) {

        Logger.log(String.format("There are %d items in the adapter", mAdapter.getCount()));

		if(force || (mAdapter.getCount() == 0)) {
            if(runningThreads() == 0) {
                newThread(loadConfig).start();
            }
		}
	}

	@Override
	public void onRefresh() {
		loadFeed(true, RSSReader.CONFIG_ONLINE_PRIORITY);
	}

    /** Keep tabs on threads */

    private static int threadCount = 0;
    private List<Thread> mThreads = new ArrayList<Thread>();

    /**
     * Create a new thread from FeedLoader and add it to list
     *
     * @param loadConfig Define RSSReader loading behavior
     * @return new thread from FeedLoader runnable
     */
    private Thread newThread(int loadConfig) {
        threadCount++;
        Thread thread =  new Thread(new FeedDownloader(this, mURL, loadConfig), "feed-loader-thread-" + threadCount);
        mThreads.add(thread);
        return thread;
    }

    /**
     * Iterate through list of threads.
     * If thread is null or not alive,
     * remove it from list.
     * Otherwise, it is counted as running thread
     * @return number of running threads
     */
    private int runningThreads() {
        int running = 0;
        for(Thread t : mThreads) {
            if(t != null) {
                if(t.isAlive()) {
                    running++;
                } else {
                    mThreads.remove(t);
                }
            } else {
                mThreads.remove(t);
            }
        }
        return running;
    }

    /**
     * Interrupt all threads in list
     */
    private void interruptAll() {
        for(Thread t : mThreads) {
            if(t != null) {
                t.interrupt();
            }
        }
    }
}
