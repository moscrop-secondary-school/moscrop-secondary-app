package com.ivon.moscropsecondary.list;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.ui.RSSFragment;

import org.mcsoxford.rss.RSSItem;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by ivon on 01/07/14.
 */
public class LoadHandler extends Handler {

    public static final int START_LOAD = 0;
    public static final int ADD_ITEM = 1;
    public static final int CLEAR_ADAPTER = 2;
    public static final int INVALID_FEED = 3;
    public static final int FINISH_LOAD = 4;

    WeakReference<RSSFragment> mWeakReference;

    public LoadHandler(RSSFragment fragment) {
        mWeakReference = new WeakReference<RSSFragment>(fragment);
    }

    public void removeAllMessages() {
        removeMessages(START_LOAD);
        removeMessages(ADD_ITEM);
        removeMessages(CLEAR_ADAPTER);
        removeMessages(INVALID_FEED);
        removeMessages(FINISH_LOAD);
    }

    private int getTypeFromLink(String link) {
        if(link.contains("moscropschool")) {
            return CardUtil.TYPE_NEWS_CARD;
        } else if(link.contains("moscropnewsletters")) {
            return CardUtil.TYPE_EMAIL_CARD;
        } else if(link.contains("moscropstudents")) {
            return CardUtil.TYPE_SUBS_CARD;
        } else {
            return -1;
        }
    }

    @Override
    public void handleMessage(Message msg) {

        final int what = msg.what;

        // Holding on to this reference is ok because handleMessage does not block
        RSSFragment fragment = mWeakReference.get();

        if(fragment == null)
            return;

        switch(what) {

            case START_LOAD: {

                SwipeRefreshLayout srl = fragment.mSwipeLayout;
                ListView lv = fragment.mListView;

                if(srl != null && lv != null) {
                    srl.setRefreshing(true);
                    //srl.removeAllViews();
                    //srl.addView(lv);
                }

                break;
            }

            case ADD_ITEM: {

                RSSItem item = (RSSItem) msg.obj;

                if (item != null) {
                    String link = item.getLink().toString();
                    int type = getTypeFromLink(link);
                    RSSAdapter.RSSAdapterItem rai = new RSSAdapter.RSSAdapterItem(item, CardUtil.getCardProcessor(type));
                    List<RSSAdapter.RSSAdapterItem> list = fragment.mItems;
                    ArrayAdapter<RSSAdapter.RSSAdapterItem> adapter = fragment.mAdapter;
                    if (list != null && adapter != null) {
                        list.add(rai);
                        adapter.notifyDataSetChanged();
                    }
                }

                break;
            }

            case CLEAR_ADAPTER: {

                List<RSSAdapter.RSSAdapterItem> list = fragment.mItems;
                RSSAdapter adapter = fragment.mAdapter;

                if (list != null && adapter != null) {
                    list.clear();
                    adapter.notifyDataSetChanged();
                }

                break;
            }

            case INVALID_FEED: {

                List<RSSAdapter.RSSAdapterItem> list = fragment.mItems;
                if (list == null) {
                    return;
                }

                if (list.size() > 0) {
                    Toast.makeText(fragment.getActivity(), R.string.load_error_text, Toast.LENGTH_SHORT).show();
                } else {
                    // TODO use listview header
                    SwipeRefreshLayout srl = fragment.mSwipeLayout;

                    if (srl != null) {
                        TextView tv = new TextView(fragment.getActivity());
                        tv.setGravity(Gravity.CENTER_HORIZONTAL);
                        tv.setText(R.string.load_error_text);
                        srl.removeAllViews();
                        srl.addView(tv);
                    }
                }

                break;
            }

            case FINISH_LOAD: {

                SwipeRefreshLayout srl = fragment.mSwipeLayout;

                if (srl != null) {
                    srl.setRefreshing(false);
                }

                break;
            }
        }
    }
}