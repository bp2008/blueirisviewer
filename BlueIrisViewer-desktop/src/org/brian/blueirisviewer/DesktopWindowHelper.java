package org.brian.blueirisviewer;

import org.brian.blueirisviewer.util.IntPoint;
import org.brian.blueirisviewer.util.IntRectangle;
import org.brian.blueirisviewer.util.WindowHelper;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.DisplayMode;

public class DesktopWindowHelper implements WindowHelper
{
	@Override
	public IntRectangle GetWindowRectangle()
	{
		return new IntRectangle(org.lwjgl.opengl.Display.getX(), org.lwjgl.opengl.Display.getY(), org.lwjgl.opengl.Display.getWidth(),
				org.lwjgl.opengl.Display.getHeight());
	}

	@Override
	public void SetWindowRectangle(IntRectangle rect)
	{
		org.lwjgl.opengl.Display.setLocation(rect.x, rect.y);
		try
		{
			org.lwjgl.opengl.Display.setDisplayMode(new DisplayMode(rect.width, rect.height));
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void SetWindowPosition(IntPoint point)
	{
		org.lwjgl.opengl.Display.setLocation(point.x, point.y);
	}

	@Override
	public void SetWindowSize(IntPoint size)
	{
		try
		{
			org.lwjgl.opengl.Display.setDisplayMode(new DisplayMode(size.x, size.y));
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void SetWindowResizable(boolean resizable)
	{
		org.lwjgl.opengl.Display.setResizable(resizable);
	}
}
