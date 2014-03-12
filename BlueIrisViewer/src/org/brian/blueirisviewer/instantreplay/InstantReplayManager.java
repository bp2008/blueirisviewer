package org.brian.blueirisviewer.instantreplay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.images.DownloadedJpeg;
import org.brian.blueirisviewer.images.DownloadedTexture;
import org.brian.blueirisviewer.images.Images;
import org.brian.blueirisviewer.util.IntRunnable;
import org.brian.blueirisviewer.util.Logger;
import org.brian.blueirisviewer.util.Utilities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class InstantReplayManager
{
	/**
	 * A TreeMap that maps camera ID to a List of images
	 */
	InstantReplayImageCollection masterList;
	AtomicInteger timeOffset = new AtomicInteger(0);
	Vector<Thread> imageProcessingThreads = new Vector<Thread>();
	Thread thrCacheWrite;
	Thread thrCacheRead;
	int numCameras;
	boolean abortThreads = false;
	Vector<DownloadedJpeg> downloadedImages = new Vector<DownloadedJpeg>();
	ArrayList<DownloadedJpeg> imagesToProcess = new ArrayList<DownloadedJpeg>();
	File cacheFile;
	int historyLengthMs = 5 * 60 * 1000;
	boolean enableInstantReplay = false;
	boolean isVisible = false;
	float displayHistoryPercentageTotal = 0;
	float displayHistoryPercentageSelected = 0;

	long nextCacheResetTime;

	public InstantReplayManager(int numCameras, boolean enabled, int historyLengthMinutes) throws IOException
	{
		this.numCameras = numCameras;
		if (historyLengthMinutes < 1)
			historyLengthMinutes = 1;
		if (historyLengthMinutes > 1440)
			historyLengthMinutes = 1440;
		this.historyLengthMs = historyLengthMinutes * 60 * 1000;
		this.enableInstantReplay = enabled;
		masterList = new InstantReplayImageCollection();

		for (int i = 0; i < BlueIrisViewer.images.cameraNames.size(); i++)
		{
			downloadedImages.add(null);
			imagesToProcess.add(null);
		}

		cacheFile = new File("BlueIrisView_InstantReplayCache");
		if (!cacheFile.exists())
			cacheFile.createNewFile();
		cacheFile.deleteOnExit();

		thrCacheWrite = new Thread(new Runnable()
		{
			public void run()
			{
				FileOutputStream out = null;
				boolean isFirstWriteThrough = true;
				long bytesFreeUntil = 0;
				nextCacheResetTime = Utilities.getTimeInMs() + historyLengthMs;
				try
				{
					if (enableInstantReplay)
						out = new FileOutputStream(cacheFile.getAbsolutePath(), false);
					while (!abortThreads)
					{
						for (int i = 0; i < InstantReplayManager.this.numCameras && !abortThreads; i++)
						{
							DownloadedJpeg dj = downloadedImages.get(i);
							if (dj == null || dj.data == null || dj.data.length == 0)
								continue;
							if (!enableInstantReplay || timeOffset.get() == 0)
							{
								synchronized (imagesToProcess)
								{
									imagesToProcess.set(i, dj);
								}
							}
							downloadedImages.set(i, null);
							if (!enableInstantReplay)
								continue;
							long location = out.getChannel().position();
							if (Utilities.getTimeInMs() > nextCacheResetTime)
							{
								// We have reached the end of the history time. Now we start over.
								nextCacheResetTime = Utilities.getTimeInMs() + historyLengthMs;
								isFirstWriteThrough = false;
								out.getChannel().position(0);
								location = 0;
								bytesFreeUntil = 0;
							}
							if (!isFirstWriteThrough)
							{
								// This is not the first write-through of the cache, so we will likely have to delete
								// one or more old images to make room for the new image.
								while (location + dj.data.length > bytesFreeUntil)
								{
									InstantReplayImage oldest = masterList.removeOldest();
									int tries = 0;
									while (oldest == null && tries++ < 1000)
									{
										Sleep(5);
										oldest = masterList.removeOldest();
									}
									if (oldest == null)
										throw new Exception("Tried to remove oldest image, but it is null!");
									bytesFreeUntil += oldest.size;
									if (oldest.offset < bytesFreeUntil)
									{
										System.out.println("Exceeded previous cache size");
										bytesFreeUntil = Long.MAX_VALUE;
									}
								}
							}
							out.write(dj.data);
							out.flush();
							// Now that the image is fully written, we need to add it to the image lists.
							InstantReplayImage iri = new InstantReplayImage((byte) dj.imageId, dj.time, location,
									dj.data.length);
							masterList.add(iri);
							updateHistoryProgressVisibility();
						}
						if (abortThreads)
							break;
						try
						{
							Thread.sleep(5);
						}
						catch (InterruptedException e)
						{
						}
					}
				}
				catch (Exception e)
				{
					Logger.debug(e, InstantReplayManager.class);
				}
				finally
				{
					System.out.println("thrCacheWrite exiting");
					if (out != null)
					{
						try
						{
							out.flush();
						}
						catch (Exception e)
						{
							Logger.debug(e, InstantReplayManager.class);
						}
						try
						{
							out.close();
						}
						catch (Exception e)
						{
							Logger.debug(e, InstantReplayManager.class);
						}
					}
				}
			}
		});
		thrCacheWrite.setName("ImgCache Write");
		thrCacheWrite.start();

		thrCacheRead = new Thread(new Runnable()
		{
			public void run()
			{
				if (!enableInstantReplay)
					return;
				FileInputStream in = null;
				try
				{
					in = new FileInputStream(cacheFile.getAbsolutePath());
					long offset;
					InstantReplayImage previouslyReadImage = null;
					while (!abortThreads)
					{
						offset = timeOffset.get();
						if (offset > 0)
						{
							InstantReplayImage iri = masterList.GetNextChronologicalImage();
							if (iri == null)
							{
								previouslyReadImage = null;
								Sleep(10);
								continue; // No image is available!
							}

							if (previouslyReadImage == iri)
							{
								Sleep(10);
								continue; // This image is the same as last time!
							}

							previouslyReadImage = iri;
							// Load this image from the cache
							in.getChannel().position(iri.offset);
							byte[] imageData = new byte[iri.size];
							int read = in.read(imageData, 0, imageData.length);
							while (read < imageData.length)
								read += in.read(imageData, read, imageData.length - read);

							// Now that the image is fully read, we need to send it to the image processing threads.
							synchronized (imagesToProcess)
							{
								imagesToProcess
										.set(iri.cameraId, new DownloadedJpeg(imageData, iri.cameraId, iri.time));
							}
							if (abortThreads)
								break;
						}
						else
						{
							previouslyReadImage = null;
							Sleep(10);
						}
					}
				}
				catch (Exception e)
				{
					Logger.debug(e, InstantReplayManager.class);
				}
				finally
				{
					System.out.println("thrCacheRead exiting");
					if (in != null)
					{
						try
						{
							in.close();
						}
						catch (Exception e)
						{
							Logger.debug(e, InstantReplayManager.class);
						}
					}
				}
			}
		});
		thrCacheRead.setName("ImgCache Read");
		thrCacheRead.start();
		// Create image processing threads
		for (int i = 0; i < numCameras; i++)
			imageProcessingThreads.add(new Thread(new IntRunnable(i)
			{
				public void run()
				{
					DownloadedTexture dt;
					DownloadedJpeg dj;
					while (!abortThreads)
					{
						synchronized (imagesToProcess)
						{
							dj = imagesToProcess.get(myInt);
							imagesToProcess.set(myInt, null);
						}
						if (dj != null)
						{
							Pixmap pm = Images.Get(dj.data);

							if (abortThreads)
							{
								if (pm != null)
									pm.dispose();
								break;
							}

							if (pm != null)
							{
								dt = new DownloadedTexture(pm, myInt);
								BlueIrisViewer.images.downloadedTextures.add(dt);
							}
						}
						if (abortThreads)
							break;
						try
						{
							Thread.sleep(10);
						}
						catch (InterruptedException e)
						{
						}
					}
				}
			}));
		for (int i = 0; i < numCameras; i++)
		{
			imageProcessingThreads.get(i).setName("ImgProc" + i);
			imageProcessingThreads.get(i).start();
		}
	}

	protected void Sleep(int ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException e)
		{
		}
	}

	public void setTimeOffset(float value)
	{
		if (!enableInstantReplay)
			return;
		if (value * BlueIrisViewer.fScreenWidth < 20)
			value = 0f;
		displayHistoryPercentageSelected = value;
		timeOffset.set((int) (value * historyLengthMs));
		masterList.SetTimeOffset((int) (value * historyLengthMs));
	}

	public boolean IsProcessingLiveCamera(int i)
	{
		if (i < numCameras)
		{
			if (downloadedImages.get(i) != null)
				return true;
			else if (timeOffset.get() == 0)
				synchronized (imagesToProcess)
				{
					if (imagesToProcess.get(i) != null)
						return true;
				}
		}
		return false;
	}

	public boolean IsProcessingCachedCamera(int i)
	{
		if (!enableInstantReplay)
			return false;
		if (i < numCameras)
		{
			if (timeOffset.get() > 0)
				synchronized (imagesToProcess)
				{
					if (imagesToProcess.get(i) != null)
						return true;
				}
		}
		return false;
	}

	public void LiveImageReceived(int id, byte[] data)
	{
		downloadedImages.set(id, new DownloadedJpeg(data, id, Utilities.getTimeInMs()));
	}

	private void setVisible(boolean isVisible)
	{
		this.isVisible = isVisible;
	}

	public void updateHistoryProgressVisibility()
	{
		long[] timeRange = masterList.getTimeRange();
		long currentHistorySpan = (timeRange[1] - timeRange[0]);
		displayHistoryPercentageTotal = (float) ((double) currentHistorySpan / (double) historyLengthMs);
	}

	public void render(SpriteBatch batch)
	{
		if (!enableInstantReplay)
			return;
		if (isVisible)
		{
			batch.draw(BlueIrisViewer.texDarkGray, 0, 0, BlueIrisViewer.fScreenWidth, 40);
			batch.draw(BlueIrisViewer.texLightGray, 0, 40, BlueIrisViewer.fScreenWidth, 3);
			batch.draw(BlueIrisViewer.texDarkGreen, 0, 0, BlueIrisViewer.fScreenWidth * displayHistoryPercentageTotal,
					40);
			batch.draw(BlueIrisViewer.texRed, BlueIrisViewer.fScreenWidth * displayHistoryPercentageSelected - 2, 0, 4,
					40);
			BlueIrisViewer.ui.DrawText(batch, GetTimeInConvenientUnit(timeOffset.get()) + " delay", 30, 30);
		}
	}

	DecimalFormat twoPlacePrecision = new DecimalFormat("#.##");

	private String GetTimeInConvenientUnit(int ms)
	{
		StringBuilder sb = new StringBuilder();
		if (ms > 3600000)
		{
			int hours = ms / 3600000;
			sb.append(hours).append(" Hours, ");
			ms = ms % 3600000;
		}
		if (ms > 60000)
		{
			int minutes = ms / 60000;
			sb.append(minutes).append(" Minutes, ");
			ms = ms % 60000;
		}
		sb.append(twoPlacePrecision.format(ms / 1000.0)).append(" Seconds");
		return sb.toString();
	}

	public void dispose()
	{
		abortThreads = true;
	}

	private boolean pointerIsDown = false;

	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		if (!enableInstantReplay || screenX > BlueIrisViewer.iScreenWidth)
			return false;
		if (pointer == 0)
			pointerIsDown = true;
		return handleDragEvent(screenX, screenY);
	}

	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		if (!enableInstantReplay || screenX > BlueIrisViewer.iScreenWidth)
			return false;
		pointerIsDown = false;
		return handleDragEvent(screenX, screenY);
	}

	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		if (!enableInstantReplay || screenX > BlueIrisViewer.iScreenWidth)
			return false;
		if (pointerIsDown)
			return handleDragEvent(screenX, screenY);
		return false;
	}

	public boolean mouseMoved(int screenX, int screenY)
	{
		if (!enableInstantReplay || screenX > BlueIrisViewer.iScreenWidth)
			return false;
		if (screenY > BlueIrisViewer.iScreenHeight - 80)
			setVisible(true);
		else
			setVisible(false);
		return false;
	}

	private boolean handleDragEvent(int screenX, int screenY)
	{
		if (screenY > BlueIrisViewer.iScreenHeight - 40)
		{
			if (BlueIrisViewer.fScreenWidth <= 0)
				setTimeOffset(0f);
			else
				setTimeOffset(screenX / BlueIrisViewer.fScreenWidth);
			return true;
		}
		return false;
	}

	public int getCurrentTimeOffset()
	{
		return timeOffset.get();
	}
}
