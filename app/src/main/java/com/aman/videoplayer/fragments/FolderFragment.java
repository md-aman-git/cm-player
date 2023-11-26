package com.aman.videoplayer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aman.videoplayer.R;
import com.aman.videoplayer.adapters.FolderAdapter;

import static com.aman.videoplayer.activities.MainActivity.foldersOfVideos;
import static com.aman.videoplayer.activities.MainActivity.videoFiles;

public class FolderFragment extends Fragment {

    RecyclerView recyclerView;
    FolderAdapter folderAdapter;
    View view;
    public FolderFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_folder, container, false);
        recyclerView = view.findViewById(R.id.folderList);
        recyclerView.setHasFixedSize(true);
        if (foldersOfVideos != null && !(foldersOfVideos.size() < 1))
        {
            folderAdapter = new FolderAdapter(getContext(), videoFiles, foldersOfVideos);
            recyclerView.setAdapter(folderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                    RecyclerView.VERTICAL, false));
        }
        return view;
    }
}