package com.mwent.raspberryradio;

import java.net.URISyntaxException;
import java.net.URL;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
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

public class MainActivity extends SlidingFragmentActivity implements OnClickListener
{

	protected ListFragment mFrag;

	ImageButton buttonPrev, buttonStop, buttonPlay, buttonNext;
	ImageView albumImage;
	TextView songInfo;

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
		String s = getResources().getString(R.string.song_name);
		switch (v.getId())
		{
		case R.id.prev:
			s = ClientService.clientAPI.prev();
			break;
		case R.id.stop:
			ClientService.clientAPI.stop();
			break;
		case R.id.play:
			s = ClientService.clientAPI.play();
			break;
		case R.id.next:
			s = ClientService.clientAPI.next();
			break;
		}

		showAlbumImage();

		setSongText(s);
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

	private void showAlbumImage()
	{
		URL cover = ClientService.clientAPI.getAlbumCover();
		ImageView album = (ImageView)findViewById(R.id.album_image);
		if (cover != null)
		{
			try
			{
				album.setImageURI(Uri.parse(cover.toURI().toString()));
			}
			catch (URISyntaxException e)
			{
				album.setImageDrawable(getResources().getDrawable(R.drawable.default_album_image));
			}
		}
		else
			album.setImageDrawable(getResources().getDrawable(R.drawable.default_album_image));

	}
}
