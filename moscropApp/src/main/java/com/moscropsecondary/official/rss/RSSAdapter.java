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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ivon on 30/06/14.
 */
public class RSSAdapter extends ArrayAdapter<RSSItem> {

    private final int RSS_CARD_HEIGHT;
    List<RSSItem> mItems = null;

    private final int textColor1;
    private final int textColor2;

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

        ImageView bgImage = (ImageView) view.findViewById(R.id.CardBgImg);
        ImageView tagIcon = (ImageView) view.findViewById(R.id.CardTagIcon);
        TextView tagListText = (TextView) view.findViewById(R.id.CardTagList);
        TextView timestampText = (TextView) view.findViewById(R.id.CardTimestamp);
        TextView title = (TextView) view.findViewById(R.id.rlc_title);

        String[] metadata = item.metadata.split(",");

        // Set background color
        int bgColor = Color.WHITE;
        switch(position % 4) {
            case 0:
            case 3:
                bgColor = R.color.backgrounddd;
                break;
            case 1:
            case 2:
                bgColor = 0xff34495e;
                break;
        }
        view.setBackgroundColor(bgColor);

        int textColor = Color.WHITE;
        switch(position % 4) {
            case 0:
            case 3:
                textColor = textColor1;
                break;
            case 1:
            case 2:
                textColor = textColor2;
                break;
        }

        // Set background image
        if (bgImage != null) {
            if (metadata.length >= 2) {
                bgImage.setVisibility(View.VISIBLE);
                Picasso.with(getContext())
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
                Picasso.with(getContext())
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
            timestampText.setText(getRelativeTime(item.date));
            timestampText.setTextColor(textColor);
        }

        // Set title
        if (title != null) {
            title.setText(item.title);
            title.setTextColor(textColor);
        }

        return view;
    }

    private String getRelativeTime(long time) {

        String timestamp = "";

        long nowMillis = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(nowMillis);

        long postMillis = time;
        Calendar post = Calendar.getInstance();
        post.setTimeInMillis(postMillis);

        long diffMillis = nowMillis - postMillis;

        if (diffMillis < 60*60*1000) {
            long minAgo = diffMillis / (60*1000);
            timestamp = minAgo + " minutes ago";
        } else if (diffMillis < 24*60*60*1000) {
            long hoursAgo = diffMillis / (60*60*1000);
            timestamp = hoursAgo + " hours ago";
        } else if (post.get(Calendar.DAY_OF_MONTH) == calOneDayAgo(now)) {
            timestamp = "Yesterday";
        } else {
            long daysBetween = daysBetween(post, now);
            if (daysBetween <= 7) {
                timestamp = daysBetween + " days ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
                timestamp = sdf.format(new Date(postMillis));
            }
        }

        return timestamp;
    }

    private int calOneDayAgo(Calendar cal) {
        cal.setTimeInMillis(cal.getTimeInMillis() - (24*60*60*1000));
        int date = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTimeInMillis(cal.getTimeInMillis() + (24*60*60*1000));
        return date;
    }

    /**
     * Calculates the number of days between two Calendar dates.
     * @param cal1
     *          Calendar date that occurs first
     * @param cal2
     *          Calendar object that occurs second
     * @return  Number of days between cal1 and cal2
     */
    private long daysBetween(Calendar cal1, Calendar cal2) {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(cal1.getTimeInMillis());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long millis1 = cal.getTimeInMillis();

        cal.setTimeInMillis(cal2.getTimeInMillis());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long millis2 = cal.getTimeInMillis();

        long numDays = (millis2 - millis1) / (24*60*60*1000);
        return numDays;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //Logger.log("notifyDataSetChanged");
    }
}
