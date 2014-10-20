package com.ivon.moscropsecondary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by ivon on 19/10/14.
 */
public class NavigationDrawerFragment extends NavigationDrawerBase {

    private ListView mDrawerListView;

    @Override
    public View onCreateDrawer(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        mDrawerListView.setAdapter(new ArrayAdapter<String>(
                getActivity(),      // TODO Work out if getActionBar().getThemedContext() is necessary
                R.layout.drawer_list_item,
                android.R.id.text1,
                getActivity().getResources().getStringArray(R.array.navigation_items)
        ));
        mDrawerListView.setItemChecked(getCurrentSelectedPosition(), true);
        return mDrawerListView;
    }

    @Override
    protected ListView getNavigationItemsList() {
        return mDrawerListView;
    }
}
