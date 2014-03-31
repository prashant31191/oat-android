package com.worth.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class NetworkRequest {
	
	private final String LOGTAG = "NetworkRequest";
	private NetworkCallback callingClass;
	
	/**
	 * Classes that require network requests will need to implement
	 * this interface. When the network request is completed in this class,
	 * the information is passed back to the calling class.  
	 */
	public interface NetworkCallback {
		public void onRequestComplete(String jsonString);
	}
	
	/**
	 * Constructor that casts the calling class as NetworkCallback.
	 * @param n
	 */
	public NetworkRequest(NetworkCallback n) {
		this.callingClass = n;
	}
	
	/**
	 * This is the function that executes the AsyncTasks. The caller specifies
	 * whether the request should be a GET or POST.
	 * @param params - contains the URL (always) and additional params (only if it is a POST)
	 * @param requestType - "get" or "post"
	 */
	public void makeRequest(NetworkParams params, String requestType) {
		if (requestType.equals("get")) 
			new GetRequest().execute(params);
		else 
			new PostRequest().execute(params);
	}
	
	/**
	 * AsyncTask used for making GET Requests. 
	 */
	private class GetRequest extends AsyncTask<NetworkParams, Void, String> {

		@Override
		protected String doInBackground(NetworkParams... params) {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(Constants.server + params[0].url);
			try {
				HttpResponse response = client.execute(request);
		        BufferedReader buffReader = new BufferedReader(
		        		new InputStreamReader(response.getEntity().getContent(), "utf-8"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = buffReader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	            return sb.toString();
			} catch (ClientProtocolException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			} catch (IOException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String json) {
			// TODO: Check that the calling class still exists
			callingClass.onRequestComplete(json);
		}
		
	}
	
	/**
	 * AsyncTask used for making POST Requests. 
	 */
	private class PostRequest extends AsyncTask<NetworkParams, Void, String> {

		@Override
		protected String doInBackground(NetworkParams... params) {
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost(Constants.server + params[0].url);
			try {
				request.setEntity(new UrlEncodedFormEntity(params[0].params));
				HttpResponse response = client.execute(request);
		        BufferedReader buffReader = new BufferedReader(
		        		new InputStreamReader(response.getEntity().getContent(), "utf-8"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = buffReader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	            return sb.toString();
			} catch (UnsupportedEncodingException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			} catch (ClientProtocolException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			} catch (IOException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String json) {
			// TODO: Check that the calling class still exists
			callingClass.onRequestComplete(json);
		}
		
	}

}
