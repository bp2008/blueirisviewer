package org.brian.blueirisviewer.util;

import java.util.Calendar;

import org.brian.blueirisviewer.BlueIrisViewer;

public class NightModeManager
{
	private boolean isNightModeNow = false;
	private boolean isFirstLoad = true;
	private long lastUpdateTime = 0;

	public NightModeManager()
	{
	}

	public void update()
	{
		long now = Utilities.getTimeInMs();
		if (Math.abs(now - lastUpdateTime) > 1000)
		{
			lastUpdateTime = now;
			RedetermineNightMode();
		}
	}

	public void RedetermineNightMode()
	{
		boolean shouldBeNightMode = false;
		if (BlueIrisViewer.bivSettings.nightModeEnabled)
		{
			long nightModeStartToday = getTimeInMsOfTimeStringToday(BlueIrisViewer.bivSettings.nightModeStartTime);
			long nightModeEndToday = getTimeInMsOfTimeStringToday(BlueIrisViewer.bivSettings.nightModeEndTime);
			if (nightModeStartToday == -1)
			{
				BlueIrisViewer.bivSettings.nightModeStartTime = "20:00";
				nightModeStartToday = getTimeInMsOfTimeStringToday(BlueIrisViewer.bivSettings.nightModeStartTime);
			}
			else if (nightModeEndToday == -1)
			{
				BlueIrisViewer.bivSettings.nightModeEndTime = "8:00";
				nightModeEndToday = getTimeInMsOfTimeStringToday(BlueIrisViewer.bivSettings.nightModeEndTime);
			}
			long now = Utilities.getTimeInMs();

			shouldBeNightMode = now >= nightModeStartToday;
			boolean shouldBeDayMode = now >= nightModeEndToday;
			if (shouldBeNightMode == shouldBeDayMode)
			{
				// Both trigger times have been passed (or neither have been passed, which for our purposes is the same
				// thing)
				if (nightModeStartToday <= nightModeEndToday)
				{
					shouldBeNightMode = false;
					shouldBeDayMode = true;
				}
				else
				{
					shouldBeDayMode = false;
					shouldBeNightMode = true;
				}
			}
			System.out.println("shouldBeNightMode=" + shouldBeNightMode);
		}
		if (shouldBeNightMode != isNightModeNow || isFirstLoad)
		{
			isFirstLoad = false;
			isNightModeNow = shouldBeNightMode;
			
			if (shouldBeNightMode)
				EnterNightMode();
			else
				EnterDayMode();
		}
	}

	private void EnterDayMode()
	{
		if (BlueIrisViewer.bivSettings.setDayModeBrightness)
			ScreenBrightness.SetBrightness(BlueIrisViewer.bivSettings.dayModeBrightness);
	}

	private void EnterNightMode()
	{
		if (BlueIrisViewer.bivSettings.setNightModeBrightness)
			ScreenBrightness.SetBrightness(BlueIrisViewer.bivSettings.nightModeBrightness);
	}

	public boolean isNightModeNow()
	{
		return isNightModeNow;
	}

	private static long getTimeInMsOfTimeStringToday(String str)
	{
		if (str.length() > 5)
			return -1;
		String[] parts = str.split(":", -1);
		if (parts.length == 2)
		{
			int firstNum = Utilities.ParseInt(parts[0], -1);
			int secondNum = Utilities.ParseInt(parts[1], -1);
			if (firstNum >= 0 && firstNum <= 23 && secondNum >= 0 && secondNum <= 59)
			{
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, firstNum);
				c.set(Calendar.MINUTE, secondNum);
				return c.getTimeInMillis();
			}
		}
		return -1;
	}

	public static boolean isValidTimeString(String str)
	{
		return getTimeInMsOfTimeStringToday(str) != -1;
	}

	// public void SanityCheck()
	// {
	// try
	// {
	// // NOW NIGHT DAY
	// Assert__IsDay(SanityCheckHelper("9:00", "10:00", "11:00"));
	// AssertIsNight(SanityCheckHelper("9:00", "11:00", "10:00"));
	//
	// Assert__IsDay(SanityCheckHelper("9:00", "6:00", "7:00"));
	// AssertIsNight(SanityCheckHelper("9:00", "7:00", "6:00"));
	//
	// AssertIsNight(SanityCheckHelper("9:00", "6:00", "11:00"));
	// Assert__IsDay(SanityCheckHelper("9:00", "11:00", "6:00"));
	// System.out.println("Success!");
	// }
	// catch (Exception ex)
	// {
	// System.out.println("Fail!");
	// }
	//
	// }
	//
	// public boolean SanityCheckHelper(String timeNow, String timeStart, String timeEnd)
	// {
	// long nightModeStartToday = getTimeInMsOfTimeStringToday(timeStart);
	// long nightModeEndToday = getTimeInMsOfTimeStringToday(timeEnd);
	// long now = getTimeInMsOfTimeStringToday(timeNow);
	//
	// boolean shouldBeNightMode = now >= nightModeStartToday;
	// boolean shouldBeDayMode = now >= nightModeEndToday;
	// if (shouldBeNightMode == shouldBeDayMode)
	// {
	// // Both trigger times have been passed (or neither have been passed, which for our purposes is the same
	// // thing)
	// if (nightModeStartToday <= nightModeEndToday)
	// {
	// shouldBeNightMode = false;
	// shouldBeDayMode = true;
	// }
	// else
	// {
	// shouldBeDayMode = false;
	// shouldBeNightMode = true;
	// }
	// }
	// return shouldBeNightMode;
	// }

	// public void Assert__IsDay(boolean isNight) throws Exception
	// {
	// if (isNight)
	// throw new Exception("Assertion failed");
	// }
	//
	// public void AssertIsNight(boolean isNight) throws Exception
	// {
	// if (!isNight)
	// throw new Exception("Assertion failed");
	// }
}
