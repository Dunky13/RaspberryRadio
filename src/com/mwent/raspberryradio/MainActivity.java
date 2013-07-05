package com.mwent.raspberryradio;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.mwent.raspberryradio.server.ServerList;
import com.mwent.raspberryradio.server.ServerSettingsActivity;
import com.mwent.raspberryradio.station.StationList;

import de.umass.lastfm.Caller;

public class MainActivity extends SlidingFragmentActivity implements OnClickListener, OnLongClickListener
{
	public static final String TAG = "MainActivity"; // logging tag
	
	public static int notificationId; // used to update notification text in the status bar
	protected ListFragment mFrag;

	private ImageButton buttonPrev, buttonStop, buttonPlay, buttonNext;
	private ImageView albumImage;
	private ProgressBar bar;
	private TextView songInfo;

	private AudioManager am;
	public Menu menu;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_main);
		setBehindContentView(R.layout.left_frame); // left and right menu
		loadVariables();

		FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();

		if (savedInstanceState == null)
		{
			mFrag = new ServerList();
			transaction.replace(R.id.left_frame, new ServerList());
		}
		else
		{
			mFrag = (ListFragment)this.getSupportFragmentManager().findFragmentById(R.id.left_frame);
		}

		loadSliderStuff(transaction);

		transaction.commit();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if (ClientService.clientAPI != null) 
		{
			UpdaterService.update(ClientService.clientAPI.getUpdate());
		}
	}

	@Override
	protected void onResume()
	{
//		// hide the settings menu button when not connected to the server
//		if(ClientService.clientAPI == null && this.menu != null)
//		{
//			MenuItem item = this.menu.findItem(R.id.action_settings);
//			item.setVisible(false);
//		}
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		// 		finish();
		//		super.stopService(new Intent(this, ClientService.class)); // Stop ClientAPI service
		//		super.stopService(new Intent(this, UpdaterService.class)); // Stop updater service
		UpdaterService.emptySongInfo();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem item = menu.findItem(R.id.action_settings);
		item.setVisible(false);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;
		switch (item.getItemId())
		{
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			getSlidingMenu().showMenu(true);
			return true;
		case R.id.action_settings:
			intent = new Intent(this, ServerSettingsActivity.class);
			intent.putExtra("type", "main");
			intent.putExtra("delete", false);
			this.startActivity(intent);
			return true;
		case R.id.action_stations:
			getSlidingMenu().showSecondaryMenu(true);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v)
	{
		if (ClientService.clientAPI == null)
		{
			showNoConnectionAlert();
			return;
		}
		switch (v.getId())
		{
		case R.id.prev:
			UpdaterService.prev();
			break;
		case R.id.stop:
			UpdaterService.stop();
			break;
		case R.id.play:
			UpdaterService.play();
			break;
		case R.id.next:
			UpdaterService.next();
			break;
		}
	}

	@Override
	public boolean onLongClick(View v)
	{
		if (ClientService.clientAPI != null)
			UpdaterService.update(ClientService.clientAPI.getUpdate());
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		int maxV = am.getStreamMaxVolume(AudioManager.STREAM_RING);
		int curV = am.getStreamVolume(AudioManager.STREAM_RING);
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			curV -= 1;
			setVolume(curV * 100d / maxV);
		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			curV += 1;
			setVolume(curV * 100d / maxV);
		}
		else if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			Intent intent = new Intent(this, ServerSettingsActivity.class);
			intent.putExtra("delete", false);
			this.startActivity(intent);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			toggle();
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			Intent intent = new Intent(this, ServerSettingsActivity.class);
			intent.putExtra("delete", true);
			this.startActivity(intent);
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}

	public boolean setSongText(final String s)
	{
		if (s != null && !s.trim().isEmpty())
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					songInfo.setText(s);
				}
			});
			return true;
		}
		return false;
	}

	public void setDefaultAlbumImage()
	{
		showProgressBar(true);
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				albumImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_album_image));
				showProgressBar(false);
			}
		});
	}

	public void showProgressBar(final boolean show)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				if (show)
				{
					bar.setVisibility(View.VISIBLE);
					albumImage.setVisibility(View.GONE);
				}
				else
				{
					bar.setVisibility(View.GONE);
					albumImage.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	public void updateInfo(final String songInfo)
	{
		if (ClientService.clientAPI.isPlaying())
		{
			showProgressBar(true);
			ClientService.stationList.firstLoadStationList();
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (setSongText(songInfo))
						showAlbumImage();
					else
					{
						setSongText(getResources().getString(R.string.no_song_name));
						setDefaultAlbumImage();
					}
				}
			}).start();
		}
	}

	public void showAlbumImage()
	{
		Caller.getInstance().setCache(null);

		String coverString = null;
		if (ClientService.clientAPI.getAlbumCoverEnabled())
		{
			coverString = ClientService.clientAPI.getAlbumCover();
		}
		if (coverString == null || coverString.trim().isEmpty())
		{
			setDefaultAlbumImage();
			return;
		}
		setImage(coverString);
	}

	public void setUpdaterTimer()
	{
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdaterTask(), 0, 1000 * 60);
	}

	private void setImage(String url)
	{
		AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>()
		{

			@Override
			protected Void doInBackground(String... params)
			{
				String url = params[0];
				final Bitmap bitmap = downloadBitmap(url);
				if (bitmap != null)
				{
					runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							albumImage.setImageBitmap(bitmap);
						}

					});
					showProgressBar(false);
				}
				else
					setDefaultAlbumImage();
				return null;
			}

		};
		task.execute(url);
	}

	private void loadVariables()
	{
		super.startService(new Intent(this, ClientService.class)); // Start ClientAPI service

		ClientService.mainActivity = this;

		setupAlbumImage();
		setupPlaybackButtons();
		setStartVolume();
	}

	private void setStartVolume()
	{
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if (ClientService.clientAPI != null)
		{
			int maxV = am.getStreamMaxVolume(AudioManager.STREAM_RING);
			int curV = am.getStreamVolume(AudioManager.STREAM_RING);
			ClientService.clientAPI.volume(curV * 100 / maxV);
		}
	}

	private void setVolume(double percentage)
	{
		if (ClientService.clientAPI != null)
		{
			ClientService.clientAPI.volume(percentage);
		}
		else
		{
			showNoConnectionAlert();
		}
	}

	private void setupAlbumImage()
	{
		bar = (ProgressBar)findViewById(R.id.album_image_loader);
		songInfo = (TextView)findViewById(R.id.song_info);
		albumImage = (ImageView)findViewById(R.id.album_image);
		albumImage.setOnLongClickListener(this);
	}

	private void setupPlaybackButtons()
	{
		buttonPrev = (ImageButton)findViewById(R.id.prev);
		buttonStop = (ImageButton)findViewById(R.id.stop);
		buttonPlay = (ImageButton)findViewById(R.id.play);
		buttonNext = (ImageButton)findViewById(R.id.next);

		buttonPrev.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
		buttonPlay.setOnClickListener(this);
		buttonNext.setOnClickListener(this);
	}

	private void showNoConnectionAlert()
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("You are not connected");
		alertDialogBuilder.setMessage("Please connect before using the buttons").setCancelable(false).setPositiveButton("Ok", null);
		alertDialogBuilder.create().show();
	}

	private Bitmap downloadBitmap(String url)
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

	private void loadSliderStuff(FragmentTransaction transaction)
	{
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);

		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);

		sm.setSecondaryMenu(R.layout.right_frame);
		sm.setSecondaryShadowDrawable(R.drawable.shadowright);

		transaction.replace(R.id.left_frame, new ServerList()); //LEFT
		transaction.replace(R.id.right_frame, new StationList()); //RIGHT
	}

	protected class UpdaterTask extends TimerTask
	{
		@Override
		public void run()
		{
			runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					if (ClientService.clientAPI != null)
						UpdaterService.update(ClientService.clientAPI.getUpdate());
				}
			});
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setNotification(String title, String message) {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(title)
		        .setContentText(message);
		
//		Intent resultIntent = new Intent("com.mwent.raspberryradio.MainActivity");
		Intent resultIntent = new Intent(this, MainActivity.class);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationId, mBuilder.build());
	}
}