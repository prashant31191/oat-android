package com.worth.oat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;

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
	private class LoginTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
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
		protected void onPostExecute(JSONObject json) {
			if (json != null) {
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
			}
		}
		
	}
	

}
