package com.aman.videoplayer.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.aman.videoplayer.utils.ApManager;
import com.aman.videoplayer.R;
import com.aman.videoplayer.fragments.QRScanFragment;

public class ReceiverActivity extends AppCompatActivity {

    WifiManager wifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        if (ApManager.isApOn(this))
            ApManager.configApState(this);
        wifiManager = (WifiManager) this.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.receiverFrameLayout, new QRScanFragment())
                .commit();
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle("Do you want to exit ?");
        builder.setMessage("Connection Will be lost... ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (wifiManager != null && wifiManager.isWifiEnabled())
                {
                    wifiManager.setWifiEnabled(false);
                }
                Intent intent = new Intent(ReceiverActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
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