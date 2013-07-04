package com.mwent.raspberryradio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.mwent.RaspberryRadio.client.AndroidClient;
import com.mwent.raspberryradio.server.ServerList;
import com.mwent.raspberryradio.server.ServerSettings;
import com.mwent.raspberryradio.station.StationList;
import com.mwent.raspberryradio.station.StationSettings;

public class ClientService extends Service
{

	public static MainActivity mainActivity;
	public static AndroidClient clientAPI;

	public static ServerList serverList;
	public static ServerSettings serverSettings;

	public static StationList stationList;
	public static StationSettings stationSettings;

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		if (clientAPI.is_connected())
		{
			clientAPI.disconnect();
		}
		clientAPI = null;
		super.onDestroy();
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
}
