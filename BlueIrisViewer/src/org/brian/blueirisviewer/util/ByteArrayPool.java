package org.brian.blueirisviewer.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.brian.blueirisviewer.GameTime;

public class ByteArrayPool
{
	private static ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>> arrayPool = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>>();
	private static ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>> previousArrayPool = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>>();
	//private static AtomicInteger pooledCount = new AtomicInteger(0);
	private static AtomicLong generationCounter = new AtomicLong(GameTime.getRealTime() / 5000);

	/**
	 * Returns a byte array of the specified size. The byte array may contain garbage data from a previous use, so
	 * assume nothing about the contents of the byte array.
	 * 
	 * @param size
	 *            The required byte array size.
	 * @return
	 */
	public static byte[] getArray(int size)
	{
		// We will reset the pool every 5 seconds so that no-longer-used byte array sizes get deleted.
		long currentGeneration = generationCounter.get();
		long newGeneration = GameTime.getRealTime() / 5000;
		if (currentGeneration < newGeneration)
		{
			// This is reached every 5 seconds by one or more threads.
			if (generationCounter.compareAndSet(currentGeneration, newGeneration))
			{
				// This is reached by precisely one thread every 5 seconds.
				//
				// Now we cycle the pools.  This is a little bit complex, so an explanation follows.
				//
				// Initially, there was just one pool of byte arrays.  But some byte array sizes stop being used during normal operation of the program.
				// -- Most notably when the window is resized while one of the image-scaling efficiency modes is enabled. --
				// The result is that memory usage could increase needlessly as unneeded byte array sizes were kept pooled forever.
				//
				// So I devised a system which would efficiently delete byte arrays that are no longer in use while remaining lock-free and thread-safe.
				//
				// Now, we always keep references to two pools.  One is older (previousArrayPool), and one is newer (arrayPool).  When a byte array is 
				// requested by outside code, we first try to get it from previousArrayPool.  If that fails, we try to get a byte array from arrayPool.  
				// If that fails also, we construct a new byte array and return it.  Regardless, when outside code is finished with a byte array, the 
				// byte array is sent back here where it is inserted into arrayPool for future use.
				//
				// The effect is that previousArrayPool quickly empties out of all the popular byte array sizes and is never replenished. But it may 
				// still contain byte arrays of sizes that are no longer in demand.  We might as well allow these byte arrays to be garbage collected.
				//
				// So every 5 seconds, we remove all remaining byte arrays from previousArrayPool and dereference the pool.  This frees up memory that 
				// would otherwise be wasted by byte arrays and byte array queues that aren't being used anymore.
				//
				// Around this time, arrayPool is the only pool containing any byte arrays.  It becomes the new previousArrayPool, while a new pool is
				// constructed to take the role of arrayPool.  The entire cycle now repeats.  Byte arrays from previousArrayPool gradually make their 
				// way to arrayPool, then unused byte arrays are again purged from previousArrayPool and the cycle repeats again endlessly.
				//
				// Accidents can happen due to the asynchronous nature of this program, but the worst case scenario is that some byte arrays are created
				// or deleted unnecessarily, incurring a slight efficiency penalty.
				ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>> retiringPool = previousArrayPool;
				previousArrayPool = arrayPool;
				arrayPool = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>>();
				cleanup(retiringPool);
			}
		}
		// Get an array from the older pool first.
		ConcurrentLinkedQueue<byte[]> q = previousArrayPool.get(size);
		if (q != null)
		{
			byte[] b = q.poll();
			if (b != null)
			{
				//pooledCount.decrementAndGet();
				return b;
			}
		}
		// No luck.  Now try the newer pool.
		q = arrayPool.get(size);
		if (q != null)
		{
			byte[] b = q.poll();
			if (b != null)
			{
				//pooledCount.decrementAndGet();
				return b;
			}
		}
		// The newer pool didn't have any arrays of the size we want.
		// So we will construct one.
		return new byte[size];
	}

	/**
	 * Recycles the specified byte array, so that future requests for a byte array of the same size may be fulfilled
	 * without allocating a new one.
	 */
	public static void returnArrayToPool(byte[] b)
	{
		ConcurrentLinkedQueue<byte[]> q = arrayPool.get(b.length);
		if (q == null)
		{
			q = new ConcurrentLinkedQueue<byte[]>();

			ConcurrentLinkedQueue<byte[]> q2 = arrayPool.putIfAbsent(b.length, q);
			if (q2 != null)
				q = q2;
		}
		q.offer(b);
		//int pooled = pooledCount.incrementAndGet();
		//System.out.println(pooled);
	}

	private static void cleanup(ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>> pool)
	{
		for (int key : pool.keySet())
			cleanup(pool.get(key));
	}

	private static void cleanup(ConcurrentLinkedQueue<byte[]> q)
	{
		if (q != null)
			while (q.poll() != null)
			{
				//pooledCount.decrementAndGet();
			}
	}
}
