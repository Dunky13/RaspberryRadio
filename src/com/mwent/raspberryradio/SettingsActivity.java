package com.mwent.raspberryradio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends Activity implements OnClickListener
{

	Button cancel, save, delete;

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

		delete = (Button)findViewById(R.id.settings_delete);
		delete.setOnClickListener(this);
	}

	private void fillSettings()
	{
		TextView id = (TextView)findViewById(R.id.settings_server_id);
		EditText serverName = (EditText)findViewById(R.id.settings_servername);
		EditText username = (EditText)findViewById(R.id.settings_username);
		EditText password = (EditText)findViewById(R.id.settings_password);
		EditText serverIp = (EditText)findViewById(R.id.settings_server_ip);
		EditText serverPort = (EditText)findViewById(R.id.settings_server_port);

		if (ClientService.settings != ServerSettings.NEW_SERVER)
		{
			id.setText(ClientService.settings.getId() + "");
			serverName.setText(ClientService.settings.getName());
			username.setText(ClientService.settings.getUsername());
			password.setText(ClientService.settings.getPassword());
			serverIp.setText(ClientService.settings.getIp());
			serverPort.setText(ClientService.settings.getPort() + "");
		}
		else
		{
			id.setText("");
			serverName.setText("");
			username.setText("");
			password.setText("");
			serverIp.setText("");
			serverPort.setText("");
		}
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
		case R.id.settings_delete:
			showDeleteAlert();
			finish();
			return;
		}
	}

	private void showDeleteAlert()
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ClientService.mainActivity);
		alertDialogBuilder.setTitle("Are you sure you want to delete");
		alertDialogBuilder.setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String name, username, password, ip;
				int id, port;
				ServerSettings setting;
				try
				{
					id = Integer.parseInt(((TextView)findViewById(R.id.settings_server_id)).getText().toString());
				}
				catch (NumberFormatException e)
				{
					Log.e("DELETE_ERROR", e.getMessage());
					finish();
					return;
				}
				name = ((EditText)findViewById(R.id.settings_servername)).getText().toString();
				username = ((EditText)findViewById(R.id.settings_username)).getText().toString();
				password = ((EditText)findViewById(R.id.settings_password)).getText().toString();
				ip = ((EditText)findViewById(R.id.settings_server_ip)).getText().toString();
				port = Integer.parseInt(((EditText)findViewById(R.id.settings_server_port)).getText().toString());
				setting = new ServerSettings(id, name, ip, port, username, password, ServerList.DELIM);
				ClientService.serverList.remove(setting);
			}
		}).setNegativeButton("Cancel", null);
		alertDialogBuilder.create().show();
	}
}
