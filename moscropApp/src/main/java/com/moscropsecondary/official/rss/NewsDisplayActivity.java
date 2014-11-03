package com.moscropsecondary.official.rss;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.ToolbarActivity;

public class NewsDisplayActivity extends ToolbarActivity {
	
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_CONTENT = "content";
	public static final String EXTRA_TITLE = "abTitle";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
		
		String url = getIntent().getStringExtra(EXTRA_URL) + "?m=1";
		String htmlContent = getIntent().getStringExtra(EXTRA_CONTENT);
		String title = getIntent().getStringExtra(EXTRA_TITLE);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
        		.replace(R.id.content_frame, NewsDisplayFragment.newInstance(url, htmlContent, title))
        		.commit();
	}

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_toolbar;
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_displaynews, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }
}
