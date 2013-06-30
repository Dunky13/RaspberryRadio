package com.mwent.raspberryradio;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends Activity implements OnClickListener
{

	Button cancel, save;

	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.activity_settings);
		//		findViewById(R.id.settings_layout).clearFocus();

		if (ClientService.settings != null)
		{
			fillSettings();
		}

		cancel = (Button)findViewById(R.id.settings_cancel);
		cancel.setOnClickListener(this);

		save = (Button)findViewById(R.id.settings_save);
		save.setOnClickListener(this);
	}

	private void fillSettings()
	{
		TextView id = (TextView)findViewById(R.id.settings_server_id);
		EditText serverName = (EditText)findViewById(R.id.settings_servername);
		EditText username = (EditText)findViewById(R.id.settings_username);
		EditText password = (EditText)findViewById(R.id.settings_password);
		EditText serverIp = (EditText)findViewById(R.id.settings_server_ip);
		EditText serverPort = (EditText)findViewById(R.id.settings_server_port);

		id.setText(ClientService.settings.getId() + "");
		serverName.setText(ClientService.settings.getName());
		username.setText(ClientService.settings.getUsername());
		password.setText(ClientService.settings.getPassword());
		serverIp.setText(ClientService.settings.getIp());
		serverPort.setText(ClientService.settings.getPort() + "");
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.settings_cancel:
			finish();
			return;
		case R.id.settings_save:
			int id;
			try
			{
				id = Integer.parseInt(((TextView)findViewById(R.id.settings_server_id)).getText().toString());
			}
			catch (NumberFormatException e)
			{
				id = 0;
			}
			id = id < 0 ? 0 : id;
			String name = ((EditText)findViewById(R.id.settings_servername)).getText().toString();
			String username = ((EditText)findViewById(R.id.settings_username)).getText().toString();
			String password = ((EditText)findViewById(R.id.settings_password)).getText().toString();
			String ip = ((EditText)findViewById(R.id.settings_server_ip)).getText().toString();
			int port = Integer.parseInt(((EditText)findViewById(R.id.settings_server_port)).getText().toString());
			ServerSettings setting = new ServerSettings(id, name, ip, port, username, password, ServerList.DELIM);
			ClientService.serverList.replace(setting);
			finish();
			return;
		case R.id.settings_delete:

			return;
		}
	}
}
