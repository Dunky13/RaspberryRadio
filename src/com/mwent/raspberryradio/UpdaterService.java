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
		if (ClientService.clientAPI != null)
		{
			String songInfo = ClientService.clientAPI.getCurrent();
			update(songInfo);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
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
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onRebind(Intent intent)
	{
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		return super.onUnbind(intent);
	}

	public static void emptySongInfo()
	{
		previousSong = "";
	}
}