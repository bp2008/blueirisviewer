package org.brian.blueirisviewer;

import java.io.File;
import java.io.IOException;

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
		SerializableObjectBase.SetSerializer(new XStreamSerializer());

		BIVSettings bivSettings = new BIVSettings();
		bivSettings.Load();
		
		if (bivSettings.borderless)
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");

		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "BlueIrisViewer";
		cfg.useGL20 = false;
		cfg.width = 1280;
		cfg.height = 720;
		cfg.resizable = !bivSettings.borderless;

		if (bivSettings.loadStartPositionAndSizeUponAppStart)
		{
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
					e.printStackTrace();
				}
			}
		}
	};
}
