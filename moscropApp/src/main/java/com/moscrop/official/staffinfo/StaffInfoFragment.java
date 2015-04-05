package com.moscrop.official.staffinfo;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moscrop.official.MainActivity;
import com.moscrop.official.R;

import java.util.ArrayList;
import java.util.List;

public class StaffInfoFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String KEY_POSITION = "position";
    private int mPosition;
    private ListView mListView;

    private StaffListAdapter mAdapter;

    private boolean mSearchViewExpanded = false;

    public static StaffInfoFragment newInstance(int position) {
    	StaffInfoFragment fragment = new StaffInfoFragment();
        fragment.mPosition = position;
    	return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);
    	
    	View rootView = inflater.inflate(R.layout.fragment_teachers, container, false);
        mListView = (ListView) rootView.findViewById(R.id.teachers_list);
        mAdapter = new StaffListAdapter(getActivity(), new ArrayList<StaffInfoModel>());
    	mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        if(savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(KEY_POSITION, mPosition);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                refreshList();
            }
        }).start();

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        mSearchViewExpanded = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_staff, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint("Search staff");

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchViewExpanded = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchViewExpanded = false;
                refreshList();
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.staff_info_dialog, null);

        StaffInfoModel model = mAdapter.getItem(position);
        String name = model.getFullName();
        String[] rooms = model.getRooms();
        String department = model.getDepartment();
        String email = model.getEmail();
        String[] websites = model.getSites();

        if (department != null && !department.equals("")) {
            View departmentGroup = dialogView.findViewById(R.id.department_group);
            departmentGroup.setVisibility(View.VISIBLE);
            TextView departmentText = (TextView) dialogView.findViewById(R.id.department);
            departmentText.setText(department);
        } else {
            View departmentGroup = dialogView.findViewById(R.id.department_group);
            departmentGroup.setVisibility(View.GONE);
        }

        if (rooms != null && rooms.length > 0) {
            View roomGroup = dialogView.findViewById(R.id.room_group);
            roomGroup.setVisibility(View.VISIBLE);
            TextView roomTitle = (TextView) dialogView.findViewById(R.id.room_title);
            if (rooms.length > 1) {
                roomTitle.append("s");
            }
            TextView roomText = (TextView) dialogView.findViewById(R.id.room);
            String roomStr = StaffInfoModel.roomsArrayToString(rooms).replace(";", ",");
            roomText.setText(roomStr);
        } else {
            View roomGroup = dialogView.findViewById(R.id.room_group);
            roomGroup.setVisibility(View.GONE);
        }

        if (email != null && !email.equals("")) {
            View emailGroup = dialogView.findViewById(R.id.email_group);
            emailGroup.setVisibility(View.VISIBLE);
            TextView emailText = (TextView) dialogView.findViewById(R.id.email);
            emailText.setText(email);
        } else {
            View emailGroup = dialogView.findViewById(R.id.email_group);
            emailGroup.setVisibility(View.GONE);
        }

        if (websites != null && websites.length > 0) {
            View websiteGroup = dialogView.findViewById(R.id.website_group);
            websiteGroup.setVisibility(View.VISIBLE);
            TextView websiteTitle = (TextView) dialogView.findViewById(R.id.website_title);
            if (websites.length > 1) {
                websiteTitle.append("s");
            }
            TextView websiteText = (TextView) dialogView.findViewById(R.id.website);
            String websiteStr = StaffInfoModel.sitesArrayToString(websites).replace(" ", "\n");
            websiteText.setText(websiteStr);
        } else {
            View websiteGroup = dialogView.findViewById(R.id.website_group);
            websiteGroup.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(name);
        builder.setView(dialogView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    private void refreshList() {

        StaffInfoDatabase db = StaffInfoDatabase.getInstance(getActivity());
        final List<StaffInfoModel> models = db.getList();
        db.close();

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.clear();
                    mAdapter.addAll(models);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void doSearch(final String query) {
        Toast.makeText(getActivity(), "Staff: " + query, Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Perform FTS query
                StaffInfoDatabase db = StaffInfoDatabase.getInstance(getActivity());
                final List<StaffInfoModel> models = db.search(query);
                db.close();

                // Load resulting list into ListView
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        mAdapter.addAll(models);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
}
