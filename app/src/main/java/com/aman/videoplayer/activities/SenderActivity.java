package com.aman.videoplayer.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.aman.videoplayer.R;
import com.aman.videoplayer.fragments.SenderFragment;


public class SenderActivity extends AppCompatActivity {

    public static WifiManager.LocalOnlyHotspotReservation mReservation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.senderFrameLayout, new SenderFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle("Do you want to exit ?");
        builder.setMessage("Connection Will be lost... ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mReservation != null) {
                    mReservation.close();
                    mReservation = null;
                }
                SenderActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
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