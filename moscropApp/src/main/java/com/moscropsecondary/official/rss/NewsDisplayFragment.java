package com.moscropsecondary.official.rss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.util.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class NewsDisplayFragment extends Fragment {
	
	private String url = null;
	private String htmlContent = "";
	private String title = "";
	
	
	public static NewsDisplayFragment newInstance(String url, String htmlContent, String title) {
		NewsDisplayFragment ndf = new NewsDisplayFragment();
		ndf.url = url;
		ndf.htmlContent = htmlContent;
		ndf.title = title;
		return ndf;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		
		View mContentView = inflater.inflate(R.layout.fragment_newsdisplay, container, false);
		
		TextView tv = (TextView) mContentView.findViewById(R.id.fnd_title);
		if(tv != null) {
			tv.setText(title);
		}
		
		WebView wv = (WebView) mContentView.findViewById(R.id.fnd_webview);
		if(wv != null) {
            wv.setBackgroundColor(getBgColor());
			wv.loadDataWithBaseURL(null, getHtmlData(htmlContent), "text/html", "UTF-8", null);
		}
		
	    return mContentView;
	}
	
	private String getHtmlData(String bodyHTML) {
	    String head = "<head><style>img{max-width: 90%; width:auto; height: auto;}</style></head>";
	    return "<html>" + head + "<body style=\"background-color:transparent\" text=\"" + getTextColor() + "\">" + bodyHTML + "</body></html>";
	}

    private int getBgColor() {
        /*TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.color.transparent, typedValue, true);
        int bgcolor = typedValue.data;
        int a = (bgcolor >> 24) & 0xFF;
        int r = (bgcolor >> 16) & 0xFF;
        int g = (bgcolor >> 8) & 0xFF;
        int b = (bgcolor >> 0) & 0xFF;
        return String.format("rgba(%d,%d,%d,%f)", r, g, b, a/255.0);*/
        return Color.TRANSPARENT;
    }

    private String getTextColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.text, typedValue, true);
        int textcolorInt = typedValue.data;
        return String.format("#%06X", (0xFFFFFF & textcolorInt));
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
        if(itemId == R.id.action_openbrowser) {
        	openExternalBrowser();
        	return true;
        } else if(itemId == R.id.action_viewsource) {
        	showSource();
        	return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
	private void openExternalBrowser() {
		if(url != null) {
			Uri webpage = Uri.parse(url);
		    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
		    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
		        startActivity(intent);
		    }
		}
	}
	
	private void showSource() {
		TextView tv = new TextView(getActivity());
		tv.setText(htmlContent);
		tv.setMovementMethod(new ScrollingMovementMethod());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setPositiveButton("export", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				exportString();
			}
			
		});
		builder.setView(tv)
			   .create()
			   .show();
	}
	
	private void exportString() {
		Logger.log("try export");
		// Create a file on external storage
		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath(), "/moscrop");
		dir.mkdirs();
		File outFile = new File(dir, "htmlStringDump.txt");
		
		try {
			FileOutputStream fos = new FileOutputStream(outFile, true);
			
			PrintWriter pw = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(fos)));
			
			pw.println(htmlContent);
			
			pw.flush();
			pw.close();
			fos.close();
			Logger.log("exported");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
