package org.brian.blueirisviewer;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlueIrisViewerSettings
{
	public Vector<String> cameraNames = new Vector<String>();
	public Vector<String> imageURLs = new Vector<String>();
	public Vector<Integer> sleepDelays = new Vector<Integer>();
	public Vector<Integer> rotateDegrees = new Vector<Integer>();

	private boolean allCams = true;
	private String serverURL = "http://localhost:80/";
	private String overrideGridLayout = "0x0";
	public boolean loadStartPositionAndSizeUponAppStart = true;
	public boolean preserveSizeWhenDragging = false;
	public boolean disableWindowDragging = false;
	public int overrideGridLayoutX = 0;
	public int overrideGridLayoutY = 0;
	public int startPositionX = -1;
	public int startPositionY = -1;
	public int startSizeW = 1280;
	public int startSizeH = 720;

	public BlueIrisViewerSettings()
	{
		String settingsFile = null;
		try
		{
			settingsFile = Utilities.ReadTextFile("BIV_Config.txt");
		}
		catch (Exception e)
		{
		}
		if (settingsFile == null || settingsFile.equals(""))
		{
			//InitializeAllCams(jpegPullPage);
			Save();
			System.exit(0);
		}
		else
		{
			String[] lines = settingsFile.split("\\r\\n|\\r|\\n");

			int startIndexForCameraList = 3;
			
			if (lines.length > 0 && lines[0].startsWith("allcams=false"))
				allCams = false;

			if (lines.length > 1 && lines[1].startsWith("BlueIrisServerURL="))
				serverURL = lines[1].substring("BlueIrisServerURL=".length());
			
			if (lines.length > 2 && lines[2].startsWith("overrideGridLayout="))
			{
				overrideGridLayout = lines[2].substring("overrideGridLayout=".length());
				ParseOverrideGridLayout();
				startIndexForCameraList = 4;
			}
			
			if (lines.length > 3 && lines[3].startsWith("startPositionAndSize="))
			{
				String startPositionAndSize = lines[3].substring("startPositionAndSize=".length());
				ParseStartPositionAndSize(startPositionAndSize);
				startIndexForCameraList = 5;
			}
			if (lines.length > 4 && lines[4].startsWith("loadStartPositionAndSizeUponAppStart="))
			{
				String sLoadStartPositionAndSizeUponAppStart = lines[4].substring("loadStartPositionAndSizeUponAppStart=".length());
				loadStartPositionAndSizeUponAppStart = sLoadStartPositionAndSizeUponAppStart.equals("true") || sLoadStartPositionAndSizeUponAppStart.equals("1");
				startIndexForCameraList = 6;
			}
			if (lines.length > 5 && lines[5].startsWith("preserveSizeWhenDragging="))
			{
				String sPreserveSizeWhenDragging = lines[5].substring("preserveSizeWhenDragging=".length());
				preserveSizeWhenDragging = sPreserveSizeWhenDragging.equals("true") || sPreserveSizeWhenDragging.equals("1");
				startIndexForCameraList = 7;
			}
			if (lines.length > 6 && lines[6].startsWith("disableWindowDragging="))
			{
				String sDisableWindowDragging = lines[6].substring("disableWindowDragging=".length());
				disableWindowDragging = sDisableWindowDragging.equals("true") || sDisableWindowDragging.equals("1");
				startIndexForCameraList = 8;
			}

			String jpegPullPage = Utilities
					.getStringViaHttpConnection(serverURL + "jpegpull.htm");

			if(jpegPullPage == null || jpegPullPage.equals(""))
				System.exit(0);
			
			if (lines.length <= startIndexForCameraList || allCams)
			{
				
				InitializeAllCams(jpegPullPage);
				
				if(lines.length <= startIndexForCameraList)
					Save();
			}
			else
			{
				for (int i = startIndexForCameraList; i < lines.length; i++)
				{
					String name = lines[i];
					i++;
					int delay = 250;
					int rotateDegrees = 0;
					if (i < lines.length)
					{
						String[] parts = lines[i].split(",");
						String sDelay = parts.length > 1 ? parts[0] : lines[i];
						String sRotate = parts.length > 1 ? parts[1] : "0";
						delay = Utilities.ParseInt(sDelay, 250);
						rotateDegrees = Utilities.ParseInt(sRotate, 0);
					}
					Add(name, delay, rotateDegrees);
				}
			}
		}
	}

	private void ParseStartPositionAndSize(String startPositionAndSize)
	{
		String[] parts = startPositionAndSize.split("x");
		if(parts.length == 4)
		{
			startPositionX = Utilities.ParseInt(parts[0], 0);
			startPositionY = Utilities.ParseInt(parts[1], 0);
			startSizeW = Utilities.ParseInt(parts[2], 1280);
			startSizeH = Utilities.ParseInt(parts[3], 720);
		}
	}

	private void ParseOverrideGridLayout()
	{
		overrideGridLayoutX = overrideGridLayoutY = 0;
		String[] parts = overrideGridLayout.split("x");
		if(parts.length == 2)
		{
			overrideGridLayoutX = Utilities.ParseInt(parts[0], 0);
			overrideGridLayoutY = Utilities.ParseInt(parts[1], 0);
			if(overrideGridLayoutX == 0 || overrideGridLayoutY == 0)
				overrideGridLayoutX = overrideGridLayoutY = 0;
		}
	}

	Pattern cameraPattern = Pattern.compile("<option\\W+value=\"([^\"]*?)\">([^<]*?)</option>");

	private void InitializeAllCams(String jpegPullPage)
	{
		if(jpegPullPage == null || jpegPullPage.equals(""))
			return;
		Matcher m = cameraPattern.matcher(jpegPullPage);
		while (m.find())
		{
			String data = m.group(1);
			String[] parts = data.split(";");
			if (parts.length > 3)
			{
				int width = Utilities.ParseInt(parts[1], 2500);
				int height = Utilities.ParseInt(parts[2], 1000);
				String[] urlParts = parts[3].split("/");
				if (urlParts.length > 0)
				{
					String name = urlParts[urlParts.length - 1];
					int delay = (width * height) / 10000;
					if(delay < 250)
						delay = 250;
					if(!m.group(2).startsWith("+") && !name.equals("index"))
						Add(name, delay, 0);
				}
			}
		}
	}

	private void Add(String name, int delay, int degreesRotate)
	{
		cameraNames.add(name);
		imageURLs.add(serverURL + "image/" + name);
		sleepDelays.add(delay);
		rotateDegrees.add(degreesRotate);
	}

	public void Save()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("allcams=" + (allCams ? "true" : "false"));
		sb.append("\r\n");
		sb.append("BlueIrisServerURL=" + serverURL);
		sb.append("\r\n");
		sb.append("overrideGridLayout=" + overrideGridLayout);
		sb.append("\r\n");
		sb.append("startPositionAndSize=" + startPositionX + "x" + startPositionY + "x" + startSizeW + "x" + startSizeH);
		sb.append("\r\n");
		sb.append("loadStartPositionAndSizeUponAppStart=" + (loadStartPositionAndSizeUponAppStart ? "true" : "false"));
		sb.append("\r\n");
		sb.append("preserveSizeWhenDragging=" + (preserveSizeWhenDragging ? "true" : "false"));
		sb.append("\r\n");
		sb.append("disableWindowDragging=" + (disableWindowDragging ? "true" : "false"));
		sb.append("\r\n");
		sb.append("# The format is: Blue Iris camera name, line break, refresh delay in milliseconds, comma, number of degrees to rotate image, line break, repeat.  To automatically populate this list, delete all but the first two lines of this file, be sure the BlueIrisServerURL is correct, and start the application again.  This file will be updated with a new camera list.");
		sb.append("\r\n");
		for (int i = 0; i < cameraNames.size(); i++)
		{
			sb.append(cameraNames.get(i));
			sb.append("\r\n");
			if (i < sleepDelays.size())
				sb.append(sleepDelays.get(i));
			else
				sb.append(250);
			sb.append(',');
			if (i < rotateDegrees.size())
				sb.append(rotateDegrees.get(i));
			else
				sb.append(0);
			sb.append("\r\n");
		}
		try
		{
			Utilities.WriteFile("BIV_Config.txt", sb.toString().getBytes());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
