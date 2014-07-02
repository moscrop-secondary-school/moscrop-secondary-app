package com.ivon.moscropsecondary.list;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.ivon.moscropsecondary.R;
import com.ivon.moscropsecondary.ui.RSSFragment;

import org.mcsoxford.rss.RSSItem;

import java.lang.ref.WeakReference;

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
        RSSFragment fragment = mWeakReference.get();

        if(fragment == null)
            return;

        switch(what) {

            case START_LOAD: {

                SwipeRefreshLayout srl = fragment.mSwipeLayout;
                RecyclerView rv = fragment.mRecyclerView;

                if(srl != null && rv != null) {
                    srl.setRefreshing(true);
                    srl.removeAllViews();
                    srl.addView(rv);
                }

                break;
            }

            case ADD_ITEM: {

                RSSItem item = (RSSItem) msg.obj;

                if (item != null) {
                    String link = item.getLink().toString();
                    int type = getTypeFromLink(link);
                    RSSAdapter.ViewModel vm = new RSSAdapter.ViewModel(item, CardUtil.getCardProcessor(type));
                    RSSAdapter adapter = fragment.mAdapter;
                    if (adapter != null) {
                        adapter.add(vm);
                    }
                }

                break;
            }

            case CLEAR_ADAPTER: {

                RSSAdapter adapter = fragment.mAdapter;

                if (adapter != null) {
                    adapter.clear();
                }

                break;
            }

            case INVALID_FEED: {

                RSSAdapter adapter = fragment.mAdapter;
                if (adapter == null) {
                    return;
                }

                if (adapter.getItemCount() > 0) {
                    Toast.makeText(fragment.getActivity(), R.string.load_error_text, Toast.LENGTH_SHORT).show();
                } else {
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