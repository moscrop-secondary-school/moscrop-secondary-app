package com.ivon.moscropsecondary.rss;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import android.widget.Toast;

import com.ivon.moscropsecondary.MainActivity;
import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class RSSFragment extends Fragment
        implements AdapterView.OnItemClickListener, OnRefreshListener, LoaderManager.LoaderCallbacks<List<RSSItem>> {

    public static final String FEED_NEWS = "moscropschool";
    public static final String FEED_NEWSLETTERS = "moscropnewsletters";
    public static final String FEED_SUBS = "moscropstudents";

    private static final String KEY_BLOGID = "blog_id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_POSITION = "position";
	
	private String mBlogId = "";
    private String mTag = "";
    private boolean mOnlineEnabled = true;

    private int mPosition = 0;

    public SwipeRefreshLayout mSwipeLayout = null;
    public ListView mListView = null;
    public RSSAdapter mAdapter = null;

	/**
	 * Create and return a new instance of RSSFragment with given parameters
	 * 
	 * @param blogId URL of the RSS feed to load and display
	 * @return New instance of RSSFragment
	 */
	public static RSSFragment newInstance(int position, String blogId, String tag) {
		RSSFragment fragment = new RSSFragment();
        fragment.mPosition = position;
		fragment.mBlogId = blogId;
        fragment.mTag = tag;
		return fragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);

    	View mContentView = inflater.inflate(R.layout.fragment_rsslist, container, false);
        //mContentView.setBackgroundColor(0xffe4e4e4);

        if(savedInstanceState != null) {
            mBlogId = savedInstanceState.getString(KEY_BLOGID, mBlogId);
            mTag = savedInstanceState.getString(KEY_TAG, mTag);
            mPosition = savedInstanceState.getInt(KEY_POSITION, mPosition);
        }

    	mSwipeLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.rlf_swipe);
        mSwipeLayout.setOnRefreshListener(this);

        // Uncomment to set colors for loading bar of SwipeRefreshLayout
        /*swipeLayout.setColorScheme(
        		android.R.color.holo_blue_dark, 
                R.color.background_holo_light, 
                android.R.color.holo_blue_dark, 
                R.color.background_holo_light);*/

        mListView = (ListView) mContentView.findViewById(R.id.rlf_list);

        // Set the adapter for the recycler view
        mAdapter = new RSSAdapter(getActivity(), new ArrayList<RSSItem>());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        loadFeed(false, false);

    	return mContentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_BLOGID, mBlogId);
        outState.putString(KEY_TAG, mTag);
        outState.putInt(KEY_POSITION, mPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.log("onActivityCreated from fragment; position: " + mPosition);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		RSSItem r = mAdapter.getItem(position);

		Intent intent = new Intent(getActivity(), NewsDisplayActivity.class);

		intent.putExtra(NewsDisplayActivity.EXTRA_URL, r.url);
		intent.putExtra(NewsDisplayActivity.EXTRA_CONTENT, r.content);
		intent.putExtra(NewsDisplayActivity.EXTRA_TITLE, r.title);

        getActivity().startActivity(intent);
	}

	/**
	 * Perform a refresh of the feed.
	 * This method will create and start
	 * an AsyncTask to download and parse
	 * a RSS feed and load it to a ListView
	 * 
	 * @param force
     *          If true, feed will refresh even if there are already items. Else feed will only refresh when empty.
	 */
	private void loadFeed(boolean force, boolean onlineEnabled) {
		if(force || (mAdapter.getCount() == 0)) {
            if(!getLoaderManager().hasRunningLoaders()) {
                mOnlineEnabled = onlineEnabled;
                getLoaderManager().restartLoader(0, null, this);    // Force a new reload
            }
		}
	}

	@Override
	public void onRefresh() {
		loadFeed(true, true);
	}

    @Override
    public Loader<List<RSSItem>> onCreateLoader(int i, Bundle bundle) {
        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(true);
        }
        return new RSSListLoader(getActivity(), mBlogId, mTag, mOnlineEnabled);
    }

    @Override
    public void onLoadFinished(Loader<List<RSSItem>> listLoader, List<RSSItem> items) {
        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(false);
        }

        if (items != null) {
            mAdapter.clear();
            if (items.size() == 0) {
                Toast.makeText(getActivity(), "No items returned", Toast.LENGTH_SHORT).show();
                // TODO replace with text view
            } else {
                for (RSSItem item : items) {
                    mAdapter.add(item);
                }
                mAdapter.notifyDataSetChanged();
            }
        } else {
            Toast.makeText(getActivity(), R.string.load_error_text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<RSSItem>> listLoader) {
        // No reference to the list provided by the loader is held
    }
}
