package com.moscropsecondary.official.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.moscropsecondary.official.adapter.CreditsPeopleAdapter.TYPE_MAIN;
import static com.moscropsecondary.official.adapter.CreditsPeopleAdapter.TYPE_TESTER;
import static com.moscropsecondary.official.util.Util.getApacheLicense;
import static com.moscropsecondary.official.util.Util.getMITLicense;
import static com.moscropsecondary.official.util.Util.resToUri;

import android.graphics.drawable.ColorDrawable;

import android.view.ViewGroup.LayoutParams;


import com.moscropsecondary.official.MainActivity;
import com.moscropsecondary.official.R;
import com.moscropsecondary.official.adapter.CreditsPeopleAdapter;
import com.moscropsecondary.official.model.CreditsPeopleItem;

public class Dialogs
{
    /**
     * Returns a dialog containing a GridView 
     * of launchers and a close button.
     *
     * @param context
     * @return
     */
    
    public static Dialog getCreditsPeopleDialog(Context context)
    {
        // Create & configure ListView
        ListView mList = new ListView(context);
        mList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mList.setSelector(new ColorDrawable(context.getResources().getColor(R.color.transparent)));
        mList.setClickable(true);
        mList.setDivider(null);
        mList.setDividerHeight(0);
        mList.setHorizontalScrollBarEnabled(false);
        mList.setVerticalScrollBarEnabled(false);
        mList.setPadding(0, (int) Util.convertDpToPixel(24, context), 0, (int) Util.convertDpToPixel(24, context));
        mList.setClipToPadding(false);

        // Create dialog base
        final Dialog mDialog = new Dialog(context, R.style.TransparentDialog);
        mDialog.setContentView(mList);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);

        final CreditsPeopleAdapter mAdapter = new CreditsPeopleAdapter(context);
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_ivon))
//                .banner(resToUri(context, R.drawable.credits_ivon_banner))
//                .name("Ivon Liu")
//                .tagline("Main Developer")
//                .description(getApacheLicense("Technology enthusiast who does 99% of the work and fixes Allan\'s mistakes\n\n"))
//                .type(TYPE_MAIN)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_allan))
//                .banner(resToUri(context, R.drawable.credits_allan_banner))
//                .name("Allan Wang")
//                .tagline("Main Developer")
//                .description(getApacheLicense("Main themer and moderator with a 1% success rate in java\n\n"))
//                .type(TYPE_MAIN)
//                .build());
//        //TODO Add info
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_ivonliu))
//                .name("Ivon Liu")
//                .tagline("Side Developer")
//                .description("Technology enthusiast who enjoys programming as a hobby. Writes many Android apps when bored. Check them out on the Play Store at http://goo.gl/0NE9OG")
//                .type(TYPE_MAIN)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_phlash))
//                .banner(resToUri(context, R.drawable.credits_phlash_banner))
//                .name("Tha PHLASH")
//                .tagline("Icon Legend")
//                .description("Icon master that creates awesomeness")
//                .type(TYPE_TESTER)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_bobby))
//                        //.banner(resToUri(context, R.drawable.credits_bobby_banner))
//                .name("Bobby McKenzie")
//                .tagline("Designer & Tester")
//                .description(getApacheLicense())
//                .type(TYPE_TESTER)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_inder))
//                .banner(resToUri(context, R.drawable.credits_inder_banner))
//                .name("Inder Deep")
//                .tagline("Designer & Tester")
//                .description("Inder came up with the design for the alternate home fragment, provided the 2 default wallpapers for the template, and helped throughly test it all. BladeXDesigns create some of the most unique and beautiful icon sets.")
//                .type(TYPE_TESTER)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_alim))
//                .banner(resToUri(context, R.drawable.credits_alim_banner))
//                .name("Alim Haque")
//                .tagline("Ghetto Tester")
//                .description("Dis dawg makes sure dat dis template functions like the bawses Pk and the1d wanted it to, designs more icons dan he would ever need in his lifetime (hey, photoshop is fun), 'n' he raps like a 21st century monk #Haquer #RealTalk")
//                .type(TYPE_TESTER)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_andrew))
//                        //.banner(resToUri(context, R.drawable.credits_andrew_banner))
//                .name("Andrew Ruffolo")
//                .tagline("The Dude")
//                .description("This guy contributed early and hasn't been seen for months. Every now and then he emerges from the shadows to wish someone a happy birthday and then crawls back into his cave where it's been said he makes YouTube videos.")
//                .type(TYPE_TESTER)
//                .build());
//        mAdapter.addItem(new CreditsPeopleItem.Builder()
//                .avatar(resToUri(context, R.drawable.credits_nitish))
//                        //.banner(resToUri(context, R.drawable.credits_nitish_banner))
//                .name("Nitish Saxena")
//                .tagline("Tester")
//                .description(getApacheLicense())
//                .type(TYPE_TESTER)
//                .build());

        mList.setAdapter(mAdapter);

        // Return the dialog object
        return mDialog;
    }

}