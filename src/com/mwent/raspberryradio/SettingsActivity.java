package com.mwent.raspberryradio;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SettingsActivity extends Activity implements OnClickListener
{
	
	Button cancel, save;

	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.activity_settings);
		findViewById(R.id.settings_layout).clearFocus();

		cancel = (Button) findViewById(R.id.settings_cancel);
		cancel.setOnClickListener(this);
		
		save = (Button) findViewById(R.id.settings_save);
		save.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.settings_cancel:
			finish();
		case R.id.settings_save:
			return;
		}
	}
	
}
