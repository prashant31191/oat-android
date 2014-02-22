package com.worth.oat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;
import com.worth.utils.Photo;

public class Login extends Activity {

	EditText user, password;
	TextView error;
	String LOGTAG = "Login";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		// Check to see if the user is already logged in
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		if (db.isUserLoggedIn()) {
			// Launch dashboard activity and finish this activity
			Intent intent = new Intent(this, Dashboard.class);
			startActivity(intent);
			finish();
		}
		
		// Grab the views
		user = (EditText) findViewById(R.id.user_login);
		password = (EditText) findViewById(R.id.password_login);
		error = (TextView) findViewById(R.id.error_login);
	}
	
	/**
	 * Called when the user wants to submit their login credentials
	 * @param v
	 */
	public void submit(View v) {
		// Retrieve the text from the views
		String userText = user.getText().toString();
		String passwordText = password.getText().toString();
		
		// Launch asynctask to login user
		new LoginTask().execute(userText, passwordText);
	}
	
	/**
	 * Redirects the user to create an account
	 * @param v
	 */
	public void launchRegistration(View v) {
		Intent intent = new Intent(this, Registration.class);
		startActivityForResult(intent, 1);
	}
	
	/**
	 * We want to start an activity for result so that the registration activity
	 * can tell this activity to finish(). This is to disallow the user from 
	 * returning to the registration or login page once logged in. 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				this.finish();
			}
		}
	}
	
	/**
	 * An AsyncTask to check the user's login credentials. Pass in the username/email
	 * and password as parameters. In onPostExecute(), check the response to see if we 
	 * should launch the dashboard or report an error.  
	 */
	private class LoginTask extends AsyncTask<String, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(String... params) {
			// Get the user and password from parameters passed in
			String user = params[0];
			String password = params[1]; 
			String status = null;
			
			// Create an HTTP POST with the login credentials
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(Constants.localhost + "/login");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user", user));
			pairs.add(new BasicNameValuePair("password", password));
			
			try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
				
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
	            JSONArray jsonArray = new JSONArray(json);
	            Log.i(LOGTAG, "json to string: " + jsonArray.toString());
		        
	            return jsonArray;
	            
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
		protected void onPostExecute(JSONArray json) {
			try {
				if (json != null) {
					// Get the user data first and add to database
					String userInfo = json.getString(0);
					JSONObject obj = new JSONObject(userInfo);
					String email = obj.getString(Constants.EMAIL_TAG);
					String username = obj.getString(Constants.USERNAME_TAG);
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					db.addUser(username, email);
					
					// Create a Photo object from the JSON and send it to dashboard
					// to download all the images
					JSONObject row;
					ArrayList<Photo> allPhotos = new ArrayList<Photo>();
					for (int i = 1; i < json.length(); i++) {
						String s = json.getString(i);
						row = new JSONObject(s);
						Photo temp = new Photo(row.getString("caption"), row.getString("photo_id"));
						allPhotos.add(temp);
					}
					
					// Launch the dashboard and send the info
					Intent intent = new Intent(Login.this, Dashboard.class);
					intent.putParcelableArrayListExtra("photos", allPhotos);
					startActivity(intent);
					finish();
				}
			} catch (JSONException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			}
		}
				
				
				/*
				try {
					if (json.getString(Constants.STATUS_TAG).equals(Constants.SUCCESS_TAG)) {
						if (Constants.debug) Log.i(LOGTAG, "login success");
						
						// Add user to local database
						DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						db.addUser(json.getString(Constants.USERNAME_TAG), 
								json.getString(Constants.EMAIL_TAG));
						
						// Launch dashboard and finish current activity 
						Intent intent = new Intent(Login.this, Dashboard.class);
						startActivity(intent);
						finish();
					} else {
						error.setText(json.getString(Constants.REASON_TAG));
					}
				} catch (JSONException e) {
			    	if (Constants.debug) Log.i(LOGTAG, e.getMessage());
				}
				*/
		
	}
	

}
