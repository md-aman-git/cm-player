package com.aman.videoplayer.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aman.videoplayer.R;
import com.aman.videoplayer.activities.ReceiverActivity;
import com.aman.videoplayer.activities.SelectFilesActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class ShareMainFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    View view;
    Button btnSend, btnReceive;
    public ShareMainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_share_main, container, false);
        btnSend = view.findViewById(R.id.sendFiles);
        btnReceive = view.findViewById(R.id.receiveFiles);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view,
                            "This feature is under progress only for Android Q. Wait...",
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            });
            btnReceive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view,
                            "This feature is under progress only for Android Q. Wait...",
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }
            });
        }
        else
        {
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentSend = new Intent(getContext(), SelectFilesActivity.class);
                    startActivity(intentSend);
                }
            });
            btnReceive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getContext() != null)
                        if (ActivityCompat.checkSelfPermission(getContext(),
                                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            Intent intentReceive = new Intent(getContext(), ReceiverActivity.class);
                            startActivity(intentReceive);
                        } else {
                            if (getActivity() != null)
                                ActivityCompat.requestPermissions(getActivity(), new
                                        String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        }
                }
            });
        }
        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intentReceive = new Intent(getContext(), ReceiverActivity.class);
                startActivity(intentReceive);
            }
            else
            {
                if (getActivity() != null)
                    ActivityCompat.requestPermissions(getActivity(), new
                            String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        }
    }
}