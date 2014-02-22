package com.worth.oat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;

public class UploadPhoto extends Activity {

	// Debugging
	String LOGTAG = "PhotoInfo";
	
	// Caption view and corresponding text
	EditText caption; 
	String captionText;
	
	// ImageView to hold the photo 
	ImageView thumbnail;
	
	// Bytearray of the photo
	byte[] photoData; 
	
	// Create Amazon S3 Client with proper credentials
	private AmazonS3Client s3Client = new AmazonS3Client(
			new BasicAWSCredentials(Constants.AMAZON_KEY, Constants.AMAZON_SECRET_KEY));
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_info);
		
		// Get the views
		caption = (EditText) findViewById(R.id.caption_photoinfo);
		thumbnail = (ImageView) findViewById(R.id.thumbnail_photoinfo);
		
		// Get previous intent which contains the photo
		photoData = this.getIntent().getByteArrayExtra("photo");
		
		// Set the imageview to the photo
		Bitmap b = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
		thumbnail.setImageBitmap(Bitmap.createScaledBitmap(b, 75, 75, false));
	}

	/**
	 * Called when the user submits the form by pressing the 'upload' 
	 * button. This function calls the asynctask which does the uploading.
	 */
	public void uploadToServer(View v) {
		// Retrieve caption 
		captionText = caption.getText().toString();
		
		// Username is needed to create a photo_id
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		String username = db.getUserDetails().get("username");
		
		// Begin upload
		new UploadServerTask(captionText, username).execute();
	}
	
	/**
	 * Upload the photo metadata (caption and photo_id) to the server. If the upload
	 * is successful, we can now upload the actual photo to Amazon S3 storage with the
	 * same photo_id. 
	 */
	private class UploadServerTask extends AsyncTask<Void, Void, JSONObject> {
		
		String caption;
		String username;
		
		UploadServerTask(String caption, String username) {
			this.caption = caption;
			this.username = username;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			// Create a photo_id
			// Create an instance of SimpleDateFormat used for formatting 
			// the string representation of date (month/day/year)
			DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH:mm:ss", Locale.getDefault());

			// Get the date today using Calendar object.
			Date today = Calendar.getInstance().getTime();        
			
			// Using DateFormat format method we can create a string 
			// representation of a date with the defined format.
			String reportDate = df.format(today);
			
			String photoId = username + "_" + reportDate + ".jpeg";
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(Constants.localhost + "/upload_photo");
			
			List<NameValuePair> postInfo = new ArrayList<NameValuePair>();
			postInfo.add(new BasicNameValuePair("caption", caption));
			postInfo.add(new BasicNameValuePair("username", username));
			postInfo.add(new BasicNameValuePair("photo_id", photoId));
			
			try {
				post.setEntity(new UrlEncodedFormEntity(postInfo));
				
		        // Execute HTTP Post Request
		        HttpResponse response = client.execute(post);
		        
		        // Convert response into JSON 
		        BufferedReader buffReader = new BufferedReader(
		        		new InputStreamReader(response.getEntity().getContent(), "utf-8"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = buffReader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	            String json = sb.toString();
	            JSONObject jsonObj = new JSONObject(json);
		        
	            return jsonObj;
	            
			} catch (UnsupportedEncodingException e) {
		    	if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			} catch (ClientProtocolException e) {
		    	if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			} catch (IOException e) {
		    	if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			} catch (JSONException e) {
		    	if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			if (jsonObject != null) {
				try {
					// If the result is successful, we want to launch the next asynctask which
					// uploads the actual photo to Amazon S3 storage
					if (jsonObject.getString(Constants.STATUS_TAG).equals(Constants.SUCCESS_TAG)) {
						if (Constants.debug) Log.i(LOGTAG, "upload to server success");
						String username = jsonObject.getString(Constants.USERNAME_TAG);
						String photoId = jsonObject.getString("photo_id");
						new UploadStorageTask(username, photoId).execute();
					}
				} catch (JSONException e) {
					if (Constants.debug) Log.i(LOGTAG, e.getMessage());
				}
			}
			
		}
	
	}
	
	/**
	 * Upload the photo to Amazon S3
	 */
	private class UploadStorageTask extends AsyncTask<Void, Void, Void> {
		
		String username;
		String photoId;
		
		UploadStorageTask(String username, String photoId) {
			this.username = username;
			this.photoId = photoId;
		}

		@Override
		protected Void doInBackground(Void... params) {
			// Create an inputstream from the photo bytearray
			InputStream is = new ByteArrayInputStream(photoData);
			
			// Get content length of byte array
			Long contentLength = Long.valueOf(photoData.length);
			
			// Add photo length to object metadata
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(contentLength);
			
			// Execute the put request
			PutObjectRequest request = new PutObjectRequest(Constants.BUCKET_NAME, 
					username + "/pictures/" + photoId, is, metadata);
			try {
				s3Client.putObject(request);
			} catch (AmazonClientException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage()); 
				// An error occured when trying to put the object
				// What to do in this case?
				// - retry the request one more time
				// 	 -- if it fails again, alert the user and also remove the info from the flask server
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			// Send information back to the parent activity (TakePhoto.class)
			Intent returnIntent = new Intent();
			returnIntent.putExtra("caption", captionText);
			returnIntent.putExtra("photo", photoData);
			returnIntent.putExtra("photoId", photoId);
			setResult(RESULT_OK, returnIntent);
			finish();
		}
		
	}

}
