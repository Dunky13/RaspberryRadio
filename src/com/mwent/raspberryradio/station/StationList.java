package com.mwent.raspberryradio.station;

import info.mwent.RaspberryRadio.shared.CommandStationList;
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
import com.mwent.raspberryradio.ClientService;
import com.mwent.raspberryradio.R;
import com.mwent.raspberryradio.UpdaterService;

public class StationList extends ListFragment implements OnClickListener, OnLongClickListener
{
	public static final char DELIM = ';';
	private List<StationSettings> _stations;
	private List<Integer> stationsIds;
	private StationSettingsAdapter adapter;

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

		adapter = new StationSettingsAdapter(getActivity());
		read();
		loadStationList();

		setListAdapter(adapter);
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
			processStationClick(settings);

		}
	}

	public boolean onLongClick(View v)
	{
		if (v.getId() != R.id.list_item)
		{
			firstLoadStationList();
		}
		return false;
	}

	public void add(StationSettings setting)
	{
		setting.setId(getID());
		_stations.add(setting);
		ClientService.clientAPI.add(setting.getIp());
		firstLoadStationList();
		//		write();
	}

	public void remove(StationSettings setting)
	{

		int pos = listHasSetting(_stations, setting);
		if (pos >= 0)
		{
			stationsIds.remove((Object)pos);
			_stations.remove(pos);
			ClientService.clientAPI.removeStation(setting.getPos());
			firstLoadStationList();
			return;
		}

	}

	public void firstLoadStationList()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				read();
				ClientService.mainActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						loadStationList();
					}
				});

			}
		}).start();
	}

	private void loadStationList()
	{

		adapter.clear();
		if (_stations != null)
			adapter.addAll(_stations);

		adapter.add(StationSettings.NEW_STATION);
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

	private void read()
	{

		_stations = new ArrayList<StationSettings>();
		stationsIds = new ArrayList<Integer>();
		if (ClientService.clientAPI == null)
			return;
		List<CommandStationList> stations = ClientService.clientAPI.getListAll();

		for (CommandStationList station : stations)
		{
			String name = station.getName().trim().isEmpty() ? station.getHost() : station.getName();
			StationSettings setting = new StationSettings(0, name, station.getHost(), station.getPos(), DELIM);
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

	private void processStationClick(StationSettings settings)
	{
		ClientService.stationSettings = settings;
		//		settings.

		if (ClientService.mainActivity != null)
		{
			ClientService.mainActivity.toggle();
			UpdaterService.update(ClientService.clientAPI.setStation(settings.getPos()));
		}
	}

	public class StationSettingsAdapter extends ArrayAdapter<StationSettings>
	{

		public StationSettingsAdapter(Context context)
		{
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}

			Button title = (Button)convertView.findViewById(R.id.list_item);
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

}