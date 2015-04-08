package com.moscrop.official;


import android.app.Activity;
import android.app.Fragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.moscrop.official.util.ThemesUtil;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public abstract class NavigationDrawerBase extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */

    private DrawerLayout mDrawerLayout;
    private View mDrawerContentContainer;

    private int mCurrentSelectedPosition = 0;
    public int getCurrentSelectedPosition() {
        return mCurrentSelectedPosition;
    }
    private boolean mFromSavedInstanceState;

    public NavigationDrawerBase() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition, mFromSavedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return onCreateDrawer(inflater, container, savedInstanceState);
    }

    protected abstract View onCreateDrawer(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
    protected abstract ListView getNavigationItemsList();
    protected abstract NavDrawerAdapter getNavigationDrawerAdapter();

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mDrawerContentContainer);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     * @param drawerContentContainer   The android:id of this second child in the DrawerLayout.
     */
    public void setUp(DrawerLayout drawerLayout, int drawerContentContainer) {
        mDrawerLayout = drawerLayout;
        mDrawerContentContainer = getActivity().findViewById(drawerContentContainer);

        // set a custom shadow that overlays the main content when the drawer opens
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[] {R.attr.drawer_shadow});
        int attributeResourceId = a.getResourceId(0, 0);
        a.recycle();
        mDrawerLayout.setDrawerShadow(attributeResourceId, GravityCompat.START);

        // set status bar color to primaryDark
        mDrawerLayout.setStatusBarBackgroundColor(ThemesUtil.getThemePrimaryColor(getActivity()));
    }

    protected void selectItem(int position) {
        selectItem(position, false);
    }

    private void selectItem(final int position, final boolean fromSavedInstanceState) {
        mCurrentSelectedPosition = position;
        if (getNavigationItemsList() != null && position < getNavigationItemsList().getCount()
                && getNavigationDrawerAdapter() != null
                && mCallbacks != null && mCallbacks.shouldSetDrawerItemSelected(position)) {
            getNavigationDrawerAdapter().setSelectedItem(position);
        }

        /*
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerContentContainer);
        }
        if (mCallbacks != null) {

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mCallbacks.onNavigationDrawerItemSelected(position, fromSavedInstanceState);
                }

            }, 300);
        }*/

        if (mCallbacks != null) {

            if (mDrawerLayout != null) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawer(mDrawerContentContainer);
                    }
                }, mCallbacks.getNavigationDrawerCloseDelay(position));
            }

            mCallbacks.onNavigationDrawerItemSelected(position, fromSavedInstanceState);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        public void onNavigationDrawerItemSelected(int position, boolean fromSavedInstanceState);
        public int getNavigationDrawerCloseDelay(int position);
        public boolean shouldSetDrawerItemSelected(int position);
    }
}
