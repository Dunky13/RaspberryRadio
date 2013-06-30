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
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mwent.RaspberryRadio.client.AndroidClient;
import com.mwent.raspberryradio.ServerSettings.ServerSettingsException;

public class ServerList extends ListFragment implements OnClickListener
{
	private static final String _serverFileName = "servers";
	private static final char DELIM = ';';
	private List<ServerSettings> servers;
	private ServerSettingsAdapter adapter;
	private ServerList _this;
	private AndroidClient clientAPI;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this._this = this;
		adapter = new ServerSettingsAdapter(getActivity());
		read();
		servers.add(new ServerSettings(
				"Test server 1",
				"192.168.1.89",
				6584,
				"root",
				"Admin",
				';',
				R.drawable.ic_action_bookmark,
				true));
		loadServersList();

		setListAdapter(adapter);
	}

	public void add(ServerSettings setting)
	{
		if (!servers.contains(setting))
		{
			servers.add(setting);
			loadServersList();
		}
	}

	public void loadServersList()
	{
		adapter.clear();
		adapter.addAll(servers);

		adapter.add(ServerSettings.NEW_SERVER);
	}

	public class ServerSettingsAdapter extends ArrayAdapter<ServerSettings>
	{

		public ServerSettingsAdapter(Context context)
		{
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			//			ImageView icon = (ImageView)convertView.findViewById(R.id.row_icon);
			//			icon.setImageResource(getItem(position).getImage());

			TextView title = (TextView)convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).getName());
			title.setCompoundDrawablesWithIntrinsicBounds(getItem(position).getImage(), 0, 0, 0);

			convertView.setTag(getItem(position));
			convertView.setOnClickListener(_this);

			return convertView;
		}
	}

	private List<ServerSettings> read()
	{
		servers = new ArrayList<ServerSettings>();
		try
		{
			FileInputStream fis = getActivity().openFileInput(_serverFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				if (!line.trim().isEmpty())
					servers.add(new ServerSettings(line, DELIM));
			}
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ServerSettingsException e)
		{
			e.printStackTrace();
		}
		return servers;
	}

	@SuppressWarnings("unused")
	private void write()
	{
		try
		{
			FileOutputStream fos = getActivity().openFileOutput(_serverFileName, Context.MODE_PRIVATE);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (ServerSettings server : servers)
			{
				if (server.isWritable())
				{
					bw.write(server.toString());
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
	public void onClick(View v) {
		ServerSettings settings = (ServerSettings)v.getTag();
		if (settings.equals(ServerSettings.NEW_SERVER)) {
			startActivity(new Intent(getActivity(), SettingsActivity.class));
		} else {
			//TODO: Open connection with server
			Log.d("Setting: ", settings.toString());
			clientAPI = new AndroidClient(settings.getIp(), settings.getPort());
			clientAPI.connect(settings.getUsername(), settings.getPassword());
			clientAPI.play();
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                                                  
			clientAPI.stop();
			clientAPI.disconnect();
		}
	}
}