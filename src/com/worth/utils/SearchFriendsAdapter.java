package com.worth.utils;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.worth.oat.R;
import com.worth.oat.SearchFriends;

public class SearchFriendsAdapter extends BaseAdapter {
	
	private ArrayList<String> usernames;
	private Activity activity;
	private static LayoutInflater inflater;
	
	public SearchFriendsAdapter(Activity a, ArrayList<String> data) {
		activity = a;
		usernames = data;
		inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return usernames.size();
	}

	@Override
	public Object getItem(int position) {
		return usernames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (convertView == null) 
			v = inflater.inflate(R.layout.custom_search_friends_item, null);
		
		String user = usernames.get(position);
		
		// Get the views
		final TextView username = (TextView) v.findViewById(R.id.username_custom_search_item);
		final Button addFriendButton = (Button) v.findViewById(R.id.add_remove_friend);
		
		// Check the cache to see if the search user is already a friend. If the user is a friend,
		// display the option to 'remove friend' instead of 'add friend'.
		DatabaseHandler db = new DatabaseHandler(activity.getApplicationContext());
		if (db.findFriend(user)) {
			addFriendButton.setTag(2);
			addFriendButton.setText("remove friend");
		} else {
			addFriendButton.setTag(1);
			addFriendButton.setText("add friend");
		}
		
		// When the user clicks on the button to add a friend, call the parent
		// activity to add the friend. Also, send it the button 
		// so that the activity can change it when the friend is added.
		addFriendButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int tag = (Integer) addFriendButton.getTag();
				String friendToAdd = username.getText().toString();
				if (tag == 1) 
					((SearchFriends) activity).addFriend(friendToAdd, addFriendButton);
			    else 
			    	((SearchFriends) activity).removeFriend(friendToAdd, addFriendButton);
			}
		});
		
		username.setText(user);
		
		return v;
	}

}
