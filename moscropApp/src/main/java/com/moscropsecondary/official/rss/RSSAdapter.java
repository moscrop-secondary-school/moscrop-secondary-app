package com.moscropsecondary.official.rss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.rss.CardUtil.CardProcessor;

import java.util.List;

/**
 * Created by ivon on 30/06/14.
 */
public class RSSAdapter extends ArrayAdapter<RSSItem> {

    List<RSSItem> mItems = null;

    public RSSAdapter(Context context, List<RSSItem> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        mItems = items;
    }

    public static class RSSAdapterItem {

        public final RSSItem item;
        public final CardProcessor processor;

        public RSSAdapterItem(RSSItem r, CardProcessor c) {
            item = r;
            processor = c;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.rss_list_card, null);
        }

        RSSItem item = mItems.get(position);

        TextView title = (TextView) view.findViewById(R.id.rlc_title);
        View divider = view.findViewById(R.id.rlc_divider);
        TextView description = (TextView) view.findViewById(R.id.rlc_description);

        // Set title
        if(title != null) {
            title.setText(item.title);
        }

        // Set divider color
        if(divider != null) {
            divider.setBackgroundColor(0xff33b5e5);
        }

        // Set description
        if(description != null) {
            description.setText(item.preview);
        }

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //Logger.log("notifyDataSetChanged");
    }
}
