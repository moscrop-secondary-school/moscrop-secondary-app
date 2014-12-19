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
        TextView title = (TextView) view.findViewById(R.id.rlc_title);
        View divider = view.findViewById(R.id.rlc_divider);
        TextView description = (TextView) view.findViewById(R.id.rlc_description);

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

        // Set title
        if (title != null) {
            title.setText(item.title);
        }

        // Set divider color
        if (divider != null) {
            divider.setBackgroundColor(0xff33b5e5);
        }

        // Set description
        if (description != null) {
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
