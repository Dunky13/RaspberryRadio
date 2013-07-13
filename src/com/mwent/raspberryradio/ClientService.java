package com.mwent.raspberryradio;

import info.mwent.RaspberryRadio.AndroidAPI;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.IBinder;
import android.util.Log;
import com.mwent.raspberryradio.server.ServerList;
import com.mwent.raspberryradio.server.ServerSettings;
import com.mwent.raspberryradio.station.StationList;
import com.mwent.raspberryradio.station.StationSettings;
import com.mwent.raspberryradio.station.StationSettingsActivity;

public class ClientService extends Service
{

	public static MainActivity mainActivity;
	public static AndroidAPI clientAPI;

	public static ServerList serverList;
	public static ServerSettings serverSettings;
	public static ServerSettings connectedServer;

	public static StationList stationList;
	public static StationSettings stationSettings;
	public static StationSettingsActivity stationSettingsActivity;

	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		if (clientAPI.isConnected())
		{
			clientAPI.disconnect();
		}
		clientAPI = null;
		super.onDestroy();
	}

	@Override
	public void onRebind(Intent intent)
	{
		super.onRebind(intent);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		return super.onUnbind(intent);
	}

	public static Bitmap downloadBitmap(String url)
	{
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);

		try
		{
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK)
			{
				Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				InputStream inputStream = null;
				try
				{
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					return bitmap;
				}
				finally
				{
					if (inputStream != null)
					{
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		}
		catch (Exception e)
		{
			getRequest.abort();
			Log.w("ImageDownloader", "Error while retrieving bitmap from " + url + " - " + e.getMessage());

		}
		finally
		{
			if (client != null)
			{
				client.close();
			}
		}
		return null;
	}
}