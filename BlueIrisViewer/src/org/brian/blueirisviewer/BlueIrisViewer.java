package org.brian.blueirisviewer;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class BlueIrisViewer implements ApplicationListener
{
	private Object resizeLock = new Object();
	private OrthographicCamera camera;
	// private OrthographicCamera pixelPerfectCamera;
	private SpriteBatch batch;
	float screenWidth = 1;
	float screenHeight = 1;
	float imageWidth = 1;
	float imageHeight = 1;
	int cols = 1;
	int rows = 1;
	AtomicInteger fullScreenedImageId = new AtomicInteger(-1);
	long ctr = 0;
	public boolean abortThreads = false;
	BlueIrisViewerSettings bivSettings;
	Vector<Thread> downloaderThreads = new Vector<Thread>();
	Vector<Texture> textures = new Vector<Texture>();

	ConcurrentLinkedQueue<DownloadedTexture> downloadedTextures = new ConcurrentLinkedQueue<DownloadedTexture>();
	WindowHelper windowHelper;

	boolean positionInitialized = false;

	public BlueIrisViewer(WindowHelper windowHelper)
	{
		this.windowHelper = windowHelper;
	}

	@Override
	public void create()
	{
		// DisplayMode[] dms = Gdx.graphics.getDisplayModes();
		Texture.setEnforcePotImages(false);

		float w = screenWidth = Gdx.graphics.getWidth();
		float h = screenHeight = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w, h);
		camera.setToOrtho(false, w, h);
		camera.update();

		batch = new SpriteBatch();

		Gdx.input.setInputProcessor(myInputProcessor);

		Thread thrStart = new Thread(new Runnable()
		{
			public void run()
			{
				// Precalculate what we can about where to draw images
				synchronized (resizeLock)
				{
					bivSettings = new BlueIrisViewerSettings();

					double sqrt = Math.sqrt(bivSettings.imageURLs.size());
					cols = (int) Math.ceil(sqrt);
					rows = (int) sqrt;
					if (cols * rows < bivSettings.imageURLs.size())
						rows++;
					if (bivSettings.overrideGridLayoutX > 0 && bivSettings.overrideGridLayoutY > 0)
					{
						cols = bivSettings.overrideGridLayoutX;
						rows = bivSettings.overrideGridLayoutY;
					}
					imageWidth = screenWidth / cols;
					imageHeight = screenHeight / rows;
				}

				// Populate texture list
				for (int i = 0; i < bivSettings.imageURLs.size(); i++)
					textures.add(null);

				// Create downloader threads
				for (int i = 0; i < bivSettings.imageURLs.size(); i++)
					downloaderThreads.add(new Thread(new IntRunnable(i)
					{
						public void run()
						{
							String myUrl = bivSettings.imageURLs.get(myInt);
							int mySleepDelay = bivSettings.sleepDelays.get(myInt).intValue();
							while (!abortThreads)
							{
								// System.out.println("Downloading " + myUrl + Utilities.getTimeInMs());
								Pixmap pm = Images.Get(myUrl);

								if (abortThreads)
								{
									if (pm != null)
										pm.dispose();
									break;
								}

								if (pm != null)
									downloadedTextures.add(new DownloadedTexture(pm, myInt));

								int sleepFor = mySleepDelay;
								int full = fullScreenedImageId.get();

								if (full == myInt)
									sleepFor = 0;
								else if (full > -1)
									sleepFor += 1000;

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
			}
		});
		thrStart.start();
	}

	@Override
	public void dispose()
	{
		abortThreads = true;

		batch.dispose();

		downloadedTextures.clear();
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		downloadedTextures.clear();
		for (int i = 0; i < textures.size(); i++)
		{
			Texture texture = textures.get(i);
			if (texture != null)
				texture.dispose();
		}
	}

	@Override
	public void render()
	{
		if (!positionInitialized && bivSettings != null && windowHelper != null)
		{
			if (bivSettings.loadStartPositionAndSizeUponAppStart)
			{
				windowHelper.SetWindowRectangle(new IntRectangle(bivSettings.startPositionX,
						bivSettings.startPositionY, bivSettings.startSizeW, bivSettings.startSizeH));
			}
			positionInitialized = true;
		}
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		// Update GameTime
		GameTime.tick();

		// Create new textures as necessary

		DownloadedTexture dt = downloadedTextures.poll();
		while (dt != null)
		{
			// System.out.println(ctr++);
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

		int full = fullScreenedImageId.get();
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

				int rotateDegrees = bivSettings.rotateDegrees.get(i).intValue();
				boolean rotate90OneWayOrAnother = rotateDegrees == 90 || rotateDegrees == 270 || rotateDegrees == -90
						|| rotateDegrees == -270;

				Rectangle rect = new Rectangle(imageWidth * col, imageHeight * row, imageWidth, imageHeight);
				FitImageIntoRect(tex, rect, rotate90OneWayOrAnother);

				batch.draw(tex, (float) rect.x, (float) rect.y, rect.width / 2, rect.height / 2, (float) rect.width,
						(float) rect.height, 1f, 1f, (float) rotateDegrees, 0, 0, tex.getWidth(), tex.getHeight(),
						false, false);
			}
		}
		if (full > -1 && full < textures.size())
		{
			int i = full;
			Texture tex = textures.get(i);
			if (tex != null)
			{
				int rotateDegrees = bivSettings.rotateDegrees.get(i).intValue();
				boolean rotate90OneWayOrAnother = rotateDegrees == 90 || rotateDegrees == 270 || rotateDegrees == -90
						|| rotateDegrees == -270;

				Rectangle rect = new Rectangle(0, 0, screenWidth, screenHeight);
				FitImageIntoRect(tex, rect, rotate90OneWayOrAnother);

				batch.draw(tex, (float) rect.x, (float) rect.y, rect.width / 2, rect.height / 2, (float) rect.width,
						(float) rect.height, 1f, 1f, (float) rotateDegrees, 0, 0, tex.getWidth(), tex.getHeight(),
						false, false);
			}
		}

		batch.end();
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

	@Override
	public void resize(int w, int h)
	{
		synchronized (resizeLock)
		{
			screenWidth = w;
			screenHeight = h;

			if (bivSettings != null)
			{
				double sqrt = Math.sqrt(bivSettings.imageURLs.size());
				cols = (int) Math.ceil(sqrt);
				rows = (int) sqrt;
				if (cols * rows < bivSettings.imageURLs.size())
					rows++;
				if (bivSettings.overrideGridLayoutX > 0 && bivSettings.overrideGridLayoutY > 0)
				{
					cols = bivSettings.overrideGridLayoutX;
					rows = bivSettings.overrideGridLayoutY;
				}
				imageWidth = screenWidth / cols;
				imageHeight = screenHeight / rows;
			}

			camera.setToOrtho(false, w, h);
			camera.update();
		}
	}

	@Override
	public void pause()
	{
		GameTime.pause();
		if (windowHelper != null && bivSettings != null && !bivSettings.disableWindowDragging && positionInitialized)
		{
			IntRectangle currentPosition = windowHelper.GetWindowRectangle();
			bivSettings.startPositionX = currentPosition.x;
			bivSettings.startPositionY = currentPosition.y;
			bivSettings.startSizeW = currentPosition.width;
			bivSettings.startSizeH = currentPosition.height;
			bivSettings.Save();
		}
	}

	@Override
	public void resume()
	{
		GameTime.unpause();
	}

	public InputProcessor myInputProcessor = new InputProcessor()
	{
		IntRectangle storedPosition = new IntRectangle(0, 0, 1, 1);
		int downX = 0;
		int downY = 0;
		long nextMove = Long.MIN_VALUE;
		int lastDownImageId = -1;
		boolean movedSinceLastDown = false;

		@Override
		public boolean keyDown(int keycode)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean keyUp(int keycode)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean keyTyped(char character)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button)
		{
			if (pointer == 0)
			{
				if (windowHelper != null)
				{
					storedPosition = windowHelper.GetWindowRectangle();
					downX = screenX + storedPosition.x;
					downY = screenY + storedPosition.y;
					nextMove = Utilities.getTimeInMs() + 200;
					movedSinceLastDown = false;
				}
				int col = (int) (screenX / imageWidth);
				int row = (int) (screenY / imageHeight);
				lastDownImageId = (row * cols) + col;
			}
			return true;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button)
		{
			if (!movedSinceLastDown)
			{
				if (fullScreenedImageId.get() == -1)
				{
					int col = (int) (screenX / imageWidth);
					int row = (int) (screenY / imageHeight);
					if (lastDownImageId == (row * cols) + col && lastDownImageId < textures.size())
						fullScreenedImageId.set(lastDownImageId);
				}
				else
					fullScreenedImageId.set(-1);
			}
			return true;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer)
		{
			long timeNow = Utilities.getTimeInMs();
			if (pointer == 0 && windowHelper != null && timeNow > nextMove)
			{
				// If we allow moves too fast, it won't give the window time to move from the last event and everything
				// goes wrong.
				// This does cause movement to be choppy, but safe.
				nextMove = timeNow + 100;
				if (!bivSettings.disableWindowDragging)
				{
					IntRectangle currentPosition = windowHelper.GetWindowRectangle();
					int currentX = screenX + currentPosition.x;
					int currentY = screenY + currentPosition.y;
					int dx = currentX - downX;
					int dy = currentY - downY;
					if (bivSettings.preserveSizeWhenDragging)
						windowHelper.SetWindowRectangle(new IntRectangle(storedPosition.x + dx, storedPosition.y + dy,
								currentPosition.width, currentPosition.height));
					else
						windowHelper.SetWindowPosition(new IntPoint(storedPosition.x + dx, storedPosition.y + dy));
				}
				movedSinceLastDown = true;
			}
			return true;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean scrolled(int amount)
		{
			// TODO Auto-generated method stub
			return false;
		}
	};
}
