package org.brian.blueirisviewer;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import org.brian.blueirisviewer.util.Logger;
import org.brian.blueirisviewer.util.OSDetection;
import org.brian.blueirisviewer.util.SerializableObjectBase;

import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main
{
	static LwjglApplication app;
	static BlueIrisViewer biv;

	public static void main(String[] args)
	{
		if (OSDetection.isWindows())
		{
			try
			{
				if (OSDetection.is64Bit())
					BlueIrisViewer.bLibjpegTurboAvailable = NativeUtils.extractFileFromJar("/libturbojpeg64.dll",
							"libturbojpeg.dll");
				else
					BlueIrisViewer.bLibjpegTurboAvailable = NativeUtils.extractFileFromJar("/libturbojpeg32.dll",
							"libturbojpeg.dll");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				if (NativeUtils.extractFileFromJar("/ScreenBrightness.exe", "ScreenBrightness.exe"))
					BlueIrisViewer.sScreenBrightnessProgramPath = "ScreenBrightness.exe";
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		SerializableObjectBase.SetSerializer(new XStreamSerializer());

		BIVSettings bivSettings = new BIVSettings();
		bivSettings.Load();

		if (bivSettings.borderless)
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");

		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = bivSettings.windowTitle;
		cfg.useGL30 = false;
		cfg.width = 1280;
		cfg.height = 720;
		cfg.resizable = !bivSettings.borderless;

		if (bivSettings.loadStartPositionAndSizeUponAppStart)
		{
			// Ensure the stored position is on-screen
			int centerX = bivSettings.startPositionX + (bivSettings.startSizeW / 2);
			int centerY = bivSettings.startPositionY + (bivSettings.startSizeH / 2);

			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] allScreens = env.getScreenDevices();

			boolean positionIsOnScreen = false;
			Rectangle firstScreen = null;
			for (int i = 0; i < allScreens.length; i++)
			{
				Rectangle screen = allScreens[i].getDefaultConfiguration().getBounds();
				if (firstScreen == null)
					firstScreen = screen;
				if (screen.contains(centerX, centerY))
				{
					positionIsOnScreen = true;
					break;
				}
			}
			if (!positionIsOnScreen && firstScreen != null)
			{
				bivSettings.startPositionX = firstScreen.x + 20;
				bivSettings.startPositionY = firstScreen.y + 20;
				bivSettings.startSizeW = firstScreen.width - 100;
				bivSettings.startSizeH = firstScreen.height - 150;
			}

			cfg.x = bivSettings.startPositionX;
			cfg.y = bivSettings.startPositionY;
			cfg.width = bivSettings.startSizeW;
			cfg.height = bivSettings.startSizeH;
		}

		biv = new BlueIrisViewer(new DesktopWindowHelper());
		app = new LwjglApplication(biv, cfg);
		app.addLifecycleListener(listener);
	}

	private static LifecycleListener listener = new LifecycleListener()
	{
		@Override
		public void resume()
		{
		}

		@Override
		public void pause()
		{
		}

		@Override
		public void dispose()
		{
			app.removeLifecycleListener(listener);

			if (biv.restart)
			{
				StringBuilder cmd = new StringBuilder();
				cmd.append("\"").append(System.getProperty("java.home"));
				cmd.append(File.separator).append("bin").append(File.separator).append("java\" -jar \"");
				File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
				cmd.append(jarFile.getAbsolutePath()).append("\"");
				// try
				// {
				// Utilities.WriteTextFile("restart.txt", cmd.toString());
				// }
				// catch (Exception e1)
				// {
				// e1.printStackTrace();
				// }
				try
				{
					Runtime.getRuntime().exec(cmd.toString());
				}
				catch (IOException e)
				{
					Logger.debug(e, this);
				}
			}
		}
	};
}
