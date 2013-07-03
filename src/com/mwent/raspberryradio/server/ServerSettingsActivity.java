package com.mwent.raspberryradio.server;

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
import com.mwent.raspberryradio.ClientService;
import com.mwent.raspberryradio.R;

public class ServerSettingsActivity extends Activity implements OnClickListener
{

	Button cancel, save, delete;

	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.activity_server_settings);
		//		findViewById(R.id.settings_layout).clearFocus();
		cancel = (Button)findViewById(R.id.settings_cancel);
		cancel.setOnClickListener(this);

		save = (Button)findViewById(R.id.settings_save);
		save.setOnClickListener(this);

		delete = (Button)findViewById(R.id.settings_delete);
		delete.setOnClickListener(this);

		if (ClientService.serverSettings != null)
		{
			fillSettings();
		}

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
			processSave();
			finish();
			return;
		case R.id.settings_delete:
			showDeleteAlert();
			finish();
			return;
		}
	}

	private void processSave()
	{

		ServerSettings setting = getSetting();
		if (setting == null)
			return;
		if (setting.getId() < 0)
		{
			setting.setId(0);
			ClientService.serverList.add(setting);
		}
		else
			ClientService.serverList.replace(setting);
	}

	private void fillSettings()
	{
		TextView id = (TextView)findViewById(R.id.settings_server_id);
		EditText serverName = (EditText)findViewById(R.id.settings_servername);
		EditText username = (EditText)findViewById(R.id.settings_username);
		EditText password = (EditText)findViewById(R.id.settings_password);
		EditText serverIp = (EditText)findViewById(R.id.settings_server_ip);
		EditText serverPort = (EditText)findViewById(R.id.settings_server_port);

		if (ClientService.serverSettings != ServerSettings.NEW_SERVER)
		{
			id.setText(ClientService.serverSettings.getId() + "");
			serverName.setText(ClientService.serverSettings.getName());
			username.setText(ClientService.serverSettings.getUsername());
			password.setText(ClientService.serverSettings.getPassword());
			serverIp.setText(ClientService.serverSettings.getIp());
			serverPort.setText(ClientService.serverSettings.getPort() + "");
		}
		else
		{
			delete.setVisibility(View.GONE);
			id.setText("");
			serverName.setText("");
			username.setText("");
			password.setText("");
			serverIp.setText("");
			serverPort.setText("");
		}
	}

	private ServerSettings getSetting()
	{
		String name, username, password, ip;
		int id, port;
		try
		{
			id = Integer.parseInt(((TextView)findViewById(R.id.settings_server_id)).getText().toString());
		}
		catch (NumberFormatException e)
		{
			Log.e("DELETE_ERROR", e.getMessage());
			finish();
			return null;
		}
		name = ((EditText)findViewById(R.id.settings_servername)).getText().toString();
		username = ((EditText)findViewById(R.id.settings_username)).getText().toString();
		password = ((EditText)findViewById(R.id.settings_password)).getText().toString();
		ip = ((EditText)findViewById(R.id.settings_server_ip)).getText().toString();
		port = Integer.parseInt(((EditText)findViewById(R.id.settings_server_port)).getText().toString());
		return new ServerSettings(id, name, ip, port, username, password, ServerList.DELIM);
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
				ServerSettings setting = getSetting();
				ClientService.serverList.remove(setting);
			}
		}).setNegativeButton("Cancel", null);
		alertDialogBuilder.create().show();
	}
}
