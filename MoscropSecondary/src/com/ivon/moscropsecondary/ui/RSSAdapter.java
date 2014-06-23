package com.ivon.moscropsecondary.ui;

import java.util.ArrayList;

import org.mcsoxford.rss.RSSItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivon.moscropsecondary.R;

public class RSSAdapter extends ArrayAdapter<RSSItem> {

	public interface CardProcessor {
		public abstract String toProcessedTitle(String s);
		public abstract String toProcessedDescription(String s);
		public abstract int getMaxLines();
		public abstract int getCardColor();
	}
	
	private ArrayList<RSSItem> entries;
    private Context context;
    private CardProcessor cardProcessor;

	public RSSAdapter(Context context, ArrayList<RSSItem> entries, CardProcessor cardProcessor) {
		super(context, android.R.layout.simple_list_item_1, entries);
	    this.entries = entries;
	    this.context = context;
	    this.cardProcessor = cardProcessor;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		
        View view = convertView;
        if (view == null) {
        	LayoutInflater inflater = (LayoutInflater) context
        			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.rss_list_item, null);
        }
        
        // Set title
        RSSItem r = entries.get(position);
        TextView title = (TextView) view.findViewById(R.id.rli_title);
        if(title != null) {
        	if(cardProcessor != null) {
        		title.setText(cardProcessor.toProcessedTitle(r.getTitle()));
        	} else {
        		title.setText("Unknown");
        	}
        }
        
        // Set description
        TextView description = (TextView) view.findViewById(R.id.rli_description);
        if(description != null) {
        	if(cardProcessor != null) {
        		description.setText(cardProcessor.toProcessedDescription(r.getDescription()));
        		
        		int maxLines = cardProcessor.getMaxLines();
        		if(maxLines > 0) {
        			description.setMaxLines(maxLines);
        		}
        	} else {
        		description.setText("No description available");
        	}
        }
        
        // Set divider color
        View divider = view.findViewById(R.id.rli_divider);
        if(divider != null) {
        	if(cardProcessor != null) {
        		divider.setBackgroundColor(cardProcessor.getCardColor());
        	} else {
        		divider.setBackgroundColor(0xff33b5e5);
        	}
        }
        
        return view;
    }
}
