package com.moscropsecondary.official.rss;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.moscropsecondary.official.MainActivity;
import com.moscropsecondary.official.R;
import com.moscropsecondary.official.ToolbarActivity;
import com.moscropsecondary.official.util.Logger;
import com.moscropsecondary.official.util.Preferences;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RSSFragment extends Fragment
        implements AdapterView.OnItemClickListener, OnRefreshListener, LoaderManager.LoaderCallbacks<RSSResult>, AbsListView.OnScrollListener {

    public static final String FEED_NEWS = "moscropschool";
    public static final String FEED_NEWSLETTERS = "moscropnewsletters";
    public static final String FEED_SUBS = "moscropstudents";

    private static final String KEY_BLOGID = "blog_id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_POSITION = "position";
	
	private String mBlogId = "";
    private String mTag = "";
    private boolean mAppend = false;
    private boolean mOnlineEnabled = true;

    private boolean mScrolling = false;

    private int mPosition = 0;

    private View mSpinnerContainer;
    private ArrayAdapter<String> mSpinnerAdapter;

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

        mListView = (ListView) mContentView.findViewById(R.id.rlf_list);

        // Set the adapter for the recycler view
        mAdapter = new RSSAdapter(getActivity(), new ArrayList<RSSItem>());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);

        if (firstLaunch()) {
            loadFeed(false, false, true);
        } else {
            loadFeed(false, false, false);
        }
        mSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());

        return mContentView;
    }

    private boolean firstLaunch() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        return prefs.getBoolean(Preferences.App.Keys.FIRST_LAUNCH, Preferences.App.Default.FIRST_LAUNCH);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpToolbarSpinner();
    }

    private void setUpToolbarSpinner() {

        // Add spinner container
        Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
        mSpinnerContainer= LayoutInflater.from(getActivity()).inflate(R.layout.actionbar_spinner, toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(mSpinnerContainer, lp);

        // Update tags list
        String[] spinnerTagsArray = null;
        try {
            spinnerTagsArray = RSSTagCriteria.getSubscribedTags(getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (spinnerTagsArray != null) {
            mSpinnerAdapter.clear();
            mSpinnerAdapter.add("Subscribed");
            mSpinnerAdapter.add("All");
            for (String tag : spinnerTagsArray) {
                mSpinnerAdapter.add(tag);
            }
        }

        // Initialize spinner and set adapter
        Spinner spinner = (Spinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(mSpinnerAdapter);

        // Set initial selection
        int position = 0;
        for (int i=0; i<mSpinnerAdapter.getCount(); i++) {
            String tag = mSpinnerAdapter.getItem(i);
            if (tag.equals(mTag)) {
                position = i;
                break;
            }
        }
        spinner.setSelection(position);

        // When item is selected, set mTag and then reload the feed
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                String tag = mSpinnerAdapter.getItem(position);
                if (!tag.equals(mTag)) {
                    mTag = tag;
                    loadFeed(true, false, false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
        toolbar.removeView(mSpinnerContainer);
        SharedPreferences.Editor prefs = getActivity().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS).edit();
        prefs.putString(Preferences.App.Keys.RSS_LAST_TAG, mTag);
        prefs.apply();
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
        ((MainActivity) getActivity()).onSectionAttached(-1);
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
	private void loadFeed(boolean force, boolean append, boolean onlineEnabled) {
		if(force || (mAdapter.getCount() == 0)) {
            if(!getLoaderManager().hasRunningLoaders()) {
                mAppend = append;
                mOnlineEnabled = onlineEnabled;
                getLoaderManager().restartLoader(0, null, this);    // Force a new reload
            }
		}
	}

	@Override
	public void onRefresh() {
		loadFeed(true, false, true);
	}

    @Override
    public Loader<RSSResult> onCreateLoader(int i, Bundle bundle) {
        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(true);
        }
        return new RSSListLoader(getActivity(), mBlogId, mTag, mAppend, mOnlineEnabled);
    }

    @Override
    public void onLoadFinished(Loader<RSSResult> listLoader, RSSResult result) {
        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(false);
        }

        if (result != null) {
            if (result.append) {

            } else {
                mAdapter.clear();
                List<RSSItem> items = result.items;
                if (items.size() == 0) {
                    Toast.makeText(getActivity(), "No items returned", Toast.LENGTH_SHORT).show();
                    // TODO replace with text view
                } else {
                    for (RSSItem item : items) {
                        mAdapter.add(item);
                    }
                    mAdapter.notifyDataSetChanged();
                    if (firstLaunch()) {
                        SharedPreferences.Editor prefs = getActivity().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS).edit();
                        prefs.putBoolean(Preferences.App.Keys.FIRST_LAUNCH, false);
                        prefs.apply();
                    }
                }
            }
        } else {
            Toast.makeText(getActivity(), R.string.load_error_text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<RSSResult> listLoader) {
        // No reference to the list provided by the loader is held
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
            mScrolling = true;
        } else {
            mScrolling = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //boolean loadMore =  firstVisibleItem + visibleItemCount >= totalItemCount-1;

        if (mScrolling) {
            boolean loadMore = false;
            if (mListView != null && mListView.getChildAt(mListView.getChildCount() - 1) != null
                    && mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1
                    && mListView.getChildAt(mListView.getChildCount() - 1).getBottom() <= mListView.getHeight()) {
                loadMore = true;
            }

            if (loadMore) {
                Logger.log("Trying to load more feed");
                loadFeed(true, true, true);
                mScrolling = false;
            }
        }
    }
}