package com.moscrop.official.staffinfo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moscrop.official.R;

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

        StaffInfoModel model = mModels.get(position);

        TextView nameText = (TextView) view.findViewById(R.id.staff_name);
        nameText.setText(model.getFullName());

        TextView departmentText = (TextView) view.findViewById(R.id.staff_department);
        departmentText.setText(model.getDepartment());

        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.setColorFilter(generateRandomColor(model), PorterDuff.Mode.SRC_ATOP);

        ImageView iconView = (ImageView) view.findViewById(R.id.staff_icon);
        iconView.setImageResource(getIconDrawable(model.getDepartment()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            iconView.setBackground(circle);
        } else {
            //noinspection deprecation
            iconView.setBackgroundDrawable(circle);
        }

        return view;
    }

    private int[] iconBgColors = new int[] {
            /*0xFFF44336,
            0xFF673AB7,
            0xFF3F51B5,
            0xFF4CAF50,
            0xFFFF5722*/
            0xFF81D4FA
    };

    private int generateRandomColor(StaffInfoModel model) {
        /*int index = Math.abs(model.hashCode()) % iconBgColors.length;
        return iconBgColors[index];*/
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.toolbar_color, typedValue, true);
        return typedValue.data;
    }

    private int getIconDrawable(String department) {
        switch(department.toLowerCase()) {
            case "applied skills":
                return R.drawable.ic_staff_applied_skills;
            case "visual and performing arts":
                return R.drawable.ic_staff_art;
            case "career programs":
                return R.drawable.ic_staff_career_prep;
            case "english":
                return R.drawable.ic_staff_english;     // TODO replace with dedicated english icon
            case "library":
            case "library assistant":
                return R.drawable.ic_staff_library;
            case "mathematics":
                return R.drawable.ic_staff_math;
            case "physical education":
                return R.drawable.ic_staff_physical_education;
            case "science":
            case "science lab coordinator":
                return R.drawable.ic_staff_science;
            case "social studies":
                return R.drawable.ic_staff_socials;
            case "languages":
                return R.drawable.ic_staff_language;
            default:
                return R.drawable.ic_staff_youth_worker;
        }
    }
}
