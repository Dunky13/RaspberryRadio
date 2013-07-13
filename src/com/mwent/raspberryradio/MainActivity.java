package com.mwent.raspberryradio;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.mwent.raspberryradio.server.ServerList;
import com.mwent.raspberryradio.server.ServerSettingsActivity;
import com.mwent.raspberryradio.station.StationList;
import com.mwent.raspberryradio.station.StationSettingsActivity;
import com.mwent.raspberryradio.station.StationURL;

public class MainActivity extends SlidingFragmentActivity implements OnClickListener, OnLongClickListener
{
	public static final String TAG = "MainActivity"; // logging tag
	public static final int MAX_VOLUME = 15; //max volume
	private static final ScheduledExecutorService executor = 
			  Executors.newSingleThreadScheduledExecutor();

	public static int notificationId; // used to update notification text in the status bar
	protected ListFragment mFrag;

	private ImageButton buttonPrev, buttonStop, buttonPlay, buttonNext;
	private ImageView albumImage;
	private ProgressBar bar;
	private TextView songInfo;

	public AlertDialog.Builder alertDialogBuilder;
	public NotificationManager mNotificationManager;
	public Menu menu;

	private int curVol = 1;

	private Bitmap bitmap;

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

		Uri data = getIntent().getData();
		if (data != null)
		{
			ClientService.stationSettings = StationURL.parse(data);
			startActivity(new Intent(ClientService.mainActivity, StationSettingsActivity.class));
		}

		loadSliderStuff(transaction);

		transaction.commit();

		//		setTheme(android.R.style.Theme_Holo);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if (ClientService.clientAPI != null)
		{
			UpdaterService.update(ClientService.clientAPI.getCurrent());
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
	protected void onDestroy()
	{
		try
		{
			mNotificationManager.cancel(notificationId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		super.onDestroy();
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
			mNotificationManager.cancel(notificationId);
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
		switch (v.getId())
		{
		case R.id.album_image:
			if (ClientService.clientAPI != null)
				UpdaterService.update(ClientService.clientAPI.getCurrent());
			break;
		case android.R.id.home:
			Log.d("long click", "home");
			break;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			if (ClientService.clientAPI != null)
			{
				curVol--;
				if (curVol < 0)
					curVol = 0;
				setVolume(curVol * 100d / MAX_VOLUME);
				showVolumeToaster(curVol);
			}
			else
			{
				showNoConnectionAlert();
			}
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			if (ClientService.clientAPI != null)
			{
				curVol++;
				if (curVol > MAX_VOLUME)
					curVol = MAX_VOLUME;
				setVolume(curVol * 100d / MAX_VOLUME);
				showVolumeToaster(curVol);
			}
			else
			{
				showNoConnectionAlert();
			}
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			Intent intent = new Intent(this, ServerSettingsActivity.class);
			intent.putExtra("type", "main");
			intent.putExtra("delete", false);
			this.startActivity(intent);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void showVolumeToaster(int curVol) {
		LayoutInflater inflater = getLayoutInflater();
		View volumeToastLayout = inflater.inflate(R.layout.volume_toast,
				(ViewGroup) findViewById(R.id.toast_layout));

		SeekBar volumeSeekBar = (SeekBar) volumeToastLayout.findViewById(R.id.volumeSeekBar);
		volumeSeekBar.setProgress(curVol);
		volumeSeekBar.setMax(MAX_VOLUME);
		volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					setVolume(progress * 100d / MAX_VOLUME);
				}
			}
		});

		alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(volumeToastLayout);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.create().show();
		
//		Runnable hideDialog= new Runnable() {
//		    public void run() {
//		    	alertDialogBuilder.hide();
//		    }
//		};
//		
//		executor.schedule(hideDialog, 5, TimeUnit.SECONDS);
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

	//	@Override
	//	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	//	{
	//		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
	//		if (scanResult != null)
	//		{
	//			Log.e("QR", scanResult.toString());
	//		}
	//	}

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
		bitmap = null;
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
					{
						showAlbumImage(songInfo);
					}
					else
					{
						setSongText(getResources().getString(R.string.no_song_name));
						setDefaultAlbumImage();
						setNotification("RaspberryRadio", songInfo, ClientService.serverSettings.getName());
					}
				}
			}).start();
		}
	}

	public void showAlbumImage(String songInfo)
	{
		//		Caller.getInstance().setCache(null);

		String coverString = null;
		if (ClientService.clientAPI.isAlbumCoversEnabled())
		{
			coverString = ClientService.clientAPI.getAlbumCover();
		}
		if (coverString == null || coverString.trim().isEmpty())
		{
			setDefaultAlbumImage();
			return;
		}
		setImage(coverString, songInfo);
	}

	public void setUpdaterTimer()
	{
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdaterTask(), 0, 1000 * 60);
	}

	private void setImage(String url, final String songInfo)
	{
		AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>()
				{

			@Override
			protected Void doInBackground(String... params)
			{
				String url = params[0];
				bitmap = ClientService.downloadBitmap(url);
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

			@Override
			protected void onPostExecute(Void result)
			{
				setNotification("RaspberryRadio", songInfo, ClientService.serverSettings.getName());
			}
				};
				task.execute(url);
	}

	private void loadVariables()
	{
		super.startService(new Intent(this, ClientService.class)); // Start ClientAPI service

		ClientService.mainActivity = this;

		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		setupAlbumImage();
		setupPlaybackButtons();
		setStartVolume();
	}

	private void setStartVolume()
	{
		//		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		//am.getStreamMaxVolume(AudioManager.STREAM_RING);
		curVol = 7;//am.getStreamVolume(AudioManager.STREAM_RING);
		if (ClientService.clientAPI != null)
		{
			ClientService.clientAPI.setVolume(curVol * 100d / MAX_VOLUME);
		}
	}

	private void setVolume(double percentage)
	{
		ClientService.clientAPI.setVolume(percentage);
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
		alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("You are not connected");
		alertDialogBuilder.setMessage("Please connect before using the buttons").setCancelable(false).setPositiveButton("Ok", null);
		alertDialogBuilder.create().show();
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
						UpdaterService.update(ClientService.clientAPI.getCurrent());
				}
			});
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setNotification(String title, String message, String station)
	{
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setOngoing(true).setAutoCancel(false).setContentTitle(title).setContentText(station).setSubText(message);

		if (bitmap != null)
			mBuilder.setLargeIcon(bitmap);
		Intent resultIntent = new Intent(this, MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		mNotificationManager.notify(notificationId, mBuilder.build());
	}
}