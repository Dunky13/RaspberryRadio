package com.mwent.raspberryradio;

import android.os.Bundle;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class TestSlider extends MainActivity
{
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		//		setBehindContentView(R.id.menu_frame_one);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame_one, new ServerList()).commit();

		getSlidingMenu().setSecondaryMenu(R.layout.menu_frame_two);
		getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame_two, new SampleListFragment()).commit();

	}
}