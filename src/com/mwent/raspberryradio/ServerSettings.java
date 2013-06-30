package com.mwent.raspberryradio;

public class ServerSettings
{
	public static final int DEFAULT_PORT = 6584;
	public static final ServerSettings NEW_SERVER = new ServerSettings(
		"New Server",
		"",
		DEFAULT_PORT,
		"",
		"",
		';',
		R.drawable.ic_action_add,
		false);

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
		 * 0 = Name
		 * 1 = Host
		 * 2 = Port
		 * 3 = User
		 * 4 = Pass
		 */
		_DELIM = delim;
		String[] split = line.split(_DELIM + "");
		if (split.length < 5)
			throw new ServerSettingsException("Too little arguments for the line: " + line);
		if (split.length > 5)
			throw new ServerSettingsException("Too much arguments for the line: " + line);
		setName(split[0]);
		_ip = split[1];
		_username = split[3];
		_password = split[4];

		try
		{
			_port = Integer.parseInt(split[2]);
		}
		catch (NumberFormatException e)
		{
			_port = DEFAULT_PORT;
		}

		_image = R.drawable.ic_action_bookmark;
		setWritable(true);
	}

	public ServerSettings(String name, String ip, int port, String username, String password, char delim)
	{
		_name = name;
		_ip = ip;
		_port = port;
		_username = username;
		_password = password;
		_DELIM = delim;

		_image = R.drawable.ic_action_bookmark;
		setWritable(true);
	}

	public ServerSettings(String name, String ip, int port, String username, String password, char delim, int icon, boolean writable)
	{
		_name = name;
		_ip = ip;
		_port = port;
		_username = username;
		_password = password;
		_DELIM = delim;
		_image = icon;
		setWritable(writable);
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

	public void setName(String name)
	{
		this._name = name;
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
		this._username = username;
	}

	public void setPassword(String password)
	{
		this._password = password;
	}

	public void setWritable(boolean writable)
	{
		_writable = writable;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(_name);
		sb.append(_DELIM);
		sb.append(_ip);
		sb.append(_DELIM);
		sb.append(_port);
		sb.append(_DELIM);
		sb.append(_username);
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
		return this.getName().equals(other.getName());
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
