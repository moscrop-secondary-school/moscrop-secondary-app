package com.ivon.moscropsecondary.rss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.rss.CardUtil.CardProcessor;

import org.mcsoxford.rss.RSSItem;

import java.util.List;

/**
 * Created by ivon on 30/06/14.
 */
public class RSSAdapter extends ArrayAdapter<RSSAdapter.RSSAdapterItem> {

    List<RSSAdapterItem> mItems = null;

    public RSSAdapter(Context context, List<RSSAdapterItem> items) {
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

        RSSAdapterItem rai = mItems.get(position);
        RSSItem r = rai.item;
        CardProcessor c = rai.processor;

        TextView title = (TextView) view.findViewById(R.id.rlc_title);
        View divider = view.findViewById(R.id.rlc_divider);
        TextView description = (TextView) view.findViewById(R.id.rlc_description);

        // Set title
        if(title != null) {
            if(c != null) {
                title.setText(c.toProcessedTitle(r.getTitle()));
            } else {
                title.setText("Unknown");
            }
        }

        // Set divider color
        if(divider != null) {
            if(c != null) {
                divider.setBackgroundColor(c.getCardColor());
            } else {
                divider.setBackgroundColor(0xff33b5e5);
            }
        }

        // Set description
        if(description != null) {
            if(c != null) {
                description.setText(c.toProcessedDescription(r.getDescription()));

                int maxLines = c.getMaxLines();
                if(maxLines > 0) {
                    description.setMaxLines(maxLines);
                }
            } else {
                description.setText("No description available");
            }
        }

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //Logger.log("notifyDataSetChanged");
    }
}
