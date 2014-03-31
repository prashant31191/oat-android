package com.worth.caching;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.worth.utils.Constants;

public class DiskCache {
	
	private DiskLruCache cache;
	private CompressFormat compressFormat = CompressFormat.JPEG;
	private int compressQuality = 70;
	private final String LOGTAG = "DiskCache";
	private final int BUFFER_SIZE = 8 * 1024;
	
	public DiskCache(Context context, String uniqueName, int diskSize) {
		final File cacheDir = getDiskCacheDir(context, uniqueName);
		try {
			cache = DiskLruCache.open(cacheDir, 1, 1, diskSize);
		} catch (IOException e) {
			if (Constants.debug) Log.i(LOGTAG, e.getMessage());
		}
	}
	
	private File getDiskCacheDir(Context context, String uniqueName) {
		final String cachePath = 
				Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
				!Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
					context.getCacheDir().getPath();
		
		return new File(cachePath + File.separator + uniqueName);
	}
	
	private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor) 
			throws IOException, FileNotFoundException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(editor.newOutputStream(0), BUFFER_SIZE);
			return bitmap.compress(compressFormat, compressQuality, out);
		} finally {
			if (out != null) out.close();
		}
	}
	
	public void put(String k, Bitmap data) {
		String key = editKey(k);
		DiskLruCache.Editor editor = null;
		if (containsKey(key)) {
			if (Constants.debug) Log.i(LOGTAG, "key already exists in disk cache");
			return;
		}
		try {
			editor = cache.edit(key);
			if (editor == null) return;
		
			if (writeBitmapToFile(data, editor)) {
				cache.flush();
				editor.commit();
				if (Constants.debug) Log.i(LOGTAG, "image put on disk cache " + key);
			} else {
				editor.abort();
				if (Constants.debug) Log.i(LOGTAG, "ERROR: image put on disk cache " + key);
			}
		} catch (IOException e) {
			if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			try {
				if (editor != null) {
					editor.abort();
				}
			} catch (IOException ignored) {}
		}
	}
	
	public Bitmap getBitmap(String k) {
		if (Constants.debug) Log.i(LOGTAG, "getting from disk cache");
		String key = editKey(k);
		Bitmap bitmap = null;
		DiskLruCache.Snapshot snapshot = null;
		try {
			snapshot = cache.get(key);
			if (snapshot == null) return null;
			final InputStream in = snapshot.getInputStream(0);
			if (in != null) {
				final BufferedInputStream buffIn = new BufferedInputStream(in, BUFFER_SIZE);
				bitmap = BitmapFactory.decodeStream(buffIn);
			}
		} catch (IOException e) {
			if (Constants.debug) Log.i(LOGTAG, e.getMessage());
		} finally {
			if (snapshot != null) snapshot.close();
		}
		return bitmap;
	}
	
	public boolean containsKey(String k) {
		String key = editKey(k);
		boolean contained = false;
		DiskLruCache.Snapshot snapshot = null;
		try {
			snapshot = cache.get(key);
			contained = snapshot != null;
		} catch (IOException e) {
			if (Constants.debug) Log.i(LOGTAG, e.getMessage()); 
		} finally {
			if (snapshot != null) snapshot.close();
		}
		return contained;
	}
	
	public void clearCache() {
		try {
			if (Constants.debug) Log.i(LOGTAG, "clearing disk cache");
			cache.delete();
		} catch (IOException e) {
			if (Constants.debug) Log.i(LOGTAG, e.getMessage());
		}
	}
	
	private String editKey(String k) {
		String[] split = k.split("\\.");
		return split[0];
	}

}
