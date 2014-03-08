package org.brian.blueirisviewer.images;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.GameTime;
import org.brian.blueirisviewer.util.IntRunnable;
import org.brian.blueirisviewer.util.Utilities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Images
{
	Vector<String> cameraNames = new Vector<String>();
	Vector<String> imageURLs = new Vector<String>();
	Vector<Integer> sleepDelays = new Vector<Integer>();
	Vector<Integer> rotateDegrees = new Vector<Integer>();

	ConcurrentLinkedQueue<DownloadedTexture> downloadedTextures = new ConcurrentLinkedQueue<DownloadedTexture>();
	Vector<Thread> downloaderThreads = new Vector<Thread>();
	Vector<Texture> textures = new Vector<Texture>();

	boolean abortThreads = false;
	boolean isInitialized = false;
	boolean isInitializing = false;

	float imageWidth = 1;
	float imageHeight = 1;
	int cols = 1;
	int rows = 1;
	long connectedAtTime = 0;

	AtomicInteger fullScreenedImageId = new AtomicInteger(-1);

	public int getRowCount()
	{
		return rows;
	}

	public int getColCount()
	{
		return cols;
	}

	public void setFullScreenedImageId(int id)
	{
		fullScreenedImageId.set(id);
	}

	public int getFullScreenedImageId()
	{
		return fullScreenedImageId.get();
	}

	public boolean isInitialized()
	{
		return isInitialized;
	}

	public float getImageWidth()
	{
		return imageWidth;
	}

	public float getImageHeight()
	{
		return imageHeight;
	}

	public int getNumImages()
	{
		return textures.size();
	}

	Pattern cameraPattern = Pattern.compile("<option\\W+value=\"([^\"]*?)\">([^<]*?)</option>");

	public void Initialize()
	{
		if (isInitialized || isInitializing)
			return;
		Thread thrStart = new Thread(new Runnable()
		{
			public void run()
			{
				if (isInitialized)
					return;
				try
				{
					isInitializing = true;
					String jpegPullPage = Utilities.getStringViaHttpConnection(BlueIrisViewer.bivSettings.serverURL
							+ "jpegpull.htm");
					if (jpegPullPage == null || jpegPullPage.equals(""))
						return;
					// Precalculate what we can about where to draw images
					synchronized (BlueIrisViewer.getResizeLock())
					{
						Matcher m = cameraPattern.matcher(jpegPullPage);
						while (m.find())
						{
							String data = m.group(1);
							String[] parts = data.split(";");
							if (parts.length > 3)
							{
								int width = Utilities.ParseInt(parts[1], 2500);
								int height = Utilities.ParseInt(parts[2], 1000);
								String[] urlParts = parts[3].split("/");
								if (urlParts.length > 0)
								{
									String name = urlParts[urlParts.length - 1];
									int delay = (width * height) / 10000;
									if (delay < 250)
										delay = 250;
									if (!m.group(2).startsWith("+") && !name.equals("index"))
									{
										cameraNames.add(name);
										imageURLs.add(BlueIrisViewer.bivSettings.serverURL + "image/" + name);
										sleepDelays.add(delay);
										rotateDegrees.add(0);
									}
								}
							}
						}
						double sqrt = Math.sqrt(imageURLs.size());
						cols = (int) Math.ceil(sqrt);
						rows = (int) sqrt;
						if (cols * rows < imageURLs.size())
							rows++;
						HandleOverrideGridSize();
						imageWidth = BlueIrisViewer.fScreenWidth / cols;
						imageHeight = BlueIrisViewer.fScreenHeight / rows;
					}

					// Populate texture list
					for (int i = 0; i < imageURLs.size(); i++)
						textures.add(null);

					// Create downloader threads
					for (int i = 0; i < imageURLs.size(); i++)
						downloaderThreads.add(new Thread(new IntRunnable(i)
						{
							public void run()
							{
								String myUrl = imageURLs.get(myInt);
								DownloadedTexture dt = null;
								while (!abortThreads)
								{
									int sleepFor = BlueIrisViewer.bivSettings.imageRefreshDelayMS;// sleepDelays.get(myInt).intValue();
									// System.out.println("Downloading " + myUrl + Utilities.getTimeInMs());

									if (!downloadedTextures.contains(dt))
									{
										Pixmap pm = Images.Get(myUrl);

										if (abortThreads)
										{
											if (pm != null)
												pm.dispose();
											break;
										}

										if (pm != null)
										{
											dt = new DownloadedTexture(pm, myInt);
											downloadedTextures.add(dt);
										}

										int full = fullScreenedImageId.get();

										if (full == myInt)
											sleepFor = 0;
										else if (full > -1)
											sleepFor += 1000;
									}
									else if (sleepFor < 25)
										sleepFor = 25; // If we get here, the downloader is getting ahead of the render
														// thread.

									if (sleepFor > 0)
									{
										try
										{
											Thread.sleep(sleepFor);
										}
										catch (InterruptedException e)
										{
										}
									}
								}
							}
						}));

					// Start downloader threads
					for (int i = 0; i < downloaderThreads.size(); i++)
						downloaderThreads.get(i).start();

					isInitialized = true;
				}
				finally
				{
					isInitializing = false;
				}
			}
		});
		thrStart.start();
	}

	public static Pixmap Get(String url)
	{
		byte[] img = Utilities.getViaHttpConnection(url, null);
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

	public void dispose()
	{
		abortThreads = true;
		downloadedTextures.clear();
		for (int i = 0; i < textures.size(); i++)
		{
			Texture texture = textures.get(i);
			if (texture != null)
				texture.dispose();
		}
	}

	public void render(SpriteBatch batch)
	{
		// Create new textures as necessary and render them
		DownloadedTexture dt = downloadedTextures.poll();
		while (dt != null)
		{
			Texture newTex = null;
			{
				newTex = new Texture(dt.data);
				dt.data.dispose();
				newTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			}
			Texture temp = textures.set(dt.imageId, newTex);
			if (temp != null)
				temp.dispose();
			dt = downloadedTextures.poll();
		}

		if (isInitializing)
		{
			BlueIrisViewer.ui
					.DrawText(batch, "Attempting to contact server ...", 10, BlueIrisViewer.fScreenHeight - 15);
			BlueIrisViewer.ui.DrawText(batch,
					"If you need to change the server address, press 'o' to open the options.", 10,
					BlueIrisViewer.fScreenHeight - 35);
		}
		else if (!isInitialized)
		{
			BlueIrisViewer.ui.DrawText(batch, "Unable to contact server. Please check server address.", 10,
					BlueIrisViewer.fScreenHeight - 15);
			BlueIrisViewer.ui.DrawText(batch,
					"If you need to change the server address, press 'o' to open the options.", 10,
					BlueIrisViewer.fScreenHeight - 35);
		}
		else if (textures.size() == 0)
		{
			BlueIrisViewer.ui.DrawText(batch, "Unrecognized server!", 10, BlueIrisViewer.fScreenHeight - 15);
			BlueIrisViewer.ui.DrawText(batch,
					"If you need to change the server address, press 'o' to open the options.", 10,
					BlueIrisViewer.fScreenHeight - 35);
		}
		else
		{
			if(connectedAtTime == 0)
				connectedAtTime = GameTime.getRealTime();
			// Everything seems fine.
			int full = fullScreenedImageId.get();
			if (BlueIrisViewer.bivSettings.imageFillMode == 0
					|| (BlueIrisViewer.bivSettings.imageFillMode == 1 && full == -1))
			{
				for (int i = 0; i < textures.size(); i++)
				{
					if (full == i)
						continue;
					Texture tex = textures.get(i);
					if (tex != null)
					{
						int col = i % cols;
						int row = i / cols;
						row = (rows - row) - 1;

						int rot = rotateDegrees.get(i).intValue();
						boolean rotate90OneWayOrAnother = rot == 90 || rot == 270 || rot == -90 || rot == -270;

						Rectangle rect = new Rectangle(imageWidth * col, imageHeight * row, imageWidth, imageHeight);
						if (BlueIrisViewer.bivSettings.imageFillMode == 0)
							FitImageIntoRect(tex, rect, rotate90OneWayOrAnother);

						batch.draw(tex, (float) rect.x, (float) rect.y, rect.width / 2, rect.height / 2,
								(float) rect.width, (float) rect.height, 1f, 1f, (float) rot, 0, 0, tex.getWidth(),
								tex.getHeight(), false, false);
					}
				}
			}
			if (full > -1 && full < textures.size())
			{
				int i = full;
				Texture tex = textures.get(i);
				if (tex != null)
				{
					int rot = rotateDegrees.get(i).intValue();
					boolean rotate90OneWayOrAnother = rot == 90 || rot == 270 || rot == -90 || rot == -270;

					Rectangle rect = new Rectangle(0, 0, BlueIrisViewer.fScreenWidth, BlueIrisViewer.fScreenHeight);
					if (BlueIrisViewer.bivSettings.imageFillMode == 0)
						FitImageIntoRect(tex, rect, rotate90OneWayOrAnother);

					batch.draw(tex, (float) rect.x, (float) rect.y, rect.width / 2, rect.height / 2,
							(float) rect.width, (float) rect.height, 1f, 1f, (float) rot, 0, 0, tex.getWidth(),
							tex.getHeight(), false, false);
				}
			}
			if(connectedAtTime + 10000 > GameTime.getRealTime())
			{
				BlueIrisViewer.ui
				.DrawText(batch, "Connected!", 10, BlueIrisViewer.fScreenHeight - 15);
		BlueIrisViewer.ui.DrawText(batch,
				"Press 'o' to open the options.", 10,
				BlueIrisViewer.fScreenHeight - 35);
			}
		}
	}

	private void FitImageIntoRect(Texture tex, Rectangle rect, boolean rotate90)
	{
		if (rotate90)
		{
			// Calculate the rectangle position that we want the image to end up in (easy)
			double oh = tex.getWidth();
			double ow = tex.getHeight();
			double rw = rect.width;
			double rh = rect.height;
			double newWidth = rw;
			double newHeight = oh / ow * rw;
			if (newHeight > rh)
			{
				newWidth = ow / oh * rh;
				newHeight = rh;

				rect.x += (rect.width - newWidth) / 2;
				rect.width = (float) newWidth;
			}
			else
			{
				rect.y += (rect.height - newHeight) / 2;
				rect.height = (float) newHeight;
			}
			// Now calculate the 90 degree rotated version of rect
			float widthDiff = rect.width - rect.height;
			float heightDiff = widthDiff * -1;
			rect.x += widthDiff / 2;
			rect.y += heightDiff / 2;
			float tmp = rect.width;
			rect.width = rect.height;
			rect.height = tmp;
		}
		else
		{
			double ow = tex.getWidth();
			double oh = tex.getHeight();
			double rw = rect.width;
			double rh = rect.height;
			double newWidth = rw;
			double newHeight = oh / ow * rw;
			if (newHeight > rh)
			{
				newWidth = ow / oh * rh;
				newHeight = rh;

				rect.x += (rect.width - newWidth) / 2;
				rect.width = (float) newWidth;
			}
			else
			{
				rect.y += (rect.height - newHeight) / 2;
				rect.height = (float) newHeight;
			}
		}

	}

	public void resize(int w, int h)
	{
		if (BlueIrisViewer.bivSettings != null)
		{
			double sqrt = Math.sqrt(imageURLs.size());
			cols = (int) Math.ceil(sqrt);
			rows = (int) sqrt;
			if (cols * rows < imageURLs.size())
				rows++;
			HandleOverrideGridSize();
			imageWidth = (float) w / cols;
			imageHeight = (float) h / rows;
		}
	}

	private void HandleOverrideGridSize()
	{
		if (BlueIrisViewer.bivSettings.bOverrideGridLayout && BlueIrisViewer.bivSettings.overrideGridLayoutX > 0
				&& BlueIrisViewer.bivSettings.overrideGridLayoutY > 0)
		{
			cols = BlueIrisViewer.bivSettings.overrideGridLayoutX;
			rows = BlueIrisViewer.bivSettings.overrideGridLayoutY;
		}
	}

	public void GridSettingsChanged()
	{
		resize(BlueIrisViewer.iScreenWidth, BlueIrisViewer.iScreenHeight);
	}
}
