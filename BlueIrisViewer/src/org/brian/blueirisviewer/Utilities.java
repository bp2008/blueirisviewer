package org.brian.blueirisviewer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities
{
	private static String sessionCookie = null;

	public static long getTimeInMs()
	{
		Calendar c = Calendar.getInstance();
		long currentTime = c.getTimeInMillis();
		return currentTime;
	}

	/**
	 * Gets a byte[] of data from the specified URL. If the connection is redirected to a different host, the Android
	 * browser is opened and sent to the new address, and an empty byte array is returned from this function.
	 * 
	 * @param sUrl
	 *            The URL to get data from.
	 * @return
	 */
	public static byte[] getViaHttpConnection(String sUrl)
	{
		if (sUrl == null || sUrl.equals(""))
			return new byte[0];
		HttpURLConnection con = null;
		InputStream in = null;
		try
		{
			URL url = new URL(sUrl);
			con = (HttpURLConnection) url.openConnection();

			con.setConnectTimeout(10000);
			con.setReadTimeout(30000);

			String sCookie = sessionCookie;
			if (sCookie != null && !sCookie.equals(""))
				con.setRequestProperty("Cookie", "session=" + sCookie);

			InputStream inStream = con.getInputStream();

			in = new BufferedInputStream(inStream);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int read = 0;
			while (read != -1)
			{
				out.write(buf, 0, read);
				read = in.read(buf);
			}
			HandleSetCookie(con.getHeaderField("Set-Cookie"));
			return out.toByteArray();
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		finally
		{
			if (con != null)
				((HttpURLConnection) con).disconnect();
			try
			{
				if (in != null)
					in.close();
			}
			catch (Exception ex)
			{
			}
		}
		return new byte[0];
	}

	private static Pattern sessionCookieFinder = Pattern.compile("session=(.*?); path=/;");

	private static void HandleSetCookie(String setCookie)
	{
		if (setCookie == null || setCookie.equals(""))
			return;
		Matcher matcher = sessionCookieFinder.matcher(setCookie);
		if (matcher.find())
		{
			String session = matcher.group(1);
			if (session != null && !session.equals(sessionCookie))
				sessionCookie = session;
		}
	}

	public static String getStringViaHttpConnection(String sUrl)
	{
		byte[] bytes = getViaHttpConnection(sUrl);
		return Utilities.ToString(bytes);
	}

	/**
	 * Writes the specified data to the file, deleting any previously existing file content.
	 * 
	 * @param fullPath
	 *            Full path to the file.
	 * @param bytes
	 *            The bytes to write.
	 * @throws Exception
	 */
	public static void WriteFile(String fullPath, byte[] bytes) throws Exception
	{
		WriteFile(fullPath, bytes, false);
	}

	/**
	 * Writes the specified data to the file, optionally appending to the existing file content.
	 * 
	 * @param fullPath
	 *            Full path to the file.
	 * @param bytes
	 *            The bytes to write.
	 * @param append
	 *            If true, the existing file content will not be deleted.
	 * @throws Exception
	 */
	public static void WriteFile(String fullPath, byte[] bytes, boolean append) throws Exception
	{
		File file = new File(fullPath);
		if (file.exists() && !append)
			file.delete();
		else
		{
			String parentDir = file.getAbsoluteFile().getParent();
			if (!EnsureDirectoryExists(parentDir))
			{
				throw new Exception("Could not access path " + file.getAbsolutePath());
			}
		}
		FileOutputStream outFile = null;
		try
		{
			outFile = new FileOutputStream(file, append);
			outFile.write(bytes);
			outFile.flush();
		}
		catch (Exception ex)
		{
			if (outFile != null)
			{
				try
				{
					outFile.close();
				}
				catch (Exception e2)
				{
					throw new Exception("Exception thrown when closing file " + file.getName(), e2);
				}
			}
			throw new Exception("Exception thrown when writing file " + file.getName(), ex);
		}
		finally
		{
			try
			{
				if (outFile != null)
					outFile.close();
			}
			catch (Exception ex)
			{
				// Ignore
			}
		}
	}

	/**
	 * Writes the specified String to the file, deleting any previously existing file content.
	 * 
	 * @param fullPath
	 *            Full path to the file.
	 * @param text
	 *            The text to write.
	 * @throws Exception
	 */
	public static void WriteTextFile(String fullPath, String text) throws Exception
	{
		WriteTextFile(fullPath, text, false);
	}

	/**
	 * Writes the specified String to the file, optionally appending to instead of deleting old content.
	 * 
	 * @param fullPath
	 *            Full path to the file.
	 * @param text
	 *            The text to write.
	 * @param append
	 *            If true, the text will be appended to any existing data in the file.
	 * @throws Exception
	 */
	public static void WriteTextFile(String fullPath, String text, boolean append) throws Exception
	{
		if (text == null)
			text = "";
		WriteFile(fullPath, text.getBytes("UTF-8"), append);
	}

	public static String ReadTextFile(String fullPath) throws Exception
	{
		byte[] bytes = ReadFile(fullPath);
		return Utilities.ToString(bytes);
	}

	public static byte[] ReadFile(String fullPath) throws Exception
	{
		File file = new File(fullPath);
		if (!file.exists())
			return new byte[0];
		FileInputStream inFile = null;
		inFile = new FileInputStream(file);
		ByteArrayOutputStream out = null;
		try
		{
			out = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int read = 0;
			while (read != -1)
			{
				out.write(buf, 0, read);
				read = inFile.read(buf, 0, 8192);
			}
			return out.toByteArray();
		}
		catch (Exception ex)
		{
			if (inFile != null)
			{
				try
				{
					inFile.close();
				}
				catch (Exception e2)
				{
					throw new Exception("Exception thrown when closing file " + file.getName(), e2);
				}
			}
			throw new Exception("Exception thrown when reading file " + file.getName(), ex);
		}
		finally
		{
			try
			{
				if (inFile != null)
					inFile.close();
			}
			catch (Exception ex)
			{
				// Ignore
			}
			try
			{
				if (out != null)
					out.close();
			}
			catch (Exception ex)
			{
				// Ignore
			}
		}
	}

	/**
	 * Converts the specified byte array to a String using UTF-8 encoding.
	 * 
	 * @param data
	 *            A byte array representing String data in UTF-8 encoding.
	 * @return The String represented by the byte array. Returns an empty String in the event of an encoding error.
	 */
	public static String ToString(byte[] data)
	{
		if (data != null && data.length > 0)
			try
			{
				return new String(data, "UTF-8");
			}
			catch (UnsupportedEncodingException ex)
			{
			}
		return "";
	}

	/**
	 * Tries to create the specified directory, returning true if the directory already exists or was created by the
	 * function.
	 * 
	 * @param path
	 * @return
	 */
	public static boolean EnsureDirectoryExists(String path)
	{
		if (path == null || path.equals(""))
			return false;
		File directory = new File(path);
		if (!directory.exists())
			return directory.mkdirs();
		return true;
	}

	public static int ParseInt(String str, int defaultValue)
	{
		if (str == null || str.equals(""))
			return defaultValue;
		int i;
		try
		{
			i = Integer.valueOf(str).intValue();
		}
		catch (Exception ex)
		{
			i = defaultValue;
		}
		return i;
	}

	public static float ParseFloat(String str, float defaultValue)
	{
		if (str == null || str.equals(""))
			return defaultValue;
		float f;
		try
		{
			f = Float.valueOf(str).floatValue();
		}
		catch (Exception ex)
		{
			f = defaultValue;
		}
		return f;
	}
}
