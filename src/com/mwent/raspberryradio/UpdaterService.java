package com.mwent.raspberryradio;

import info.mwent.RaspberryRadio.client.exceptions.DisconnectException;
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
			String songInfo = null;
			try
			{
				songInfo = ClientService.clientAPI.getCurrent();
			}
			catch (DisconnectException e)
			{
				ClientService.mainActivity.hideRightSide(true);
			}
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
		try
		{
			update(ClientService.clientAPI.prev());
		}
		catch (DisconnectException e)
		{
			ClientService.mainActivity.hideRightSide(true);
		}
	}

	public static void next()
	{
		try
		{
			update(ClientService.clientAPI.next());
		}
		catch (DisconnectException e)
		{
			ClientService.mainActivity.hideRightSide(true);
		}
	}

	public static void play()
	{
		String play = "-1";
		try
		{
			play = ClientService.clientAPI.play();
		}
		catch (DisconnectException e)
		{
			//			Log.e("Disconnect Error", e.getMessage());
			ClientService.mainActivity.hideRightSide(true);
		}
		//		Log.e("Play Test", play);
		update(play);
	}

	public static void stop()
	{
		try
		{
			ClientService.clientAPI.stop();
		}
		catch (DisconnectException e)
		{
			ClientService.mainActivity.hideRightSide(true);
		}
		finally
		{
			emptySongInfo();
		}
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