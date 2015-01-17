package com.moscropsecondary.official.rss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.rss.CardUtil.CardProcessor;
import com.moscropsecondary.official.util.DateUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ivon on 30/06/14.
 */
public class RSSAdapter extends ArrayAdapter<RSSItem> {

    private final int RSS_CARD_HEIGHT;
    List<RSSItem> mItems = null;

    private final int textColor1;
    private final int textColor2;
    private final int bgColor1;
    private final int bgColor2;

    public RSSAdapter(Context context, List<RSSItem> items) {

        super(context, android.R.layout.simple_list_item_1, items);
        mItems = items;
        RSS_CARD_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.rss_card_height);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.rss_card_text_1, typedValue, true);
        textColor1 = typedValue.data;
        theme.resolveAttribute(R.attr.rss_card_text_2, typedValue, true);
        textColor2 = typedValue.data;
        theme.resolveAttribute(R.attr.rss_card_bg_1, typedValue, true);
        bgColor1 = typedValue.data;
        theme.resolveAttribute(R.attr.rss_card_bg_2, typedValue, true);
        bgColor2 = typedValue.data;
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
            view.setLayoutParams(new AbsListView.LayoutParams(GridView.AUTO_FIT, RSS_CARD_HEIGHT));
        }

        RSSItem item = mItems.get(position);

        int textColor = Color.TRANSPARENT;
        int bgColor = Color.TRANSPARENT;
        switch(position % 4) {
            case 0:
            case 3:
                textColor = textColor1;
                bgColor = bgColor1;
                break;
            case 1:
            case 2:
                textColor = textColor2;
                bgColor = bgColor2;
                break;
        }

        loadCardWithRssItem(getContext(), view, item, bgColor, textColor);

        return view;
    }

    public static void loadCardWithRssItem(Context context, View view, RSSItem item, int bgColor, int textColor) {
        ImageView bgImage = (ImageView) view.findViewById(R.id.CardBgImg);
        ImageView tagIcon = (ImageView) view.findViewById(R.id.CardTagIcon);
        TextView tagListText = (TextView) view.findViewById(R.id.CardTagList);
        TextView timestampText = (TextView) view.findViewById(R.id.CardTimestamp);
        TextView title = (TextView) view.findViewById(R.id.rlc_title);

        String[] metadata = item.metadata.split(",");

        // Set background color
//        int bgColor = Color.WHITE;
//        switch(position % 4) {
//            case 0:
//            case 3:
//                bgColor = R.color.backgrounddd;
//                break;
//            case 1:
//            case 2:
//                bgColor = 0xff34495e;
//                break;
//        }
//        view.setBackgroundColor(bgColor);

        view.setBackgroundColor(bgColor);

        // Set background image
        if (bgImage != null) {
            if (metadata.length >= 2) {
                bgImage.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(metadata[1])
                        .fit().centerCrop()
                        .into(bgImage);
            } else {
                bgImage.setVisibility(View.GONE);
            }
        }

        // Set icon
        if (tagIcon != null) {
            if (!item.tags[0].equals(RSSTagCriteria.NO_IMAGE)) {
                tagIcon.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(item.tags[0])
                        .fit().centerCrop()
                        .into(tagIcon);
            } else {
                tagIcon.setVisibility(View.GONE);
            }
        }

        // Set tags list
        if (tagListText != null) {
            String tags = "";
            // Start at i=1 because tags[0] is used to store image url
            for (int i=1; i<item.tags.length; i++) {
                tags += item.tags[i];
                if (i < item.tags.length-1) {
                    tags += ", ";
                }
            }
            tagListText.setText(tags);
            tagListText.setTextColor(textColor);
        }

        // Set post time
        if (timestampText != null) {
            timestampText.setText(DateUtil.getRelativeTime(item.date));
            timestampText.setTextColor(textColor);
        }

        // Set title
        if (title != null) {
            title.setText(item.title);
            title.setTextColor(textColor);
        }
    }



    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //Logger.log("notifyDataSetChanged");
    }
}
