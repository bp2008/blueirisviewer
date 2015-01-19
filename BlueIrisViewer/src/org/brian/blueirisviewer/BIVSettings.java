package org.brian.blueirisviewer;

import java.util.ArrayList;

import org.brian.blueirisviewer.util.SerializableObjectBase;

public class BIVSettings extends SerializableObjectBase
{
	public String serverURL = "http://127.0.0.1:80/";
	public boolean loadStartPositionAndSizeUponAppStart = true;
	public boolean disableWindowDragging = false;
	public int overrideGridLayoutX = 0;
	public int overrideGridLayoutY = 0;
	public int startPositionX = -1;
	public int startPositionY = -1;
	public int startSizeW = 1280;
	public int startSizeH = 720;
	public boolean borderless = false;
	public boolean restartBorderlessToggle = false;
	public int imageRefreshDelayMS = 250;
	public boolean bOverrideGridLayout = false;
	public int imageFillMode = 0; // 0: "Preserve Aspect Ratio", 1: "Stretch to Fill"
	public boolean modalUI = true;
	public String username = "";
	public String password = "";
	public int imageResolutionMode = 2;
	public boolean overrideJpegQuality = false;
	public int jpegQuality = 60;
	public boolean instantReplayEnabled = false;
	public int instantReplayHistoryLengthMinutes = 5;
	public ArrayList<String> hiddenCams = new ArrayList<String>();
	public boolean logErrorsToDisk = true;
	public String windowTitle = "BlueIrisViewer";
	
	public String[] getHiddenCamsStringArray()
	{
		String[] strs = new String[hiddenCams.size()];
		for(int i = 0; i < strs.length; i++)
			strs[i] = hiddenCams.get(i);
		return strs;
	}
}
