package com.worth.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Photo {
	
	String caption;
	String photoId = null;
	byte[] photo = null;
	
	public Photo(String caption, String photoId) {
		this.caption = caption;
		this.photoId = photoId;
	};
	
	public Photo(String caption, byte[] photo, String photoId) {
		this.caption = caption;
		this.photo = photo;
		this.photoId = photoId;
	};
	
	public String getCaption() {
		return caption;
	}
	
	public Bitmap getPhoto() {
		Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
		return bitmap;
	}
	
	public String getPhotoId() {
		return photoId;
	}

}
