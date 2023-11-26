package com.aman.videoplayer.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aman.videoplayer.R;
import com.aman.videoplayer.fragments.SelectAppsFragment;
import com.aman.videoplayer.fragments.SelectDocumentFragment;
import com.aman.videoplayer.fragments.SelectMusicFragment;
import com.aman.videoplayer.fragments.SelectVideoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SelectFilesActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView selectNavMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_files);
        selectNavMenu = findViewById(R.id.selectFilesNavMenu);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.selectFrameLayout, new SelectDocumentFragment())
                .commit();
        selectNavMenu.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.selectFolder:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.selectFrameLayout, new SelectDocumentFragment())
                        .commit();
                item.setChecked(true);
                break;
            case R.id.selectVideos:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.selectFrameLayout, new SelectVideoFragment())
                        .commit();
                item.setChecked(true);
                break;
            case R.id.selectMusics:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.selectFrameLayout, new SelectMusicFragment())
                        .commit();
                item.setChecked(true);
                break;
            case R.id.selectApps:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.selectFrameLayout, new SelectAppsFragment())
                        .commit();
                item.setChecked(true);
                break;
        }
        return false;
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