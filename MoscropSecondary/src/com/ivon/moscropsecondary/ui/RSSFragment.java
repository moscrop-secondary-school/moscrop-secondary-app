package com.ivon.moscropsecondary.ui;

import java.io.File;
import java.util.ArrayList;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.ui.RSSAdapter.CardProcessor;
import com.ivon.moscropsecondary.util.Logger;

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
	
	private RSSFeed feed = null;
	SwipeRefreshLayout swipeLayout;
	
	private String feedToLoad = BLOGGER_URL;
	
	ListView feedList = null;
	ArrayList<RSSItem> mArrayList = new ArrayList<RSSItem>();
	private RSSAdapter mAdapter = null;
	private TextView tv;
	
	private CardProcessor cardProcessor;
	
	/**
	 * Create and return a new instance of RSSFragment with given parameters
	 * 
	 * @param feed URL of the RSS feed to load and display
	 * @param cardProcessor A CardProcessor to define the design of the list item cards
	 * @return New instance of RSSFragment
	 */
	public static RSSFragment newInstance(String feed, CardProcessor cardProcessor) {
		RSSFragment n = new RSSFragment();
		n.feedToLoad = feed;
		n.cardProcessor = cardProcessor;
		return n;
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
    	
    	swipeLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        /*swipeLayout.setColorScheme(
        		android.R.color.holo_blue_dark, 
                R.color.background_holo_light, 
                android.R.color.holo_blue_dark, 
                R.color.background_holo_light);*/
    	
        /*int topPaddingHeightDp = (int) (getResources().getDimension(R.dimen.card_list_top) / getResources().getDisplayMetrics().density);
        int bottomPaddingHeightDp = (int) (getResources().getDimension(R.dimen.card_list_bottom) / getResources().getDisplayMetrics().density);
        
        LinearLayout topPadding = new LinearLayout(getActivity());
        topPadding.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams topParam = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, topPaddingHeightDp);
        topPadding.setLayoutParams(topParam);
        
        LinearLayout bottomPadding = new LinearLayout(getActivity());
        bottomPadding.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams bottomParam = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, bottomPaddingHeightDp);
        bottomPadding.setLayoutParams(bottomParam);*/
        
        View header = inflater.inflate(R.layout.rss_list_header, null);
        View footer = inflater.inflate(R.layout.rss_list_footer, null);
        
    	feedList = (ListView) mContentView.findViewById(R.id.news_list);
    	
    	feedList.addHeaderView(header, null, false);
    	feedList.addFooterView(footer, null, false);
    	
    	mAdapter = new RSSAdapter(getActivity(), mArrayList, cardProcessor);
    	feedList.setAdapter(mAdapter);
    	feedList.setOnItemClickListener(this);
    	//feedList.setOnScrollListener(this);
    	
    	tv = new TextView(getActivity());
    	doRefresh(false);
    	
    	return mContentView;
    }
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	menu.findItem(R.id.action_refresh).setVisible(true);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
        if(itemId == R.id.action_refresh) {
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
    		feedList.removeFooterView(tv);
    	}
    	
    	@Override
    	protected final RSSFeed doInBackground(String... urls) {
    		
    		ConnectivityManager cm =
    		        (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    		
        	RSSReader reader = new RSSReader(cm);
    		try {
    			feed = reader.load(urls[0], getCacheFile(urls[0]));
    		} catch (RSSReaderException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		reader.close();
    		
    		try {
    			Thread.sleep(500);
    		} catch (InterruptedException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
    		return feed;
    	}
    	
    	@Override
		protected void onPostExecute(RSSFeed feed) {
			Logger.log("onposeexecute");
			
			if( (feed != null) && (feed.getItems().size() > 0) ) {
				onValidFeed(feed);
			} else {
				onInvalidFeed(feed);
			}
			swipeLayout.setRefreshing(false);
    	}
    	
    	private void onValidFeed(RSSFeed feed) {
			Logger.log("feed is not null, it has " + feed.getItems().size() + " items.");
			mArrayList.clear();
			mAdapter.notifyDataSetChanged();
			for(RSSItem r : feed.getItems()) {
				if(r.getTitle() != null) {
					mArrayList.add(r);
					mAdapter.notifyDataSetChanged();
				}
			}
    	}
    	
    	private void onInvalidFeed(RSSFeed feed) {
    		tv.setGravity(Gravity.CENTER);
    		tv.setTypeface(null, Typeface.BOLD_ITALIC);
    		feedList.setFooterDividersEnabled(false);
    		if(feed == null) {
    			Logger.log("feed is null");
    			tv.setText("Error loading feed");
    		} else { 
    			Logger.log("feed has 0 items");
    			tv.setText("Nothing to display");
    		}
    		feedList.addFooterView(tv);
    	}
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		RSSItem r = mArrayList.get(position-1);
		
		Intent intent = new Intent(getActivity(), NewsDisplayActivity.class);
		Logger.log("Link is " + r.getLink());
		intent.putExtra(NewsDisplayActivity.EXTRA_URL, r.getLink().toString());
		intent.putExtra(NewsDisplayActivity.EXTRA_CONTENT, r.getDescription());
		
		String title = "";
		if(cardProcessor != null) {
			title = cardProcessor.toProcessedTitle(r.getTitle());
		} else {
			title = "Unknown";
		}
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
		
		if(force || (feed == null)) {
			
			// Refresh only if forced or if feed is null
			
			if(isConnected()) {
				if(swipeLayout != null) swipeLayout.setRefreshing(true);
				FeedLoaderTask mTask = new FeedLoaderTask();
				mTask.execute(feedToLoad);
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
