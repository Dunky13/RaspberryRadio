package com.mwent.raspberryradio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UpdaterService extends Service
{

	@Override
	public void onCreate()
	{
		super.onCreate();
		String songInfo = ClientService.clientAPI.getUpdate();
		update(songInfo);
	}

	public static void update(final String songInfo)
	{
		if (ClientService.clientAPI.isPlaying())
		{
			ClientService.stationList.firstLoadStationList();
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					if (ClientService.mainActivity.setSongText(songInfo))
						ClientService.mainActivity.showAlbumImage();
					else
					{
						ClientService.mainActivity.setSongText(ClientService.mainActivity.getResources().getString(
							R.string.no_song_name));
						ClientService.mainActivity.setDefaultAlbumImage();
					}
				}
			}).start();
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