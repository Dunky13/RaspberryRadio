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
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends FragmentActivity
{

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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getActionBar().setDisplayHomeAsUpEnabled(true);

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

		serverMenu = new SlidingMenu(this);
		serverMenu.setMode(SlidingMenu.LEFT);
		serverMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		//		serverMenu.setBehind
		//		Display display = ((WindowManager)getBaseContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		//		Point size = new Point();
		//		display.getSize(size);
		//		int percentage = 42;
		//
		//		serverMenu.setBehindWidth((int)((percentage / 100d) * size.x));
		serverMenu.setBackgroundColor(Color.parseColor("#B5B5B5"));
		//		//		serverMenu.setShadowWidthRes(R.dimen.shadow_width);
		//		//		serverMenu.setShadowDrawable(R.drawable.shadow);
		//		//		serverMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		serverMenu.setFadeDegree(0.35f);
		serverMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		//		sa = new ServerActivity();
		//		sa.onCreate(savedInstanceState);
		//		sa.setServers(servers);
		//
		//		Intent intent = new Intent(this, SlidingTitleBar.class);
		//		//intent.putExtra("SERVERS", servers.toArray(new String[0]));
		//		startActivity(intent);

		serverMenu.setMenu(R.layout.content_frame);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;
		switch (item.getItemId())
		{
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			showServers();
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

	private void showServers()
	{

		serverMenu.toggle();
		for (String server : servers)
		{
			Log.v("Server: ", server);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
