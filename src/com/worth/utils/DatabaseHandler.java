package com.worth.utils;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 2;
	
	// Database name
	private static final String DATABASE_NAME = "oat";
	
	// Table names
	private static final String TABLE_USER = "user"; 
	
	// User table column names
	private static final String KEY_NAME = "username";
	private static final String KEY_EMAIL = "email";
	
	// String to create the user table
	private static final String CREATE_USER_TABLE = 
			"CREATE TABLE " + TABLE_USER + "("
			+ KEY_NAME + " TEXT," 
			+ KEY_EMAIL + " TEXT UNIQUE)";
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_USER_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
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
	 * Wipe the tables clean (log the user out)
	 */
	public void logout() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_USER, null, null);	
		db.close();
	}
	
}
