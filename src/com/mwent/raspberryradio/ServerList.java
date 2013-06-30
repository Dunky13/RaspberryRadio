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
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.mwent.RaspberryRadio.client.AndroidClient;
import com.mwent.raspberryradio.ServerSettings.ServerSettingsException;

public class ServerList extends ListFragment implements OnClickListener
{
	private static final String _serverFileName = "servers";
	public static final char DELIM = ';';
	private List<ServerSettings> servers;
	private ServerSettingsAdapter adapter;
	public AndroidClient clientAPI;

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
		//		add(new ServerSettings("Radio GaGa", "192.168.43.103", 6584, "root", "Admin", ';', R.drawable.ic_action_bookmark, true));
		//		add(new ServerSettings("Radio Marc", "mwent.info", 6584, "root", "Admin", ';', R.drawable.ic_action_bookmark, true));
		loadServersList();

		setListAdapter(adapter);
	}

	public void add(ServerSettings setting)
	{
		if (!servers.contains(setting))
		{
			servers.add(setting);
			loadServersList();
			write();
		}
	}

	public void replace(ServerSettings setting)
	{
		ServerSettings orig;
		for (int i = 0; i < servers.size(); i++)
		{
			orig = servers.get(i);
			if (orig.equals(setting))
			{
				servers.set(i, setting);
				loadServersList();
				write();
				return;
			}
		}
		add(setting);
	}

	public void remove(ServerSettings settings)
	{
		ServerSettings orig;
		for (int i = 0; i < servers.size(); i++)
		{
			orig = servers.get(i);
			if (orig.equals(settings))
			{
				servers.remove(i);
				loadServersList();
				write();
				return;
			}
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
			convertView.setOnClickListener(ClientService.serverList);
			convertView.setOnLongClickListener(new OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					ClientService.settings = (ServerSettings)v.getTag();
					startActivity(new Intent(getActivity(), SettingsActivity.class));
					return false;
				}
			});

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

	public void write()
	{
		try
		{
			FileOutputStream fos = getActivity().openFileOutput(_serverFileName, Context.MODE_PRIVATE);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (ServerSettings server : servers)
			{
				Log.d(server.getName(), server.isWritable() + "");
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

	@Override
	public void onClick(View v)
	{
		ServerSettings settings = (ServerSettings)v.getTag();

		if (ClientService.clientAPI != null)
		{
			ClientService.settings = null;
			ClientService.clientAPI.disconnect();
		}

		if (settings == ServerSettings.NEW_SERVER)
		{
			startActivity(new Intent(getActivity(), SettingsActivity.class));
		}
		else
		{
			ClientService.settings = settings;
			ClientService.clientAPI = new AndroidClient(settings.getIp(), settings.getPort());
			try
			{
				ClientService.clientAPI.connect(settings.getUsername(), settings.getPassword());
			}
			catch (Exception e)
			{
				showLoginErrorAlert(e.getMessage());
				return;
			}

			if (ClientService.mainActivity != null)
			{
				ClientService.mainActivity.toggle();
				ClientService.mainActivity.setSongText(ClientService.clientAPI.getUpdate());
			}

		}
	}

	private void showLoginErrorAlert(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ClientService.mainActivity);
		alertDialogBuilder.setTitle("Connection did not succeed");
		alertDialogBuilder.setMessage(message).setCancelable(false).setPositiveButton("Ok", null);
		alertDialogBuilder.create().show();
	}
}