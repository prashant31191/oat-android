package com.worth.fragments;

import com.worth.oat.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SearchContacts extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.search_contacts, container, false);
		return v;
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
	}


}
