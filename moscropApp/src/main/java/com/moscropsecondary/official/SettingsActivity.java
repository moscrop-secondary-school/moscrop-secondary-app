package com.moscropsecondary.official;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.moscropsecondary.official.util.ThemesUtil;

public class SettingsActivity extends ToolbarActivity {

    private boolean mThemeRequiresUpdate = false;

	@Override
    protected void onCreate(Bundle savedInstanceState) {

        int theme = ThemesUtil.getThemeResFromPreference(this);
        setTheme(theme);

        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
        		.replace(R.id.content_frame, new SettingsFragment())
        		.commit();
	}

    @Override
    protected void onResume() {
        super.onResume();
        if(mThemeRequiresUpdate) {
            startActivity(new Intent(this, SettingsActivity.class));
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
