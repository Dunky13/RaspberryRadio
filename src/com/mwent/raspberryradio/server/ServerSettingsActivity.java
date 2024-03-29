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
	public static final String TAG = "ServerSettingsActivity";

	Button cancel, save, delete, disconnect;
	boolean deleteButton;

	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);

		deleteButton = getIntent().getBooleanExtra("delete", true);
		setContentView(R.layout.activity_server_settings);
		//		findViewById(R.id.settings_layout).clearFocus();
		cancel = (Button)findViewById(R.id.settings_cancel);
		cancel.setOnClickListener(this);

		save = (Button)findViewById(R.id.settings_save);
		save.setOnClickListener(this);

		delete = (Button)findViewById(R.id.settings_delete);
		disconnect = (Button)findViewById(R.id.settings_disconnect);

		if (deleteButton)
		{
			delete.setOnClickListener(this);
			delete.setVisibility(View.VISIBLE);
			disconnect.setVisibility(View.GONE);
		}
		else
		{
			disconnect.setOnClickListener(this);
			disconnect.setVisibility(View.VISIBLE);
			delete.setVisibility(View.GONE);
		}

		if (ClientService.connectedServer != null || ClientService.serverSettings != null)
		{
			String type = getIntent().getStringExtra("type");
			if (type.equals("main"))
			{
				fillSettings(ClientService.connectedServer);
			}
			else
			{
				fillSettings(ClientService.serverSettings);
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onStop()
	{
		finish();
		super.onStop();
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
		case R.id.settings_disconnect:
			showDisconnectAlert();
			finish();
			return;
		}
	}

	private void processSave()
	{

		ServerSettings setting = getSetting(false);
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

	private void fillSettings(ServerSettings settings)
	{
		TextView id = (TextView)findViewById(R.id.settings_server_id);
		EditText serverName = (EditText)findViewById(R.id.settings_servername);
		EditText username = (EditText)findViewById(R.id.settings_username);
		EditText password = (EditText)findViewById(R.id.settings_password);
		EditText serverIp = (EditText)findViewById(R.id.settings_server_ip);
		EditText serverPort = (EditText)findViewById(R.id.settings_server_port);

		if (settings != ServerSettings.NEW_SERVER)
		{
			id.setText(settings.getId() + "");
			serverName.setText(settings.getName());
			username.setText(settings.getUsername());
			password.setText(settings.getPassword());
			serverIp.setText(settings.getIp());
			serverPort.setText(settings.getPort() + "");
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

	private ServerSettings getSetting(boolean delete)
	{
		String name, username, password, ip;
		int id, port;
		try
		{
			id = Integer.parseInt(((TextView)findViewById(R.id.settings_server_id)).getText().toString());
		}
		catch (NumberFormatException e)
		{
			if (delete)
			{
				Log.e("DELETE_ERROR", e.getMessage());
				finish();
				return null;
			}
			id = -1;
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
				ServerSettings setting = getSetting(true);
				ClientService.serverList.remove(setting);
			}
		}).setNegativeButton("Cancel", null);
		alertDialogBuilder.create().show();
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
				if (ClientService.clientAPI.isConnected())
				{
					ClientService.clientAPI.disconnect();
					if (ClientService.updaterService != null)
						ClientService.mainActivity.stopService(ClientService.updaterService);
					ClientService.mainActivity.setSongText(getResources().getString(R.string.no_song_playing));
					ClientService.mainActivity.setDefaultAlbumImage();
					ClientService.mainActivity.hideRightSide(true);
					ClientService.mainActivity.toggle();
				}

				if (ClientService.mainActivity.menu != null)
				{
					ClientService.mainActivity.hideRightSide(true);
				}
			}
		}).setNegativeButton("Cancel", null);
		alertDialogBuilder.create().show();
	}
}