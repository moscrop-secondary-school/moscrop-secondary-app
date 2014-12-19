package com.moscropsecondary.official.rss;

import android.content.Context;
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
import com.moscropsecondary.official.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by ivon on 30/06/14.
 */
public class RSSAdapter extends ArrayAdapter<RSSItem> {

    private final int RSS_CARD_HEIGHT;
    List<RSSItem> mItems = null;

    public RSSAdapter(Context context, List<RSSItem> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        mItems = items;
        RSS_CARD_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.rss_card_height);
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
            Random random = new Random();
            int i = random.nextInt(5);
            if (i >= 3) {       // 2/5 chance
                view.setTag("hasImage");
            }
        }

        RSSItem item = mItems.get(position);

        ImageView bgImage = (ImageView) view.findViewById(R.id.CardBgImg);
        CircularImageView tagIcon = (CircularImageView) view.findViewById(R.id.CardTagIcon);
        TextView tagListText = (TextView) view.findViewById(R.id.CardTagList);
        TextView timestampText = (TextView) view.findViewById(R.id.CardTimestamp);
        TextView title = (TextView) view.findViewById(R.id.rlc_title);

        // Set background image
        if (bgImage != null) {

            if (view.getTag() != null && ((String) view.getTag()).equals("hasImage")) {
                String uri = "";
                Random random = new Random();
                int i = random.nextInt(4);
                switch (i) {
                    case 0:
                        uri = "https://nsidc.org/sites/nsidc.org/files/images//snowycreek.jpg";
                        break;
                    case 1:
                        uri = "http://media.komonews.com/images/130129_silverdale_snow_file_lg.jpg";
                        break;
                    case 2:
                        uri = "http://www.voicechronicle.com/wp-content/uploads/2014/11/weather_snow_1234_pg_E1.jpg";
                        break;
                    case 3:
                        uri = "http://michaelpohlman.files.wordpress.com/2012/01/snow_blizzard.jpg";
                        break;
                }
                Picasso.with(getContext())
                        .load(uri)
                        .fit().centerCrop()
                        .into(bgImage);
            } else {
                bgImage.setVisibility(View.GONE);
            }
        }

        // Set icon
        if (tagIcon != null) {

            String uri = "";
            Random random = new Random();
            int i = random.nextInt(4);
            switch (i) {
                case 0:
                    uri = "https://nsidc.org/sites/nsidc.org/files/images//snowycreek.jpg";
                    break;
                case 1:
                    uri = "http://media.komonews.com/images/130129_silverdale_snow_file_lg.jpg";
                    break;
                case 2:
                    uri = "http://www.voicechronicle.com/wp-content/uploads/2014/11/weather_snow_1234_pg_E1.jpg";
                    break;
                case 3:
                    uri = "http://michaelpohlman.files.wordpress.com/2012/01/snow_blizzard.jpg";
                    break;
            }
            Picasso.with(getContext())
                    .load(uri)
                    .fit().centerCrop()
                    .into(tagIcon);
        }

        // Set tags list
        if (tagListText != null) {
            String tags = "";
            for (int i=0; i<item.tags.length; i++) {
                tags += item.tags[i];
                if (i < item.tags.length-1) {
                    tags += ", ";
                }
            }
            tagListText.setText(tags);
        }

        // Set post time
        if (timestampText != null) {
            timestampText.setText(getRelativeTime(item.date));
        }

        // Set title
        if (title != null) {
            title.setText(item.title);
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
            long hoursAgo = diffMillis / 60*60*1000;
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
        cal.setTimeInMillis(cal.getTimeInMillis() - 24*60*60*1000);
        int date = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTimeInMillis(cal.getTimeInMillis() + 24*60*60*1000);
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

        long numDays = (millis2 - millis1) / 24*60*60*3600;
        return numDays;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //Logger.log("notifyDataSetChanged");
    }
}
