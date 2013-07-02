package com.mwent.raspberryradio;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.mwent.raspberryradio.server.ServerList;
import com.mwent.raspberryradio.server.ServerSettingsActivity;
import com.mwent.raspberryradio.station.StationList;
import de.umass.lastfm.Caller;

public class MainActivity extends SlidingFragmentActivity implements OnClickListener
{

	protected ListFragment mFrag;

	ImageButton buttonPrev, buttonStop, buttonPlay, buttonNext;
	ImageView albumImage;
	TextView songInfo;

	AudioManager am;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_main);
		setBehindContentView(R.layout.left_frame); // left and right menu

		setupPlaybackButtons();
		setVolume();

		super.startService(new Intent(this, ClientService.class)); // Start ClientAPI service

		ClientService.mainActivity = this;

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

	private void setVolume()
	{
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if (ClientService.clientAPI != null)
		{
			int maxV = am.getStreamMaxVolume(AudioManager.STREAM_RING);
			int curV = am.getStreamVolume(AudioManager.STREAM_RING);
			ClientService.clientAPI.volume(curV * 100 / maxV);
		}
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

	public void loadSliderStuff(FragmentTransaction transaction)
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		finish();
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
			//			startActivity(new Intent("com.mwent.raspberryradio.SETTINGS"));
			intent = new Intent(this, ServerSettingsActivity.class);
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
		//		String s = getResources().getString(R.string.song_name);
		switch (v.getId())
		{
		case R.id.prev:
			UpdaterService.prev();
			//			s = ClientService.clientAPI.prev();
			break;
		case R.id.stop:
			UpdaterService.stop();
			//			ClientService.clientAPI.stop();
			break;
		case R.id.play:
			UpdaterService.play();
			//			s = ClientService.clientAPI.play();
			break;
		case R.id.next:
			UpdaterService.next();
			//			s = ClientService.clientAPI.next();
			break;
		}

		//		showAlbumImage();
		//		setSongText(s);
	}

	public void setSongText(String s)
	{
		if (s != null)
		{
			TextView song = (TextView)findViewById(R.id.song_info);
			song.setText(s);
		}
	}

	private void showNoConnectionAlert()
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("You are not connected");
		alertDialogBuilder.setMessage("Please connect before using the buttons").setCancelable(false).setPositiveButton("Ok", null);
		alertDialogBuilder.create().show();
	}

	public void showAlbumImage()
	{
		ImageView album = (ImageView)findViewById(R.id.album_image);
		Caller.getInstance().setCache(null);
		String coverString = (ClientService.clientAPI.getAlbumCover());

		System.out.println(coverString);
		if (coverString == null || coverString.trim().isEmpty())
		{
			setDefaultAlbumImage(album);
			return;
		}
		setImage(coverString);
		//		Bitmap image = downloadImage(coverString);
		//		album.setImageBitmap(image);
		//
		//		URL cover = null;
		//		try
		//		{
		//			cover = new URL(coverString);
		//		}
		//		catch (MalformedURLException e1)
		//		{
		//			e1.printStackTrace();
		//		}
		//		if (cover != null)
		//		{
		//			try
		//			{
		//				album.setImageURI(Uri.parse(cover.toURI().toString()));
		//			}
		//			catch (URISyntaxException e)
		//			{
		//				setDefaultAlbumImage(album);
		//			}
		//		}
		//		else
		//		{
		//			setDefaultAlbumImage(album);
		//
		//		}
	}

	private void setDefaultAlbumImage(ImageView album)
	{
		album.setImageDrawable(getResources().getDrawable(R.drawable.default_album_image));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		int maxV = am.getStreamMaxVolume(AudioManager.STREAM_RING);
		int curV = am.getStreamVolume(AudioManager.STREAM_RING);
		Log.d("VOLUME", curV + "");
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			curV -= 1;
		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			curV += 1;
		}
		if (ClientService.clientAPI != null)
		{
			ClientService.clientAPI.volume(curV * 100 / maxV);
		}
		else
		{
			showNoConnectionAlert();
		}

		return super.onKeyDown(keyCode, event);
	}

	public void setUpdaterAlarm()
	{
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		PendingIntent intent = PendingIntent.getActivity(
			getApplicationContext(),
			-1,
			new Intent("com.mwent.raspberryradio.UPDATER"),
			PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setRepeating(AlarmManager.RTC, 0, 1000 * 30, intent); // every minute		
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
				runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{

						//						ViewGroup.MarginLayoutParams imageViewParams = new ViewGroup.MarginLayoutParams(
						//							ViewGroup.MarginLayoutParams.MATCH_PARENT,
						//							ViewGroup.MarginLayoutParams.FILL_PARENT);
						//						LayoutParams layout = new LayoutParams(imageViewParams);
						ImageView album = (ImageView)findViewById(R.id.album_image);
						album.setImageBitmap(bitmap);
						//						album.setLayoutParams(layout);
					}

				});
				return null;
			}

		};
		task.execute(url);
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
			// Could provide a more explicit error message for IOException or IllegalStateException
			getRequest.abort();
			Log.w("ImageDownloader", "Error while retrieving bitmap from " + url + " - " + e.toString());
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
					UpdaterService.update(ClientService.clientAPI.getUpdate());
				}
			});
		}
	}
}
