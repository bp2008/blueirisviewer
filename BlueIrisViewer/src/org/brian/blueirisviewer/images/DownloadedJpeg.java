package org.brian.blueirisviewer.images;

public class DownloadedJpeg
{
	public byte[] data;
	public int imageId;
	public long time;

	public DownloadedJpeg(byte[] data, int imageId, long time)
	{
		this.data = data;
		this.imageId = imageId;
		this.time = time;
	}
}
