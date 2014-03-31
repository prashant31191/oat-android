package com.worth.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.worth.oat.R;
import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;
import com.worth.utils.NetworkFunctions;
import com.worth.utils.NetworkRequest;
import com.worth.utils.NetworkRequest.NetworkCallback;
import com.worth.utils.SearchFriendsAdapter;

public class SearchUsername extends Fragment implements NetworkCallback {
	
	EditText searchText;
	ListView myList;
	ArrayList<String> listItems;
	ArrayAdapter<String> adapter;
	final String LOGTAG = "SearchUsername";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.search_username, container, false);
		
		// Grab the views
		searchText = (EditText) v.findViewById(R.id.edittext_searchusername);
		myList = (ListView) v.findViewById(R.id.list_searchusername);
	
		// Create the initial listview which only displays what the user types
		// into the edittext
		listItems = new ArrayList<String>();
		adapter = new ArrayAdapter<String>
			(getActivity(), android.R.layout.simple_list_item_1, listItems);
		myList.setAdapter(adapter);
		
		// Set a textwatcher on the edittext
		TextWatcher watcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// When the someone is typing in the edittext, we want to clear out
				// listItems and show the 'click here...' text in the listview. We also
				// have to change the listview onclicklistener to have it search for the
				// user in the db
				listItems.clear();
				if (!s.toString().equals("")) {
					listItems.add("click here to search for '" + s + "'");
				}
				adapter = new ArrayAdapter<String>
					(getActivity(), android.R.layout.simple_list_item_1, listItems);
				myList.setAdapter(adapter);
				myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						String user = searchText.getText().toString();
						NetworkRequest request = new NetworkRequest(SearchUsername.this);
						new NetworkFunctions(request).searchForUser(user);
						// show spinner
					}
					
				});
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			
		};
		searchText.addTextChangedListener(watcher);
		
		return v;
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
	}
	
	@Override
	public void onRequestComplete(String jsonString) {
		if (jsonString != null) {
			try {
				DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
				String username = db.getUserDetails().get("username");
				JSONArray json = new JSONArray(jsonString);
				listItems.clear();
				int len = json.length();
				if (len >= 1) {
					JSONObject obj;
					for (int i = 0; i < len; i++) {
						obj = json.getJSONObject(i);
						String friend = obj.getString("username");
						// If one of the results is our own username, do not display it
						if (!username.equals(friend)) {
							listItems.add(friend);
						}
					}
					SearchFriendsAdapter friendsAdapter = new SearchFriendsAdapter(getActivity(), listItems);
					myList.setAdapter(friendsAdapter);
				} else {
					// no users found
					listItems.add("No such username found");
					adapter.notifyDataSetChanged();
					myList.setClickable(false);
				}
				
			} catch (JSONException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			}
		}
	}
	
}
