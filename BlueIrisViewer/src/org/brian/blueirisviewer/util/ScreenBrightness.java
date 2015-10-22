package org.brian.blueirisviewer.util;

import java.io.IOException;

import org.brian.blueirisviewer.BlueIrisViewer;

public class ScreenBrightness
{
	public static void SetBrightness(int brightnessLevel)
	{
		if (string.IsNullOrWhitespace(BlueIrisViewer.sScreenBrightnessProgramPath))
			return;
		if (brightnessLevel < 0)
			brightnessLevel = 0;
		else if (brightnessLevel > 100)
			brightnessLevel = 100;
		try
		{
			Runtime.getRuntime().exec(BlueIrisViewer.sScreenBrightnessProgramPath + " " + brightnessLevel);
		}
		catch (IOException ex)
		{
			Logger.debug(ex, ScreenBrightness.class);
		}
	}
}
