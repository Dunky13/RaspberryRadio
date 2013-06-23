package com.mwent.raspberryradio;

import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ServerActivity extends BaseActivity
{

	public ServerActivity()
	{
		super(R.string.app_name);
	}

	List<String> listItems = new ArrayList<String>();

	ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_frame_one);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame_one, new SampleListFragment()).commit();

		setSlidingActionBarEnabled(true);

	}

	//	public void setServers(String[] servers)
	//	{
	//		for (String server : servers)
	//		{
	//			listItems.add(server);
	//		}
	//		adapter.notifyDataSetChanged();
	//	}
}
