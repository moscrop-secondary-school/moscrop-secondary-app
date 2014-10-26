package com.ivon.moscropsecondary;

import android.app.Activity;
import android.os.Bundle;
import com.ivon.moscropsecondary.util.ThemeUtil;

public abstract class MoscropBaseActivity extends Activity {
	public boolean leftActivityWithSlideAnim = false;
	public int mTheme = -1;

	@Override
	protected void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		ThemeUtil.setTheme(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ThemeUtil.reloadTheme(this);

		leftActivityWithSlideAnim = false;
	}

	public void setLeftWithSlideAnim(boolean newValue) {
		this.leftActivityWithSlideAnim = newValue;
	}
}
