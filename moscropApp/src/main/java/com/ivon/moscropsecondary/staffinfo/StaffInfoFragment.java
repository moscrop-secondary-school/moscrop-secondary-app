package com.ivon.moscropsecondary.staffinfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class StaffInfoFragment extends Fragment {

    private int mPosition;
    private View mContentView;

    private List<String> mList = new ArrayList<String>();
    private ArrayAdapter mAdapter;

    public static StaffInfoFragment newInstance(int position) {
    	StaffInfoFragment fragment = new StaffInfoFragment();
        fragment.mPosition = position;
    	return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);
    	
    	mContentView = inflater.inflate(R.layout.fragment_teachers, container, false);
    	ListView mListView = (ListView) mContentView.findViewById(R.id.teachers_list);
        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mList);
    	mListView.setAdapter(mAdapter);

        testDB();

        return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
    }

    private void testDB() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                doTestDB();
            }
        }).start();
    }

    private void doTestDB() {
        StaffInfoDatabase db = new StaffInfoDatabase(getActivity());
        List<String> list = db.listAllByLastName();
        mList.clear();
        for(String s : list) {
            mList.add(s);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}
