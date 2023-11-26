package com.aman.videoplayer.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.VideoFiles;
import com.aman.videoplayer.fragments.FilesFragment;
import com.aman.videoplayer.fragments.FolderFragment;
import com.aman.videoplayer.fragments.ShareMainFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_PERMISSION = 23;
    public static ArrayList<VideoFiles> videoFiles = new ArrayList<>();
    public static ArrayList<String> foldersOfVideos = new ArrayList<>();
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = findViewById(R.id.bottomFragment);
        permission();
    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            videoFiles = getAllAudioFromDevice(MainActivity.this);
            //initViewPager();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrameLayout, new FilesFragment()).commit();
            navigationView.setOnNavigationItemSelectedListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                videoFiles = getAllAudioFromDevice(MainActivity.this);
                //initViewPager();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFrameLayout, new FolderFragment()).commit();
                navigationView.setOnNavigationItemSelectedListener(this);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }
    }

    public ArrayList<VideoFiles> getAllAudioFromDevice(final Context context) {

        String MY_PREFS_NAME = "SortOrder";
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String order = prefs.getString("sorting", "sortByName");
        final ArrayList<VideoFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
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
        Cursor c = context.getContentResolver().query(uri, projection,
                null, null, sortOrder);
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
                VideoFiles videoFiles = new VideoFiles(id, title, path,
                        album, date_added, date_modified,
                        artist, duration, size, file_name, width, height, resolution);
                int slashFirstIndex = path.lastIndexOf("/");
                String subStr = path.substring(0, path.lastIndexOf("/"));
                int slashIndex = subStr.lastIndexOf("/");
                String folder = subStr.substring(slashIndex + 1, slashFirstIndex);
                Log.d("subStr", subStr + "");
                Log.e("folder", folder + "");
                if (!foldersOfVideos.contains(subStr))
                    foldersOfVideos.add(subStr);
                tempAudioList.add(videoFiles);
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
        } else {
            setTheme(R.style.LightTheme);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menuFolderFragment:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFrameLayout, new FolderFragment())
                        .commit();
                item.setChecked(true);
                break;
            case R.id.videoFragment:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFrameLayout, new FilesFragment())
                        .commit();
                item.setChecked(true);
                break;
            case R.id.shareMainFragment:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFrameLayout, new ShareMainFragment())
                        .commit();
                item.setChecked(true);
                break;
        }
        return false;
    }

}