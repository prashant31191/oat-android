package com.worth.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class NetworkFunctions {
	
	private NetworkRequest networkRequest; 
	private final String LOGIN = "/login";
	private final String REGISTER = "/create_user";
	private final String ADD_FRIEND = "/add_friend";
	private final String REMOVE_FRIEND = "/remove_friend";
	private final String SEARCH_USER = "/search_user";
	
	/**
	 * The calling class need to instantiate a NetworkRequest because NetworkRequest
	 * needs to know which class to send the callback to. The instance of NetworkRequest
	 * is then sent to this constructor. 
	 * @param n
	 */
	public NetworkFunctions(NetworkRequest n) {
		this.networkRequest = n;
	}
	
	/**
	 * Attempts to log the user into the system
	 * @param user - can be either the username or email
	 * @param password
	 */
	public void login(String user, String password) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("user", user));
		pairs.add(new BasicNameValuePair("password", password));
		NetworkParams params = new NetworkParams(LOGIN, pairs);
		networkRequest.makeRequest(params, "post");
	}
	
	/**
	 * Register the user in the remote database
	 * @param username
	 * @param password
	 * @param email
	 * @param phoneNum
	 */
	public void register(String username, String password, String email, String phoneNum) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", username));
		pairs.add(new BasicNameValuePair("password", password));
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("phone_number", phoneNum));
		NetworkParams params = new NetworkParams(REGISTER, pairs);
		networkRequest.makeRequest(params, "post");
	}
	
	/**
	 * Add the friend to the username's friend's list
	 * @param username
	 * @param friend
	 */
	public void addFriend(String username, String friend) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", username));
		pairs.add(new BasicNameValuePair("friend", friend));
		NetworkParams params = new NetworkParams(ADD_FRIEND, pairs);
		networkRequest.makeRequest(params, "post");
	}
	
	/**
	 * Remove the friend from the username's friend's list
	 * @param username
	 * @param friend
	 */
	public void removeFriend(String username, String friend) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", username));
		pairs.add(new BasicNameValuePair("friend", friend));
		NetworkParams params = new NetworkParams(REMOVE_FRIEND, pairs);
		networkRequest.makeRequest(params, "post");
	}
	
	/**
	 * Search for a user (called from searchFriends). We need to create the URL
	 * which contains the username to search for.
	 * @param username
	 */
	public void searchForUser(String username) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		String url = SEARCH_USER + "?username=" + username; // Create the URL for the GET request
		NetworkParams params = new NetworkParams(url, pairs);
		networkRequest.makeRequest(params, "get");
	}
	
	

}
