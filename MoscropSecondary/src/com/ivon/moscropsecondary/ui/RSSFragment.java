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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.ui.RSSAdapter.CardProcessor;
import com.ivon.moscropsecondary.util.Logger;

public class RSSFragment extends AbstractFeedFragment implements OnScrollListener, OnItemClickListener {

	ListView feedList = null;
	ArrayList<RSSItem> mArrayList = new ArrayList<RSSItem>();
	private RSSAdapter mAdapter = null;
	private TextView tv;
	
	private CardProcessor cardProcessor;
	
	public static RSSFragment newInstance(String feed, CardProcessor cardProcessor) {
		RSSFragment n = new RSSFragment();
		n.feedToLoad = feed;
		n.cardProcessor = cardProcessor;
		return n;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);
    	setRetainInstance(true);
    	
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
    	doRefresh();
    	
    	return mContentView;
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
	
	@Override
	public void onRefresh() {
		super.onRefresh();
		FeedLoaderTask mTask = new FeedLoaderTask();
		//feedToLoad = TEST_EMAIL_URL;
		mTask.execute(feedToLoad);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		int index = view.getFirstVisiblePosition();
		if(index == 0) {
			View v = view.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			if(top == 0) {
				swipeLayout.setEnabled(true);
			} else {
				swipeLayout.setEnabled(false);
			}
		} else {
			swipeLayout.setEnabled(false);
		}		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}
}
