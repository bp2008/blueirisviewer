package org.brian.blueirisviewer.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
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
		long currentGeneration = generationCounter.get();
		long newGeneration = GameTime.getRealTime() / 5000;
		if (currentGeneration < newGeneration)
		{
			if (generationCounter.compareAndSet(currentGeneration, newGeneration))
			{
				// Cycle the pools; create a new one and delete all byte arrays found in the oldest.
				ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>> retiringPool = previousArrayPool;
				previousArrayPool = arrayPool;
				arrayPool = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<byte[]>>();
				cleanup(retiringPool);
			}
		}
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
