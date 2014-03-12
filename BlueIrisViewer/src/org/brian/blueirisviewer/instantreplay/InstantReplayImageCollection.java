package org.brian.blueirisviewer.instantreplay;

import org.brian.blueirisviewer.util.Utilities;

public class InstantReplayImageCollection
{
	InstantReplayImage head = null;
	InstantReplayImage tail = null;
	InstantReplayImage activeImage = null;
	long timeOffset = 0;

	/**
	 * Adds the image to the collection.
	 * 
	 * @param iri
	 *            The InstantReplayImage to add to the collection.
	 */
	public void add(InstantReplayImage iri)
	{
		synchronized (this)
		{
			if (head == null)
				head = tail = iri;
			else
			{
				iri.prev = tail;
				if (head.next == null)
					head.next = iri;
				tail.next = iri;
				tail = iri;
			}
		}
	}

	/**
	 * Removes and returns the oldest image in the collection.
	 * 
	 * @return
	 */
	public InstantReplayImage removeOldest()
	{
		synchronized (this)
		{
			if (head == null)
				return null;
			if (activeImage == head)
				activeImage = activeImage.next;
			InstantReplayImage oldest = head; // The oldest node is the old head.
			head = oldest.next; // The new head is the old head's next node.
			if (head == null)
				tail = null; // This was the only node, and now it is gone, so head and tail should both equal null.
			else
				head.prev = null; // The new head no longer has a previous node.
			oldest.next = null; // The old head no longer has a next node, as it is detached from the linked list.
			return oldest;
		}
	}

	public InstantReplayImage getOldest() throws Exception
	{
		synchronized (this)
		{
			return head;
		}
	}

	private InstantReplayImage getNearestImageAtOrBeforeTime(long timeInMs)
	{
		synchronized (this)
		{
			if (head == null)
				return null;
			long distFromHead = timeInMs - head.time;
			if (distFromHead <= 0)
				return head; // Requested time is older than or equal to the oldest image we have
			long distFromTail = tail.time - timeInMs;
			if (distFromTail <= 0)
				return tail; // Requested time is newer than or equal to the newest image we have

			// If we get here, we know the requested time lies somewhere between our oldest and newest images.
			if (distFromHead <= distFromTail)
			{
				// Requested time is closer to the head. Search from the head.
				InstantReplayImage node = head;
				while (node.next.time <= timeInMs)
					// Is the next node a better match for the requested time?
					node = node.next; // Yes
				return node;
			}
			else
			{
				// Requested time is closer to the tail. Search from the tail.
				InstantReplayImage node = tail;
				while (node.prev.time > timeInMs)
					// Is the previous node a better match for the requested time?
					node = node.prev; // Yes
				return node.prev; // The above loop terminated when it found that node.prev was the one we wanted.
			}
		}
	}

	public void SetTimeOffset(long timeOffset)
	{
		synchronized (this)
		{
			this.timeOffset = timeOffset;
			// if (timeOffset == 0)
			activeImage = null;
		}
	}

	/**
	 * Returns the next image, chronologically, compared to the last one. If no new image is available yet, or if the
	 * previous one has not yet expired, the previously returned image will be returned again. Null will only be
	 * returned if there are no images in the collection.
	 * 
	 * @return
	 */
	public InstantReplayImage GetNextChronologicalImage()
	{
		synchronized (this)
		{
			if (timeOffset == 0)
				return tail;
			if (activeImage == null)
			{
				activeImage = getNearestImageAtOrBeforeTime(Utilities.getTimeInMs() - timeOffset);
				return activeImage;
			}
			else
			{
				long currentTime = Utilities.getTimeInMs();
				if (activeImage.time > currentTime - timeOffset)
					return activeImage;
				if (activeImage.next == null)
					return activeImage;
				activeImage = activeImage.next;
				return activeImage;
			}
		}
	}

	public long[] getTimeRange()
	{
		synchronized (this)
		{
			if (head == null)
				return new long[] { 0, 0 };
			return new long[] { head.time, tail.time };
		}
	}

	// private InstantReplayImage binarySearch(long timeInMs, int start, int end)
	// {
	// if (end < start)
	// {
	// if (end < 0 || end >= imageArray.size())
	// return null;
	// return getItemAtZeroBasedIndex(end); // Return the older value in the likely event of an inexact match.
	// }
	// else
	// {
	// int mid = start + ((end - start) / 2);
	// InstantReplayImage iri = getItemAtZeroBasedIndex(mid);
	// if (iri == null)
	// return binarySearch(timeInMs, mid + 1, end);
	// else if (iri.time > timeInMs)
	// return binarySearch(timeInMs, start, mid - 1);
	// else if (iri.time < timeInMs)
	// return binarySearch(timeInMs, mid + 1, end);
	// else
	// return iri;
	// }
	// }
	//
	// private InstantReplayImage getItemAtZeroBasedIndex(int zeroBasedIndex)
	// {
	// zeroBasedIndex += currentIndex;
	// if (zeroBasedIndex >= imageArray.size())
	// zeroBasedIndex -= imageArray.size();
	// return imageArray.get(zeroBasedIndex);
	// }
}