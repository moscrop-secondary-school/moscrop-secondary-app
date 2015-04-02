package com.moscrop.official;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.moscrop.official.egg.LLandActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ivon on 19/10/14.
 */
public class NavigationDrawerFragment extends NavigationDrawerBase {

    // Section Constants
    public static final int NEWS = 0;
    public static final int EMAIL = 1;
    //public static final int STUDENT = 2;
    public static final int EVENTS = 2;
    public static final int TEACHERS = 3;
    public static final int SETTINGS = 4;
    public static final int ABOUT = 5;
    public static final int CONTACT = 6;

    private NavDrawerAdapter mDrawerAdapter;
    private ListView mDrawerList;

    @Override
    public View onCreateDrawer(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDrawerList = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        initList(inflater);
        return mDrawerList;
    }

    @Override
    protected ListView getNavigationItemsList() {
        return mDrawerList;
    }

    private void initList(LayoutInflater inflater) {

        // Initialize the adapter
        String[] drawerItems = getActivity().getResources().getStringArray(R.array.navigation_items);

        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[] {R.attr.drawer_icon_array});
        int arrayResourceId = a.getResourceId(0, 0);
        a.recycle();

        TypedArray images = getResources().obtainTypedArray(arrayResourceId);
        List<Integer> drawerIcons = new ArrayList<Integer>();

        for (int i=0; i<images.length(); i++) {
            drawerIcons.add(images.getResourceId(i, 0));
        }
        images.recycle();

        Integer[] dividerPositions = new Integer[] {4};

        mDrawerAdapter = new NavDrawerAdapter(
                getActivity(),
                new ArrayList<String>(Arrays.asList(drawerItems)),
                drawerIcons,
                new ArrayList<Integer>(Arrays.asList(dividerPositions))
        );   // TODO: use getSupportActionBar().getThemedContext()

        // Add header
        View headerView = inflater.inflate(R.layout.drawer_header, mDrawerList, false);
        View eggTrigger = headerView.findViewById(R.id.circleAvatar);
        eggTrigger.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getActivity(), LLandActivity.class);
                startActivity(intent);
                return true;
            }
        });
        mDrawerList.addHeaderView(headerView, null, false);

        /*// Add footer
        View footerView = inflater.inflate(R.layout.drawer_footer, mDrawerList, false);
        mDrawerList.addFooterView(footerView, null, false);*/

        // Set list adapter
        mDrawerList.setAdapter(mDrawerAdapter);

        // Apply onClick listeners to ListView items, footer, and header
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long index) {
                selectItem(position - mDrawerList.getHeaderViewsCount());
            }
        });
        /*footerView.findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(SETTINGS);
            }
        });
        footerView.findViewById(R.id.btnAbout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(ABOUT);
            }
        });
        footerView.findViewById(R.id.btnContact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(CONTACT);
            }
        });*/

        // Load banner using Picasso to initialize its singleton ahead of time
        //Picasso.with(this).load(R.drawable.banner).into((ImageView) headerView.findViewById(R.id.imgBanner));

        mDrawerList.setItemChecked(getCurrentSelectedPosition(), true);
    }
}
