package org.brian.blueirisviewer.images;

import com.badlogic.gdx.graphics.Pixmap;

public class DownloadedTexture
{
	public Pixmap data;
	public int imageId;

	public DownloadedTexture(Pixmap data, int imageId)
	{
		this.data = data;
		this.imageId = imageId;
	}
}
