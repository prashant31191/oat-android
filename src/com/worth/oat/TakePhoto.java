package com.worth.oat;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.worth.utils.CameraPreview;
import com.worth.utils.Constants;

public class TakePhoto extends Activity {
	
	String LOGTAG = "TakePhoto";
	Button photoInfo;
	FrameLayout preview;
	byte[] photoData;
	private int UPLOAD_PHOTO = 2;
	private Camera mCamera;
	private CameraPreview mPreview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.take_photo);
		
		// Get the camera
		mCamera = getCamera();
		
		// Get the button that sends the user to fill in photo metadata and hide it
		photoInfo = (Button) findViewById(R.id.launch_metadata);
		photoInfo.setVisibility(View.GONE);
		
		// Create the camera preview
		mPreview = new CameraPreview(this, mCamera);
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (Constants.debug) Log.i(LOGTAG, "onPause()");
		mCamera.stopPreview();
		mCamera.setPreviewCallback(null);
		mPreview.getHolder().removeCallback(mPreview);
		mCamera.release();
		mCamera = null;
	}
	
	@Override 
	protected void onResume() {
		super.onResume();
		if (Constants.debug) Log.i(LOGTAG, "onResume()");
		if (mCamera == null) {
			if (Constants.debug) Log.i(LOGTAG, "camera is null");
			mCamera = getCamera();
			mCamera.setPreviewCallback(null);
			mPreview = new CameraPreview(this, mCamera);
			preview.addView(mPreview);
		}
	}

	/**
	 * This function is called when the user presses the 'capture' button.
	 * The camera takes a picture and uses the JPEG callback.
	 */
	public void takePhoto(View v) {
		// Take the picture and use the jpeg callback
		mCamera.takePicture(null, null, jpegCallback);
	}
	
	/**
	 * This function is called when the user presses the 'proceed' button after
	 * he has taken a picture and would like to fill out metadata.  
	 */
	public void launchPhotoInfo(View v) {
		Intent intent = new Intent(this, UploadPhoto.class);
		intent.putExtra("photo", photoData);
		startActivityForResult(intent, UPLOAD_PHOTO);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == UPLOAD_PHOTO) {
			if (resultCode == RESULT_OK) {
				// Receive data from UploadPhoto (caption and photo)
				String caption = data.getStringExtra("caption");
				byte[] photoData = data.getByteArrayExtra("photo");
				String photoId = data.getStringExtra("photoId");
				
				// Send the same data to the parent activity
				Intent returnIntent = new Intent();
				returnIntent.putExtra("caption", caption);
				returnIntent.putExtra("photo", photoData);
				returnIntent.putExtra("photoId", photoId);
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		}
		
	}
	
	/**
	 * Get the Camera instance
	 */
	private Camera getCamera() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			Log.i(LOGTAG, e.getMessage());
			
		}
		return c;
	}
	
	/**
	 * Callback for when the captured picture is ready in JPEG format. 
	 * Launch the PhotoInfo activity which allows the user to fill in metadata
	 * about the photo. Send the bytearray to the activity as well. 
	 */
	private PictureCallback jpegCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			photoInfo.setVisibility(View.VISIBLE);
			photoData = data;
		}
		
	};

}
