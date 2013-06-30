package com.mwent.raspberryradio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity
{

	protected ListFragment mFrag;

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
			intent = new Intent(this, SettingsActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.action_stations:
			getSlidingMenu().showSecondaryMenu(true);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
