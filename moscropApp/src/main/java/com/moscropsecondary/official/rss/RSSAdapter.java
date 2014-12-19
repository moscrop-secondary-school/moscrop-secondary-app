package com.moscropsecondary.official.rss;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moscropsecondary.official.R;

import java.util.List;

/**
 * Created by ivon on 30/06/14.
 */
/*public class RSSAdapter extends ArrayAdapter<RSSItem> {

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
}*/

public class RSSAdapter extends RecyclerView.Adapter<RSSAdapter.ViewHolder> {

    private List<RSSItem> mItems = null;

    public void add(RSSItem item) {
        mItems.add(item);
    }

    public void clear() {
        mItems.clear();
    }

    public RSSItem getItem(int position) {
        return mItems.get(position);
    }

    public RSSAdapter(List<RSSItem> items) {
        mItems = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {


        public TextView titleText;
        public TextView descText;

        public ViewHolder(View v) {
            super(v);
            titleText = (TextView) v.findViewById(R.id.rlc_title);
            descText = (TextView) v.findViewById(R.id.rlc_description);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rss_list_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RSSItem item = mItems.get(position);
        holder.titleText.setText(item.title);
        holder.descText.setText(item.preview);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

}
