package com.moscrop.official.rss;

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

import com.moscrop.official.R;
import com.moscrop.official.util.DateUtil;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.rss_list_card, null);
            view.setLayoutParams(new AbsListView.LayoutParams(GridView.AUTO_FIT, RSS_CARD_HEIGHT));
        }

        RSSItem item = mItems.get(position);

        int textColor = getCardTextColor(position);
        int bgColor = getCardBackgroundColor(position);

        loadCardWithRssItem(getContext(), view, item, bgColor, textColor);

        return view;
    }

    public int getCardTextColor(int position) {
        switch(getColorType(position)) {
            case 1:
                return textColor1;
            case 2:
                return textColor2;
            default:
                return Color.TRANSPARENT;
        }
    }

    public int getCardBackgroundColor(int position) {
        switch(getColorType(position)) {
            case 1:
                return bgColor1;
            case 2:
                return bgColor2;
            default:
                return Color.TRANSPARENT;
        }
    }

    /**
     * Determine which variant of each color to use
     * @param position
     *          Position of the card
     * @return
     *          1 if bgColor1 or textColor1 is required,
     *          2 if bgColor2 or textColor2 is required.
     */
    private int getColorType(int position) {

        int width = getContext().getResources().getInteger(R.integer.rss_list_width);

        if (width == 2) {

            switch (position % 4) {
                case 0:
                case 3:
                    return 1;
                case 1:
                case 2:
                    return 2;
            }

        } else if (width == 3) {

            switch (position % 6) {
                case 0:
                case 2:
                case 4:
                    return 1;
                case 1:
                case 3:
                case 5:
                    return 2;
            }

        } else if (width == 4) {

            switch (position % 8) {
                case 0:
                case 2:
                case 5:
                case 7:
                    return 1;
                case 1:
                case 3:
                case 4:
                case 6:
                    return 2;
            }

        } else if (width == 5) {

            switch (position % 10) {
                case 0:
                case 2:
                case 4:
                case 6:
                case 8:
                    return 1;
                case 1:
                case 3:
                case 5:
                case 7:
                case 9:
                    return 2;
            }
        }

        return 0;
    }

    /**
     * Helper method to populate a RSS card layout with an RSSItem
     *
     * @param view
     *          Layout to populate with RSSItem
     * @param item
     *          RSSItem to populate layout with
     * @param bgColor
     *          Color to set the card background to
     * @param textColor
     *          Color to set the card text to
     */
    public static void loadCardWithRssItem(Context context, View view, RSSItem item, int bgColor, int textColor) {
        ImageView bgImage = (ImageView) view.findViewById(R.id.CardBgImg);
        ImageView tagIcon = (ImageView) view.findViewById(R.id.CardTagIcon);
        TextView tagListText = (TextView) view.findViewById(R.id.CardTagList);
        TextView timestampText = (TextView) view.findViewById(R.id.CardTimestamp);
        TextView title = (TextView) view.findViewById(R.id.rlc_title);

        // Process the metadata into an usable String array format
        view.setBackgroundColor(bgColor);

        // Set background image
        if (bgImage != null) {
            if (!item.bgImage.equals("@null")) {
                bgImage.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(item.bgImage)
                        .fit().centerCrop()
                        .into(bgImage);
            } else {
                bgImage.setVisibility(View.GONE);
            }
        }

        // Set icon
        if (tagIcon != null) {
            if (!item.icon.equals("no image")) {    // TODO not sure if this is needed anymore
                tagIcon.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(item.icon)
                        .fit().centerCrop()
                        .into(tagIcon);
            } else {
                tagIcon.setVisibility(View.GONE);
            }
        }

        // Set tags list
        if (tagListText != null) {
            /*String tags = "";
            // Start at i=1 because tags[0] is used to store image url
            for (int i=0; i<item.categories.length; i++) {
                tags += item.categories[i];
                if (i < item.categories.length-1) {
                    tags += ", ";
                }
            }*/
            tagListText.setText(item.category);
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
}
