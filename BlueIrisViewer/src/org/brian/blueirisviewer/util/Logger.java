package org.brian.blueirisviewer.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.brian.blueirisviewer.BlueIrisViewer;

public class Logger
{
	private static final boolean logStuff = true;
	public static final boolean logActivityLifecycle = logStuff;
	public static final boolean logDatabaseAccess = logStuff;
	public static final boolean logDebug = logStuff;
	public static final boolean logOhCrap = logStuff;
	private static String newline = System.getProperty("line.separator");
	private static final String logFile = System.getProperty("BlueIrisViewer_Errors.txt");

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
		StringBuilder sb = new StringBuilder();
		sb.append(" *** Oh No! *** ").append(newline);
		if (string.IsNullOrEmpty(message))
			message = "An exception has occurred";
		sb.append(message);
		sb.append(":").append(newline);
		if (ex == null)
			sb.append("Exception object was null").append(newline);
		else
		{
			String exmsg = ex.getMessage();
			if (exmsg == null)
				sb.append("Exception message was null").append(newline);
			else
				sb.append(exmsg).append(newline);
		}
		if (!string.IsNullOrEmpty(additionalInfo))
		{
			sb.append("Additional Info:").append(newline);
			sb.append(additionalInfo).append(newline);
		}
		
		StringWriter errors = new StringWriter();
		ex.printStackTrace(new PrintWriter(errors));
		sb.append(errors.toString()).append(newline);

		System.err.print(sb.toString());

		if (BlueIrisViewer.bivSettings.logErrorsToDisk)
		{
			try
			{
				Utilities.WriteTextFile(logFile, sb.toString(), true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void ohCrap(String message, Object sender)
	{
		if (!logOhCrap)
			return;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"Oh No!\" message from ").append(sender.getClass().toString()).append(":").append(newline);
		sb.append(message).append(newline);

		System.err.print(sb.toString());
		
		if (BlueIrisViewer.bivSettings.logErrorsToDisk)
		{
			try
			{
				Utilities.WriteTextFile(logFile, sb.toString(), true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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

		StringBuilder sb = new StringBuilder();
		
		sb.append("(").append(cl.toString()).append("): ").append(message).append(newline);

		System.err.print(sb.toString());
		
		if (BlueIrisViewer.bivSettings.logErrorsToDisk)
		{
			try
			{
				Utilities.WriteTextFile(logFile, sb.toString(), true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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

		StringBuilder sb = new StringBuilder();
		
		sb.append(message).append(":").append(newline);
		
		if (ex == null)
			sb.append("Exception object was null").append(newline);
		else
		{
			String exmsg = ex.getMessage();
			if (exmsg == null)
				sb.append("Exception message was null").append(newline);
			else
				sb.append(exmsg).append(newline);
		}
		if (!string.IsNullOrEmpty(additionalInfo))
		{
			sb.append("Additional Info:").append(newline);
			sb.append(additionalInfo).append(newline);
		}
		
		StringWriter errors = new StringWriter();
		ex.printStackTrace(new PrintWriter(errors));
		sb.append(errors.toString()).append(newline);

		System.err.print(sb.toString());
		
		if (BlueIrisViewer.bivSettings.logErrorsToDisk)
		{
			try
			{
				Utilities.WriteTextFile(logFile, sb.toString(), true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void debug(String message, Object sender)
	{
		if (!logDebug)
			return;
		
		StringBuilder sb = new StringBuilder();
		
		// System.err.print("Debug message from ");
		sb.append("(");
		if (sender != null)
			sb.append(sender.getClass().toString());
		else
			sb.append("null");
		sb.append("): ");
		// System.err.println(":");
		sb.append(message).append(newline);
		
		System.err.print(sb.toString());
		
		if (BlueIrisViewer.bivSettings.logErrorsToDisk)
		{
			try
			{
				Utilities.WriteTextFile(logFile, sb.toString(), true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
