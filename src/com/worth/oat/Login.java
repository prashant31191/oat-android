package com.worth.oat;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;
import com.worth.utils.NetworkFunctions;
import com.worth.utils.NetworkRequest;
import com.worth.utils.NetworkRequest.NetworkCallback;
import com.worth.utils.Photo;

public class Login extends Activity implements NetworkCallback {

	EditText user, password;
	TextView error;
	String LOGTAG = "Login";
	String jsonString; 
	
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
		
		// Make the request
		NetworkRequest network = new NetworkRequest(this);
		new NetworkFunctions(network).login(userText, passwordText);
		
		// Launch asynctask to login user
		// new LoginTask().execute(userText, passwordText);
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
	
	@Override
	public void onRequestComplete(String json) {
		if (json != null) {
			try {
				JSONArray jsonArray = new JSONArray(json);
				JSONObject obj = jsonArray.getJSONObject(0);
				
				if (obj.getString(Constants.STATUS_TAG).equals(Constants.SUCCESS_TAG)) {
					String email = obj.getString(Constants.EMAIL_TAG);
					String username = obj.getString(Constants.USERNAME_TAG);
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					db.addUser(username, email);
					
					// Create photo objects from the JSON and send it to dashboard
					// to download all the images
					JSONObject row;
					ArrayList<Photo> allPhotos = new ArrayList<Photo>();
					for (int i = 1; i < jsonArray.length(); i++) {
						row = jsonArray.getJSONObject(i);
						Photo temp = new Photo(row.getString("caption"), row.getString("photo_id"));
						allPhotos.add(temp);
					}
					// Add all photos to android database
					int numAdded = db.addPhotos(allPhotos);
					if (Constants.debug) Log.i(LOGTAG, "number of photos added to db: " + numAdded);
					
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
	}
	

}
