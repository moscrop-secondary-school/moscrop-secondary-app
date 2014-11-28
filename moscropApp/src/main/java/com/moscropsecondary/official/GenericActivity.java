package com.moscropsecondary.official;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.moscropsecondary.official.util.ThemesUtil;

public class GenericActivity extends ToolbarActivity {

    private boolean mThemeRequiresUpdate = false;

    public static final String TYPE_KEY = "type";
    public static final int TYPE_SETTINGS = 0;
    public static final int TYPE_ABOUT = 1;

	@Override
    protected void onCreate(Bundle savedInstanceState) {

        int theme = ThemesUtil.getThemeResFromPreference(this);
        setTheme(theme);

        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        int type = getIntent().getIntExtra(TYPE_KEY, TYPE_SETTINGS);
        switch (type) {
            case TYPE_SETTINGS:
                getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new SettingsFragment())
                    .commit();
                getSupportActionBar().setTitle("Settings");
                break;
            case TYPE_ABOUT:
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new AboutFragment())
                        .commit();
                getSupportActionBar().setTitle("About");
                break;
        }
	}

    @Override
    protected void onResume() {
        super.onResume();
        if(mThemeRequiresUpdate) {
            startActivity(new Intent(this, GenericActivity.class));
            finish();
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_toolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
