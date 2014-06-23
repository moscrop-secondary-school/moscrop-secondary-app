package com.ivon.moscropsecondary.ui;

import org.mcsoxford.rss.RSSFeed;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.R.id;
import com.ivon.moscropsecondary.util.Logger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class AbstractFeedFragment extends Fragment implements OnRefreshListener {
	
	public static final String MOSCROP_CHEMISTRY_URL = "http://moscropchemistry.wordpress.com/feed/";
	public static final String REDDIT_URL = "http://www.reddit.com/r/aww/.rss";
	public static final String HUGO_BARA_URL = "http://gplus-to-rss.appspot.com/rss/+HugoBarra";
	public static final String MOSCROP_PAGE_URL = "http://gplus-to-rss.appspot.com/rss/108865428316172309900";
	public static final String TEST_EMAIL_URL = "http://emails2rss.appspot.com/rss?id=1af3a2260113d04b2c0b99e0f751921b9365";
	public static final String BLOGGER_URL = "http://moscropschool.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_NEWS_URL = "http://moscropschool.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_SUBS_URL = "http://moscropstudents.blogspot.ca/feeds/posts/default?alt=rss";
	public static final String BLOGGER_NEWSLETTER_URL = "http://moscropnewsletters.blogspot.ca/feeds/posts/default?alt=rss";
	
	protected RSSFeed feed = null;
	SwipeRefreshLayout swipeLayout;
	
	protected String feedToLoad = BLOGGER_URL;
	
	protected boolean isConnected() {
		ConnectivityManager cm =
				(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
			 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
				activeNetwork.isConnected();
		return isConnected;
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
	
	protected void doRefresh() {
		if(feed == null)
			onRefresh();
		else
			Logger.log("feed is not null");
	}
    
	@Override 
	public void onRefresh() {
		if(isConnected()) {
			if(swipeLayout != null) swipeLayout.setRefreshing(true);
		} else {
			Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}
	}
}
