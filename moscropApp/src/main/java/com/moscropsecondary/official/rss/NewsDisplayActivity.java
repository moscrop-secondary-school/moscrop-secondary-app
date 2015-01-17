package com.moscropsecondary.official.rss;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.moscropsecondary.official.R;
import com.moscropsecondary.official.ToolbarActivity;
import com.moscropsecondary.official.util.ThemesUtil;

public class NewsDisplayActivity extends ToolbarActivity
        implements ThemesUtil.ThemeChangedListener {

    public static final String SHARED_ELEMENT_NAME = "sharedElementName";

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

        int theme = ThemesUtil.getDetailThemeResFromPreference(this);
        setTheme(theme);
        mThemeRequiresUpdate = false;   // We just set the latest theme
        ThemesUtil.registerThemeChangedListener(this);

        super.onCreate(savedInstanceState);

		/*String url = getIntent().getStringExtra(EXTRA_URL) + "?m=1";
		String htmlContent = getIntent().getStringExtra(EXTRA_CONTENT);
		String title = getIntent().getStringExtra(EXTRA_TITLE);*/

        // Retrieve shared element
        /*FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        ViewCompat.setTransitionName(contentFrame, SHARED_ELEMENT_NAME);*/

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
    protected int getLayoutResource() {
        return R.layout.activity_newsdisplay;
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

    /*public static void launch(ToolbarActivity activity, View transitionView, String url, String content, String title) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, SHARED_ELEMENT_NAME);
        Intent intent = new Intent(activity, NewsDisplayActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_CONTENT, content);
        intent.putExtra(EXTRA_TITLE, title);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }*/

    @Override
    public void finish() {
        super.finish();

        // override transitions to skip the standard window animations
        overridePendingTransition(0, 0);
    }
}
