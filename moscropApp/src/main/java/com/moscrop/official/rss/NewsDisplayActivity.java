package com.moscrop.official.rss;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.moscrop.official.R;
import com.moscrop.official.util.ThemesUtil;

public class NewsDisplayActivity extends ActionBarActivity
        implements ThemesUtil.ThemeChangedListener {

	public static final String EXTRA_URL = "url";
	public static final String EXTRA_CONTENT = "content";
	public static final String EXTRA_TITLE = "abTitle";

    public static final String EXTRA_ORIENTATION = "orientation";
    public static final String EXTRA_LEFT = "left";
    public static final String EXTRA_TOP = "top";
    public static final String EXTRA_WIDTH = "width";
    public static final String EXTRA_HEIGHT = "height";
	public static final String EXTRA_TOOLBAR_FROM = "toolbarFrom";
	public static final String EXTRA_TOOLBAR_TO = "toolbarTo";
    public static final String EXTRA_TITLE_COLOR = "titleColor";
    public static final String EXTRA_RSS_ITEM = "rssItem";

    private boolean mThemeRequiresUpdate = false;
    private NewsDisplayFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

        // Set the theme and register for theme updates
        int theme = ThemesUtil.getThemeResFromPreference(this, ThemesUtil.THEME_TYPE_DETAIL);
        setTheme(theme);
        mThemeRequiresUpdate = false;   // We just set the latest theme
        ThemesUtil.registerThemeChangedListener(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsdisplay);

        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            mFragment = NewsDisplayFragment.newInstance(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mFragment, "NewsDisplayFragment")
                    .commit();
        } else {
            mFragment = (NewsDisplayFragment) getSupportFragmentManager().findFragmentByTag("NewsDisplayFragment");
        }
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_displaynews, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mFragment.onToolbarBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            mFragment.onBackKeyPressed();
            return true;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    public void onThemeChanged() {
        mThemeRequiresUpdate = true;
    }

    @Override
    public void finish() {
        super.finish();

        // override transitions to skip the standard window animations
        overridePendingTransition(0, 0);
    }
}
