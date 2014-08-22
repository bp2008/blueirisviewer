package org.brian.blueirisviewer.util;


public interface WindowHelper
{
	public IntRectangle GetWindowRectangle();
	public void SetWindowRectangle(IntRectangle rect);
	public void SetWindowPosition(IntPoint point);
	public void SetWindowSize(IntPoint size);
	public void SetWindowResizable(boolean resizable);
	public void SetWindowBorderless(boolean borderless);
}
