package com.ivon.moscropsecondary.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ivon.moscropsecondary.R;

public class TeachersFragment extends Fragment {

    private int mPosition;
    private View mContentView;

    public static TeachersFragment newInstance(int position) {
    	TeachersFragment fragment = new TeachersFragment();
        fragment.mPosition = position;
    	return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);
    	
    	mContentView = inflater.inflate(R.layout.fragment_teachers, container, false);
    	ListView mListView = (ListView) mContentView.findViewById(R.id.teachers_list);
    	String[] listItems = new String[20];
    	for(int i=0; i<listItems.length; i++) {
    		listItems[i] = "Teacher " + (i+1);
    	}
    	mListView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems));
        return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).onSectionAttached(mPosition);
    }
}
