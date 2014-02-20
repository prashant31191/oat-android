package com.worth.oat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;

public class PhotoInfo extends Activity {

	String LOGTAG = "PhotoInfo";
	EditText caption; 
	ImageView thumbnail;
	byte[] photoData;
	
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
		String captionText = caption.getText().toString();
		
		// Username is needed to create a photo_id
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		String username = db.getUserDetails().get("username");
		
		// Begin upload
		new UploadTask(captionText, username).execute();
	}
	
	private class UploadTask extends AsyncTask<Void, Void, JSONObject> {
		
		String caption;
		String username;
		
		UploadTask(String caption, String username) {
			this.caption = caption;
			this.username = username;
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			// Create a photo_id
			// Create an instance of SimpleDateFormat used for formatting 
			// the string representation of date (month/day/year)
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

			// Get the date today using Calendar object.
			Date today = Calendar.getInstance().getTime();        
			
			// Using DateFormat format method we can create a string 
			// representation of a date with the defined format.
			String reportDate = df.format(today);
			
			String photoId = username + "_" + reportDate;
			
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
					if (jsonObject.getString(Constants.STATUS_TAG).equals(Constants.SUCCESS_TAG)) {
						if (Constants.debug) Log.i(LOGTAG, "upload success");
					}
				} catch (JSONException e) {
					if (Constants.debug) Log.i(LOGTAG, e.getMessage());
				}
			}
			if (Constants.debug) Log.i(LOGTAG, "json is null");
			
		}
	
	}

}
