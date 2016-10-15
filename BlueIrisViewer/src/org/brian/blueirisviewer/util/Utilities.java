package org.brian.blueirisviewer.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Utilities
{
	public static String sessionCookie = null;

	public static long getTimeInMs()
	{
		Calendar c = Calendar.getInstance();
		long currentTime = c.getTimeInMillis();
		return currentTime;
	}

	/**
	 * Gets a byte[] of data from the specified URL using POST or GET as determined by the arguments.
	 * 
	 * @param sUrl
	 *            The URL to get data from.
	 * @param postData
	 *            Data to POST. If null, the request will be a GET request with no payload instead.
	 * @return
	 */
	public static byte[] getViaHttpConnection(String sUrl, PostData postData) throws ReAuthenticateException
	{
		return getViaHttpConnection(sUrl, postData, null, null);
	}

	/**
	 * Gets a byte[] of data from the specified URL using POST or GET as determined by the arguments.
	 * 
	 * @param sUrl
	 *            The URL to get data from.
	 * @param postData
	 *            Data to POST. If null, the request will be a GET request with no payload instead.
	 * @return
	 */
	private static byte[] getViaHttpConnection(String sUrl, PostData postData, String user, String pass)
			throws ReAuthenticateException
	{
		if (sUrl == null || sUrl.equals(""))
			return new byte[0];
		HttpURLConnection con = null;
		InputStream in = null;
		OutputStream out = null;
		try
		{
			URL url = new URL(sUrl);
			con = (HttpURLConnection) url.openConnection();
			// Handle SSL connection if it is an https URL
			if (HttpsURLConnection.class.isInstance(con))
			{
				HttpsURLConnection sCon = (HttpsURLConnection) con;

				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
				{
					private final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};

					public void checkClientTrusted(X509Certificate[] chain, String authType)
							throws CertificateException
					{
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType)
							throws CertificateException
					{
					}

					public X509Certificate[] getAcceptedIssuers()
					{
						return _AcceptedIssuers;
					}
				} };

				try
				{
					SSLContext sc = SSLContext.getInstance("TLS");
					sc.init(null, trustAllCerts, new java.security.SecureRandom());

					sCon.setSSLSocketFactory(sc.getSocketFactory());
					sCon.setHostnameVerifier(new HostnameVerifier()
					{
						public boolean verify(String arg0, SSLSession arg1)
						{
							return true;
						}
					});
				}
				catch (Exception ex)
				{
					Logger.debug(ex, Utilities.class);
				}
			}
			con.setInstanceFollowRedirects(false);
			con.setConnectTimeout(10000);
			con.setReadTimeout(30000);
			con.setRequestProperty("User-Agent", "BlueIrisViewer");
			con.setRequestProperty("Accept-Encoding", "gzip");

			if (postData != null)
			{
				con.setDoOutput(true);
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Length", String.valueOf(postData.getData().length));
				con.setRequestProperty("Content-Type", postData.getContentType());
				out = con.getOutputStream();
				BufferedOutputStream bOutStream = new BufferedOutputStream(out);
				bOutStream.write(postData.getData());
				bOutStream.flush();
			}
			else
			{
				String sCookie = sessionCookie;
				if (sCookie != null && !sCookie.equals(""))
					con.setRequestProperty("Cookie", "session=" + sCookie);
			}

			if (user != null && pass != null)
			{
				String encoded = Base64.encodeToString((user + ":" + pass).getBytes("UTF-8"), false);
				con.setRequestProperty("Authorization", "Basic " + encoded);
			}

			InputStream inStream = null;
			try
			{
				inStream = con.getInputStream();
			}
			catch (java.io.IOException ex)
			{
				if (ex.getMessage().contains("HTTP response code: 401") || con.getResponseCode() == 401)
					throw new ReAuthenticateException();
				else
					throw ex;
			}
			int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				// All is fine
			}
			else if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307
					|| responseCode == 308)
			{
				// Is redirect response
				throw new ReAuthenticateException();
			}
			String enc = con.getContentEncoding();

			in = new BufferedInputStream(inStream);
			if (enc != null && string.ToLower(enc).equals("gzip"))
				in = new GZIPInputStream(inStream);

			ByteArrayOutputStream baout = null;
			try
			{
				baout = new ByteArrayOutputStream();
				byte[] buf = new byte[8192];
				int read = 0;
				while (read != -1)
				{
					baout.write(buf, 0, read);
					read = in.read(buf);
				}
				HandleSetCookie(con.getHeaderField("Set-Cookie"));
				bytesTotal.addAndGet(baout.size());
				return baout.toByteArray();
			}
			catch (Exception ex)
			{
				throw ex;
			}
			finally
			{
				try
				{
					baout.close();
				}
				catch (Exception ex)
				{
					// Ignore
				}
			}
		}
		catch (ReAuthenticateException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			Logger.debug(ex, Utilities.class, "URL: " + sUrl);
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
		return new byte[0];
	}

	public static String getStringViaHttpConnection(String sUrl)
	{
		return getStringViaHttpConnection(sUrl, null);
	}

	public static String getStringViaHttpConnection(String sUrl, PostData postData)
	{
		byte[] bytes;
		try
		{
			bytes = getViaHttpConnection(sUrl, postData);
		}
		catch (ReAuthenticateException ex)
		{
			Logger.debug(ex, Utilities.class);
			bytes = new byte[0];
		}
		return Utilities.ToString(bytes);
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

	/**
	 * Tries to URL Encode the specified String value. If an UnsupportedEncodingException is thrown, the String is
	 * returned as-is.
	 * 
	 * @param val
	 *            The String to URL Encode.
	 * @return The URL Encoded String.
	 */
	public static String UrlEncode(String val)
	{
		try
		{
			return URLEncoder.encode(val, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return val;
		}
	}

	public static String UrlDecode(String val)
	{
		try
		{
			return URLDecoder.decode(val, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return val;
		}
	}

	/**
	 * Decodes the message as UTF-8, MD5 hashes the resulting byte array, and converts the MD5 hash to hexidecimal
	 * String format. May return null if there is an error.
	 * 
	 * @param message
	 *            The message to hash.
	 * @return
	 */
	public static String Hex_MD5(String message)
	{
		try
		{
			return Encryption.BytesToHex(MD5(message.getBytes("UTF-8")));
		}
		catch (NoSuchAlgorithmException e)
		{
			return null;
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}

	public static byte[] MD5(byte[] message) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(message);
		return thedigest;
	}

	static AtomicLong bytesTotal = new AtomicLong(0);
	static AtomicLong bytesLast3Seconds = new AtomicLong(0);

	public static void tick3Seconds()
	{
		bytesLast3Seconds.set(bytesTotal.get());
		bytesTotal.set(0);
	}

	public static long getCurrentBytesPer3Seconds()
	{
		return bytesLast3Seconds.get();
	}

	public static String getTimestamp()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dt = new Date();
		String S = sdf.format(dt); // formats to 09/23/2009 13:53:28.238
		return S;
	}
}
