package com.moscropsecondary.official;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NavDrawerAdapter extends BaseAdapter
{
	// Essential Resources
	private List<String> mDrawerItems;
    private List<Integer> mDrawerIcons;
    private List<Integer> mDividerPositions;
	private Context mContext;
	
	// Current index and custom fonts
	private int currentPage;
	private Typeface fontNormal;
	private Typeface fontSelected;
	
	public NavDrawerAdapter(Context context)
	{
		this.mDrawerItems = new ArrayList<String>();
		this.mContext = context;
		
		//this.fontNormal = Typeface.create("sans-serif-light", Typeface.NORMAL);
		//this.fontSelected = Typeface.create("sans-serif", Typeface.BOLD);
	}
	
	public NavDrawerAdapter(Context context, List<String> drawerItems, List<Integer> drawerIcons, List<Integer> dividerPositions)
	{
		this.mDrawerItems = drawerItems;
        this.mDrawerIcons = drawerIcons;
        this.mDividerPositions = dividerPositions;
		this.mContext = context;
		
		//this.fontNormal = Typeface.create("sans-serif-light", Typeface.NORMAL);
		//this.fontSelected = Typeface.create("sans-serif", Typeface.BOLD);
	}
	
	public void addItem(String drawerItem)
	{
		mDrawerItems.add(drawerItem);
	}

    public void addItems(String[] drawerItems)
    {
        for (String drawerItem : drawerItems) {
            addItem(drawerItem);
        }
    }
	
	public void setCurrentPage(int currentPage)
	{
		this.currentPage = currentPage;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount()
	{
		return mDrawerItems.size();
	}
	
	@Override
	public String getItem(int position)
	{
		return mDrawerItems.get(position);
	}
	
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		
		if (convertView == null)
		{
			LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.drawer_item, parent, false);
			
			holder = new ViewHolder();
			//holder.txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
            holder.icon = (ImageView) convertView.findViewById(R.id.drawer_item_icon);
            holder.text = (TextView) convertView.findViewById(R.id.drawer_item_text);
            holder.divider = convertView.findViewById(R.id.divider);

			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Set text
		//holder.txtTitle.setText(mDrawerItems.get(position));
        holder.text.setText(mDrawerItems.get(position));
		
		// Set bold if selected
		//holder.txtTitle.setTypeface((currentPage == position) ? fontSelected : fontNormal);
        //holder.listButton.setTypeface((currentPage == position) ? fontSelected : fontNormal);

        // Set icon
        holder.icon.setImageResource(mDrawerIcons.get(position));

        // Set divider
        if (mDividerPositions.contains(position)) {
            holder.divider.setVisibility(View.VISIBLE);
        } else {
            holder.divider.setVisibility(View.GONE);
        }

        return convertView;
	}
	
	private class ViewHolder
	{
		//public TextView txtTitle;
        public ImageView icon;
        public TextView text;
        public View divider;
	}
}
