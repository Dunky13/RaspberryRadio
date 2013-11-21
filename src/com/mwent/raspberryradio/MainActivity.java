package com.mwent.raspberryradio;

import info.mwent.RaspberryRadio.client.exceptions.DisconnectException;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
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
import com.dfsvmerojcl.AdController;
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

	public static int notificationId; // used to update notification text in the status bar
	protected ListFragment mFrag;

	private ImageButton _buttonPrev, _buttonStop, _buttonPlay, _buttonNext;
	private ImageView _albumImage;
	private ProgressBar _bar;
	private TextView _songInfo;
	private SeekBar _volumeSeekBar;

	public AlertDialog.Builder alertDialogBuilder;
	public NotificationManager mNotificationManager;
	public Menu menu;

	private int _curVol = 1;

	private Bitmap _bitmap;
	private SlidingMenu _sm;
	private int _noConnectionAlertCounter;
	private AdController _adController;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setBehindContentView(R.layout.left_frame); // left and right menu
		loadVariables();

		slidingStuff(savedInstanceState);

		//		setTheme(android.R.style.Theme_Holo);

		_adController = new AdController(this, this.getResources().getString(R.string.leadbolt_ad_id));
		_adController.loadAd();
	}

	private void slidingStuff(Bundle savedInstanceState)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		if (savedInstanceState == null)
		{
			mFrag = new ServerList();
			transaction.replace(R.id.left_frame, new ServerList());
		}
		else
		{
			mFrag = (ListFragment)getSupportFragmentManager().findFragmentById(R.id.left_frame);
		}

		Uri data = getIntent().getData();
		if (data != null)
		{
			ClientService.stationSettings = StationURL.parse(data);
			startActivity(new Intent(MainActivity.this, StationSettingsActivity.class));
		}

		loadSliderStuff(transaction);

		transaction.commit();
		hideRightSide(true);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if (ClientService.clientAPI != null)
		{
			try
			{
				UpdaterService.update(ClientService.clientAPI.getCurrent());
			}
			catch (DisconnectException e)
			{
				ClientService.mainActivity.hideRightSide(true);
			}
		}
	}

	@Override
	protected void onStop()
	{
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
		_adController.destroyAd();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.action_settings).setVisible(false);
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
			setDefaultAlbumImage();
			setSongText(getResources().getString(R.string.no_song_playing));
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
			{
				try
				{
					UpdaterService.update(ClientService.clientAPI.getCurrent());
				}
				catch (DisconnectException e)
				{
					ClientService.mainActivity.hideRightSide(true);
				}
			}
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
				_curVol--;
				if (_curVol < 0)
				{
					_curVol = 0;
				}
				setVolume(_curVol * 100d / MAX_VOLUME);
				showVolumeToaster(_curVol);
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
				_curVol++;
				if (_curVol > MAX_VOLUME)
				{
					_curVol = MAX_VOLUME;
				}
				setVolume(_curVol * 100d / MAX_VOLUME);
				showVolumeToaster(_curVol);
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

	private void showVolumeToaster(int curVol)
	{
		LayoutInflater inflater = getLayoutInflater();
		View volumeToastLayout = inflater.inflate(R.layout.volume_toast, (ViewGroup)findViewById(R.id.toast_layout));

		_volumeSeekBar = (SeekBar)volumeToastLayout.findViewById(R.id.volumeSeekBar);
		_volumeSeekBar.setProgress(curVol);
		_volumeSeekBar.setMax(MAX_VOLUME);
		//		volumeSeekBar.onKey
		alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(volumeToastLayout);
		alertDialogBuilder.setCancelable(true);

		alertDialogBuilder.setOnKeyListener(getKeyListener());
		final AlertDialog dialog = alertDialogBuilder.create();
		dialog.show();

		final Runnable restartDialog = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					Thread.sleep(3000);
					dialog.dismiss();
				}
				catch (InterruptedException e)
				{
				}
			}
		};
		final RestartableThread restartThread = new RestartableThread(restartDialog);
		restartThread.start();
		_volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				restartThread.restart();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				restartThread.interrupt();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (fromUser)
				{
					setVolume(progress * 100d / MAX_VOLUME);
				}
				restartThread.restart();
			}
		});
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
					_songInfo.setText(s);
				}
			});
			return true;
		}
		return false;
	}

	public void setDefaultAlbumImage()
	{
		_bitmap = null;
		showProgressBar(true);
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				_albumImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_album_image));
				showProgressBar(false);
			}
		});
	}

	public void showProgressBar(final boolean show)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (show)
				{
					_bar.setVisibility(View.VISIBLE);
					_albumImage.setVisibility(View.GONE);
				}
				else
				{
					_bar.setVisibility(View.GONE);
					_albumImage.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	public void updateInfo(final String songInfo)
	{
		try
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
		catch (DisconnectException e)
		{
			ClientService.mainActivity.hideRightSide(true);
		}
	}

	public void showAlbumImage(String songInfo)
	{
		//		Caller.getInstance().setCache(null);

		String coverString = null;
		if (ClientService.clientAPI.isAlbumCoversEnabled())
		{
			try
			{
				coverString = ClientService.clientAPI.getAlbumCover();
			}
			catch (DisconnectException e)
			{
				ClientService.mainActivity.hideRightSide(true);
			}
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
				_bitmap = ClientService.downloadBitmap(url);
				if (_bitmap != null)
				{
					runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							_albumImage.setImageBitmap(_bitmap);
						}

					});
					showProgressBar(false);
				}
				else
				{
					setDefaultAlbumImage();
				}
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
		_curVol = 7;
		if (ClientService.clientAPI != null)
		{
			try
			{
				ClientService.clientAPI.setVolume(_curVol * 100d / MAX_VOLUME);
			}
			catch (DisconnectException e)
			{
				ClientService.mainActivity.hideRightSide(true);
			}
		}
	}

	private void setVolume(double percentage)
	{
		try
		{
			ClientService.clientAPI.setVolume(percentage);
		}
		catch (DisconnectException e)
		{
			ClientService.mainActivity.hideRightSide(true);
		}
	}

	private void setupAlbumImage()
	{
		_bar = (ProgressBar)findViewById(R.id.album_image_loader);
		_songInfo = (TextView)findViewById(R.id.song_info);
		_albumImage = (ImageView)findViewById(R.id.album_image);
		_albumImage.setOnLongClickListener(this);
	}

	private void setupPlaybackButtons()
	{
		_noConnectionAlertCounter = 0;

		_buttonPrev = (ImageButton)findViewById(R.id.prev);
		_buttonStop = (ImageButton)findViewById(R.id.stop);
		_buttonPlay = (ImageButton)findViewById(R.id.play);
		_buttonNext = (ImageButton)findViewById(R.id.next);

		_buttonPrev.setOnClickListener(this);
		_buttonStop.setOnClickListener(this);
		_buttonPlay.setOnClickListener(this);
		_buttonNext.setOnClickListener(this);
	}

	private void showNoConnectionAlert()
	{
		alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("You are not connected");
		if (_noConnectionAlertCounter < 5)
			alertDialogBuilder.setMessage("Please connect before using the buttons").setCancelable(false)
				.setPositiveButton("Ok", null);
		else
			alertDialogBuilder.setMessage("Seriously, stawp... \nPlease connect -.-").setCancelable(false)
				.setPositiveButton("Ok", null);
		alertDialogBuilder.create().show();
		_noConnectionAlertCounter++;
	}

	private void loadSliderStuff(FragmentTransaction transaction)
	{
		_sm = getSlidingMenu();
		_sm.setMode(SlidingMenu.LEFT_RIGHT);
		_sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		_sm.setShadowWidthRes(R.dimen.shadow_width);
		_sm.setShadowDrawable(R.drawable.shadow);

		_sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		_sm.setFadeDegree(0.35f);

		_sm.setSecondaryMenu(R.layout.right_frame);
		_sm.setSecondaryShadowDrawable(R.drawable.shadowright);

		transaction.replace(R.id.left_frame, new ServerList()); //LEFT
		transaction.replace(R.id.right_frame, new StationList()); //RIGHT
	}

	private OnKeyListener getKeyListener()
	{
		return new OnKeyListener()
		{

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
			{
				if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
				{
					if (ClientService.clientAPI != null)
					{
						_curVol--;
						if (_curVol < 0)
						{
							_curVol = 0;
						}
						setVolume(_curVol * 100d / MAX_VOLUME);
						if (_volumeSeekBar != null)
						{
							_volumeSeekBar.setProgress(_curVol);
							//						dialog.dismiss();
							//						showVolumeToaster(curVol);
						}
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
						_curVol++;
						if (_curVol > MAX_VOLUME)
						{
							_curVol = MAX_VOLUME;
						}
						setVolume(_curVol * 100d / MAX_VOLUME);
						if (_volumeSeekBar != null)
						{
							_volumeSeekBar.setProgress(_curVol);
							//						dialog.dismiss();
							//						showVolumeToaster(curVol);
						}
					}
					else
					{
						showNoConnectionAlert();
					}
					return true;
				}
				return false;
			}
		};

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
					{
						try
						{
							UpdaterService.update(ClientService.clientAPI.getCurrent());
						}
						catch (DisconnectException e)
						{
							ClientService.mainActivity.hideRightSide(true);
						}
					}
				}
			});
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setNotification(String title, String message, String station)
	{
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
			.setOngoing(true).setAutoCancel(false).setContentTitle(title).setContentText(station).setSubText(message);

		if (_bitmap != null)
		{
			mBuilder.setLargeIcon(_bitmap);
		}
		Intent resultIntent = new Intent(this, MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		mNotificationManager.notify(notificationId, mBuilder.build());
	}

	public void hideRightSide(boolean b)
	{
		if (_sm != null)
		{
			if (b)
				_sm.setMode(SlidingMenu.LEFT);
			else
				_sm.setMode(SlidingMenu.LEFT_RIGHT);
		}
		if (menu != null)
		{
			menu.findItem(R.id.action_settings).setVisible(!b);
			menu.findItem(R.id.action_stations).setVisible(!b);
		}
	}
}