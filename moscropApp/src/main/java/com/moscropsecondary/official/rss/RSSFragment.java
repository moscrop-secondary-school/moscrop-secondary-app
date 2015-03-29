package com.moscropsecondary.official.rss;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.moscropsecondary.official.MainActivity;
import com.moscropsecondary.official.R;
import com.moscropsecondary.official.SettingsFragment;
import com.moscropsecondary.official.ToolbarActivity;
import com.moscropsecondary.official.ToolbarSpinnerAdapter;
import com.moscropsecondary.official.util.Logger;
import com.moscropsecondary.official.util.Preferences;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RSSFragment extends Fragment implements AdapterView.OnItemClickListener,
        OnRefreshListener, LoaderManager.LoaderCallbacks<RSSResult>, AbsListView.OnScrollListener,
        SettingsFragment.SubscriptionListChangedListener, MainActivity.CustomTitleFragment {

    public static final String FEED_NEWS = "moscropschool";
    public static final String FEED_NEWSLETTERS = "moscropnewsletters";
    public static final String FEED_SUBS = "moscropstudents";

    private static final String KEY_BLOGID = "blog_id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_HAS_SPINNER = "has_spinner";
    private static final String KEY_POSITION = "position";

    private static final long STALE_POST_THRESHOLD = 5*60*1000;
    private static long lastRefreshMillis = 0;

    private boolean mAlreadyStartingDetailActivity = false;

	private String mBlogId = "";
    private String mTag = "";
    private String mSearchQuery = null;
    private boolean mAppend = false;
    private boolean mOnlineEnabled = true;
    private boolean mShowCacheWhileLoadingOnline = false;

    private boolean mScrolling = false;

    private int mPosition = 0;

    private View mSpinnerContainer;
    private ToolbarSpinnerAdapter mSpinnerAdapter;
    private boolean mHasSpinner = true;
    private boolean mSpinnerAdded = false;

    public SwipeRefreshLayout mSwipeLayout = null;
    public GridView mListView = null;
    public RSSAdapter mAdapter = null;

    private boolean mSearchViewExpanded = false;

    private boolean mSubscriptionListUpdated = false;

    /**
	 * Create and return a new instance of RSSFragment with given parameters
	 *
	 * @param blogId URL of the RSS feed to load and display
	 */
	public static RSSFragment newInstance(int position, String blogId, String tag) {
		RSSFragment fragment = new RSSFragment();
        fragment.mPosition = position;
		fragment.mBlogId = blogId;
        fragment.mTag = tag;
        if (tag.equals("Student Bulletin")) {
            fragment.mHasSpinner = false;
        } else {
            fragment.mHasSpinner = true;
        }
		return fragment;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set to true to show toolbar menu
    	setHasOptionsMenu(true);

        // Inflate the main view
    	View mContentView = inflater.inflate(R.layout.fragment_rsslist, container, false);

        // Retrieve data that was persisted across a configuration change
        if(savedInstanceState != null) {
            mBlogId = savedInstanceState.getString(KEY_BLOGID, mBlogId);
            mTag = savedInstanceState.getString(KEY_TAG, mTag);
            mHasSpinner = savedInstanceState.getBoolean(KEY_HAS_SPINNER, mHasSpinner);
            mPosition = savedInstanceState.getInt(KEY_POSITION, mPosition);
        }

        // Initialize views
    	mSwipeLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.rlf_swipe);
        mSwipeLayout.setOnRefreshListener(this);

        mListView = (GridView) mContentView.findViewById(R.id.rlf_list);

        // Set the adapter for the recycler view
        mAdapter = new RSSAdapter(getActivity(), new ArrayList<RSSItem>());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);

        if (firstLaunch()) {
            // On first launch, load everything and force online
            // because there is assumed to be nothing cached
            loadFeed(false, null, false, true, true, false);      // Just in case there is some cache
        } else {
            // Otherwise do not load online automatically unless
            // the user specified it in SharedPreferences.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean autoRefresh = prefs.getBoolean(Preferences.Keys.AUTO_REFRESH, Preferences.Default.AUTO_REFRESH);
            boolean existingLoadedPostsAreStale = (System.currentTimeMillis() - lastRefreshMillis) > STALE_POST_THRESHOLD;
            boolean loadOnline = existingLoadedPostsAreStale && (autoRefresh && (savedInstanceState == null));
            loadFeed(false, null, false, loadOnline, true, false);
        }

        // Initialize and add the toolbar spinner
        mSpinnerAdapter = new ToolbarSpinnerAdapter(getActivity(), new ArrayList<String>());

        if (mHasSpinner) {
            setUpToolbarSpinner();
            SettingsFragment.registerSubscriptionListChangedListener(this);     // Receive updates when the user changed their subscriptions
        }

        return mContentView;
    }

    /**
     * Helper method to determine if this is the
     * first time the app has been launched
     *
     * @return  true if app hasn't been launched, otherwise false
     */
    private boolean firstLaunch() {
        SharedPreferences prefs = getActivity().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS);
        return prefs.getBoolean(Preferences.App.Keys.FIRST_LAUNCH, Preferences.App.Default.FIRST_LAUNCH);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAlreadyStartingDetailActivity = false;

        // Only add the spinner if the fragment is
        // set to have a spinner. For example,
        // sometimes this fragment is locked to
        // certain tags such as "Student Bulletin"
        // and does not require a spinner to change tags.
        if (mHasSpinner) {
            if (!mSpinnerAdded) {
                setUpToolbarSpinner();
            }
        } else {
            // Allow the MainActivity to determine the toolbar title instead
            ((MainActivity) getActivity()).onSectionAttached(mPosition);
        }

        // If there has been a change to the user's subcriptions,
        // we must update the spinner list and reload the feed
        // to reflect those changes.
        if (mSubscriptionListUpdated) {
            updateSpinnerList();

            // TODO only reload when the user is viewing "Subscribed",
            // TODO otherwise their subscription preferences have no
            // TODO affect on the posts shown.
            loadFeed(true, null, false, false, false, false);
        }
    }

    @Override
    public void onSubscriptionListChanged() {
        mSubscriptionListUpdated = true;
    }

    private void setUpToolbarSpinner() {

        if (!mSearchViewExpanded) {

            Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();

            // Remove all previously added spinners
            toolbar.removeView(mSpinnerContainer);

            // Add spinner container
            mSpinnerContainer = LayoutInflater.from(getActivity()).inflate(R.layout.actionbar_spinner, toolbar, false);
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(mSpinnerContainer, lp);

            mSpinnerAdded = true;

            // Update tags list
            updateSpinnerList();

            // Initialize spinner and set adapter
            Spinner spinner = (Spinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
            spinner.setAdapter(mSpinnerAdapter);

            // Set initial selection
            int position = 0;
            for (int i = 0; i < mSpinnerAdapter.getCount(); i++) {
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
                        loadFeed(true, null, false, false, false, true);    // Not loading online, so don't worry about cache
                        // Stop previous loader and start new one
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    /**
     * Helper method used to retrieve a list
     * of tags the user has subscribed to
     * and add them to the spinner adapter
     */
    private void updateSpinnerList() {
        String[] spinnerTagsArray = null;
        try {
            spinnerTagsArray = RSSTagCriteria.getSubscribedTags(getActivity());
        } catch (IOException | JSONException e) {
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

        mSubscriptionListUpdated = false;   // we just updated
    }

    @Override
    public void onStop() {
        super.onStop();

        // Search view is automatically closed during onStop(),
        // so we must let the rest of the app know that is is
        // no longer expanded.
        mSearchViewExpanded = false;

        // If the fragment has a spinner, we must save the
        // last viewed tag so the fragment will open up to
        // that tag the next time the user enters this fragment.
        // If the fragment does not have a spinner, such as
        // when the tag to view is locked to "Student Bulletin",
        // do not save the tag.
        if (mHasSpinner) {
            SharedPreferences.Editor prefs = getActivity().getSharedPreferences(Preferences.App.NAME, Context.MODE_MULTI_PROCESS).edit();
            prefs.putString(Preferences.App.Keys.RSS_LAST_TAG, mTag);
            prefs.apply();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the custom title as to not
        // interfere with other fragments'
        // custom titles.
        removeCustomTitle();
    }

    @Override
    public void removeCustomTitle() {
        if (getActivity() != null) {
            Toolbar toolbar = ((ToolbarActivity) getActivity()).getToolbar();
            if (toolbar != null) {
                toolbar.removeView(mSpinnerContainer);
                mSpinnerAdded = false;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_BLOGID, mBlogId);
        outState.putString(KEY_TAG, mTag);
        outState.putBoolean(KEY_HAS_SPINNER, mHasSpinner);
        outState.putInt(KEY_POSITION, mPosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_rss, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint("Search news");

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchViewExpanded = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchViewExpanded = false;
                if (mHasSpinner) {
                    // Re-add spinner once search bar has collapsed
                    setUpToolbarSpinner();
                }
                if (mSearchQuery != null) {
                    // Return to the main feed
                    loadFeed(true, null, false, false, false, true);
                }
                return true;
            }
        });

    	super.onCreateOptionsMenu(menu, inflater);
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

        if (!mAlreadyStartingDetailActivity) {

            mAlreadyStartingDetailActivity = true;

            RSSItem r = mAdapter.getItem(position);

            Intent intent = new Intent(getActivity(), NewsDisplayActivity.class);

            // Pass on information about the RSS post
            // to the NewsDisplayActivity.
            intent.putExtra(NewsDisplayActivity.EXTRA_URL, r.url);
            intent.putExtra(NewsDisplayActivity.EXTRA_CONTENT, r.content);
            intent.putExtra(NewsDisplayActivity.EXTRA_TITLE, r.title);

            // Pass on information about the position and state
            // of the card to the NewsDisplayActivity.
            int orientation = getResources().getConfiguration().orientation;
            int[] screenLocation = new int[2];
            view.getLocationOnScreen(screenLocation);
            intent.putExtra(NewsDisplayActivity.EXTRA_ORIENTATION, orientation)
                    .putExtra(NewsDisplayActivity.EXTRA_LEFT, screenLocation[0])
                    .putExtra(NewsDisplayActivity.EXTRA_TOP, screenLocation[1])
                    .putExtra(NewsDisplayActivity.EXTRA_WIDTH, view.getWidth())
                    .putExtra(NewsDisplayActivity.EXTRA_HEIGHT, view.getHeight())
                    .putExtra(NewsDisplayActivity.EXTRA_TOOLBAR_FROM, getFromColor(position))
                    .putExtra(NewsDisplayActivity.EXTRA_TOOLBAR_TO, getToolbarColor())
                    .putExtra(NewsDisplayActivity.EXTRA_TITLE_COLOR, getRssTitleColor(position))
                    .putExtra(NewsDisplayActivity.EXTRA_RSS_ITEM, r);

            getActivity().startActivity(intent);

            // Override transitions: we don't want the normal
            // window animation in addition to our custom one
            getActivity().overridePendingTransition(0, 0);
        }
	}

    /**
     * Get the color the card was
     * when it was displayed in the list
     *
     * @param position
     *          Position of the card
     * @return  integer color of the form 0xAARRGGBB
     */
    private int getFromColor(int position) {
        return mAdapter.getCardBackgroundColor(position);
    }

    /**
     * Get the color of the toolbar
     *
     * @return  integer color of the form 0xAARRGGBB
     */
    private int getToolbarColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.toolbar_color, typedValue, true);
        int bgcolor = typedValue.data;
        return bgcolor;
    }

    /**
     * Get the color of the title text for
     * a specified card.
     *
     * @param position
     *          Position of the card
     * @return  integer color of the form 0xAARRGGBB
     */
    private int getRssTitleColor(int position) {
        int resID = R.attr.rss_card_text_1;
        switch(position % 4) {
            case 0:
            case 3:
                resID = R.attr.rss_card_text_1;
                break;
            case 1:
            case 2:
                resID = R.attr.rss_card_text_2;
                break;
        }

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(resID, typedValue, true);
        return typedValue.data;
    }

    /**
     * Perform a refresh of the feed.
     * This method will create and start
     * an AsyncTaskLoader to download
     * and parse a RSS feed.
     *
     * @param force
     *          If true, feed will refresh even if there are already items. Else feed will only refresh when empty.
     * @param searchQuery
     *          Query for posts matching this String.
     *          Set null for no search.
     * @param append
     *          True to only load posts after the oldest cached post
     *          and for results to be added to the bottom of the RSS list.
     *          False for a normal load that performs a full refresh
     *          of the cache and completely reloads the list.
     * @param onlineEnabled
     *          True to allow loading online.
     *          False to load only from cache.
     * @param showCacheWhileLoadingOnline
     *          True to perform a load from cache so the user
     *          is not staring at a blank screen while waiting
     *          for content to load from online.
     *          False to only reload list content once an
     *          updated feed is done downloading and parsing.
     * @param runEvenIfHasRunningLoaders
     *          True to allow the load to run even if
     *          other loads are already running.
     *          False to cancel this load request if
     *          other loads are already running.
     */
	private void loadFeed(boolean force, String searchQuery, boolean append, boolean onlineEnabled, boolean showCacheWhileLoadingOnline, boolean runEvenIfHasRunningLoaders) {
		if(force || (mAdapter.getCount() == 0)) {
            if(runEvenIfHasRunningLoaders || !getLoaderManager().hasRunningLoaders()) {

                mSearchQuery = searchQuery;
                mAppend = append;
                mOnlineEnabled = onlineEnabled;
                mShowCacheWhileLoadingOnline = showCacheWhileLoadingOnline && mOnlineEnabled;

                if (mOnlineEnabled) {
                    lastRefreshMillis = System.currentTimeMillis();
                }

                Logger.log("refreshing list");
                getLoaderManager().restartLoader(0, null, this);    // Force a new reload
            }
		}
	}

	@Override
	public void onRefresh() {
		loadFeed(true, null, false, true, false, false);     // Don't show cache as old data is still not cleared
	}

    public void doSearch(String query) {

        // TODO debug toast, remove before release
        Toast.makeText(getActivity(), "News: " + query, Toast.LENGTH_SHORT).show();

        // Search request handled by loadFeed()
        loadFeed(true, query, false, false, false, true);
    }

    @Override
    public Loader<RSSResult> onCreateLoader(int i, Bundle bundle) {
        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(true);
        }
        long oldestPostDate = System.currentTimeMillis();
        if (mAdapter.getCount() > 0 && mAppend) {
            oldestPostDate = mAdapter.getItem(mAdapter.getCount()-1).date;
        }

        return new RSSListLoader(getActivity(), mBlogId, mTag, mSearchQuery, mAppend, oldestPostDate, mOnlineEnabled, mShowCacheWhileLoadingOnline);
    }

    @Override
    public void onLoadFinished(Loader<RSSResult> listLoader, RSSResult result) {

        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(false);
        }

        if (result != null && result.items != null) {

            Logger.log("Result code: " + result.resultCode);

            switch(result.resultCode) {
                case RSSResult.RESULT_OK:
                    updateListOnLoadFinished(result);
                    break;
                case RSSResult.RESULT_REDUNDANT:
                    Logger.log("RSS load finished for tag " + mTag + ", result is redundant");
                    break;
                case RSSResult.RESULT_REDO_ONLINE:
                    updateListOnLoadFinished(result);
                    loadFeed(true, null, false, true, false, true);
                    break;
                case RSSResult.RESULT_FAIL:
                    Toast.makeText(getActivity(), R.string.load_error_text, Toast.LENGTH_SHORT).show();
                    break;
            }

        } else {
            Toast.makeText(getActivity(), R.string.load_error_text, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateListOnLoadFinished(RSSResult result) {
        if (result.append) {

            List<RSSItem> items = result.items;
            if (items.size() == 0) {
                Toast.makeText(getActivity(), "No more items to load", Toast.LENGTH_SHORT).show();
                // TODO replace with text view
            } else {
                for (RSSItem item : items) {
                    mAdapter.add(item);
                }
                mAdapter.notifyDataSetChanged();
            }

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
    }

    @Override
    public void onLoaderReset(Loader<RSSResult> listLoader) {
        // No reference to the list provided by the loader is held
        // So no resetting is needed to be done here
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

        if (mScrolling) {
            boolean loadMore = false;
            if (mListView != null && mListView.getChildAt(mListView.getChildCount() - 1) != null
                    && mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1
                    && mListView.getChildAt(mListView.getChildCount() - 1).getBottom() <= mListView.getHeight()) {
                loadMore = true;
            }

            if (loadMore) {
                loadFeed(true, null, true, true, false, false);  // No need to show cache, old posts still there
                mScrolling = false;
            }
        }
    }
}
