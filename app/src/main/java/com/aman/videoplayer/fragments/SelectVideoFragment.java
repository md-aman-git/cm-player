package com.aman.videoplayer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.ShareFiles;
import com.aman.videoplayer.adapters.SelectVideoAdapter;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class SelectVideoFragment extends Fragment {

    View view;
    RecyclerView selectVideoRV;
    ArrayList<ShareFiles> selectVideos = new ArrayList<>();
    public SelectVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_select_video, container, false);
        selectVideoRV = view.findViewById(R.id.selectVideoRV);
        if (getContext() != null) {
            selectVideos = getAllVideoFiles(getContext());
            if (selectVideos != null) {
                SelectVideoAdapter selectVideoAdapter = new SelectVideoAdapter(getContext(), selectVideos);
                selectVideoRV.setAdapter(selectVideoAdapter);
                selectVideoRV.setLayoutManager(new LinearLayoutManager(getContext(),
                        RecyclerView.VERTICAL, false));
            }
        }
        return view;
    }
    public ArrayList<ShareFiles> getAllVideoFiles(final Context context) {

        String MY_PREFS_NAME = "SortOrder";
        SharedPreferences prefs = null;
        if (getContext() != null)
        prefs = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String order = null;
        if (prefs != null) {
            order = prefs.getString("sorting", "sortByName");
        }
        String sortOrder = null;
        assert order != null;
        switch (order) {
            case "sortByName":
                sortOrder = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;
            case "sortByDate":
                sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";
                break;
            case "sortBySize":
                sortOrder = MediaStore.MediaColumns.SIZE + " DESC";
                break;
        }
        final ArrayList<ShareFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };
        Cursor c = context.getContentResolver().query(uri, projection,
                null, null, sortOrder);
        if (c != null) {
            while (c.moveToNext()) {
                String id = c.getString(0);
                String title = c.getString(1);
                String path = c.getString(2);
                String file_name = c.getString(3);
                int duration = c.getInt(4);
                String size = c.getString(5);
                ShareFiles shareFiles = new ShareFiles(title, path, size,
                        duration, file_name, id, 'v');
                Log.e("Path", path);
                tempAudioList.add(shareFiles);
            }
            c.close();
        }
        return tempAudioList;
    }
}