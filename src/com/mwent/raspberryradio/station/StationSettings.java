package com.mwent.raspberryradio.station;

import com.mwent.raspberryradio.R;

public class StationSettings
{
	public static final StationSettings NEW_STATION = new StationSettings(
		-1,
		"New Station",
		"",
		-1,
		';',
		R.drawable.ic_action_add,
		false);

	public static final StationSettings NEW_STATION_BY_QR = new StationSettings(
		-1,
		"New Station using QR-code",
		"",
		-1,
		';',
		R.drawable.ic_action_add,
		false);

	private int _id;

	private String _host;
	private String _name;

	private int _pos;

	private final char _DELIM;

	private final int _image;

	private boolean _writable;

	public StationSettings(int id, String name, String ip, int pos)
	{
		this(id, name, ip, pos, StationList.DELIM);
	}

	public StationSettings(int id, String name, String ip, int pos, char delim)
	{
		this(id, name, ip, pos, delim, R.drawable.ic_action_record, true);
	}

	public StationSettings(int id, String name, String ip, int pos, char delim, int icon, boolean writable)
	{
		_id = id;
		_name = name;
		_host = ip;
		_pos = pos;
		_DELIM = delim;
		_image = icon;
		setWritable(writable);
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public String getIp()
	{
		return _host;
	}

	public int getPos()
	{
		return _pos;
	}

	public int getImage()
	{
		return _image;
	}

	public boolean isWritable()
	{
		return _writable;
	}

	public void setId(int id)
	{
		_id = id;
	}

	public void setName(String name)
	{
		this._name = name.trim();
	}

	public void setIp(String ip)
	{
		this._host = ip;
	}

	public void setWritable(boolean writable)
	{
		_writable = writable;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(_id);
		sb.append(_DELIM);
		sb.append(_name.trim());
		sb.append(_DELIM);
		sb.append(_host.trim());
		if (isWritable())
			return sb.toString();
		return "";
	}

	@Override
	public boolean equals(Object obj)
	{
		StationSettings other = (StationSettings)obj;
		return this.getId() == other.getId();
	}

	public class StationSettingsException extends Exception
	{
		private static final long serialVersionUID = -2974979154669045853L;

		public StationSettingsException(String message)
		{
			super(message);
		}
	}
}
