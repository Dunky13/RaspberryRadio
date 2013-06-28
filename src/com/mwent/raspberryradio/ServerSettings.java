package com.mwent.raspberryradio;

public class ServerSettings
{
	private String ip;
	private int port;

	private String username;
	private String password;

	private String name;

	private final char DELIM;

	public ServerSettings(String line, char delim) throws ServerSettingsException
	{
		DELIM = delim;
		String[] split = line.split(DELIM + "");
		if (split.length < 5)
			throw new ServerSettingsException("Too little arguments for the line: " + line);
		if (split.length > 5)
			throw new ServerSettingsException("Too much arguments for the line: " + line);
		setName(split[0]);
		ip = split[1];
		username = split[2];
		password = split[3];

		try
		{
			port = Integer.parseInt(split[4]);
		}
		catch (NumberFormatException e)
		{
			port = 6584;
		}
	}

	public String getName()
	{
		return name;
	}

	public String getIp()
	{
		return ip;
	}

	public int getPort()
	{
		return port;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(DELIM);
		sb.append(ip);
		sb.append(DELIM);
		sb.append(port);
		sb.append(DELIM);
		sb.append(username);
		sb.append(DELIM);
		sb.append(password);
		return "";
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
