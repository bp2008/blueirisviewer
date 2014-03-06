package org.brian.blueirisviewer;

public interface WindowHelper
{
	public IntRectangle GetWindowRectangle();
	public void SetWindowRectangle(IntRectangle rect);
	public void SetWindowPosition(IntPoint point);
	public void SetWindowSize(IntPoint size);
}
