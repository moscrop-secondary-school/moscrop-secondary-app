package com.moscropsecondary.official.rss;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.ToolbarActivity;
import com.moscropsecondary.official.util.ThemesUtil;

public class NewsDisplayActivity extends ToolbarActivity
        implements ThemesUtil.ThemeChangedListener {
	
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_CONTENT = "content";
	public static final String EXTRA_TITLE = "abTitle";
    private boolean mThemeRequiresUpdate = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

        int theme = ThemesUtil.getThemeResFromPreference(this);
        setTheme(theme);
        mThemeRequiresUpdate = false;   // We just set the latest theme
        ThemesUtil.registerThemeChangedListener(this);

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
    protected void onResume() {
        super.onResume();
        if(mThemeRequiresUpdate) {
            startActivity(new Intent(this, NewsDisplayActivity.class));
            finish();
        }
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
    @Override
    public void onThemeChanged() {
        mThemeRequiresUpdate = true;
    }
}
