package com.moscrop.official;        //TODO fix error

import android.app.Fragment;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.moscrop.official.adapter.CreditsPeopleAdapter;
import com.moscrop.official.model.CreditsPeopleItem;

import static com.moscrop.official.adapter.CreditsPeopleAdapter.TYPE_MAIN;
import static com.moscrop.official.adapter.CreditsPeopleAdapter.TYPE_TESTER;
import static com.moscrop.official.util.Util.resToUri;

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

        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[] {
                R.attr.credits_ivon_banner,
                R.attr.credits_allan_banner,
                R.attr.credits_hazhir_banner
        });

        mAdapter.addItem(new CreditsPeopleItem.Builder()
                .avatar(resToUri(getActivity(), R.drawable.credits_ivon))
                .banner(resToUri(getActivity(), a.getResourceId(0, 0)))
                .name("Ivon Liu")
                .tagline("Main Developer")
                .description(getString(R.string.ivon_description))
                .type(TYPE_MAIN)
                .build());
        mAdapter.addItem(new CreditsPeopleItem.Builder()
                .avatar(resToUri(getActivity(), R.drawable.credits_allan))
                .banner(resToUri(getActivity(), a.getResourceId(1, 0)))
                .name("Allan Wang")
                .tagline("Side Developer")
                .description(getString(R.string.allan_description))
                .type(TYPE_MAIN)
                .build());
        mAdapter.addItem(new CreditsPeopleItem.Builder()
                .avatar(resToUri(getActivity(), R.drawable.credits_hazhir))
                .banner(resToUri(getActivity(), a.getResourceId(2, 0)))
                .name("Hazhir Good")
                .tagline("Backend and Support")
                .description(getString(R.string.hazhir_description))
                .type(TYPE_TESTER)
                .build());
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