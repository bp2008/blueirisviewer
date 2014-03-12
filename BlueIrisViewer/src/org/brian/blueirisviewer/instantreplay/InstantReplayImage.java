package org.brian.blueirisviewer.instantreplay;

public class InstantReplayImage
{
	public InstantReplayImage next, prev;
	public byte cameraId;
	public long time;
	public long offset;
	public int size;

	public InstantReplayImage(byte cameraId, long time, long offset, int size)
	{
		this.cameraId = cameraId;
		this.time = time;
		this.offset = offset;
		this.size = size;
	}
}
