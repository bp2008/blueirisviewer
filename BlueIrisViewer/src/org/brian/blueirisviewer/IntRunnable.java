package org.brian.blueirisviewer;

public abstract class IntRunnable implements Runnable
{
	public int myInt;

	public IntRunnable(int myInt)
	{
		this.myInt = myInt;
	}
}
