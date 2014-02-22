package com.worth.oat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.worth.utils.Constants;
import com.worth.utils.DashboardListAdapter;
import com.worth.utils.DatabaseHandler;
import com.worth.utils.Photo;

public class Dashboard extends Activity {
	
	String LOGTAG = "Dashboard";
	private int CAPTURE_PHOTO = 1;
	byte[] photoData;
	String caption;
	ListView myList;
	DashboardListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		
		// Get the activity listview
		myList = (ListView) findViewById(R.id.dashboard_listview);
		
		// Set the listview to our custom adapter
		ArrayList<Photo> photoArray = new ArrayList<Photo>();
		adapter = new DashboardListAdapter(this, photoArray);
		myList.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// clear cache here
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dashboard, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())  {
			case R.id.logout:
				DatabaseHandler db = new DatabaseHandler(getApplicationContext());
				db.logout();
				Intent intent = new Intent(this, Login.class);
				startActivity(intent);
				finish();
				return true;
		}
		return false;
	}
	
	/**
	 * OnClickListener for the 'take photo' button. This launches the TakePhoto activity.
	 * We want to start it for a result because the TakePhoto activity will send back 
	 * the picture data and metadata so that we can add it to our listview. 
	 */
	public void launchPhotoActivity(View v) {
		Intent intent = new Intent(this, TakePhoto.class);
		startActivityForResult(intent, CAPTURE_PHOTO);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_PHOTO) {
			if (resultCode == RESULT_OK) {
				if (Constants.debug) Log.i(LOGTAG, "Activity Result == RESULT_OK"); 
				caption = data.getStringExtra("caption");
				photoData = data.getByteArrayExtra("photo");
				String photoId = data.getStringExtra("photoId");
				Photo photoInfo = new Photo(caption, photoData, photoId);
				adapter.insertData(photoInfo);
				adapter.notifyDataSetChanged();
			}
		}
	}

}
