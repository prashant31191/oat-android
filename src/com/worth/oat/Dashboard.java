package com.worth.oat;

import java.util.ArrayList;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.worth.caching.ImageLoader;
import com.worth.fragments.UserPhotoList;
import com.worth.utils.Constants;
import com.worth.utils.DatabaseHandler;
import com.worth.utils.Photo;

public class Dashboard extends Activity {
	
	private final String USER_PHOTOS = "userPhotos";
	private final String LOGTAG = "Dashboard";
	private final int CAPTURE_PHOTO = 1;
	
	byte[] photoData;
	String caption;
	ListView myList;
	
	// This is essentially an interface to the cache. We instantiate it in
	// this activity so that fragments of this activity can access it through
	// a getter function (getImageLoader())
	ImageLoader imageLoader; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		
		// Instantiate the cache
		imageLoader = new ImageLoader(getApplicationContext()); 
		
		// Get the photo ArrayList that was passed in from logging in
		ArrayList<Photo> photoArray = getIntent().getParcelableArrayListExtra("photos");
		if (photoArray == null) {
			// We enter this condition if for some reason onDestroy() is called and
			// we have to rebuild our list of photos (but we didn't get the list from Login.class).
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			photoArray = db.getAllPhotos();
			if (Constants.debug) Log.i(LOGTAG, "onCreate - no intent with photoArray");
			if (Constants.debug) Log.i(LOGTAG, "number of photos retrieved from db: " + photoArray.size());
		}
		
		// Pass it to the fragment
		Bundle b = new Bundle();
		b.putParcelableArrayList("photos", photoArray);
		UserPhotoList userPhotoFrag = new UserPhotoList();
		userPhotoFrag.setArguments(b);
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.content_frame, userPhotoFrag, USER_PHOTOS);
		ft.commit();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// clear cache here
		imageLoader.clear();
		if (Constants.debug) Log.i(LOGTAG, "onDestroy()");
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
			case R.id.searchFriends:
				searchFriends();
				return true;
			case R.id.takePhoto:
				launchPhotoActivity();
				return true;
		}
		return false;
	}
	
	/**
	 * A getter method for fragments to access the cache
	 * @return
	 */
	public ImageLoader getImageLoader() {
		return imageLoader; 
	}
	
	/**
	 * Launch the activity to search for friends to add
	 */
	public void searchFriends() {
		Intent intent = new Intent(this, SearchFriends.class);
		startActivity(intent);
	}
	
	/**
	 * OnClickListener for the 'take photo' button. This launches the TakePhoto activity.
	 * We want to start it for a result because the TakePhoto activity will send back 
	 * the picture data and metadata so that we can add it to our listview. 
	 */
	public void launchPhotoActivity() {
		Intent intent = new Intent(this, TakePhoto.class);
		startActivityForResult(intent, CAPTURE_PHOTO);
	}
	
	/**
	 * We receive a result from the UploadPhoto activity that contains the Photo
	 * including its actual byte[]. We then pass the Photo to the proper list to
	 * display as the most recent Photo.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_PHOTO) {
			if (resultCode == RESULT_OK) {
				if (Constants.debug) Log.i(LOGTAG, "Activity Result == RESULT_OK"); 
				caption = data.getStringExtra("caption");
				photoData = data.getByteArrayExtra("photo");
				String photoId = data.getStringExtra("photoId");
				Photo photoInfo = new Photo(caption, photoData, photoId);
				
				// Add to database
				DatabaseHandler db = new DatabaseHandler(getApplicationContext());
				db.addPhoto(photoInfo);
				
				// Add to listview in fragment
				UserPhotoList f = (UserPhotoList) getFragmentManager().findFragmentByTag(USER_PHOTOS);
				f.updateList(photoInfo); // Call the fragment's function that add's the Photo
			}
		}
	}
	
	/**
	 * Override the back button to act as the home button so that onDestroy()
	 * isn't called.
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

}
