package com.mwent.raspberryradio.station;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import android.net.Uri;

public class StationURL
{

	public static StationSettings parse(Uri uri)
	{
		return parse(uri.toString());

	}

	public static StationSettings parse(String url)
	{
		url = url.replace("\"", "");
		String[] split = url.split("\\/\\?");
		try
		{
			url = split[0] + "/?" + URLEncoder.encode(split[1], "UTF-8");
		}
		catch (UnsupportedEncodingException e1)
		{
		}
		URI uri = null;
		try
		{
			uri = new URI(url);
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
			return null;
		}

		return parse(uri);
	}

	public static StationSettings parse(URI uri)
	{
		String[] queries = uri.getQuery().split("&");
		Map<String, String> queryMap = new HashMap<String, String>();
		for (String query : queries)
		{
			String[] partial = query.split("=", 2);
			if (partial.length == 2)
				queryMap.put(removeCrap(partial[0]), removeCrap(partial[1]));
		}
		String name = "";
		String ip = "";
		if (queryMap.containsKey("station"))
			ip = (String)queryMap.get("station");
		else
			return null;
		if (queryMap.containsKey("name"))
			name = (String)queryMap.get("name");
		else if (!ip.isEmpty())
			name = ip;
		else
			return null;
		return new StationSettings(StationSettings.NEW_STATION.getId(), name, ip, StationSettings.NEW_STATION.getPos());
	}

	private static String removeCrap(String string)
	{
		StringBuilder sb = new StringBuilder(string);
		if (sb.charAt(0) == '\'')
			sb.deleteCharAt(0);
		if (sb.charAt(sb.length() - 1) == '\'')
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
