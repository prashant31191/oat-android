package com.worth.oat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.worth.utils.DatabaseHandler;

public class Dashboard extends Activity {
	
	private int CAPTURE_PHOTO = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
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

}
