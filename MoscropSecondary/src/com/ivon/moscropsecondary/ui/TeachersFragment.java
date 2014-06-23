package com.ivon.moscropsecondary.ui;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.R.id;
import com.ivon.moscropsecondary.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TeachersFragment extends Fragment {
	
    private View mContentView;

    public static TeachersFragment newInstance() {
    	TeachersFragment t = new TeachersFragment();
    	return t;
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
}
