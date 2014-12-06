package com.moscropsecondary.official;        //TODO fix error

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.moscropsecondary.official.adapter.CreditsPeopleAdapter;
import com.moscropsecondary.official.model.CreditsPeopleItem;

import static com.moscropsecondary.official.adapter.CreditsPeopleAdapter.TYPE_MAIN;
import static com.moscropsecondary.official.adapter.CreditsPeopleAdapter.TYPE_TESTER;
import static com.moscropsecondary.official.util.Util.resToUri;

public class AboutFragment extends Fragment
{
    // ID Keys
    private final int CREDITS_PEOPLE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView mList = new ListView(getActivity());
        mList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mList.setSelector(new ColorDrawable(getActivity().getResources().getColor(R.color.transparent)));
        mList.setClickable(true);
        mList.setDivider(null);
        mList.setDividerHeight(0);
        mList.setHorizontalScrollBarEnabled(false);
        mList.setVerticalScrollBarEnabled(false);
        mList.setPadding(0, 0, 0, 0);
        mList.setClipToPadding(false);

        mList.setAdapter(setupAdapter());

        return mList;
    }

    public ListAdapter setupAdapter() {
        CreditsPeopleAdapter mAdapter = new CreditsPeopleAdapter(getActivity());
        mAdapter.addItem(new CreditsPeopleItem.Builder()
                .avatar(resToUri(getActivity(), R.drawable.credits_ivon))
                .banner(resToUri(getActivity(), R.drawable.credits_ivon_banner))
                .name("Ivon Liu")
                .tagline("Main Developer")
                .description("Technology enthusiast who does 99% of the work and fixes Allan\'s mistakes")
                .type(TYPE_MAIN)
                .build());
        mAdapter.addItem(new CreditsPeopleItem.Builder()
                .avatar(resToUri(getActivity(), R.drawable.credits_allan))
                .banner(resToUri(getActivity(), R.drawable.credits_allan_banner))
                .name("Allan Wang")
                .tagline("Main Developer")
                .description("Main themer and moderator with a 1% success rate in java")
                .type(TYPE_MAIN)
                .build());
        mAdapter.addItem(new CreditsPeopleItem.Builder()
                .avatar(resToUri(getActivity(), R.drawable.credits_hazhir))
                .banner(resToUri(getActivity(), R.drawable.credits_hazhir_banner))
                .name("Hazhir Good")
                .tagline("Backend and Support")
                .description("10% backing; 20% skill; 15% super duper awesomely chill; 5% design; 50% divine. 100% reason to remember the sign")
                .type(TYPE_MAIN)
                .build());
        //TODO Fix TYPE_TESTER
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(getActivity(), R.drawable.avatar_dark))
//                .banner(resToUri(getActivity(), R.drawable.banner_dark))
//                .name("Fourth Person")
//                .tagline("Random Guy")
//                .description("Poor fourth person who is forgotten and has no friends")
//                .type(TYPE_MAIN)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(getActivity(), R.drawable.ic_launcher))
//                .banner(resToUri(getActivity(), R.drawable.ic_ab_drawer))
//                .name("Fifth Person")
//                .tagline("Random Guy")
//                .description("Poor fifth person who is forgotten and has no friends")
//                .type(TYPE_MAIN)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(getActivity(), R.drawable.ic_launcher))
//                .banner(resToUri(getActivity(), R.drawable.ic_ab_drawer))
//                .name("Sixth Person")
//                .tagline("Random Guy")
//                .description("Poor sixth person who is forgotten and has no friends")
//                .type(TYPE_MAIN)
//                .build());

        return mAdapter;
    }

}