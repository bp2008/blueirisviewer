package org.brian.blueirisviewer;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "BlueIrisViewer";
		cfg.useGL20 = false;
		cfg.width = 1280;
		cfg.height = 720;
		new LwjglApplication(new BlueIrisViewer(new DesktopWindowHelper()), cfg);
	}
}
