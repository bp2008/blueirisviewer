package org.brian.blueirisviewer.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

public class PostData
{
	private byte[] data;
	private String contentType;

	public PostData(HashMap<String, String> postDataMap) throws UnsupportedEncodingException
	{
		ArrayList<String> args = new ArrayList<String>();
		for (Entry<String, String> entry : postDataMap.entrySet())
			args.add(Utilities.UrlEncode(entry.getKey()) + "=" + Utilities.UrlEncode(entry.getValue()));
		String sPostData = string.JoinStringArrayList("&", args);
		this.data = sPostData.getBytes("UTF-8");
		this.contentType = "application/x-www-form-urlencoded";
	}

	public PostData(byte[] data, String contentType)
	{
		this.data = data;
		this.contentType = contentType;
	}

	public PostData(String data, String contentType)
	{
		try
		{
			this.data = data.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.debug(e, this);
			this.data = new byte[0];
		}
		this.contentType = contentType;
	}
	public PostData(JSONObject jsonObject)
	{
		try
		{
			this.data = jsonObject.toJSONString().getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.debug(e, this);
			this.data = new byte[0];
		}
		this.contentType = "text/json";
	}

	public byte[] getData()
	{
		return data;
	}

	public String getContentType()
	{
		return contentType;
	}
}
