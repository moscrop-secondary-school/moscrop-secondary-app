package com.moscrop.official;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ivon on 12/11/14.
 */
public class ToolbarSpinnerAdapter extends ArrayAdapter<String> {

    List<String> mItems = null;

    public ToolbarSpinnerAdapter(Context context, List<String> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        mItems = items;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.spinner_actionbar_title, null);
            view.setTag("NON_DROPDOWN");
        }

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(mItems.get(position));
        return view;
    }
}
