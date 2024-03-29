package com.mwent.raspberryradio.server;

import info.mwent.RaspberryRadio.AndroidAPI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.mwent.raspberryradio.ClientService;
import com.mwent.raspberryradio.R;
import com.mwent.raspberryradio.UpdaterService;
import com.mwent.raspberryradio.server.ServerSettings.ServerSettingsException;

public class ServerList extends ListFragment implements OnClickListener
{
	private static final String _serverFileName = "servers";
	private static final int _timeOut = 10 * 1000; //10 seconds
	public static final char DELIM = ';';
	private List<ServerSettings> servers;
	private List<Integer> lastServerId;
	private ServerSettingsAdapter adapter;
	//	public AndroidClient clientAPI;

	ImageView albumImage;
	TextView songInfo;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		ClientService.serverList = this;

		adapter = new ServerSettingsAdapter(getActivity());
		read();
		loadServersList();

		setListAdapter(adapter);
	}

	@Override
	public void onClick(View v)
	{
		ServerSettings settings = (ServerSettings)v.getTag();

		if (ClientService.clientAPI != null)
		{
			ClientService.serverSettings = null;
			ClientService.connectedServer = null;
			ClientService.clientAPI.disconnect();
			ClientService.clientAPI = null;
		}

		if (settings == ServerSettings.NEW_SERVER)
		{
			ClientService.serverSettings = ServerSettings.NEW_SERVER;
			Intent intent = new Intent(getActivity(), ServerSettingsActivity.class);
			intent.putExtra("type", "settings");
			intent.putExtra("delete", true);
			startActivity(intent);
		}
		else
		{
			processServerClick(settings);
		}
	}

	public void add(ServerSettings setting)
	{
		if (listHasSetting(servers, setting) >= 0)
		{
			setting.setId(getID());
		}
		servers.add(setting);
		loadServersList();
		write();
	}

	public void replace(ServerSettings setting)
	{
		int pos = listHasSetting(servers, setting);
		if (pos >= 0)
		{
			servers.set(pos, setting);
			loadServersList();
			write();
			return;
		}
		add(setting);
	}

	public void remove(ServerSettings setting)
	{
		int pos = listHasSetting(servers, setting);
		if (pos >= 0)
		{
			lastServerId.remove((Object)pos);
			servers.remove(pos);
			loadServersList();
			write();
			return;
		}
	}

	private void loadServersList()
	{
		adapter.clear();
		adapter.addAll(servers);
		adapter.add(ServerSettings.NEW_SERVER);
	}

	private int getID()
	{
		Collections.sort(lastServerId);
		return lastServerId.get(lastServerId.size() - 1) + 1;
	}

	private int listHasSetting(List<ServerSettings> list, ServerSettings setting)
	{
		ServerSettings orig;
		for (int i = 0; i < servers.size(); i++)
		{
			orig = servers.get(i);
			if (orig.equals(setting))
			{
				return i;
			}
		}
		return -1;
	}

	private void read()
	{

		servers = new ArrayList<ServerSettings>();
		lastServerId = new ArrayList<Integer>();
		try
		{
			FileInputStream fis = getActivity().openFileInput(_serverFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				if (!line.trim().isEmpty())
				{
					ServerSettings setting = new ServerSettings(line, DELIM);
					if (lastServerId.contains(setting.getId()))
					{
						int last = getID();
						setting.setId(last);
						lastServerId.add(last);
					}
					else
					{
						lastServerId.add(setting.getId());
					}
					servers.add(setting);
				}
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
	}

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
			bw.close();
			fos.close();
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

	private void processServerClick(final ServerSettings settings)
	{
		ClientService.serverSettings = settings;
		ClientService.clientAPI = new AndroidAPI(settings.getIp(), settings.getPort());
		try
		{
			ClientService.clientAPI.connect(settings.getUsername(), settings.getPassword(), _timeOut);
		}
		catch (Exception e)
		{
			showLoginErrorAlert(e.getMessage());
			return;
		}
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				//TODO: nullpointer exception.
				ClientService.clientAPI.enableAlbumCovers();
				// TODO: Check if this code that was commented really makes it slower
				ClientService.stationList.firstLoadStationList();
				if (ClientService.mainActivity != null)
				{
					ClientService.mainActivity.setUpdaterTimer();
					ClientService.updaterService = new Intent(ClientService.mainActivity, UpdaterService.class);
					ClientService.mainActivity.startService(ClientService.updaterService); // Start updater service
					// show the settings menu button when connected to the server
				}
				// Start the updater
			}
		}).start();

		ClientService.connectedServer = settings;

		if (ClientService.mainActivity != null)
		{
			ClientService.mainActivity.hideRightSide(false);
			ClientService.mainActivity.toggle();
		}
	}

	private void showLoginErrorAlert(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ClientService.mainActivity);
		alertDialogBuilder.setTitle("Connection did not succeed");
		alertDialogBuilder.setMessage(message).setCancelable(false).setPositiveButton("Ok", null);
		alertDialogBuilder.create().show();
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

			Button title = (Button)convertView.findViewById(R.id.list_item);
			title.setText(getItem(position).getName());
			title.setCompoundDrawablesWithIntrinsicBounds(getItem(position).getImage(), 0, 0, 0);

			convertView.setTag(getItem(position));
			convertView.setOnClickListener(ClientService.serverList);
			convertView.setOnLongClickListener(new OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					ClientService.serverSettings = (ServerSettings)v.getTag();
					Intent intent = new Intent(getActivity(), ServerSettingsActivity.class);
					intent.putExtra("type", "settings");
					intent.putExtra("delete", true);
					startActivity(intent);
					return false;
				}
			});

			return convertView;
		}
	}
}