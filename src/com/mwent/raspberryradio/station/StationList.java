package com.mwent.raspberryradio.station;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import com.mwent.RaspberryRadio.client.AndroidClient;
import com.mwent.RaspberryRadio.shared.CommandStationList;
import com.mwent.raspberryradio.ClientService;
import com.mwent.raspberryradio.R;

public class StationList extends ListFragment implements OnClickListener
{
	private static final String _serverFileName = "servers";
	public static final char DELIM = ';';
	private List<StationSettings> _stations;
	private List<Integer> stationsIds;
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
		ClientService.stationList = this;

		adapter = new ServerSettingsAdapter(getActivity());
		read();
		loadStationList();

		setListAdapter(adapter);
	}

	public void add(StationSettings setting)
	{
		if (listHasSetting(_stations, setting) >= 0)
		{
			setting.setId(getID());
		}
		_stations.add(setting);
		loadStationList();
		//		write();
	}

	public void replace(StationSettings setting)
	{
		int pos = listHasSetting(_stations, setting);
		if (pos >= 0)
		{
			_stations.set(pos, setting);
			loadStationList();
			//			write();
			return;
		}
		add(setting);
	}

	public void remove(StationSettings setting)
	{
		int pos = listHasSetting(_stations, setting);
		if (pos >= 0)
		{
			stationsIds.remove((Object)pos);
			_stations.remove(pos);
			loadStationList();
			//			write();
			return;
		}

	}

	private int getID()
	{
		Collections.sort(stationsIds);
		return stationsIds.get(stationsIds.size() - 1) + 1;
	}

	private int listHasSetting(List<StationSettings> list, StationSettings setting)
	{
		StationSettings orig;
		for (int i = 0; i < _stations.size(); i++)
		{
			orig = _stations.get(i);
			if (orig.equals(setting))
			{
				return i;
			}
		}
		return -1;
	}

	public void firstLoadStationList()
	{
		read();
		loadStationList();
	}

	public void loadStationList()
	{
		adapter.clear();
		adapter.addAll(_stations);

		adapter.add(StationSettings.NEW_STATION);
	}

	public class ServerSettingsAdapter extends ArrayAdapter<StationSettings>
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

			Button title = (Button)convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).getName());
			title.setCompoundDrawablesWithIntrinsicBounds(getItem(position).getImage(), 0, 0, 0);

			convertView.setTag(getItem(position));
			convertView.setOnClickListener(ClientService.stationList);
			convertView.setOnLongClickListener(new OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					ClientService.stationSettings = (StationSettings)v.getTag();
					startActivity(new Intent(getActivity(), StationSettingsActivity.class));
					return false;
				}
			});

			return convertView;
		}
	}

	private void read()
	{

		_stations = new ArrayList<StationSettings>();
		stationsIds = new ArrayList<Integer>();
		if (ClientService.clientAPI == null)
			return;
		List<CommandStationList> stations = ClientService.clientAPI.listAll();

		for (CommandStationList station : stations)
		{
			StationSettings setting = new StationSettings(0, station.getName(), station.getHost(), station.getPos(), DELIM);
			if (stationsIds.contains(setting.getId()))
			{
				int last = getID();
				setting.setId(last);
				stationsIds.add(last);
			}
			else
			{
				stationsIds.add(setting.getId());
			}
			_stations.add(setting);
		}
	}

	public void write()
	{
		try
		{
			FileOutputStream fos = getActivity().openFileOutput(_serverFileName, Context.MODE_PRIVATE);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (StationSettings server : _stations)
			{
				//				Log.d(server.getName(), server.isWritable() + "");
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
		StationSettings settings = (StationSettings)v.getTag();

		if (settings == StationSettings.NEW_STATION)
		{
			ClientService.stationSettings = StationSettings.NEW_STATION;
			startActivity(new Intent(getActivity(), StationSettingsActivity.class));
		}
		else
		{
			ClientService.stationSettings = settings;

			if (ClientService.mainActivity != null)
			{
				ClientService.clientAPI.setStation(settings.getPos());
				ClientService.mainActivity.toggle();
				//ClientService.mainActivity.setSongText(ClientService.clientAPI.getUpdate());
			}

		}
	}
}