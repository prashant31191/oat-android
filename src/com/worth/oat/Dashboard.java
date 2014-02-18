package com.worth.oat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.worth.utils.DatabaseHandler;

public class Dashboard extends Activity {

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

}
