package org.brian.blueirisviewer.images;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.GameTime;
import org.brian.blueirisviewer.instantreplay.InstantReplayManager;
import org.brian.blueirisviewer.ui.ServerSetupWnd;
import org.brian.blueirisviewer.util.Encryption;
import org.brian.blueirisviewer.util.IntPoint;
import org.brian.blueirisviewer.util.IntRunnable;
import org.brian.blueirisviewer.util.Logger;
import org.brian.blueirisviewer.util.Utilities;
import org.brian.blueirisviewer.util.string;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Images
{
	public Vector<String> allCameraNames = new Vector<String>();
	public Vector<String> cameraNames = new Vector<String>();
	public Vector<IntPoint> cameraResolutions = new Vector<IntPoint>();
	public Vector<String> imageURLs = new Vector<String>();
	public Vector<Integer> sleepDelays = new Vector<Integer>();
	public Vector<Integer> rotateDegrees = new Vector<Integer>();

	public ConcurrentLinkedQueue<DownloadedTexture> downloadedTextures = new ConcurrentLinkedQueue<DownloadedTexture>();
	Vector<Thread> downloaderThreads = new Vector<Thread>();
	Vector<Texture> textures = new Vector<Texture>();

	boolean abortThreads = false;
	boolean isInitialized = false;
	boolean isInitializing = false;
	boolean serverRequiresAuthentication = false;
	static boolean hasAutoOpenedServerSetup = false;

	float imageWidth = 1;
	float imageHeight = 1;
	AtomicInteger aImageWidth = new AtomicInteger(320);
	AtomicInteger aImageHeight = new AtomicInteger(240);
	AtomicInteger screenWidth = new AtomicInteger(1280);
	AtomicInteger screenHeight = new AtomicInteger(720);
	int cols = 1;
	int rows = 1;
	long connectedAtTime = 0;

	AtomicInteger fullScreenedImageId = new AtomicInteger(-1);

	public InstantReplayManager instantReplayManager;

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

	public Images()
	{
	}

	public void Initialize()
	{
		Initialize(false);
	}

	private void Initialize(final boolean isAuthenticationRetry)
	{
		if (isInitialized || isInitializing)
			return;
		Thread thrStart = new Thread(new Runnable()
		{
			public void run()
			{
				if (isInitialized || isInitializing)
					return;
				synchronized (Images.this)
				{
					if (!isAuthenticationRetry)
						Utilities.sessionCookie = "";
					if (isInitialized || isInitializing)
						return;
					try
					{
						screenWidth.set(BlueIrisViewer.iScreenWidth);
						screenHeight.set(BlueIrisViewer.iScreenHeight);
						isInitializing = true;
						String processedServerURL = ProcessURL(BlueIrisViewer.bivSettings.serverURL);
						String page = Utilities.getStringViaHttpConnection(processedServerURL + "jpegpull.htm");
						if (page.contains("<title>Blue Iris Login</title>"))
						{
							// Page is login page
							if (isAuthenticationRetry)
								return;
							serverRequiresAuthentication = true;
							HandleLogin(processedServerURL);
							isInitializing = false;
							Initialize(true);
							return;
						}
						else
						{
							serverRequiresAuthentication = false;
							if (string.IsNullOrEmpty(page))
								return; // Page is empty!
						}
						// No going back beyond this point.

						// Assume page is jpegpull as requested.

						// Precalculate what we can about where to draw images
						synchronized (BlueIrisViewer.getResizeLock())
						{
							synchronized (allCameraNames)
							{
								Matcher m = cameraPattern.matcher(page);
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
											int delay = (width * height) / 10000; // Note: This delay is not used
											if (delay < 250)
												delay = 250;
											if (!m.group(2).startsWith("+") && !name.equals("index"))
											{
												allCameraNames.add(name);
												if (!SettingsSayToHideCamera(name))
												{
													cameraNames.add(name);
													cameraResolutions.add(new IntPoint(width, height));
													imageURLs.add(processedServerURL + "image/" + name);
													sleepDelays.add(delay);
													rotateDegrees.add(0);
												}
											}
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
							aImageWidth.set((int) Math.ceil(imageWidth));
							aImageHeight.set((int) Math.ceil(imageHeight));
						}

						instantReplayManager = new InstantReplayManager(imageURLs.size(),
								BlueIrisViewer.bivSettings.instantReplayEnabled,
								BlueIrisViewer.bivSettings.instantReplayHistoryLengthMinutes);

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
									while (!abortThreads)
									{
										int sleepFor = BlueIrisViewer.bivSettings.imageRefreshDelayMS;// sleepDelays.get(myInt).intValue();
										// System.out.println("Downloading " + myUrl + Utilities.getTimeInMs());

										if (!instantReplayManager.IsProcessingLiveCamera(myInt))
										{
											int full = fullScreenedImageId.get();

											byte[] img = Utilities.getViaHttpConnection(myUrl
													+ GetImageModeQueryString(myInt, full), null);

											if (abortThreads)
												break;
											else if (img.length > 0)
												instantReplayManager.LiveImageReceived(myInt, img);
											if (!BlueIrisViewer.bivSettings.instantReplayEnabled
													|| BlueIrisViewer.images.instantReplayManager
															.getCurrentTimeOffset() == 0)
											{
												if (full == myInt)
													sleepFor = 0;
												else if (full > -1)
													sleepFor += 1000;
											}
										}
										else if (sleepFor < 25)
											sleepFor = 25; // If we get here, the downloader is getting
															// ahead of the render thread.

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
						{
							downloaderThreads.get(i).setName("ImgDld" + i);
							downloaderThreads.get(i).start();
						}

						isInitialized = true;
					}
					catch (IOException e)
					{
						Logger.debug(e, Images.class);
						isInitializing = false;
					}
					finally
					{
						isInitializing = false;
					}
				}
			}

			private boolean SettingsSayToHideCamera(String name)
			{
				return BlueIrisViewer.bivSettings.hiddenCams.contains(name);
			}

			private String GetImageModeQueryString(int cameraIndex, int fullScreenCameraIndex)
			{
				boolean allowContextBasedSizing = !BlueIrisViewer.bivSettings.instantReplayEnabled
						|| BlueIrisViewer.images.instantReplayManager.getCurrentTimeOffset() == 0;

				String queryStr = "";
				if (BlueIrisViewer.bivSettings.imageResolutionMode == 0)
				{
					// Highest efficiency
					IntPoint nativeResolution = cameraResolutions.get(cameraIndex);
					int newWidth;
					if (fullScreenCameraIndex == cameraIndex && allowContextBasedSizing)
						newWidth = ReduceImageDimensionsAndReturnNewWidth(nativeResolution.x, nativeResolution.y,
								screenWidth.get(), screenHeight.get());
					else
					{
						newWidth = ReduceImageDimensionsAndReturnNewWidth(nativeResolution.x, nativeResolution.y,
								aImageWidth.get(), aImageHeight.get());
					}
					queryStr += "?w=" + newWidth;
				}
				else if (BlueIrisViewer.bivSettings.imageResolutionMode == 1)
				{
					// Balanced
					IntPoint nativeResolution = cameraResolutions.get(cameraIndex);
					int newWidth;
					if (fullScreenCameraIndex == cameraIndex && allowContextBasedSizing)
						newWidth = ReduceImageDimensionsAndReturnNewWidth(nativeResolution.x, nativeResolution.y,
								screenWidth.get(), screenHeight.get());
					else
					{
						newWidth = ReduceImageDimensionsAndReturnNewWidth(nativeResolution.x, nativeResolution.y,
								Math.min(aImageWidth.get() * 2, screenWidth.get()),
								Math.min(aImageHeight.get() * 2, screenHeight.get()));
					}
					queryStr += "?w=" + newWidth;
				}
				else if (BlueIrisViewer.bivSettings.imageResolutionMode == 2)
				{
					// High Quality
					IntPoint nativeResolution = cameraResolutions.get(cameraIndex);
					int newWidth;
					if (fullScreenCameraIndex == cameraIndex && allowContextBasedSizing)
						newWidth = ReduceImageDimensionsAndReturnNewWidth(nativeResolution.x, nativeResolution.y,
								screenWidth.get() * 2, screenHeight.get() * 2);
					else
					{
						newWidth = ReduceImageDimensionsAndReturnNewWidth(nativeResolution.x, nativeResolution.y,
								Math.min(aImageWidth.get() * 2, screenWidth.get() * 2),
								Math.min(aImageHeight.get() * 2, screenHeight.get() * 2));
					}
					queryStr += "?w=" + newWidth;
				}
				else if (BlueIrisViewer.bivSettings.imageResolutionMode == 3)
				{
					// Maximum Quality
					IntPoint nativeResolution = cameraResolutions.get(cameraIndex);
					int newWidth = ReduceImageDimensionsAndReturnNewWidth(nativeResolution.x, nativeResolution.y,
							screenWidth.get() * 2, screenHeight.get() * 2);
					queryStr += "?w=" + newWidth;
				}
				else
				{
					// No Optimizations
				}

				if (BlueIrisViewer.bivSettings.overrideJpegQuality)
				{
					if (string.IsNullOrEmpty(queryStr))
						queryStr += "?";
					else
						queryStr += "&";
					queryStr += "q=" + BlueIrisViewer.bivSettings.jpegQuality;
				}
				return queryStr;
			}

			private void HandleLogin(String processedServerURL)
			{
				String pwDecrypted = Encryption.Decrypt(BlueIrisViewer.bivSettings.password);
				if (pwDecrypted.equals(BlueIrisViewer.bivSettings.password))
				{
					BlueIrisViewer.bivSettings.password = Encryption.Encrypt(pwDecrypted);
					BlueIrisViewer.bivSettings.Save();
				}
				Utilities.getStringViaHttpConnection(processedServerURL
						+ "?page=jpegpull.htm&login="
						+ Utilities.Hex_MD5(BlueIrisViewer.bivSettings.username + ":" + Utilities.sessionCookie + ":"
								+ pwDecrypted));
			}

			private String ProcessURL(String serverURL)
			{
				StringBuilder sb = new StringBuilder();
				if (serverURL.startsWith("http://"))
				{
				}
				else if (serverURL.startsWith("https://"))
				{
				}
				else if (serverURL.startsWith("://"))
					sb.append("http");
				else if (serverURL.startsWith("//"))
					sb.append("http:");
				else if (serverURL.startsWith("/"))
					sb.append("http:/");
				else
					sb.append("http://");

				sb.append(serverURL);

				if (!serverURL.endsWith("/"))
					sb.append("/");

				return sb.toString();
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
		if (instantReplayManager != null)
			instantReplayManager.dispose();
		abortThreads = true;
		for (int i = 0; i < textures.size(); i++)
		{
			Texture texture = textures.get(i);
			if (texture != null)
				texture.dispose();
		}
		DownloadedTexture dt = downloadedTextures.poll();
		while (dt != null)
		{
			dt.data.dispose();
			dt = downloadedTextures.poll();
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
			BlueIrisViewer.ui.DrawText(batch,
					(serverRequiresAuthentication ? "Attempting to log in with configured user name and password ..."
							: "Attempting to contact server ..."), 10, BlueIrisViewer.fScreenHeight - 15);
			BlueIrisViewer.ui.DrawText(batch,
					"If you need to change the server options, press 'o' to open the options.", 10,
					BlueIrisViewer.fScreenHeight - 35);
		}
		else if (!isInitialized)
		{
			BlueIrisViewer.ui.DrawText(batch,
					(serverRequiresAuthentication ? "Unable to log in with configured user name and password."
							: "Unable to contact server. Please check server address."), 10,
					BlueIrisViewer.fScreenHeight - 15);
			BlueIrisViewer.ui.DrawText(batch,
					"If you need to change the server options, press 'o' to open the options.", 10,
					BlueIrisViewer.fScreenHeight - 35);
			if (BlueIrisViewer.bivSettings.serverURL.equals("http://127.0.0.1:80/") && !hasAutoOpenedServerSetup)
			{
				hasAutoOpenedServerSetup = true;
				BlueIrisViewer.ui.openWindow(ServerSetupWnd.class);
			}
		}
		else if (textures.size() == 0)
		{
			BlueIrisViewer.ui.DrawText(batch, "Unrecognized server!", 10, BlueIrisViewer.fScreenHeight - 15);
			BlueIrisViewer.ui.DrawText(batch,
					"If you need to change the server options, press 'o' to open the options.", 10,
					BlueIrisViewer.fScreenHeight - 35);
			if (BlueIrisViewer.bivSettings.serverURL.equals("http://127.0.0.1:80/") && !hasAutoOpenedServerSetup)
			{
				hasAutoOpenedServerSetup = true;
				BlueIrisViewer.ui.openWindow(ServerSetupWnd.class);
			}
		}
		else
		{
			if (connectedAtTime == 0)
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
			if (connectedAtTime + 10000 > GameTime.getRealTime())
			{
				BlueIrisViewer.ui.DrawText(batch, "Connected!", 10, BlueIrisViewer.fScreenHeight - 15);
				BlueIrisViewer.ui.DrawText(batch, "Press 'o' to open the options.", 10,
						BlueIrisViewer.fScreenHeight - 35);
			}
		}
		if (instantReplayManager != null)
			instantReplayManager.render(batch);
	}

	private int ReduceImageDimensionsAndReturnNewWidth(int originalWidth, int originalHeight, int destinationWidth,
			int destinationHeight)
	{
		double ow = originalWidth;
		double oh = originalHeight;
		double rw = destinationWidth;
		double rh = destinationHeight;
		double newWidth = rw;
		double newHeight = oh / ow * rw;
		if (newHeight > rh)
			newWidth = ow / oh * rh;
		return (int) newWidth;
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
		screenWidth.set(w);
		screenHeight.set(h);
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
			aImageWidth.set((int) Math.ceil(imageWidth));
			aImageHeight.set((int) Math.ceil(imageHeight));
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

	public Object[] GetCameraNamesObjectArray()
	{
		synchronized (allCameraNames)
		{
			if (this.allCameraNames.size() == 0)
				return null;
			Object[] cameraNamesObjects = new Object[this.allCameraNames.size()];
			for (int i = 0; i < this.allCameraNames.size(); i++)
				cameraNamesObjects[i] = allCameraNames.get(i);
			return cameraNamesObjects;
		}
	}
}
