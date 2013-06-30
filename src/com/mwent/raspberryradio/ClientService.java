package com.mwent.raspberryradio;

import java.net.URISyntaxException;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mwent.RaspberryRadio.client.AndroidClient;

public class ClientService extends Service {

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
		if(clientAPI.is_connected())
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
	
	public static void updatePlayInfo(View view) {
		ImageView albumImage = (ImageView) view.findViewById(R.id.album_image);
		TextView songInfo = (TextView) view.findViewById(R.id.song_info);
		
		URL albumCoverUrl = clientAPI.getAlbumCover();
		String currentlyPlaying = clientAPI.getUpdate();

		try {
			albumImage.setImageURI(Uri.parse(albumCoverUrl.toURI().toString()));
		} catch (URISyntaxException e) {
			albumImage.setImageDrawable(view.getResources().getDrawable(R.drawable.default_album_image));
			Log.e("ALBUM_IMAGE_ERROR", e.getMessage());
		}
		songInfo.setText(currentlyPlaying);
		
	}
}
