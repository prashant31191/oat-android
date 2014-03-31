package com.worth.fragments;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.worth.oat.R;
import com.worth.utils.Constants;
import com.worth.utils.DashboardListAdapter;
import com.worth.utils.Photo;

public class UserPhotoList extends Fragment {

	ListView list;
	ArrayList<Photo> photos;
	ArrayList<Photo> visiblePhotos = new ArrayList<Photo>();
	DashboardListAdapter adapter;
	
	private int currentIndex = 0;
	private boolean loading = false;
	private final String LOGTAG = "UserPhotoList";
	private final int ADDITIONAL_LOAD_SIZE = 2;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.user_photo_list, container, false);
		
		list = (ListView) v.findViewById(R.id.user_photo_listview);
		
		if ((photos = getArguments().getParcelableArrayList("photos")) != null) {
			adapter = new DashboardListAdapter(getActivity(), visiblePhotos);
			addMorePhotos();
			loading = false;
			list.setAdapter(adapter);
		}
		
		list.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					if (!loading) {
						if (Constants.debug) Log.i(LOGTAG, "Hit bottom of listview");
						addMorePhotos();
						loading = false;
					}
				}
				
			}
		});
		
		return v;
	}
	
	public void updateList(Photo p) {
		photos.add(0, p);
		currentIndex++;
		adapter.insertData(p);
		adapter.notifyDataSetChanged();
	}
	
	private void addMorePhotos() {
		loading = true;
		int i = 0;
		while (visiblePhotos.size() != photos.size() && i < ADDITIONAL_LOAD_SIZE) {
			Log.i(LOGTAG, "adding more photos");
			visiblePhotos.add(photos.get(currentIndex));
			if (Constants.debug) Log.i(LOGTAG, "size of visible photos: " + visiblePhotos.size() +
					" || size of all photos: " + photos.size());
			currentIndex++;
			i++;
		}
		if (i == ADDITIONAL_LOAD_SIZE) 
			adapter.notifyDataSetChanged();
	}
	
}
