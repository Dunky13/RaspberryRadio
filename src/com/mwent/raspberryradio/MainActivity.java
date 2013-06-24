package com.mwent.raspberryradio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setBehindContentView(R.layout.menu_frame_one); // left and right menu
	
		if (savedInstanceState == null)
		{
			FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
			mFrag = new ServerList();
			t.replace(R.id.menu_frame_one, new ServerList());
			t.commit();
		}
		else
		{
			mFrag = (ListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame_one);
		}

		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		servers = new ArrayList<String>();
		try
		{
			FileInputStream fis = openFileInput(_serverFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				if (!line.trim().isEmpty())
					servers.add(line);
			}
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		servers.add("192.168.0.100");
		servers.add("94.208.147.102");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private static final String _serverFileName = "servers";

	List<String> servers;
	SlidingMenu serverMenu;
	SlidingMenu stationMenu;

	@Override
	protected void onStop()
	{
		super.onStop();
		try
		{
			FileOutputStream fos = openFileOutput(_serverFileName, Context.MODE_PRIVATE);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (String server : servers)
			{
				if (!server.trim().isEmpty())
				{
					bw.write(server);
					bw.write("\r\n");
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;
		switch (item.getItemId())
		{
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			toggle();
			return true;
		case R.id.action_settings:
			intent = new Intent(this, SettingsActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.action_stations:

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
