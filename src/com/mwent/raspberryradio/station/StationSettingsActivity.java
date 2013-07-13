package com.mwent.raspberryradio.station;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.mwent.raspberryradio.ClientService;
import com.mwent.raspberryradio.R;
import com.mwent.raspberryradio.station.qr.IntentIntegrator;

public class StationSettingsActivity extends Activity implements OnClickListener
{

	Button cancel, save, delete;

	public StationSettingsActivity()
	{
		super();
		//		onCreate(new Bundle());
	}

	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		ClientService.stationSettingsActivity = this;

		setContentView(R.layout.activity_station_settings);

		cancel = (Button)findViewById(R.id.settings_station_cancel);
		cancel.setOnClickListener(this);

		save = (Button)findViewById(R.id.settings_station_save);
		save.setOnClickListener(this);

		delete = (Button)findViewById(R.id.settings_station_delete);
		delete.setOnClickListener(this);

		if (ClientService.stationSettings != null)
		{
			fillSettings();
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

	private void fillSettings()
	{
		TextView id = (TextView)findViewById(R.id.settings_station_id);
		TextView pos = (TextView)findViewById(R.id.settings_station_pos);
		EditText serverName = (EditText)findViewById(R.id.settings_station_name);
		EditText serverIp = (EditText)findViewById(R.id.settings_station_ip);

		if (ClientService.stationSettings == StationSettings.NEW_STATION)
		{
			delete.setVisibility(View.GONE);
			save.setVisibility(View.VISIBLE);
			serverIp.setEnabled(true);
			serverIp.requestFocus();
			id.setText("");
			pos.setText("");
			serverName.setText("New Server");
			serverName.setEnabled(true);
			serverIp.setText("");
		}
		else if (ClientService.stationSettings == StationSettings.NEW_STATION_BY_QR)
		{
			IntentIntegrator integrator = new IntentIntegrator(this);
			integrator.initiateScan();
		}
		else if (!ClientService.stationList.has(ClientService.stationSettings))
		{
			delete.setVisibility(View.GONE);
			save.setVisibility(View.VISIBLE);
			serverIp.requestFocus();

			id.setText(ClientService.stationSettings.getId() + "");
			serverName.setText(ClientService.stationSettings.getName());
			serverIp.setText(ClientService.stationSettings.getIp());
			pos.setText(ClientService.stationSettings.getPos() + "");
		}

		else
		{
			id.setText(ClientService.stationSettings.getId() + "");
			serverName.setText(ClientService.stationSettings.getName());
			serverIp.setText(ClientService.stationSettings.getIp());
			pos.setText(ClientService.stationSettings.getPos() + "");
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.settings_station_cancel:
			finish();
			return;
		case R.id.settings_station_save:
			saveStation();
			finish();
			return;
		case R.id.settings_station_delete:
			showDeleteAlert();
			finish();
			return;
		}
	}

	private void saveStation()
	{
		StationSettings setting = getSettings();
		ClientService.stationList.add(setting);
	}

	private StationSettings getSettings()
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
		return setting;
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
				StationSettings setting = getSettings();
				ClientService.stationList.remove(setting);
			}
		}).setNegativeButton("Cancel", null);
		alertDialogBuilder.create().show();
	}

	private void getQR(String name, String station)
	{
		AsyncTask<String, Void, Bitmap> task = new AsyncTask<String, Void, Bitmap>()
		{

			@Override
			protected Bitmap doInBackground(String... params)
			{
				String url = params[0];
				Bitmap bitmap = ClientService.downloadBitmap(url);
				return bitmap;
			}
		};
		String url = "station://radio.mwent.info/?name=\"" + name + "\"&station=\"" + station + "\"";
		try
		{
			url = URLEncoder.encode(url, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
		}
		int width = 300;
		int height = 300;
		String qr = "http://chart.apis.google.com/chart?cht=qr&chs=" + width + "x" + height + "&chld=Q|0&choe=UTF-8&chl=" + url;
		try
		{
			Bitmap b = task.execute(qr).get();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}

	}
}
