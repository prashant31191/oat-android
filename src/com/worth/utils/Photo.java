package com.worth.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

public class Photo implements Parcelable {
	
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
	
	public Photo(Parcel in) {
		caption = in.readString();
		photoId = in.readString();
		//in.readByteArray(photo);
	}
	
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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(caption);
		dest.writeString(photoId);
		//dest.writeByteArray(photo);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Photo createFromParcel(Parcel source) {
			return new Photo(source);
		}

		@Override
		public Photo[] newArray(int size) {
			return new Photo[size];
		}
		
	};

}
