package com.mwent.raspberryradio;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ServerList extends ListFragment
{

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity());
		adapter.add(new ServerItem("192.168.0.100", R.drawable.ic_action_bookmark));
		adapter.add(new ServerItem("94.208.147.102", R.drawable.ic_action_bookmark));
		setListAdapter(adapter);
	}

	private class ServerItem
	{
		public String serverName;
		public int serverIcon;

		public ServerItem(String serverName, int serverIcon)
		{
			this.serverName = serverName;
			this.serverIcon = serverIcon;
		}
	}

	public class SampleAdapter extends ArrayAdapter<ServerItem>
	{

		public SampleAdapter(Context context)
		{
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			ImageView icon = (ImageView)convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).serverIcon);
			TextView title = (TextView)convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).serverName);

			return convertView;
		}

	}
}