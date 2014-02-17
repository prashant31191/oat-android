package com.worth.oat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.worth.utils.Constants;

public class Registration extends Activity {

	TextView error;
	EditText email, username, password, passwordConfirm;
	String phoneNumber;
	String usernameText, pwText, pwConfirmText, emailText;
	String LOG_TAG = "Registration";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		
		// Retrieve the views
		email = (EditText) findViewById(R.id.email_registration);
		username = (EditText) findViewById(R.id.username_registration);
		password = (EditText) findViewById(R.id.password_registration);
		passwordConfirm = (EditText) findViewById(R.id.password_confirm_registration);
		error = (TextView) findViewById(R.id.error_registration);
		
		// Get the phone number
		TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		phoneNumber = tMgr.getLine1Number();
	}

	/**
	 * Called when the user presses the button to submit their registration
	 * @param v
	 */
	public void submit(View v) {
		// Clear any previous error messages
		error.setText("");
		
		// Get the text from the views
		usernameText = username.getText().toString();
		pwText = password.getText().toString();
		pwConfirmText = passwordConfirm.getText().toString();
		emailText = email.getText().toString();
		
		// Check the validity of the input
		if (validateInput(usernameText, emailText, pwText, pwConfirmText)) {
			new registerTask().execute();
		} 
	}
	
	/**
	 * Checks to see if the user inputs are valid for registration
	 * @param username
	 * @param email
	 * @param pw
	 * @param pwConfirm
	 * @return true if all fields are valid, false otherwise
	 */
	private boolean validateInput(String username, String email, String pw, String pwConfirm) {
		
	    // Check the validity of the email input with built in android library
		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			error.setText("Invalid email address");
			error.setTextColor(Color.rgb(255,0,0));
			return false;
		}
		
		// Check username validity
		if (username.equals("") || username == null) {
			error.setText("Username cannot be blank");
			error.setTextColor(Color.rgb(255,0,0));
			return false;
		} else if (username.length() > 20) {
			error.setText("Username cannot exceed 20 characters");
			error.setTextColor(Color.rgb(255,0,0));
			return false;
		} 
		String expression = "^[a-zA-Z0-9]{1,20}$";
	    Pattern pattern = Pattern.compile(expression);
	    Matcher matcher = pattern.matcher(username);
	    if (!matcher.matches()) {
			error.setText("Username cannot contain illegal characters");
			error.setTextColor(Color.rgb(255,0,0));
			return false;
	    }
	    
	    // Check password validity
	    if (pw.equals("") || pw == null) {
			error.setText("Password is not valid");
			error.setTextColor(Color.rgb(255,0,0));
			return false;
	    }
		if (!pw.equals(pwConfirm)) {
			error.setText("Passwords do not match");
			error.setTextColor(Color.rgb(255,0,0));
			return false;
		}
		
		return true;
	}
	
	/**
	 * Register the user on a background thread. If the registration is successful,
	 * the user will be redirected to the dashboard. Otherwise, report the error.
	 */
	private class registerTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(Constants.localhost + "/create_user");
			String status = null;
			
		    try {
		        // Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		        nameValuePairs.add(new BasicNameValuePair("username", usernameText));
		        nameValuePairs.add(new BasicNameValuePair("email", emailText));
		        nameValuePairs.add(new BasicNameValuePair("password", pwText));
		        nameValuePairs.add(new BasicNameValuePair("phone_number", phoneNumber));
		        
		        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

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
		        status = jsonObj.getString("status");
		        if (Constants.debug) Log.i(LOG_TAG, status); 
		        
		    } catch (ClientProtocolException e) {
		    	if (Constants.debug) Log.i(LOG_TAG, e.getMessage());
		    } catch (IOException e) {
		    	if (Constants.debug) Log.i(LOG_TAG, e.getMessage());
		    } catch (JSONException e) {
		    	if (Constants.debug) Log.i(LOG_TAG, e.getMessage());
			}
		    
			return status;
		}
		
		@Override
		protected void onPostExecute(String status) {
			if (status != null) {
				if (status.equals("failure")) {
					error.setText("Username/Email already exists");
				} else {
					// successful registration - launch dashboard
				}
			}
		}
		
		
	}

}
