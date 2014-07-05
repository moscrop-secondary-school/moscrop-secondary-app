package com.ivon.moscropsecondary.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ivon.moscropsecondary.R;

public class CalendarFragment extends Fragment {

    private int mPosition;
    private View mContentView;
    
    public static CalendarFragment newInstance(int position) {
    	CalendarFragment fragment = new CalendarFragment();
        fragment.mPosition = position;
    	return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	setHasOptionsMenu(true);
    	
    	mContentView = inflater.inflate(R.layout.fragment_events, container, false);
    	ListView mListView = (ListView) mContentView.findViewById(R.id.events_list);
    	String[] listItems = new String[20];
    	for(int i=0; i<listItems.length; i++) {
    		listItems[i] = "Event " + (i+1);
    	}
    	mListView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems));
    	
    	return mContentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(mPosition);
    }
}
