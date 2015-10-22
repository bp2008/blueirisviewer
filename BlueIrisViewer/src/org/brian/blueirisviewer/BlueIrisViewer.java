package org.brian.blueirisviewer;

import org.brian.blueirisviewer.images.Images;
import org.brian.blueirisviewer.ui.MainOptionsWnd;
import org.brian.blueirisviewer.ui.UI;
import org.brian.blueirisviewer.ui.WindowOptionsWnd;
import org.brian.blueirisviewer.util.IntPoint;
import org.brian.blueirisviewer.util.IntRectangle;
import org.brian.blueirisviewer.util.NightModeManager;
import org.brian.blueirisviewer.util.Utilities;
import org.brian.blueirisviewer.util.WindowHelper;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BlueIrisViewer implements ApplicationListener
{
	private static Object resizeLock = new Object();

	public static Object getResizeLock()
	{
		return resizeLock;
	}

	public boolean restart = false;

	private OrthographicCamera camera;
	// private OrthographicCamera pixelPerfectCamera;
	private SpriteBatch batch;

	public static float fScreenWidth = 1;
	public static float fScreenHeight = 1;
	public static int iScreenWidth = 1;
	public static int iScreenHeight = 1;

	public static Images images;
	public static UI ui;
	public static BIVSettings bivSettings;
	public static NightModeManager nightModeManager;

	private long lastResize = 0;
	private long lastHandledResize = 0;
	private boolean isDraggingButton0 = false;
	private long skipThisResize = 0;

	public static WindowHelper windowHelper;

	public static Texture texLightGray, texDarkGreen, texDarkGray, texRed;

	public static boolean bLibjpegTurboAvailable = false;
	public static String sScreenBrightnessProgramPath = null;

	public BlueIrisViewer(WindowHelper windowHelper)
	{
		BlueIrisViewer.windowHelper = windowHelper;
	}

	@Override
	public void create()
	{
		// DisplayMode[] dms = Gdx.graphics.getDisplayModes();
		// Texture.setEnforcePotImages(false);

		bivSettings = new BIVSettings();
		bivSettings.Load();
		float w = fScreenWidth = iScreenWidth = Gdx.graphics.getWidth();
		float h = fScreenHeight = iScreenHeight = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w, h);
		camera.setToOrtho(false, w, h);
		camera.update();

		batch = new SpriteBatch();

		nightModeManager = new NightModeManager();
		
		Gdx.input.setInputProcessor(myInputProcessor);

		ui = new UI();

		if (bivSettings.restartBorderlessToggle)
		{
			// The restartBorderlessToggle flag should no longer be used.
			bivSettings.restartBorderlessToggle = false;
			bivSettings.Save();
			ui.openWindow(MainOptionsWnd.class);
			ui.openWindow(WindowOptionsWnd.class);
		}

		texLightGray = Create1x1ColorTexture(new Color(0.667f, 0.667f, 0.667f, 0.667f));
		texDarkGreen = Create1x1ColorTexture(new Color(0f, 0.444f, 0f, 0.5f));
		texDarkGray = Create1x1ColorTexture(new Color(0.333f, 0.333f, 0.333f, 0.5f));
		texRed = Create1x1ColorTexture(new Color(1f, 0f, 0f, 0.667f));

		images = new Images();
		images.Initialize();
	}

	private Texture Create1x1ColorTexture(Color color)
	{
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(color);
		pixmap.fill();
		return new Texture(pixmap);
	}

	@Override
	public void dispose()
	{
		images.dispose();
		ui.dispose();
		batch.dispose();

		texLightGray.dispose();
		texDarkGreen.dispose();
		texDarkGray.dispose();
		texRed.dispose();
	}

	@Override
	public void render()
	{
		if (bivSettings.restartBorderlessToggle)
		{
			BlueIrisViewer.bivSettings.Save();
			restart = true;
			Gdx.app.exit();
		}
		if (lastHandledResize != lastResize && skipThisResize != lastResize
				&& lastResize + 250 < GameTime.getRealTime())
		{
			// This code prevents an issue where you can resize the window and then drag it with the touch events,
			// causing the window to revert to its previous size
			// Last resize was at least 250 ms ago, and we haven't yet handled it by setting the DisplayMode.
			lastHandledResize = lastResize;
			if (windowHelper != null)
				windowHelper.SetWindowRectangle(windowHelper.GetWindowRectangle());
		}
		
		nightModeManager.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		// Update GameTime
		GameTime.tick();

		images.render(batch);

		batch.end();
		ui.render();
	}

	@Override
	public void resize(int w, int h)
	{
		synchronized (resizeLock)
		{
			if (!isDraggingButton0 && !bivSettings.disableWindowDragging && !bivSettings.borderless)
			{
				// This code prevents an issue where you can resize the window and then drag it with the touch events,
				// causing the window to revert to its previous size
				System.out.println(w + "x" + h + " isDraggingButton0: " + isDraggingButton0);
				lastResize = GameTime.getRealTime();
				if (skipThisResize == 0)
					skipThisResize = lastResize;
			}

			fScreenWidth = iScreenWidth = w;
			fScreenHeight = iScreenHeight = h;

			images.resize(w, h);
			ui.resize(w, h);

			camera.setToOrtho(false, w, h);
			camera.update();
		}
	}

	@Override
	public void pause()
	{
		// GameTime.pause();
		if (windowHelper != null && bivSettings != null)
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
		// GameTime.unpause();
	}

	public InputProcessor myInputProcessor = new InputProcessor()
	{
		IntRectangle windowPosAtMouseDown = new IntRectangle(0, 0, 1, 1);
		int mouseDownGlobalCoordX = 0;
		int mouseDownGlobalCoordY = 0;
		long nextMove = Long.MIN_VALUE;
		int lastDownImageId = -1;
		boolean movedSinceLastDown = false;

		@Override
		public boolean keyDown(int keycode)
		{
			return ui.stage.keyDown(keycode);
		}

		@Override
		public boolean keyUp(int keycode)
		{
			return ui.stage.keyUp(keycode);
		}

		@Override
		public boolean keyTyped(char character)
		{
			if (ui.stage.keyTyped(character))
				return true;
			if (character == 'o' || character == 'O' || character == '' /* ESC */)
			{
				if (ui.isAnyWindowOpen())
					ui.closeAllUIWindows();
				else
					ui.openWindow(MainOptionsWnd.class);
				return true;
			}
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button)
		{
			if (ui.stage.touchDown(screenX, screenY, pointer, button)
					|| (images != null && images.instantReplayManager != null && images.instantReplayManager.touchDown(
							screenX, screenY, pointer, button)))
				return true;
			if (pointer == 0 && button == 0)
			{
				isDraggingButton0 = true;
				if (windowHelper != null)
				{
					// Store the current window position in global coordinates
					windowPosAtMouseDown = windowHelper.GetWindowRectangle();
					// Store the mouse position in global coordinates
					mouseDownGlobalCoordX = screenX + windowPosAtMouseDown.x;
					mouseDownGlobalCoordY = screenY + windowPosAtMouseDown.y;
					nextMove = Utilities.getTimeInMs() + 200;
					movedSinceLastDown = false;
				}
				int col = (int) (screenX / images.getImageWidth());
				int row = (int) (screenY / images.getImageHeight());
				lastDownImageId = (row * images.getColCount()) + col;
			}
			return true;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button)
		{
			if (button == 0)
				isDraggingButton0 = false;
			if (ui.stage.touchUp(screenX, screenY, pointer, button))
				return true;
			if (images != null && images.instantReplayManager != null
					&& images.instantReplayManager.touchUp(screenX, screenY, pointer, button))
				return true;

			if (images.getNumImages() > 1 && !movedSinceLastDown && button == 0)
			{
				if (images.getFullScreenedImageId() == -1)
				{
					if (BlueIrisViewer.bivSettings.imageFillMode == 2)
					{
						int y = (int)BlueIrisViewer.fScreenHeight - screenY;
						for (int i = 0; i < images.blueIrisRectsPrecalc.size(); i++)
						{
							if (images.blueIrisRectsPrecalc.get(i).contains(screenX, y))
								images.setFullScreenedImageId(i);
						}
					}
					else
					{
						int col = (int) (screenX / images.getImageWidth());
						int row = (int) (screenY / images.getImageHeight());
						if (lastDownImageId == (row * images.getColCount()) + col
								&& lastDownImageId < images.getNumImages())
							images.setFullScreenedImageId(lastDownImageId);
					}
				}
				else
					images.setFullScreenedImageId(-1);
			}
			return true;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer)
		{
			if (ui.stage.touchDragged(screenX, screenY, pointer)
					|| (images != null && images.instantReplayManager != null && images.instantReplayManager
							.touchDragged(screenX, screenY, pointer)))
				return true;
			long timeNow = Utilities.getTimeInMs();
			if (pointer == 0 && windowHelper != null && timeNow > nextMove && isDraggingButton0)
			{
				// If we allow moves too fast, it won't give the window time to move from the
				// last event and everything goes wrong.
				// This does cause movement to be choppy, but safe.
				nextMove = timeNow + 16;
				if (!bivSettings.disableWindowDragging)
				{
					// Get the window's current position so we can find the mouse's global coordinates
					IntRectangle currentWindowPosition = windowHelper.GetWindowRectangle();
					int mouseNowGlobalCoordX = screenX + currentWindowPosition.x;
					int mouseNowGlobalCoordY = screenY + currentWindowPosition.y;
					// Find the mouse's current distance from its position when the button was pressed down
					int dx = mouseNowGlobalCoordX - mouseDownGlobalCoordX;
					int dy = mouseNowGlobalCoordY - mouseDownGlobalCoordY;
					windowHelper.SetWindowPosition(new IntPoint(windowPosAtMouseDown.x + dx, windowPosAtMouseDown.y
							+ dy));
				}
				movedSinceLastDown = true;
			}
			return true;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY)
		{
			return ui.stage.mouseMoved(screenX, screenY)
					|| (images != null && images.instantReplayManager != null && images.instantReplayManager
							.mouseMoved(screenX, screenY));
		}

		@Override
		public boolean scrolled(int amount)
		{
			return ui.stage.scrolled(amount);
		}
	};
}
