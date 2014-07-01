package com.ivon.moscropsecondary.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.list.CardUtil;
import com.ivon.moscropsecondary.list.RSSAdapter;
import com.ivon.moscropsecondary.list.RSSAdapter.OnItemClickListener;
import com.ivon.moscropsecondary.list.RSSAdapter.ViewModel;
import com.ivon.moscropsecondary.util.Logger;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RSSFragment extends Fragment implements OnItemClickListener, OnRefreshListener {

	public static final String MOSCROP_CHEMISTRY_URL = "http://moscropchemistry.wordpress.com/feed/";
	public static final String REDDIT_URL = "http://www.reddit.com/r/aww/.rss";
	public static final String HUGO_BARA_URL = "http://gplus-to-rss.appspot.com/rss/+HugoBarra";
	public static final String MOSCROP_PAGE_URL = "http://gplus-to-rss.appspot.com/rss/108865428316172309900";
	public static final String TEST_EMAIL_URL = "http://emails2rss.appspot.com/rss?id=1af3a2260113d04b2c0b99e0f751921b9365";
	public static final String BLOGGER_URL = "http://moscropschool.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_NEWS_URL = "http://moscropschool.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_SUBS_URL = "http://moscropstudents.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_NEWSLETTER_URL = "http://moscropnewsletters.blogspot.ca/feeds/posts/default?alt=rss";

    private static final String KEY_URL = "url";
    private static final String KEY_TYPE = "type";
	
	private String mURL = BLOGGER_URL;
    private RSSFeed mFeed = null;

    SwipeRefreshLayout mSwipeLayout;
    RecyclerView mRecyclerView = null;
    RSSAdapter mAdapter = null;
    RecyclerView.LayoutManager mLayoutManager;
    List<ViewModel> mItems = new ArrayList<ViewModel>();

    private int mType;  // TODO determine this from the category of RSS Item instead of globally defined

	/**
	 * Create and return a new instance of RSSFragment with given parameters
	 * 
	 * @param feed URL of the RSS feed to load and display
	 * @param type Type of CardProcessor that is used
	 * @return New instance of RSSFragment
	 */
	public static RSSFragment newInstance(String feed, int type) {
		RSSFragment fragment = new RSSFragment();
		fragment.mURL = feed;
        fragment.mType = type;
		return fragment;
	}
	
	/**
	 * Check network connectivity state
	 * 
	 * @return True if device is connected to network. Otherwise false. 
	 */
	private boolean isConnected() {
		ConnectivityManager cm =
				(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
			 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = 
				activeNetwork != null &&
				activeNetwork.isConnected();
		return isConnected;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);

    	View mContentView = inflater.inflate(R.layout.fragment_rsslist, container, false);
        mContentView.setBackgroundColor(0xffe4e4e4);

        if(savedInstanceState != null) {
            mURL = savedInstanceState.getString(KEY_URL, mURL);
            mType = savedInstanceState.getInt(KEY_TYPE, mType);
        }

    	mSwipeLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setEnabled(false); // TODO: Temporarily disabled

        // Uncomment to set colors for loading bar of SwipeRefreshLayout
        /*swipeLayout.setColorScheme(
        		android.R.color.holo_blue_dark, 
                R.color.background_holo_light, 
                android.R.color.holo_blue_dark, 
                R.color.background_holo_light);*/

        mRecyclerView = (RecyclerView) mContentView.findViewById(R.id.rlf_list);
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Set the adapter for the recycler view
        mAdapter = new RSSAdapter(mItems);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

    	doRefresh(false);
    	
    	return mContentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_URL, mURL);
        outState.putInt(KEY_TYPE, mType);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	menu.findItem(R.id.action_refresh).setVisible(true);
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

    private class FeedLoaderTask extends AsyncTask<String, Void, RSSFeed> {
		
    	private File getCacheFile(String uri) {
    		// Create the file to save to
    	    File fileDir = getActivity().getCacheDir();
    	    String condensedUri = uri.replaceAll("\\W+","");
    	    String fileName = condensedUri + "_cache.xml";
    	    File file = new File(fileDir, fileName);
    	    return file;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		//feedList.removeFooterView(tv);
    	}
    	
    	@Override
    	protected final RSSFeed doInBackground(String... urls) {
    		
    		ConnectivityManager cm =
    		        (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    		
        	RSSReader reader = new RSSReader(cm);
    		try {
    			mFeed = reader.load(urls[0], getCacheFile(urls[0]));
    		} catch (RSSReaderException e) {
    			e.printStackTrace();
    		}
    		reader.close();
    		
    		try {
    			Thread.sleep(500);
    		} catch (InterruptedException e1) {
    			e1.printStackTrace();
    		}
    		return mFeed;
    	}
    	
    	@Override
		protected void onPostExecute(RSSFeed feed) {
			Logger.log("onposeexecute");
			
			if( (feed != null) && (feed.getItems().size() > 0) ) {
				onValidFeed(feed);
			} else {
				onInvalidFeed(feed);
			}
			mSwipeLayout.setRefreshing(false);
    	}
    	
    	private void onValidFeed(RSSFeed feed) {
			Logger.log("feed is not null, it has " + feed.getItems().size() + " items.");
			mAdapter.clear();
			mAdapter.notifyDataSetChanged();
			for(RSSItem r : feed.getItems()) {
				if(r != null) {
                    ViewModel vm = new ViewModel(r, CardUtil.getCardProcessor(mType));
                    mAdapter.add(vm);
				}
			}
    	}
    	
    	private void onInvalidFeed(RSSFeed feed) {
    		/*tv.setGravity(Gravity.CENTER);
    		tv.setTypeface(null, Typeface.BOLD_ITALIC);
    		feedList.setFooterDividersEnabled(false);
    		if(feed == null) {
    			Logger.log("feed is null");
    			tv.setText("Error loading feed");
    		} else { 
    			Logger.log("feed has 0 items");
    			tv.setText("Nothing to display");
    		}
    		feedList.addFooterView(tv);*/
    	}
    }
    
	@Override
	public void onItemClick(View view, ViewModel item) {

		RSSItem r = item.mRSSItem;
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
	private void doRefresh(boolean force) {
		
		if(force || (mFeed == null)) {
			
			// Refresh only if forced or if feed is null
			
			if(isConnected()) {
				if(mSwipeLayout != null) mSwipeLayout.setRefreshing(true);
				FeedLoaderTask mTask = new FeedLoaderTask();
				mTask.execute(mURL);
			} else {
				Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
				return;
			}
			
		} else {
			// No need to refresh
			Logger.log("doRefresh(): no need to refresh");
		}
	}
	
	@Override
	public void onRefresh() {
		doRefresh(true);
	}
}
