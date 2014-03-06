package org.brian.blueirisviewer;

import com.badlogic.gdx.graphics.Pixmap;

public class Images
{
	public static Pixmap Get(String url)
	{
		byte[] img = Utilities.getViaHttpConnection(url);
		if (img.length > 0)
			try
			{
				return new Pixmap(img, 0, img.length);
			}
			catch (Exception ex)
			{
			}
		return null;
	}
	public static Pixmap Get(byte[] img)
	{
		if (img.length > 0)
			try
			{
				return new Pixmap(img, 0, img.length);
			}
			catch (Exception ex)
			{
			}
		return null;
	}
}
