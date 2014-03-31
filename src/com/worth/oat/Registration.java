package com.worth.oat;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;
import com.worth.utils.NetworkFunctions;
import com.worth.utils.NetworkRequest;
import com.worth.utils.NetworkRequest.NetworkCallback;

public class Registration extends Activity implements NetworkCallback {

	TextView error;
	EditText email, username, password, passwordConfirm;
	String phoneNumber;
	String usernameText, pwText, pwConfirmText, emailText;
	String LOGTAG = "Registration";
	
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
			NetworkRequest request = new NetworkRequest(this);
			new NetworkFunctions(request).register(usernameText, pwText, emailText, phoneNumber);
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

	@Override
	public void onRequestComplete(String j) {
		if (j != null) {
			try {
				JSONObject json = new JSONObject(j);
				if (json.getString(Constants.STATUS_TAG).equals(Constants.SUCCESS_TAG)) {
					// Add the user to the local database
					DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					db.addUser(json.getString(Constants.USERNAME_TAG), 
							json.getString(Constants.EMAIL_TAG)); 
					
					// Check to make sure user added correctly
					HashMap<String, String> details = db.getUserDetails();
					if (Constants.debug) Log.i(LOGTAG, "username: " + details.get(Constants.USERNAME_TAG)
							+ " email: " + details.get(Constants.EMAIL_TAG));
					
					// Launch dashboard and finish this activity
					Intent intent = new Intent(Registration.this, Dashboard.class);
					startActivity(intent);
					setResult(RESULT_OK);
					finish();
				} else {
					// Error occurred on server
					error.setText(json.getString(Constants.REASON_TAG));
				}
			} catch (JSONException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			}
		}
	}

}
