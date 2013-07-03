package com.mwent.raspberryradio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UpdaterService extends Service
{

	private static String previousSong = "";

	@Override
	public void onCreate()
	{
		super.onCreate();
		String songInfo = ClientService.clientAPI.getUpdate();
		update(songInfo);
	}

	public static void update(String songInfo)
	{
		if (songInfo != null && !previousSong.equals(songInfo))
		{
			previousSong = songInfo;
			ClientService.mainActivity.updateInfo(songInfo);
		}
	}

	public static void prev()
	{
		update(ClientService.clientAPI.prev());
	}

	public static void next()
	{
		update(ClientService.clientAPI.next());
	}

	public static void play()
	{
		update(ClientService.clientAPI.play());
	}

	public static void stop()
	{
		ClientService.clientAPI.stop();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}