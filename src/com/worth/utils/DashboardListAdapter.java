package com.worth.utils;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.worth.oat.R;

public class DashboardListAdapter extends BaseAdapter {
	
	String LOGTAG = "DashboardListAdapter";
    private Activity activity;
    private ArrayList<Photo> data;
    private static LayoutInflater inflater = null;
    ImageLoader imageLoader;
    
	
	public DashboardListAdapter(Activity a, ArrayList<Photo> data) {
		activity = a;
		this.data = data;
		imageLoader = new ImageLoader(a.getApplicationContext());
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	/**
	 * This function is called when the user takes a photo and returns to the dashboard.
	 * There's no need to download the photo in this case, because the the
	 * photo was passed back from the 'TakePhoto' activity to the Dashboard. So we
	 * can simply just add it to the memory cache.
	 */
	public void insertData(Photo photo) {
		// Insert in top of the list
		data.add(0, photo);
		Log.i(LOGTAG, "Inserting data into ArrayList<Photo>...new size: " + data.size());
		Log.i(LOGTAG, "Photo caption: " + photo.getCaption());
		imageLoader.addToCache(photo.getPhotoId(), photo.getPhoto());
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
        	vi = inflater.inflate(R.layout.custom_dashboard_item, null);
        
        // Get the views from the custom_dashboard_item
        TextView caption = (TextView) vi.findViewById(R.id.caption_dashboard);
        ImageView photoView = (ImageView) vi.findViewById(R.id.photo_dashboard);
        
        // Set the caption
        caption.setText(data.get(position).getCaption());
        
        // Display the photo (download it if necessary)
        imageLoader.displayImage(data.get(position), photoView);
        
		return vi;
	}

}
