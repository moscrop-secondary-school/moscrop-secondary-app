package com.moscropsecondary.official.staffinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.moscropsecondary.official.R;

import java.util.List;

/**
 * Created by ivon on 9/7/14.
 */
public class StaffListAdapter extends ArrayAdapter<StaffInfoModel> {

    List<StaffInfoModel> mModels = null;

    public StaffListAdapter(Context context, List<StaffInfoModel> models) {
        super(context, android.R.layout.simple_list_item_1, models);
        mModels = models;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.staff_list_item, null);
        }

        TextView nameText = (TextView) view.findViewById(R.id.teacher_name);
        StaffInfoModel model = mModels.get(position);
        nameText.setText(model.getName());

        return view;
    }
}