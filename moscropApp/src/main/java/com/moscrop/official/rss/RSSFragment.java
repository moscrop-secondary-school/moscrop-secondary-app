package com.moscrop.official.rss;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.moscrop.official.MainActivity;
import com.moscrop.official.R;
import com.moscrop.official.SettingsFragment;
import com.moscrop.official.ToolbarActivity;
import com.moscrop.official.ToolbarSpinnerAdapter;
import com.moscrop.official.util.Logger;
import com.moscrop.official.util.Preferences;
import com.moscrop.official.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RSSFragment extends Fragment implements AdapterView.OnItemClickListener,
        OnRefreshListener, AbsListView.OnScrollListener,
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

    private String mTag = "";
    private String mSearchQuery = null;
    private int mPage = 0;

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
	 */
	public static RSSFragment newInstance(int position, String tag) {
		RSSFragment fragment = new RSSFragment();
        fragment.mPosition = position;
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

        /*if (firstLaunch()) {
            // On first launch, load everything and force online
            // because there is assumed to be nothing cached
            loadFeed();      // Just in case there is some cache
            Toast.makeText(getActivity(), "First load may take a while", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise do not load online automatically unless
            // the user specified it in SharedPreferences.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean autoRefresh = prefs.getBoolean(Preferences.Keys.AUTO_REFRESH, Preferences.Default.AUTO_REFRESH);
            boolean existingLoadedPostsAreStale = (System.currentTimeMillis() - lastRefreshMillis) > STALE_POST_THRESHOLD;
            boolean loadOnline = existingLoadedPostsAreStale && (autoRefresh && (savedInstanceState == null));
            loadFeed(false, null, false, loadOnline, true, false);
        }*/
        loadFeed(false);

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
            loadFeed(false);
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
                        loadFeed(false);    // Not loading online, so don't worry about cache
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
            spinnerTagsArray = ParseCategoryHelper.getSubscribedTagNames(getActivity());
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
                    loadFeed(false);
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
            //intent.putExtra(NewsDisplayActivity.EXTRA_URL, r.url);
            //intent.putExtra(NewsDisplayActivity.EXTRA_CONTENT, r.content);
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

    private void loadFeed(final boolean append) {

        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(true);
        }

        ParseCategoryHelper.downloadCategoriesList(getActivity(), new Runnable() {
            @Override
            public void run() {
                final ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts")
                        .whereContainedIn("category", Arrays.asList(ParseCategoryHelper.getFilterCategories(getActivity(), mTag)))
                        .selectKeys(Arrays.asList("published", "title", "category", "bgImage"))
                        .include("category")
                        .orderByDescending("published")
                        .setLimit(Preferences.Default.LOAD_LIMIT);

                if (append) {
                    query.setSkip(Preferences.Default.LOAD_LIMIT * mPage);
                }

                if (Util.isConnected(getActivity())) {
                    query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
                } else {
                    query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ONLY);
                }

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {

                        if (mSwipeLayout != null) {
                            mSwipeLayout.setRefreshing(false);
                        }

                        if (e == null) {
                            // Remove empty cache that might get "orphaned"
                            // before we lose a way to delete it.
                            if (list == null || list.size() == 0) {
                                query.clearCachedResult();
                            }

                            if (!append) {
                                mPage = 1;
                                mAdapter.clear();
                                new ClearOutdatedCachesTask().execute();
                            } else {
                                mPage++;
                            }

                            for (ParseObject item : list) {
                                try {
                                    if (item.getParseObject("category") != null) {
                                        RSSItem post = new RSSItem(
                                                item.getObjectId(),
                                                item.getDate("published").getTime(),
                                                item.getString("title"),
                                                item.getParseObject("category").getString("name"),
                                                item.getParseObject("category").getString("icon_img"),
                                                item.getString("bgImage")
                                        );
                                        mAdapter.add(post);
                                    }
                                } catch (IllegalStateException error) {
                                    Logger.error("Error displaying \"" + item.getString("title") + "\": ", error);
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            Logger.log("Done loading");
                        } else {
                            if (e.getCode() == ParseException.CACHE_MISS) {
                                // We are offline and there is no cache available.
                                // Possible causes are:
                                // 1. User has no internet connection (at all)
                                // 2. User has a data connection, but chose to only load over WiFi

                                if (Util.getConnectionType(getActivity()) == Util.CONNECTION_TYPE_NONE) {
                                    Toast.makeText(getActivity(), "No cache available. Please try again when you have a valid internet connection.", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (!Util.isConnected(getActivity())) {
                                        Toast.makeText(getActivity(), "Loading over data is disabled. Please check your app preferences.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Error loading posts", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                if (!append) {
                                    mPage = 1;
                                    mAdapter.clear();
                                    mAdapter.notifyDataSetChanged();
                                }

                            } else {
                                Toast.makeText(getActivity(), "Error loading post", Toast.LENGTH_SHORT).show();
                            }
                            query.clearCachedResult();
                        }
                    }
                });
            }
        });
    }

    private class ClearOutdatedCachesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ParseObject[] tags = ParseCategoryHelper.getFilterCategories(getActivity(), mTag);

            // Clear appended pages
            ParseQuery<ParseObject> query = ParseQuery.getQuery("BlogPosts")
                    .whereContainedIn("category", Arrays.asList(tags))
                    .include("category")
                    .selectKeys(Arrays.asList("published", "title", "category", "bgImage"))
                    .orderByDescending("published");

            int count = -1;
            try {
                 count = query.count();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int numPages = (int) Math.ceil(((float) count) / Preferences.Default.LOAD_LIMIT);

            query.setLimit(Preferences.Default.LOAD_LIMIT);

            for (int i=1; i<numPages; i++) {
                query.setSkip(Preferences.Default.LOAD_LIMIT * i);
                query.clearCachedResult();
            }

            return null;
        }
    }

	@Override
	public void onRefresh() {
		loadFeed(false);
	}

    public void doSearch(String query) {

        // TODO debug toast, remove before release
        //Toast.makeText(getActivity(), "News: " + query, Toast.LENGTH_SHORT).show();

        // Search request handled by loadFeed()
        //loadFeed(true, query, false, false, false, true);
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
                loadFeed(true);
                mScrolling = false;
            }
        }
    }
}
