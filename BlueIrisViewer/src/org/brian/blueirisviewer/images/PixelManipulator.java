package org.brian.blueirisviewer.images;

public class PixelManipulator
{
	private boolean keepRedDropOtherChannels = false;
	private boolean makeRedBrightestChannel = false;
	private boolean makeRedAverageBrightness = false;
	private boolean grayscaleToRedIfGrayscale = false;

	private PixelManipulator()
	{
	}

	public static PixelManipulator DoNothing()
	{
		PixelManipulator pm = new PixelManipulator();
		return pm;
	}

	/**
	 * Turns the lightest parts of the image red, with the optimization that the image is assumed to be grayscale. This
	 * method simply sets the B and G color channels to 0.
	 * 
	 * @return
	 */
	public static PixelManipulator KeepRedDropOtherChannels()
	{
		PixelManipulator pm = new PixelManipulator();
		pm.keepRedDropOtherChannels = true;
		return pm;
	}

	/**
	 * Turns the lightest parts of the image red by assigning the brightest color channel value to the red channel and
	 * setting the B and G channels to 0.
	 * 
	 * @return
	 */
	public static PixelManipulator MakeRedBrightestChannel()
	{
		PixelManipulator pm = new PixelManipulator();
		pm.makeRedBrightestChannel = true;
		return pm;
	}

	/**
	 * Turns the lightest parts of the image red by assigning the average brightness value to the red channel and
	 * setting the B and G channels to 0.
	 * 
	 * @return
	 */
	public static PixelManipulator MakeRedAverageBrightness()
	{
		PixelManipulator pm = new PixelManipulator();
		pm.makeRedAverageBrightness = true;
		return pm;
	}

	/**
	 * Turns the lightest parts of the image red, only if the image is grayscale. (CURRENTLY NOT IMPLEMENTED)
	 * 
	 * @return
	 */
	public static PixelManipulator GrayscaleToRedIfGrayscale()
	{
		PixelManipulator pm = new PixelManipulator();
		pm.grayscaleToRedIfGrayscale = true;
		return pm;
	}

	public void OperateOnRGB(byte[] rgbData) throws Exception
	{
		if (grayscaleToRedIfGrayscale)
			throw new Exception("WhiteToRedIfGrayscale mode is currently unsupported");
		else if (makeRedBrightestChannel)
		{
			for (int r = 0, g = 1, b = 2; b < rgbData.length; r += 3, g += 3, b += 3)
			{
				rgbData[r] = GetHighestValue(rgbData[r], rgbData[g], rgbData[b]);
				rgbData[g] = rgbData[b] = 0;
			}
		}
		else if (makeRedAverageBrightness)
		{
			for (int r = 0, g = 1, b = 2; b < rgbData.length; r += 3, g += 3, b += 3)
			{
				rgbData[r] = GetAverageValue(rgbData[r], rgbData[g], rgbData[b]);
				rgbData[g] = rgbData[b] = 0;
			}
		}
		else if (keepRedDropOtherChannels)
		{
			for (int g = 1, b = 2; b < rgbData.length; g += 3, b += 3)
			{
				rgbData[g] = rgbData[b] = 0;
			}
		}
	}

	private byte GetAverageValue(byte a, byte b, byte c)
	{
		return (byte)(((a & 0xFF) + (b & 0xFF) + (c & 0xFF)) / 3);
	}

	private byte GetHighestValue(byte a, byte b, byte c)
	{
		int highest = a;
		if (highest < (b & 0xFF))
			highest = b & 0xFF;
		if (highest < (c & 0xFF))
			return c;
		return (byte) highest;
	}
}
