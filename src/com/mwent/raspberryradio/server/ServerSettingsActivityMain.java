package com.mwent.raspberryradio.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.mwent.raspberryradio.ClientService;
import com.mwent.raspberryradio.R;

public class ServerSettingsActivityMain extends Activity implements OnClickListener
{

	Button cancel, save, disconnect;

	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.activity_server_settings_main);
		//		findViewById(R.id.settings_layout).clearFocus();

		if (ClientService.serverSettings != null)
		{
			fillSettings();
		}

		cancel = (Button)findViewById(R.id.settings_cancel);
		cancel.setOnClickListener(this);

		save = (Button)findViewById(R.id.settings_save);
		save.setOnClickListener(this);

		disconnect = (Button)findViewById(R.id.settings_disconnect);
		disconnect.setOnClickListener(this);
	}

	private void fillSettings()
	{
		TextView id = (TextView)findViewById(R.id.settings_server_id);
		EditText serverName = (EditText)findViewById(R.id.settings_servername);
		EditText username = (EditText)findViewById(R.id.settings_username);
		EditText password = (EditText)findViewById(R.id.settings_password);
		EditText serverIp = (EditText)findViewById(R.id.settings_server_ip);
		EditText serverPort = (EditText)findViewById(R.id.settings_server_port);

		id.setText(ClientService.serverSettings.getId() + "");
		serverName.setText(ClientService.serverSettings.getName());
		username.setText(ClientService.serverSettings.getUsername());
		password.setText(ClientService.serverSettings.getPassword());
		serverIp.setText(ClientService.serverSettings.getIp());
		serverPort.setText(ClientService.serverSettings.getPort() + "");
	}

	@Override
	public void onClick(View v)
	{
		String name, username, password, ip;
		int id, port;
		ServerSettings setting;
		switch (v.getId())
		{
		case R.id.settings_cancel:
			finish();
			return;
		case R.id.settings_save:
			try
			{
				id = Integer.parseInt(((TextView)findViewById(R.id.settings_server_id)).getText().toString());
			}
			catch (NumberFormatException e)
			{
				id = -1;
			}
			name = ((EditText)findViewById(R.id.settings_servername)).getText().toString();
			username = ((EditText)findViewById(R.id.settings_username)).getText().toString();
			password = ((EditText)findViewById(R.id.settings_password)).getText().toString();
			ip = ((EditText)findViewById(R.id.settings_server_ip)).getText().toString();
			port = Integer.parseInt(((EditText)findViewById(R.id.settings_server_port)).getText().toString());
			setting = new ServerSettings(id, name, ip, port, username, password, ServerList.DELIM);
			if (id < 0)
			{
				setting.setId(0);
				ClientService.serverList.add(setting);
			}
			else
				ClientService.serverList.replace(setting);
			finish();
			return;
		case R.id.settings_disconnect:
			showDisconnectAlert();
			finish();
			return;
		}
	}

	private void showDisconnectAlert()
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ClientService.mainActivity);
		alertDialogBuilder.setTitle("Are you sure you want to disconnect?");
		alertDialogBuilder.setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (ClientService.clientAPI.is_connected())
				{
					ClientService.clientAPI.disconnect();
					ClientService.mainActivity.toggle();
				}

			}
		}).setNegativeButton("Cancel", null);
		alertDialogBuilder.create().show();
	}
}