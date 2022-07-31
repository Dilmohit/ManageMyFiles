package com.diltech.managemyfiles.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.diltech.managemyfiles.R;

public class AboutFragment extends Fragment {

    ImageView imgdev, imgtwt;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        imgdev = v.findViewById(R.id.imgDevComm);
        imgtwt = v.findViewById(R.id.imgTwitter);

        imgdev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent devcommIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dev.to/dilmohit"));
                startActivity(devcommIntent);
            }
        });

        imgtwt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/Dilmohit007"));
                startActivity(twitterIntent);
            }
        });

        return v;
    }
}