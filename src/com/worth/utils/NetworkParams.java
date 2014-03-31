package com.worth.utils;

import java.util.List;

import org.apache.http.NameValuePair;

public class NetworkParams {
	
	String url;
	List<NameValuePair> params; 
	
	/**
	 * This constructor is used for POST Requests. 
	 * @param url
	 * @param params
	 */
	NetworkParams(String url, List<NameValuePair> params) {
		this.url = url;
		this.params = params;
	}
	
	/**
	 * This constructor is used for GET Requests.
	 * @param url
	 */
	NetworkParams(String url) {
		this.url = url;
	}

}
