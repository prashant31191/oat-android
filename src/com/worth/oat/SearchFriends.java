package com.worth.oat;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;

import com.worth.fragments.SearchContacts;
import com.worth.fragments.SearchUsername;
import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;
import com.worth.utils.NetworkFunctions;
import com.worth.utils.NetworkRequest;
import com.worth.utils.NetworkRequest.NetworkCallback;

public class SearchFriends extends Activity implements NetworkCallback {
	
	Button friendButton;
	final String LOGTAG = "SeachFriends";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//actionBar.setDisplayShowTitleEnabled(false);
		
		Tab tab = actionBar.newTab()
						   .setText("search_user")
						   .setTabListener(new TabListener<SearchUsername>(
								   this, "username", SearchUsername.class));
		actionBar.addTab(tab);
		
		tab = actionBar.newTab()
					   .setText("search_contacts")
					   .setTabListener(new TabListener<SearchContacts>(
							   this, "contacts", SearchContacts.class));
		actionBar.addTab(tab);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_friends, menu);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void addFriend(String friendToAdd, Button addFriendButton) {
		friendButton = addFriendButton; 
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		HashMap<String, String> details = db.getUserDetails();
		String user = details.get("username");
		NetworkRequest request = new NetworkRequest(this);
		new NetworkFunctions(request).addFriend(user, friendToAdd);
	}
	
	public void removeFriend(String friendToRemove, Button addFriendButton) {
		friendButton = addFriendButton;
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		HashMap<String, String> details = db.getUserDetails();
		String user = details.get("username");
		NetworkRequest request = new NetworkRequest(this);
		new NetworkFunctions(request).removeFriend(user, friendToRemove);
	}
	
	/**
	 * A class that handles tab events for the two fragments in this activity. One tab
	 * is for adding friends by searching their username. The other tab is for adding
	 * friends by searching through the user's phone contacts. 
	 */
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
		
		private Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;
		
		public TabListener(Activity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}
		
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// Check to see if fragment is instantiated
			if (mFragment == null) {
				// If it isn't, instantiate it and add it to the activity
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				// Attach it
				//ft.attach(mFragment);
				ft.show(mFragment);
			}
		}
		
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				//ft.detach(mFragment);
				ft.hide(mFragment);
			}
		}
		
	}
	
	@Override
	public void onRequestComplete(String jsonString) {
		if (jsonString != null) {
			try {
				if (Constants.debug) Log.i(LOGTAG, "onRequestComplete");
				if (Constants.debug) Log.i(LOGTAG, "json string: " + jsonString);
				JSONObject json = new JSONObject(jsonString);
				if (json.getString(Constants.TAG).equals(Constants.ADD_FRIEND)) {
					if (Constants.debug) Log.i(LOGTAG, "callback for add_friend");
					if (json.getString(Constants.STATUS_TAG).equals(Constants.SUCCESS_TAG)) {
						String friend = json.getString("friend");
						if (Constants.debug) Log.i(LOGTAG, "Friend was successfully added: "
								+ friend);
						// TODO: remove the spinner
						
						// Add friend to database
						DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						db.addFriend(friend);
						
						friendButton.setText("remove friend");
						friendButton.setTag(2);
					}
				} else {
					// The JSON request was for removing a friend
					if (Constants.debug) Log.i(LOGTAG, "callback for remove_friend");
					if (json.getString(Constants.STATUS_TAG).equals(Constants.SUCCESS_TAG)) {
						String friend = json.getString("friend");
						Log.i(LOGTAG, "username of friend to remove: " + friend);
						// TODO: remove the spinner
						
						// Remove the friend from db
						DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						db.deleteFriend(friend);
						
						friendButton.setText("add friend");
						friendButton.setTag(1);
					}
				}
			} catch (JSONException e) {
				if (Constants.debug) Log.i(LOGTAG, e.getMessage());
			}
		}
		
	}
	

}
