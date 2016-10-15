package org.brian.blueirisviewer.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpHelper
{
	String sUrl;

	public HttpHelper(String URL)
	{
		sUrl = URL;
	}

	/**
	 * Initiates an HTTP GET request and returns the input stream which you can read from.
	 * 
	 * @return The HTTP input stream, or null if the connection failed.
	 */
	public InputStream GET() throws ReAuthenticateException
	{
		if (sUrl == null || sUrl.equals(""))
			return null;
		HttpURLConnection con = null;
		InputStream in = null;
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
			con.setConnectTimeout(10000);
			con.setReadTimeout(30000);
			con.setRequestProperty("User-Agent", "BlueIrisViewer");
			con.setRequestProperty("Accept-Encoding", "gzip");

			String sCookie = Utilities.sessionCookie;
			if (sCookie != null && !sCookie.equals(""))
				con.setRequestProperty("Cookie", "session=" + sCookie);

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

			return in;
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
		}
		return null;
	}

	public static String ReadUntilCharFound(char c, InputStream s) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		int tempInt;
		char lastChar = ' ';
		do
		{
			tempInt = s.read();
			if (tempInt == -1)
				throw new Exception("EOF reading header data");
			lastChar = (char) tempInt;
			sb.append(lastChar);
		}
		while (lastChar != c);
		return sb.toString();
	}

	public static String ReadUntilCompleteStringFound(String until, InputStream s) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		int tempInt;
		do
		{
			tempInt = s.read();
			if (tempInt == -1)
				throw new Exception("EOF reading header data");
			sb.append((char) tempInt);
		}
		while (!sb.substring(Math.max(0, sb.length() - until.length()), sb.length()).equalsIgnoreCase(until));
		return sb.toString();
	}
}
