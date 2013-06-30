package com.mwent.raspberryradio.station;

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

public class StationSettingsActivity extends Activity implements OnClickListener
{

	Button cancel, save, delete;

	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.activity_station_settings);

		if (ClientService.stationSettings != null)
		{
			fillSettings();
		}

		cancel = (Button)findViewById(R.id.settings_station_cancel);
		cancel.setOnClickListener(this);

		save = (Button)findViewById(R.id.settings_station_save);
		save.setOnClickListener(this);

		delete = (Button)findViewById(R.id.settings_station_delete);
		delete.setOnClickListener(this);
	}

	private void fillSettings()
	{
		TextView id = (TextView)findViewById(R.id.settings_station_id);
		EditText serverName = (EditText)findViewById(R.id.settings_station_name);
		EditText serverIp = (EditText)findViewById(R.id.settings_station_ip);

		if (ClientService.stationSettings != StationSettings.NEW_STATION)
		{
			id.setText(ClientService.stationSettings.getId() + "");
			serverName.setText(ClientService.stationSettings.getName());
			serverIp.setText(ClientService.stationSettings.getIp());
		}
		else
		{
			id.setText("");
			serverName.setText("");
			serverIp.setText("");
		}
	}

	@Override
	public void onClick(View v)
	{
		String name, ip;
		int id, pos;
		StationSettings setting;
		switch (v.getId())
		{
		case R.id.settings_station_cancel:
			finish();
			return;
		case R.id.settings_station_save:
			try
			{
				id = Integer.parseInt(((TextView)findViewById(R.id.settings_station_id)).getText().toString());
			}
			catch (NumberFormatException e)
			{
				id = -1;
			}
			try
			{
				pos = Integer.parseInt(((TextView)findViewById(R.id.settings_station_pos)).getText().toString());
			}
			catch (NumberFormatException e)
			{
				pos = -1;
			}
			name = ((EditText)findViewById(R.id.settings_station_name)).getText().toString();
			ip = ((EditText)findViewById(R.id.settings_station_ip)).getText().toString();
			setting = new StationSettings(id, name, ip, pos, StationList.DELIM);
			if (id < 0)
			{
				setting.setId(0);
				ClientService.stationList.add(setting);
			}
			else
				ClientService.stationList.replace(setting);
			finish();
			return;
		case R.id.settings_station_delete:
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
				String name, ip;
				int id, pos;
				StationSettings setting;
				try
				{
					id = Integer.parseInt(((TextView)findViewById(R.id.settings_station_id)).getText().toString());
				}
				catch (NumberFormatException e)
				{
					Log.e("DELETE_ERROR", e.getMessage());
					finish();
					return;
				}
				try
				{
					pos = Integer.parseInt(((TextView)findViewById(R.id.settings_station_pos)).getText().toString());
				}
				catch (NumberFormatException e)
				{
					pos = -1;
					finish();
					return;
				}
				name = ((EditText)findViewById(R.id.settings_station_name)).getText().toString();
				ip = ((EditText)findViewById(R.id.settings_station_ip)).getText().toString();
				setting = new StationSettings(id, name, ip, pos, StationList.DELIM);
				ClientService.stationList.remove(setting);
			}
		}).setNegativeButton("Cancel", null);
		alertDialogBuilder.create().show();
	}
}
