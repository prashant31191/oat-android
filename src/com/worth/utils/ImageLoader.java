package com.worth.utils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class ImageLoader {

	ExecutorService executor;
	String LOGTAG = "ImageLoader";
	String username;
	Handler handler = new Handler();
	MemoryCache memoryCache;
	
	// Create Amazon S3 Client with proper credentials
	private AmazonS3Client s3Client = new AmazonS3Client(
			new BasicAWSCredentials(Constants.AMAZON_KEY, Constants.AMAZON_SECRET_KEY));
	
	/**
	 * Constructor
	 */
	ImageLoader(Context context) {
		// 5 is an arbitrary number here, might want to do more research and change it
		executor = Executors.newFixedThreadPool(5);
		
		// Get username
		DatabaseHandler db = new DatabaseHandler(context);
		username = db.getUserDetails().get("username");
		
		// Create memory cache
		memoryCache = new MemoryCache();
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
			imageView.setImageBitmap(b);
			return;
		}
		
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
			ViewTreeObserver obs = imageView.getViewTreeObserver();
			obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			    public boolean onPreDraw() {
			        int height = imageView.getMeasuredHeight();
			        int width = imageView.getMeasuredWidth();
			        photo = Bitmap.createScaledBitmap(photo, width, height, false);
			        return true;
			    }
			});
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
			// Create a GET request 
			GetObjectRequest request = new GetObjectRequest(Constants.BUCKET_NAME, username 
					+ "/pictures/" + photoInfo.getPhotoId());
			S3Object object = s3Client.getObject(request);
			try {
				// Convert to bytearray and then to bitmap
				byte[] photoData = IOUtils.toByteArray(object.getObjectContent());
				Bitmap b = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
				
				// Add it to the memoryCache
				memoryCache.add(photoInfo.getPhotoId(), b);
				
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
	 * This function is called when the user returns from taking a photo.
	 * There's no need to download anything since we have the photo, so add
	 * it to cache.
	 */
	public void addToCache(String photoId, Bitmap photo) {
		memoryCache.add(photoId, photo);
	}
	
	
}
