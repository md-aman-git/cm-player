package com.aman.videoplayer.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.VideoFile;
import com.aman.videoplayer.adapters.VideoFolderAdapter;

import java.util.ArrayList;

public class VideosFolderActivity extends AppCompatActivity {

    String folder;
    ArrayList<VideoFile> videoFiles = new ArrayList<>();
    RecyclerView recyclerView;
    VideoFolderAdapter videoFolderAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_folder);
        recyclerView = findViewById(R.id.videosRecyclerView);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        folder = getIntent().getStringExtra("folderName");
        Log.e("FolderName", folder + "");
        if (folder != null) {
            videoFiles = getAllVideos(this, folder);
        }
        if (videoFiles.size() > 0)
        {
            videoFolderAdapter = new VideoFolderAdapter(this, videoFiles);
            recyclerView.setAdapter(videoFolderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));
        }
    }

    public ArrayList<VideoFile> getAllVideos(final Context context, String folder) {

        String MY_PREFS_NAME = "SortOrder";
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String order = prefs.getString("sorting", "sortByName");
        final ArrayList<VideoFile> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = null;
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
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.ALBUM,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.RESOLUTION
        };
        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%" + folder + "%"};

        Cursor c = context.getContentResolver().query(uri,
                projection, selection, selectionArgs, sortOrder);
        if (c != null) {
            while (c.moveToNext()) {
                String id = c.getString(0);
                String artist = c.getString(1);
                String title = c.getString(2);
                String path = c.getString(3);
                String file_name = c.getString(4);
                String duration = c.getString(5);
                String album = c.getString(6);
                String size = c.getString(7);
                String date_added = c.getString(8);
                String date_modified = c.getString(9);
                String width = c.getString(10);
                String height = c.getString(11);
                String resolution = c.getString(12);
                if (folder != null &&
                        album != null && folder.endsWith(album)) {
                    VideoFile videoFile = new VideoFile(id, title, path,
                            album, date_added, date_modified,
                            artist, duration, size, file_name, width, height, resolution);
                    tempAudioList.add(videoFile);
                }
            }
            c.close();
        }
        return tempAudioList;
    }

    private void setTheme() {
        String MY_PREFS_THEME = "ThemesChanger";
        SharedPreferences preferences = getSharedPreferences(MY_PREFS_THEME, MODE_PRIVATE);
        if (preferences.getString("themes", "dark").equals("dark")) {
            setTheme(R.style.DarkTheme);
        }
        else {
            setTheme(R.style.LightTheme);
        }
    }
}