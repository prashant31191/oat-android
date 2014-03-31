package com.worth.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 4;
	
	// Database name
	private static final String DATABASE_NAME = "oat";
	
	// Table names
	private static final String TABLE_USER = "user"; 
	private static final String TABLE_FRIEND = "friend";
	private static final String TABLE_PHOTO = "photo";
	
	// User table column names
	private static final String KEY_NAME = "username";
	private static final String KEY_EMAIL = "email";
	
	// Photo table column names
	private final String KEY_PHOTO_ID = "photo_id";
	private final String KEY_CAPTION = "caption";
	
	private final String KEY_ID = "id";
	
	// String to create the user table
	private static final String CREATE_USER_TABLE = 
			"CREATE TABLE " + TABLE_USER + "("
			+ KEY_NAME + " TEXT," 
			+ KEY_EMAIL + " TEXT UNIQUE)";
	
	// String to create the friend table
	private final String CREATE_FRIEND_TABLE =
			"CREATE TABLE " + TABLE_FRIEND + "("
			+ KEY_NAME + " TEXT)";
	
	// String to create the photo table
	private final String CREATE_PHOTO_TABLE =
			"CREATE TABLE " + TABLE_PHOTO + "("
			+ KEY_ID  + " INTEGER PRIMARY KEY,"
			+ KEY_PHOTO_ID + " TEXT,"
			+ KEY_CAPTION + " TEXT)";
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_USER_TABLE);
		db.execSQL(CREATE_FRIEND_TABLE);
		db.execSQL(CREATE_PHOTO_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIEND);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);
		onCreate(db);
	}
	
	/**
	 * Add a user to the USER table 
	 */
	public void addUser(String name, String email) {
		// Get the database
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Set the column values to the provided values
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_EMAIL, email);
		
		// Insert the row into the database
		db.insert(TABLE_USER, null, values);
		db.close();
	}
	
	/**
	 * Add a friend to the database
	 * @param username
	 */
	public void addFriend(String username) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, username);
		db.insert(TABLE_FRIEND, null, values);
		db.close();
	}
	
	/**
	 * Delete a friend from the database
	 * @param username
	 */
	public void deleteFriend(String username) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_FRIEND, KEY_NAME+"='"+username+"'", null);
		db.close();
	}
	
	/**
	 * Find the specified username in the friend table
	 * @param username
	 * @return
	 */
	public boolean findFriend(String username) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String query = "SELECT * FROM " + TABLE_FRIEND + " WHERE username='" + username + "'";
		Cursor cursor = db.rawQuery(query, null);
		int count = cursor.getCount();
		db.close();
		cursor.close();
		return count > 0;
	}

	/**
	 * Retrieves the user details
	 * @return hash map with the user details
	 */
	public HashMap<String, String> getUserDetails() {
		// The hash map that will be returned
		HashMap<String, String> user = new HashMap<String, String>();
		
		// Database query
		String selectQuery = "SELECT * FROM " + TABLE_USER;
		
		// Get the database and perform query
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		// Retrieve the data from the cursor
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("username", cursor.getString(0));
			user.put("email", cursor.getString(1));
		}
		cursor.close();
		db.close();
		return user;
	}
	
	/**
	 * Check to see if the user is logged in by checking the row count
	 * of the USER table
	 */
	public boolean isUserLoggedIn() {
		// Get the number of rows in the user table
		String countQuery = "SELECT * FROM " + TABLE_USER;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		
		// Clean up
		db.close();
		cursor.close();
		
		if (rowCount > 0) return true;
		return false; 
	}
	
	/**
	 * Iterate through a List of photos and add them to the database
	 * @param photos
	 * @return number of photos added
	 */
	public int addPhotos(ArrayList<Photo> photos) {
		SQLiteDatabase db = this.getWritableDatabase();
		for (Photo p : photos) {
			ContentValues values = new ContentValues();
			values.put(KEY_PHOTO_ID, p.getPhotoId());
			values.put(KEY_CAPTION, p.getCaption());
			db.insert(TABLE_PHOTO, null, values);
		}
		String query = "SELECT * FROM " + TABLE_PHOTO;
		Cursor cursor = db.rawQuery(query, null);
		int rows = cursor.getCount();
		db.close();
		cursor.close();
		return rows;
	}
	
	/**
	 * Add the photo to the database
	 * @param photo
	 */
	public void addPhoto(Photo photo) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_PHOTO_ID, photo.getPhotoId());
		values.put(KEY_CAPTION, photo.getCaption());
		db.insert(TABLE_PHOTO, null, values);
		db.close();
	}
	
	/**
	 * Returns a list of all the photos in the database
	 * @return ArrayList<Photo>
	 */
	public ArrayList<Photo> getAllPhotos() {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT * FROM " + TABLE_PHOTO + " ORDER BY " + KEY_ID;
		Cursor cursor = db.rawQuery(query, null);
		
		// The list to return
		ArrayList<Photo> photos = new ArrayList<Photo>();
		
		// Iterate through the cursor
		int rows = cursor.getCount();
		cursor.moveToFirst();
		for (int i = 0; i < rows; i++) {
			int id = cursor.getColumnIndex(KEY_PHOTO_ID);
			int capt = cursor.getColumnIndex(KEY_CAPTION);
			Photo p = new Photo(cursor.getString(capt), cursor.getString(id));
			photos.add(p);
			cursor.moveToNext();
		}
		
		cursor.close();
		db.close();
		return photos;
	}
	
	/**
	 * Wipe the tables clean (log the user out)
	 */
	public void logout() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_USER, null, null);	
		db.delete(TABLE_FRIEND, null, null);
		db.delete(TABLE_PHOTO, null, null);
		db.close();
	}
	
}
