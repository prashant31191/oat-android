package com.worth.oat;

import java.io.IOException;
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

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.worth.utils.Constants;

public class Registration extends Activity {

	TextView error;
	EditText email, username, password, passwordConfirm;
	String phoneNumber;
	String usernameText, pwText, pwConfirmText, emailText;
	
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
		// Get the text from the views
		usernameText = username.getText().toString();
		pwText = password.getText().toString();
		pwConfirmText = passwordConfirm.getText().toString();
		emailText = email.getText().toString();
		
		/*
		if (validateInput(usernameText, emailText, pwText, pwConfirmText)) {
			// execute async task
		} else {
			// display an error
		}
		*/
		new registerTask().execute();
		
	}
	
	/**
	 * Checks to see if the user inputs are valid for registration
	 * @param username
	 * @param email
	 * @param pw
	 * @param pwConfirm
	 * @return
	 */
	private boolean validateInput(String username, String email, String pw, String pwConfirm) {
		return true;
	}
	
	private class registerTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(Constants.localhost);
			
		    try {
		        // Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("username", usernameText));
		        nameValuePairs.add(new BasicNameValuePair("email", emailText));
		        nameValuePairs.add(new BasicNameValuePair("password", pwText));
		        nameValuePairs.add(new BasicNameValuePair("phone_number", phoneNumber));
		        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		        // Execute HTTP Post Request
		        HttpResponse response = client.execute(post);
		        
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    }
		    
			return null;
		}
		
		
	}

}
