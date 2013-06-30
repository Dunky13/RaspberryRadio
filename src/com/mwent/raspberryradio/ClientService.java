package com.mwent.raspberryradio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.mwent.RaspberryRadio.client.AndroidClient;

public class ClientService extends Service
{

	static MainActivity main;
	static AndroidClient clientAPI;
	public static ServerSettings settings;

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (clientAPI.is_connected())
		{
			clientAPI.disconnect();
		}
		clientAPI = null;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
