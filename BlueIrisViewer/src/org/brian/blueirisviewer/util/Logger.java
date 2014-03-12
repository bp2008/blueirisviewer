package org.brian.blueirisviewer.util;

public class Logger
{
	private static final boolean logStuff = true;
	public static final boolean logActivityLifecycle = logStuff;
	public static final boolean logDatabaseAccess = logStuff;
	public static final boolean logDebug = logStuff;
	public static final boolean logOhCrap = logStuff;

	public static void ohCrap(Exception ex, Object sender)
	{
		if (!logOhCrap)
			return;
		ohCrap(ex, sender.getClass(), "");
	}

	public static void ohCrap(Exception ex, Object sender, String additionalInfo)
	{
		if (!logOhCrap)
			return;
		ohCrap(ex, sender.getClass(), additionalInfo);
	}

	public static void ohCrap(Exception ex, @SuppressWarnings("rawtypes") Class cl)
	{
		if (!logOhCrap)
			return;
		ohCrap(ex, cl, "");
	}

	public static void ohCrap(Exception ex, @SuppressWarnings("rawtypes") Class cl, String additionalInfo)
	{
		if (!logOhCrap)
			return;
		String message = "An exception has occurred";
		if (cl != null)
			message += " in " + cl.toString();
		ohCrap(ex, message, additionalInfo);
	}

	private static void ohCrap(Exception ex, String message, String additionalInfo)
	{
		if (!logOhCrap)
			return;
		System.err.println(" *** Oh No! *** ");
		if (string.IsNullOrEmpty(message))
			message = "An exception has occurred";
		System.err.print(message);
		System.err.println(":");
		if (ex == null)
			System.err.println("Exception object was null");
		else
		{
			String exmsg = ex.getMessage();
			if (exmsg == null)
				System.err.println("Exception message was null");
			else
				System.err.println(exmsg);
		}
		if (!string.IsNullOrEmpty(additionalInfo))
		{
			System.err.println("Additional Info:");
			System.err.println(additionalInfo);
		}
		ex.printStackTrace();
	}

	public static void ohCrap(String message, Object sender)
	{
		if (!logOhCrap)
			return;
		System.err.print("\"Oh No!\" message from ");
		System.err.print(sender.getClass().toString());
		System.err.println(":");
		System.err.println(message);
	}

	public static void debug(Exception ex, @SuppressWarnings("rawtypes") Class cl, String additionalInfo)
	{
		if (!logDebug)
			return;
		String message = "An exception has occurred";
		if (cl != null)
			message += " in " + cl.toString();
		debug(ex, message, additionalInfo);
	}

	public static void debug(Exception ex, @SuppressWarnings("rawtypes") Class cl)
	{
		if (!logDebug)
			return;
		debug(ex, cl, "");
	}

	public static void debug(String message, @SuppressWarnings("rawtypes") Class cl)
	{
		if (!logDebug)
			return;
		System.err.print("(");
		System.err.print(cl.toString());
		System.err.print("): ");
		System.err.println(message);
	}

	public static void debug(Exception ex, Object sender, String additionalInfo)
	{
		if (!logDebug)
			return;
		String message = "An exception has occurred";
		if (sender != null)
			message += " in " + sender.getClass().toString();
		debug(ex, message, additionalInfo);
	}

	public static void debug(Exception ex, Object sender)
	{
		if (!logDebug)
			return;
		debug(ex, sender, "");
	}

	public static void debug(Exception ex, String message, String additionalInfo)
	{
		if (!logDebug)
			return;
		if (string.IsNullOrEmpty(message))
			message = "An exception has occurred";
		System.err.print(message);
		System.err.println(":");
		if (ex == null)
			System.err.println("Exception object was null");
		else
		{
			String exmsg = ex.getMessage();
			if (exmsg == null)
				System.err.println("Exception message was null");
			else
				System.err.println(exmsg);
		}
		if (!string.IsNullOrEmpty(additionalInfo))
		{
			System.err.println("Additional Info:");
			System.err.println(additionalInfo);
		}
		ex.printStackTrace();
	}

	public static void debug(String message, Object sender)
	{
		if (!logDebug)
			return;
		// System.err.print("Debug message from ");
		System.err.print("(");
		if (sender != null)
			System.err.print(sender.getClass().toString());
		else
			System.err.print("null");
		System.err.print("): ");
		// System.err.println(":");
		System.err.println(message);
	}
}
