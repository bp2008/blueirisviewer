package org.brian.blueirisviewer;

/**
 * This class represents a time snapshot which updates only when the tick() function is called. This makes it ideal for
 * timing of game logic, since the time values will not update during rendering or anything.
 * 
 * @author 2012 Brian Pearce
 */
public class GameTime
{
	private static long startTime;
	private static long gameTime;
	private static long realTime;
	private static boolean isPaused;
	private static long lastTickTime;
	static
	{
		startTime = realTime = lastTickTime = Utilities.getTimeInMs();
		gameTime = 0;
		isPaused = false;
	}

	/**
	 * Call this when the game gets paused. Game time will stop increasing.
	 */
	public static void pause()
	{
		isPaused = true;
	}

	/**
	 * Call this when the game gets unpaused. Game time will resume increasing.
	 */
	public static void unpause()
	{
		isPaused = false;
	}

	/**
	 * Call this one time at the beginning of each main loop iteration.
	 */
	public static void tick()
	{
		if (isPaused)
			return;
		realTime = Utilities.getTimeInMs();
		gameTime += realTime - lastTickTime;
		lastTickTime = realTime;
	}

	/**
	 * Returns the current time in the game world (in milliseconds). This time does not change while the game is paused.
	 * 
	 * @return The current time in the game world (in milliseconds). This time does not change while the game is paused.
	 */
	public static long getGameTime()
	{
		return gameTime;
	}

	/**
	 * Returns the current time in the real world (in milliseconds). This time does change while the game is paused.
	 * 
	 * @return The current time in the real world (in milliseconds). This time does change while the game is paused.
	 */
	public static long getRealTime()
	{
		return realTime;
	}

	/**
	 * Returns the real-world time when the game was initialized.
	 * 
	 * @return The real-world time when the game was initialized.
	 */
	public static long getStartTime()
	{
		return startTime;
	}

	/**
	 * Returns true if the game is paused, false if it is not paused.
	 * 
	 * @return true if the game is paused, false if it is not paused.
	 */
	public static boolean isPaused()
	{
		return isPaused;
	}
}
