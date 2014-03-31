package com.worth.caching;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.worth.utils.Constants;

public class MemoryCache {

	private LruCache<String, Bitmap> memoryCache;
	private final String LOGTAG = "MemoryCache";
	
	public MemoryCache() {
	    // Get max available VM memory, exceeding this amount will throw an
	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
	    // int in its constructor.
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = maxMemory / 8;

	    memoryCache = new LruCache<String, Bitmap>(cacheSize) {
	        @Override
	        protected int sizeOf(String key, Bitmap bitmap) {
	            // The cache size will be measured in kilobytes rather than
	            // number of items.
	            return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
	        }
	    };
	}
	
	public void add(String key, Bitmap b) {
		if (memoryCache.get(key) == null) {
			if (Constants.debug) Log.i(LOGTAG, "adding to memory cache");
			memoryCache.put(key, b);
		}
	}
	
	public Bitmap get(String key) {
		if (Constants.debug) Log.i(LOGTAG, "getting from memory cache");
		return memoryCache.get(key);
	}

	public void clear() {
		if (Constants.debug) Log.i(LOGTAG, "clearing memory cache");
		memoryCache.evictAll();
	}
}
