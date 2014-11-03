package com.moscropsecondary.official;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

public class SettingsActivity extends ToolbarActivity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
        		.replace(R.id.content_frame, new SettingsFragment())
        		.commit();
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
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }
}
