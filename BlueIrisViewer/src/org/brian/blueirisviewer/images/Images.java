package org.brian.blueirisviewer.images;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.brian.blueirisviewer.BlueIrisViewer;
import org.brian.blueirisviewer.GameTime;
import org.brian.blueirisviewer.instantreplay.InstantReplayManager;
import org.brian.blueirisviewer.ui.ServerSetupWnd;
import org.brian.blueirisviewer.util.ByteArrayPool;
import org.brian.blueirisviewer.util.Encryption;
import org.brian.blueirisviewer.util.HttpHelper;
import org.brian.blueirisviewer.util.IntPoint;
import org.brian.blueirisviewer.util.IntRunnable;
import org.brian.blueirisviewer.util.Logger;
import org.brian.blueirisviewer.util.PostData;
import org.brian.blueirisviewer.util.ReAuthenticateException;
import org.brian.blueirisviewer.util.Utilities;
import org.brian.blueirisviewer.util.string;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJDecompressor;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Images
{
	public Vector<String> allCameraNames = new Vector<String>();
	public Vector<Boolean> allCameraVisibility = new Vector<Boolean>();
	public Vector<String> cameraNames = new Vector<String>();
	public Vector<IntPoint> cameraResolutions = new Vector<IntPoint>();
	public Vector<Integer> sleepDelays = new Vector<Integer>();
	public Vector<Integer> rotateDegrees = new Vector<Integer>();
	public Vector<Rectangle> blueIrisRectsProportional = new Vector<Rectangle>();
	public Vector<Rectangle> blueIrisRectsPrecalc = new Vector<Rectangle>();
	private int mainImageWidth = 100;
	private int mainImageHeight = 100;
	private Rectangle blueIrisLayoutModelRect = new Rectangle(0, 0, 100, 100);
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
	long sessionLostTime = 0;
	boolean isHandlingSessionFailure = false;
	Object sessionFailureLock = new Object();

	public int numCams = 0;
	public int totalRawImageBytes = 0;

	String processedServerURL;

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

	Pattern cameraPattern = Pattern.compile("<option[\\w\\W]+?value=\"([^\"]*?)\">([^<]*?)</option>");

	public Images()
	{
	}

	public void Initialize()
	{
		if (isInitialized || isInitializing)
			return;
		Thread thrStart = new Thread(new Runnable()
		{
			@SuppressWarnings("unchecked")
			public void run()
			{
				if (isInitialized || isInitializing)
					return;
				synchronized (Images.this)
				{
					Utilities.sessionCookie = "";
					if (isInitialized || isInitializing)
						return;
					try
					{
						screenWidth.set(BlueIrisViewer.iScreenWidth);
						screenHeight.set(BlueIrisViewer.iScreenHeight);
						isInitializing = true;
						processedServerURL = ProcessURL(BlueIrisViewer.bivSettings.serverURL);
						try
						{
							JSONObject camlistObj = new JSONObject();
							camlistObj.put("cmd", "camlist");
							String responseStr = Utilities.getStringViaHttpConnection(processedServerURL + "json",
									new PostData(camlistObj));

							JSONParser jsonParser = new JSONParser();
							JSONObject responseObj = (JSONObject) jsonParser.parse(responseStr);

							if (isFailResponse(responseObj))
							{
								// Must log in
								serverRequiresAuthentication = true;

								if (!TryLogin())
									return;
								serverRequiresAuthentication = false;

								camlistObj.put("session", Utilities.sessionCookie);
								responseStr = Utilities.getStringViaHttpConnection(processedServerURL + "json",
										new PostData(camlistObj));
								responseObj = (JSONObject) jsonParser.parse(responseStr);
							}

							if (isFailResponse(responseObj))
								return;

							// Precalculate what we can about where to draw images
							synchronized (BlueIrisViewer.getResizeLock())
							{
								synchronized (allCameraNames)
								{
									JSONArray camArr = (JSONArray) responseObj.get("data");
									for (int i = 0; i < camArr.size(); i++)
									{
										JSONObject camDef = (JSONObject) camArr.get(i);
										if (camDef.containsKey("group"))
										{
											if (allCameraNames.size() == 0)
											{
												// This is the first group. Populate the camera data lists.
												mainImageWidth = (int) ((Long) camDef.get("width")).longValue();
												mainImageHeight = (int) ((Long) camDef.get("height")).longValue();

												JSONArray groupCams = (JSONArray) camDef.get("group");
												for (int n = 0; n < groupCams.size(); n++)
												{
													String name = (String) groupCams.get(n);
													allCameraNames.add(name);
													if (!SettingsSayToHideCamera(name))
													{
														numCams++;
														allCameraVisibility.add(true);
														cameraNames.add(name);
														cameraResolutions.add(new IntPoint(10, 10));
														sleepDelays.add(250); // Note: This delay is not used
														rotateDegrees.add(0);
													}
													else
														allCameraVisibility.add(false);
												}
												JSONArray groupCamsRects = (JSONArray) camDef.get("rects");
												for (int n = 0; n < groupCamsRects.size(); n++)
												{
													if (allCameraVisibility.get(n))
													{
														JSONArray rectData = (JSONArray) groupCamsRects.get(n);
														int left = (int) ((Long) rectData.get(0)).longValue();
														int top = (int) ((Long) rectData.get(1)).longValue();
														int right = (int) ((Long) rectData.get(2)).longValue();
														int bottom = (int) ((Long) rectData.get(3)).longValue();
														float x = (float) left / (float) mainImageWidth;
														float y = (float) ((mainImageHeight - top) - (bottom - top))
																/ (float) mainImageHeight;
														float w = (float) (right - left) / (float) mainImageWidth;
														float h = (float) (bottom - top) / (float) mainImageHeight;
														blueIrisRectsProportional.addElement(new Rectangle(x, y, w, h));
														blueIrisRectsPrecalc.addElement(new Rectangle(x, y, w, h));
													}
												}
											}
										}
										else
										{
											String name = (String) camDef.get("optionValue");
											int width = (int) ((Long) camDef.get("width")).longValue();
											int height = (int) ((Long) camDef.get("height")).longValue();
											
											if (allCameraNames.size() == 0)
											{
												// If we get here, there is no group defined. We should show only the first camera.
												mainImageWidth = width;
												mainImageHeight = height;
												allCameraNames.add(name);
												if (!SettingsSayToHideCamera(name))
												{
													numCams++;
													allCameraVisibility.add(true);
													cameraNames.add(name);
													cameraResolutions.add(new IntPoint(10, 10));
													sleepDelays.add(250); // Note: This delay is not used
													rotateDegrees.add(0);
													int left = 0;
													int top = 0;
													int right = width;
													int bottom = height;
													float x = (float) left / (float) mainImageWidth;
													float y = (float) ((mainImageHeight - top) - (bottom - top))
															/ (float) mainImageHeight;
													float w = (float) (right - left) / (float) mainImageWidth;
													float h = (float) (bottom - top) / (float) mainImageHeight;
													blueIrisRectsProportional.addElement(new Rectangle(x, y, w, h));
													blueIrisRectsPrecalc.addElement(new Rectangle(x, y, w, h));
												}
												else
													allCameraVisibility.add(false);
											}
											
											if (allCameraNames.size() > 0)
											{
												// This is a normal camera appearing after the first group.
												// Record the image dimensions.
												int idx = cameraNames.indexOf(name);
												if (idx > -1)
												{
													cameraResolutions.set(idx, new IntPoint(width, height));
													totalRawImageBytes += width * height * 3;
												}
											}
										}
									}
								}
								double sqrt = Math.sqrt(numCams);
								cols = (int) Math.ceil(sqrt);
								rows = (int) sqrt;
								if (cols * rows < numCams)
									rows++;
								HandleOverrideGridSize();
								imageWidth = BlueIrisViewer.fScreenWidth / cols;
								imageHeight = BlueIrisViewer.fScreenHeight / rows;
								aImageWidth.set((int) Math.ceil(imageWidth));
								aImageHeight.set((int) Math.ceil(imageHeight));

								blueIrisLayoutModelRect = new Rectangle(0, BlueIrisViewer.fScreenHeight,
										mainImageWidth, mainImageHeight);
								ScaleRectangleToFitScreenCentered(blueIrisLayoutModelRect);
								PrecalculateBlueIrisLayoutRectangles();
							}
						}
						catch (ParseException ex)
						{
							Logger.debug(ex, this);
						}

						instantReplayManager = new InstantReplayManager(numCams,
								BlueIrisViewer.bivSettings.instantReplayEnabled,
								BlueIrisViewer.bivSettings.instantReplayHistoryLengthMinutes);

						// Populate texture list
						for (int i = 0; i < numCams; i++)
							textures.add(null);

						// Create downloader threads
						for (int i = 0; i < numCams; i++)
							downloaderThreads.add(new Thread(new IntRunnable(i)
							{
								public void run()
								{
									String jpegUrl = processedServerURL + "image/" + cameraNames.get(myInt);
									while (!abortThreads)
									{
										try
										{
											if (BlueIrisViewer.bivSettings.useMjpegStream)
											{
												HandleThrottledMjpegStreaming();
											}
											else
											{
												HandleRefreshingJpegStreaming(jpegUrl);
											}
										}
										catch (ReAuthenticateException ex)
										{
											synchronized (sessionFailureLock)
											{
												if (!isHandlingSessionFailure && !abortThreads
														&& Math.abs(sessionLostTime - GameTime.getRealTime()) > 10000)
												{
													isHandlingSessionFailure = true;
													if (ex != null)
														Logger.debug("Blue Iris session lost", Images.class);
													if (!TryLogin())
													{
														abortThreads = true;
														serverRequiresAuthentication = true;
														sessionLostTime = GameTime.getRealTime();
														isHandlingSessionFailure = false;
														return;
													}
													sessionLostTime = GameTime.getRealTime();
													isHandlingSessionFailure = false;
												}
											}
										}
									}
								}

								public void HandleRefreshingJpegStreaming(String jpegUrl)
										throws ReAuthenticateException
								{
									int sleepFor = BlueIrisViewer.bivSettings.imageRefreshDelayMS;

									if (instantReplayManager.IsProcessingLiveCamera(myInt))
									{
										// If we get here, the downloader is getting ahead of the render thread.
										sleepFor = 5;
									}
									else
									{
										int full = fullScreenedImageId.get();

										byte[] img = Utilities.getViaHttpConnection(
												jpegUrl + GetImageModeQueryString(myInt, full), null);

										if (abortThreads)
											return;
										else if (img.length > 0)
											instantReplayManager.LiveImageReceived(myInt, img);
										if (!BlueIrisViewer.bivSettings.instantReplayEnabled
												|| BlueIrisViewer.images.instantReplayManager.getCurrentTimeOffset() == 0)
										{
											if (full == myInt)
												sleepFor = 0;
											else if (full > -1)
												sleepFor += 1000;
										}
									}

									if (sleepFor > 0 && !abortThreads)
										GradualSleepWithEarlyBreak(sleepFor);
								}

								public void HandleThrottledMjpegStreaming()
								{
									String mjpegUrl = processedServerURL + "mjpg/" + cameraNames.get(myInt)
											+ "/video.mjpg";
									try
									{
										HttpHelper httpHelper = new HttpHelper(mjpegUrl);
										InputStream inStream = httpHelper.GET();
										if (inStream == null)
											return;
										try
										{
											while (!abortThreads && BlueIrisViewer.bivSettings.useMjpegStream)
											{
												HttpHelper.ReadUntilCompleteStringFound("Content-Length: ", inStream);
												String sLength = HttpHelper.ReadUntilCharFound('\r', inStream).trim();
												int jpegLength = Utilities.ParseInt(sLength, -1);
												if (jpegLength < 0)
													throw new Exception("Content-Length was " + sLength);
												if (abortThreads)
													break;

												// Keep reading chars one by one until we reach the end of a "\r\n\r\n"
												// (indicating the end of the http headers).
												HttpHelper.ReadUntilCompleteStringFound("\n\r\n", inStream); // We
																												// already
																												// consumed
																												// a \r
																												// character
																												// after
																												// reading
																												// the
																												// length
																												// of
																												// the
																												// image.

												// Now the jpeg data begins, and it has length jpegLength
												byte[] jpegBuffer = new byte[jpegLength];
												int read = 0, newRead = 0;
												while (read < jpegLength)
												{
													newRead = inStream.read(jpegBuffer, read, jpegBuffer.length - read);
													if (newRead == 0)
														throw new Exception("EOF reading image data");
													read += newRead;
												}

												if (abortThreads)
													break;
												else if (jpegBuffer.length > 0)
												{
													// Don't submit this frame until the instant replay
													// manager is ready for it.
													while (instantReplayManager.IsProcessingLiveCamera(myInt))
													{
														GradualSleepWithEarlyBreak(5);
														if (abortThreads)
															break;
													}
													instantReplayManager.LiveImageReceived(myInt, jpegBuffer);
												}
												int sleepFor = BlueIrisViewer.bivSettings.imageRefreshDelayMS;
												// If we get here, we just submitted an image
												// to the instant replay manager.
												if (!BlueIrisViewer.bivSettings.instantReplayEnabled
														|| BlueIrisViewer.images.instantReplayManager
																.getCurrentTimeOffset() == 0)
												{
													int full = fullScreenedImageId.get();
													if (full == myInt)
														sleepFor = 0;
													else if (full > -1)
														sleepFor += 1000;
												}

												if (sleepFor > 0 && !abortThreads)
												{
													GradualSleepWithEarlyBreak(sleepFor);
												}
											}
										}
										finally
										{
											inStream.close();
										}
									}
									catch (Exception ex)
									{
										Logger.debug(ex, this);
									}
								}

								private void GradualSleepWithEarlyBreak(int totalSleepTimeMs)
								{
									if (totalSleepTimeMs <= 0)
										return;
									if (totalSleepTimeMs <= 5)
									{
										try
										{
											Thread.sleep(totalSleepTimeMs);
										}
										catch (InterruptedException e)
										{
										}
										return;
									}

									long totalNanoTimeToSleep = (long) totalSleepTimeMs * 1000000L;
									long startNanoTime = System.nanoTime();
									int sleepFor = Math.max(totalSleepTimeMs / 100, 5);
									try
									{
										while (System.nanoTime() - startNanoTime < totalNanoTimeToSleep
												&& !isCurrentCameraInNoDelayMode() && !abortThreads)
											Thread.sleep(sleepFor);
									}
									catch (InterruptedException e)
									{
									}
								}

								private boolean isCurrentCameraInNoDelayMode()
								{
									return myInt == fullScreenedImageId.get()
											&& (!BlueIrisViewer.bivSettings.instantReplayEnabled || BlueIrisViewer.images.instantReplayManager
													.getCurrentTimeOffset() == 0);
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

			@SuppressWarnings("unchecked")
			private boolean TryLogin()
			{
				try
				{
					JSONObject loginObj = new JSONObject();
					loginObj.put("cmd", "login");
					String responseStr = Utilities.getStringViaHttpConnection(processedServerURL + "json",
							new PostData(loginObj));
					JSONParser jsonParser = new JSONParser();
					JSONObject responseObj = (JSONObject) jsonParser.parse(responseStr);
					Utilities.sessionCookie = (String) responseObj.get("session");
					loginObj.put("session", Utilities.sessionCookie);
					String pwDecrypted = string.IsNullOrEmpty(BlueIrisViewer.bivSettings.password) ? "" : Encryption
							.Decrypt(BlueIrisViewer.bivSettings.password);
					if (pwDecrypted.equals(BlueIrisViewer.bivSettings.password)
							&& !string.IsNullOrEmpty(BlueIrisViewer.bivSettings.password))
					{
						BlueIrisViewer.bivSettings.password = Encryption.Encrypt(pwDecrypted);
						BlueIrisViewer.bivSettings.Save();
					}
					loginObj.put(
							"response",
							Utilities.Hex_MD5(BlueIrisViewer.bivSettings.username + ":" + Utilities.sessionCookie + ":"
									+ pwDecrypted));
					responseStr = Utilities.getStringViaHttpConnection(processedServerURL + "json", new PostData(
							loginObj));
					responseObj = (JSONObject) jsonParser.parse(responseStr);

					if (isFailResponse(responseObj))
					{
						return false;
					}
					Utilities.sessionCookie = (String) responseObj.get("session");
					return true;
				}
				catch (ParseException ex)
				{
					Logger.debug(ex, this);
					return false;
				}
			}

			private boolean isFailResponse(JSONObject responseObj)
			{
				return responseObj == null
						|| (responseObj.containsKey("result") && responseObj.get("result").equals("fail"));
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

				sb.append(serverURL.trim());

				if (!serverURL.endsWith("/"))
					sb.append("/");

				return sb.toString();
			}
		});
		thrStart.start();
	}

	// public static Pixmap Get(String url)
	// {
	// byte[] img = Utilities.getViaHttpConnection(url, null);
	// if (img.length > 0)
	// try
	// {
	// return new Pixmap(img, 0, img.length);
	// }
	// catch (Exception ex)
	// {
	// }
	// return null;
	// }

	public static Pixmap Get(byte[] img)
	{
		PixelManipulator pm;
		int pixelManipulationMode;
		if (BlueIrisViewer.nightModeManager.isNightModeNow())
			pixelManipulationMode = BlueIrisViewer.bivSettings.pixelManipulationNightMode;
		else
			pixelManipulationMode = BlueIrisViewer.bivSettings.pixelManipulationDayMode;
		switch (pixelManipulationMode)
		{
			case 1:
				pm = PixelManipulator.KeepRedDropOtherChannels();
				break;
			case 2:
				pm = PixelManipulator.MakeRedBrightestChannel();
				break;
			case 3:
				pm = PixelManipulator.MakeRedAverageBrightness();
				break;
			default:
				pm = PixelManipulator.DoNothing();
		}
		return Get(img, pm);
	}

	public static Pixmap Get(byte[] img, PixelManipulator pixelManipulator)
	{
		if (img.length > 0)
			try
			{
				if (BlueIrisViewer.bLibjpegTurboAvailable && BlueIrisViewer.bivSettings.useLibjpegTurbo)
				{
					try
					{
						TJDecompressor tjd = new TJDecompressor(img);
						int w = tjd.getWidth();
						int h = tjd.getHeight();
						Pixmap pm = new Pixmap(w, h, Format.RGB888);
						ByteBuffer bb = pm.getPixels();
						bb.rewind();
						int pixelSize = TJ.getPixelSize(TJ.PF_RGB);
						byte[] rgbData = ByteArrayPool.getArray(w * h * pixelSize);
						tjd.decompress(rgbData, 0, 0, w, w * pixelSize, h, TJ.PF_RGB, 0);
						pixelManipulator.OperateOnRGB(rgbData);
						bb.put(rgbData);
						ByteArrayPool.returnArrayToPool(rgbData);
						bb.rewind();
						tjd.close();
						return pm;
					}
					catch (NoClassDefFoundError ex)
					{
						BlueIrisViewer.bLibjpegTurboAvailable = false;
					}
					catch (UnsatisfiedLinkError ex)
					{
						BlueIrisViewer.bLibjpegTurboAvailable = false;
					}
					catch (Exception ex)
					{
						Logger.debug(ex, Images.class);
					}
				}
				return new Pixmap(img, 0, img.length);
			}
			catch (Exception ex)
			{
				Logger.debug(ex, Images.class);
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
				newTex = new Texture(dt.data, false);
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
		else if (isHandlingSessionFailure)
		{
			BlueIrisViewer.ui.DrawText(batch, "Session Lost. Attempting to reauthenticate.", 10,
					BlueIrisViewer.fScreenHeight - 15);
			BlueIrisViewer.ui.DrawText(batch,
					"If you need to change the server options, press 'o' to open the options.", 10,
					BlueIrisViewer.fScreenHeight - 35);
		}
		else if (serverRequiresAuthentication)
		{
			BlueIrisViewer.ui.DrawText(batch, "Unable to reauthenticate with configured user name and password.", 10,
					BlueIrisViewer.fScreenHeight - 15);
			BlueIrisViewer.ui.DrawText(batch,
					"If you need to change the server options, press 'o' to open the options.", 10,
					BlueIrisViewer.fScreenHeight - 35);
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
					|| (BlueIrisViewer.bivSettings.imageFillMode == 1 && full == -1)
					|| (BlueIrisViewer.bivSettings.imageFillMode == 2))
			{
				// In this block we will render every camera (except the fullscreened one) in its grid position
				// This block is skipped if imageFillMode is 1 ("Stretch to Fill") and a camera is full screened.

				for (int i = 0; i < textures.size(); i++)
				{
					if (full == i)
						continue;
					Texture tex = textures.get(i);
					if (tex != null)
					{
						if (BlueIrisViewer.bivSettings.imageFillMode == 0
								|| BlueIrisViewer.bivSettings.imageFillMode == 1)
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
						else if (BlueIrisViewer.bivSettings.imageFillMode == 2)
						{
							Rectangle rect = blueIrisRectsPrecalc.get(i);

							batch.draw(tex, (float) rect.x, (float) rect.y, rect.width / 2, rect.height / 2,
									(float) rect.width, (float) rect.height, 1f, 1f, 0, 0, 0, tex.getWidth(),
									tex.getHeight(), false, false);
						}
					}
				}
			}
			if (full > -1 && full < textures.size())
			{
				// In this block we will render the fullscreened camera
				int i = full;
				Texture tex = textures.get(i);
				if (tex != null)
				{
					int rot = rotateDegrees.get(i).intValue();
					boolean rotate90OneWayOrAnother = rot == 90 || rot == 270 || rot == -90 || rot == -270;

					Rectangle rect = new Rectangle(0, 0, BlueIrisViewer.fScreenWidth, BlueIrisViewer.fScreenHeight);
					if (BlueIrisViewer.bivSettings.imageFillMode == 0 || BlueIrisViewer.bivSettings.imageFillMode == 2)
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

	private void PrecalculateBlueIrisLayoutRectangles()
	{
		for (int i = 0; i < blueIrisRectsProportional.size(); i++)
		{
			Rectangle propRect = blueIrisRectsProportional.get(i);
			float x = (propRect.x * blueIrisLayoutModelRect.width) + blueIrisLayoutModelRect.x;
			float y = (propRect.y * blueIrisLayoutModelRect.height) + blueIrisLayoutModelRect.y;
			Rectangle preCalcRect = new Rectangle(x, y, propRect.width * blueIrisLayoutModelRect.width, propRect.height
					* blueIrisLayoutModelRect.height);
			blueIrisRectsPrecalc.set(i, preCalcRect);
		}
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

	private void ScaleRectangleToFitScreenCentered(Rectangle rect)
	{
		if (rect.height == 0 || rect.width == 0)
		{
			rect.x = rect.y = rect.width = rect.height = 0;
			return;
		}
		double ow = rect.width; // original width
		double oh = rect.height; // original height
		double or = ow / oh; // original aspect ratio
		double sw = screenWidth.get();
		double sh = screenHeight.get();
		double sr = sw / sh;
		if (or > sr)
		{
			// Rectangle will be shorter than the screen (black bars on top and bottom)
			rect.width = (float) sw;
			rect.height = (float) (sw / or);
		}
		else
		{
			// Rectangle will be narrower than the screen (black bars on sides)
			rect.height = (float) sh;
			rect.width = (float) (sh * or);
		}
		rect.x = ((float) sw - rect.width) / 2f;
		rect.y = ((float) sh - rect.height) / 2f;
	}

	private void FitImageIntoRect(Texture tex, Rectangle rect, boolean rotate90)
	{
		if (rotate90)
		{
			// Calculate the rectangle position that we want the image to end up in (easy)
			double oh = tex.getWidth(); // width and height are swapped here intentionally
			double ow = tex.getHeight(); // width and height are swapped here intentionally
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
			double sqrt = Math.sqrt(numCams);
			cols = (int) Math.ceil(sqrt);
			rows = (int) sqrt;
			if (cols * rows < numCams)
				rows++;
			HandleOverrideGridSize();
			imageWidth = (float) w / cols;
			imageHeight = (float) h / rows;
			aImageWidth.set((int) Math.ceil(imageWidth));
			aImageHeight.set((int) Math.ceil(imageHeight));
		}
		blueIrisLayoutModelRect = new Rectangle(0, h, mainImageWidth, mainImageHeight);
		ScaleRectangleToFitScreenCentered(blueIrisLayoutModelRect);
		PrecalculateBlueIrisLayoutRectangles();
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

	public String[] GetCameraNamesStringArray()
	{
		synchronized (allCameraNames)
		{
			if (this.allCameraNames.size() == 0)
				return null;
			String[] cameraNamesStrings = new String[this.allCameraNames.size()];
			for (int i = 0; i < this.allCameraNames.size(); i++)
				cameraNamesStrings[i] = allCameraNames.get(i);
			return cameraNamesStrings;
		}
	}
}
