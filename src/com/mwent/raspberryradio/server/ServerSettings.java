package com.mwent.raspberryradio.server;

import com.mwent.raspberryradio.R;

public class ServerSettings
{
	public static final int DEFAULT_PORT = 6584;
	public static final ServerSettings NEW_SERVER = new ServerSettings(
		-1,
		"New Server",
		"",
		DEFAULT_PORT,
		"",
		"",
		';',
		R.drawable.ic_action_add,
		false);

	private int _id;

	private String _ip;
	private int _port;

	private String _username;
	private String _password;

	private String _name;

	private final char _DELIM;

	private final int _image;

	private boolean _writable;

	public ServerSettings(String line, char delim) throws ServerSettingsException
	{
		/*
		 * 0 = Id
		 * 1 = Name
		 * 2 = Host
		 * 3 = Port
		 * 4 = User
		 * 5 = Pass
		 */
		_DELIM = delim;
		String[] split = line.split(_DELIM + "");
		if (split.length < 6)
			throw new ServerSettingsException("Too little arguments for the line: " + line);
		if (split.length > 6)
			throw new ServerSettingsException("Too much arguments for the line: " + line);

		setName(split[1]);
		_ip = split[2];
		setUsername(split[4]);
		setPassword(split[5]);

		try
		{
			_port = Integer.parseInt(split[2]);
		}
		catch (NumberFormatException e)
		{
			_port = DEFAULT_PORT;
		}

		try
		{
			_id = Integer.parseInt(split[0]);
		}
		catch (NumberFormatException e)
		{
			_id = -1;
		}

		_image = R.drawable.ic_action_bookmark;
		setWritable(true);
	}

	public ServerSettings(int id, String name, String ip, int port, String username, String password, char delim)
	{
		_id = id;
		_name = name;
		_ip = ip;
		_port = port;
		_username = username;
		_password = password;
		_DELIM = delim;

		_image = R.drawable.ic_action_bookmark;
		setWritable(true);
	}

	public ServerSettings(
		int id,
		String name,
		String ip,
		int port,
		String username,
		String password,
		char delim,
		int icon,
		boolean writable)
	{
		_id = id;
		_name = name;
		_ip = ip;
		_port = port;
		_username = username;
		_password = password;
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
		return _ip;
	}

	public int getPort()
	{
		return _port;
	}

	public String getUsername()
	{
		return _username;
	}

	public String getPassword()
	{
		return _password;
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
		this._ip = ip;
	}

	public void setPort(int port)
	{
		this._port = port;
	}

	public void setUsername(String username)
	{
		this._username = username.trim();
	}

	public void setPassword(String password)
	{
		this._password = password.trim();
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
		sb.append(_ip.trim());
		sb.append(_DELIM);
		sb.append(_port);
		sb.append(_DELIM);
		sb.append(_username.trim());
		sb.append(_DELIM);
		sb.append(_password);
		if (isWritable())
			return sb.toString();
		return "";
	}

	@Override
	public boolean equals(Object obj)
	{
		ServerSettings other = (ServerSettings)obj;
		return this.getId() == other.getId();
	}

	public class ServerSettingsException extends Exception
	{
		private static final long serialVersionUID = -2974979154669045853L;

		public ServerSettingsException(String message)
		{
			super(message);
		}
	}
}
