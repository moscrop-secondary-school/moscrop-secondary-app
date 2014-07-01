package com.ivon.moscropsecondary.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.list.CardUtil.CardProcessor;
import com.ivon.moscropsecondary.util.Logger;

import org.mcsoxford.rss.RSSItem;

import java.util.List;

/**
 * Created by ivon on 30/06/14.
 */
public class RSSAdapter extends RecyclerView.Adapter<RSSAdapter.ViewHolder> implements View.OnClickListener {

    OnItemClickListener itemClickListener = null;
    List<ViewModel> mItems = null;

    public RSSAdapter(List<ViewModel> items) {
        mItems = items;
    }



    public interface OnItemClickListener {
        public abstract void onItemClick(View view, ViewModel item);
    }

    public static class ViewModel {

        public final RSSItem mRSSItem;
        public final CardProcessor mCardProcessor;

        public ViewModel(RSSItem r, CardProcessor c) {
            mRSSItem = r;
            mCardProcessor = c;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTitleText;
        View mDividerView;
        TextView mDescText;

        public ViewHolder(View itemView) {
            super(itemView);

            if(itemView == null) {
                Logger.log("itemView is null!");
                return;
            }

            mTitleText = (TextView) itemView.findViewById(R.id.rlc_title);
            mDividerView = itemView.findViewById(R.id.rlc_divider);
            mDescText = (TextView) itemView.findViewById(R.id.rlc_description);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rss_list_card, null);
        v.setOnClickListener(this);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        ViewModel item = mItems.get(i);
        RSSItem r = item.mRSSItem;
        CardProcessor c = item.mCardProcessor;

        TextView title = viewHolder.mTitleText;
        View divider = viewHolder.mDividerView;
        TextView description = viewHolder.mDescText;

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

        // Associate the view with the item it is displaying
        viewHolder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void add(ViewModel item) {
        int position = mItems.size();
        add(item, position);
    }

    @Override
    public void onClick(View view) {
        if (itemClickListener != null) {
            ViewModel item = (ViewModel) view.getTag();
            itemClickListener.onItemClick(view, item);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void add(ViewModel item, int position) {
        mItems.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(ViewModel item) {
        int position = mItems.indexOf(item);
        remove(position);
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }
}
