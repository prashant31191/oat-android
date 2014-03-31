package com.worth.caching;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;
import com.worth.utils.Photo;

public class ImageLoader {

	private ExecutorService executor;
	private Context context;
	private final String LOGTAG = "ImageLoader";
	private String username;
	Handler handler = new Handler();
	private MemoryCache memoryCache;
	private DiskCache diskCache;
	private final Object cacheLock = new Object();
	private final int DISK_CACHE_SIZE = 1024 * 1024 * 10;
	private final String DISK_CACHE_NAME = "OAT_DISK_CACHE";
	private boolean cacheStarting = true;
	
	// Create Amazon S3 Client with proper credentials
	private AmazonS3Client s3Client = new AmazonS3Client(
			new BasicAWSCredentials(Constants.AMAZON_KEY, Constants.AMAZON_SECRET_KEY));
	
	/**
	 * Constructor
	 */
	public ImageLoader(Context context) {
		// 5 is an arbitrary number here, might want to do more research and change it
		executor = Executors.newFixedThreadPool(5);
		
		// Get username
		this.context = context;
		DatabaseHandler db = new DatabaseHandler(context);
		username = db.getUserDetails().get("username");
		
		// Create the memory and disk cache
		memoryCache = new MemoryCache();
		new InitDiskCacheTask().execute();
	}
	
	/**
	 * Initialize the disk cache in a separate thread. 
	 */
	class InitDiskCacheTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			synchronized(cacheLock) {
				diskCache = new DiskCache(context, DISK_CACHE_NAME, DISK_CACHE_SIZE);
				cacheStarting = false;
				cacheLock.notifyAll();
			}
			return null;
		}
	}
	
	/**
	 * This is the driving function of this class. We first check the memory
	 * cache to see if the photo is available. If not, we proceed to download
	 * the image from storage.
	 */
	public void displayImage(Photo photoInfo, ImageView imageView) {
		// If the memoryCache contains the photo, set the imageview
		Bitmap b;
		if ((b = memoryCache.get(photoInfo.getPhotoId())) != null) {
			if (Constants.debug) Log.i(LOGTAG, "image found in memory cache");
			imageView.setImageBitmap(b);
			return;
		}
		
		if (Constants.debug) Log.i(LOGTAG, "image not found in memory cache");
		// Otherwise, we need to download the file
		PhotoLoader loader = new PhotoLoader(photoInfo, imageView);
		executor.execute(loader);
	}
	
	
	/**
	 * This runnable is ran by the handler after the photo is successfully downloaded
	 * from s3 storage. It simply places the bitmap inside the imageview. The handler
	 * is necessary for this because the downloader runs in a background thread (non UI). 
	 */
	public class BitmapDisplayer implements Runnable {
		
		Bitmap photo;
		ImageView imageView;
		
		BitmapDisplayer(Bitmap b, ImageView view) {
			photo = b;
			imageView = view;
		}

		@Override
		public void run() {
			// Scale the bitmap by retrieving the dimensions of the ImageView
			/*
			ViewTreeObserver obs = imageView.getViewTreeObserver();
			obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			    public boolean onPreDraw() {
			        int height = imageView.getMeasuredHeight();
			        int width = imageView.getMeasuredWidth();
			        photo = Bitmap.createScaledBitmap(photo, width, height, false);
			        return true;
			    }
			});
			*/
	        photo = Bitmap.createScaledBitmap(photo, 55, 55, false);
			// Place the photo in the imageview
			imageView.setImageBitmap(photo);
		}
	}
	
	/**
	 * This runnable is executed by the ExecutorService. It downloads the file
	 * from s3 storage and converts it to a bitmap. It then uses the handler to
	 * display it. 
	 */
	public class PhotoLoader implements Runnable {
		
		Photo photoInfo;
		ImageView imageView;
		
		PhotoLoader(Photo p, ImageView v) {
			photoInfo = p;
			imageView = v;
		}

		@Override
		public void run() {
			String key = photoInfo.getPhotoId();
			
			// Before we make a network request, check to see if the disk cache contains
			// the photo. If it does, we want to get it and add it to the memory cache
			// and then display it in the imageview.
			synchronized (cacheLock) {
				while (cacheStarting) {
					try {
						cacheLock.wait();
					} catch (InterruptedException e) {}
				}
				if (diskCache != null) {
					if (diskCache.containsKey(key)) {
						if (Constants.debug) Log.i(LOGTAG, "image found in disk cache");
						Bitmap b = diskCache.getBitmap(key);
						memoryCache.add(key, b);
						BitmapDisplayer displayer = new BitmapDisplayer(b, imageView);
						handler.post(displayer);
						return;
					}
				}
			}
			
			// Create a GET request 
			GetObjectRequest request = new GetObjectRequest(Constants.BUCKET_NAME, username 
					+ "/pictures/" + photoInfo.getPhotoId());
			S3Object object = s3Client.getObject(request);
			try {
				// Convert to bytearray and then to bitmap
				byte[] photoData = IOUtils.toByteArray(object.getObjectContent());
				Bitmap b = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
				
				// Add it to the memoryCache
				memoryCache.add(key, b);
				
				// Add to disk cache
				synchronized (cacheLock) {
					if (diskCache != null)
						diskCache.put(key, b);
				}
				
				// Runnable which displays the image
				BitmapDisplayer displayer = new BitmapDisplayer(b, imageView);
				handler.post(displayer);
				
			} catch (IOException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			} catch (AmazonClientException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			}
		}
		
	}
	
	/**
	 * This function is called when the user returns from uploading a photo.
	 * There's no need to download anything since we have the photo, so add
	 * it to cache.
	 */
	public void addToCache(String photoId, Bitmap photo) {
		if (photoId == null) {
			if (Constants.debug) Log.i(LOGTAG, "null photoID");
		}
		if (photo == null) {
			if (Constants.debug) Log.i(LOGTAG, "null photo");
		}
		memoryCache.add(photoId, photo);
		new AddToDiskTask(photoId, photo).execute();
	}
	
	/**
	 * Add the image to the disk cache in a separate thread 
	 */
	private class AddToDiskTask extends AsyncTask<Void, Void, Void> {
		String key;
		Bitmap b;
		
		AddToDiskTask(String k, Bitmap b) {
			this.key = k;
			this.b = b;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			synchronized (cacheLock) {
				if (diskCache != null) 
					diskCache.put(key, b);
			}
			return null;
		}
	}
	
	/**
	 * Clear both disk and memory cache
	 */
	public void clear() {
		memoryCache.clear();
		new DeleteDiskCacheTask().execute();
	}
	
	private class DeleteDiskCacheTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			diskCache.clearCache();
			return null;
		}
	}
	
	
}
