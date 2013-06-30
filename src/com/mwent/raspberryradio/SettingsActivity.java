package com.mwent.raspberryradio;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity implements OnClickListener
{

	Button cancel, save;

	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.activity_settings);
		//		findViewById(R.id.settings_layout).clearFocus();

		if(ClientService.settings != null){
			fillSettings();
		}
		
		cancel = (Button)findViewById(R.id.settings_cancel);
		cancel.setOnClickListener(this);

		save = (Button)findViewById(R.id.settings_save);
		save.setOnClickListener(this);
	}

	private void fillSettings() {
		EditText serverName = (EditText) findViewById(R.id.settings_servername);
		EditText username = (EditText) findViewById(R.id.settings_username);
		EditText password = (EditText) findViewById(R.id.settings_password);
		EditText serverIp = (EditText) findViewById(R.id.settings_server_ip);
		EditText serverPort = (EditText) findViewById(R.id.settings_server_port);
		
		serverName.setText(ClientService.settings.getName());
		username.setText(ClientService.settings.getUsername());
		password.setText(ClientService.settings.getPassword());
		serverIp.setText(ClientService.settings.getIp());
		serverPort.setText(ClientService.settings.getPort()+"");
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.settings_cancel:
			finish();
		case R.id.settings_save:
			//			Intent i = new Intent(this, ServerList.class);
			//			startActivity(i);
			return;
		}
	}

}
