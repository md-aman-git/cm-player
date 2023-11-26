package com.aman.videoplayer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.utils.ApManager;
import com.aman.videoplayer.utils.FileTxThread;
import com.aman.videoplayer.services.FilesShareService;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.ShareFiles;
import com.aman.videoplayer.adapters.SharingFileAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import static com.aman.videoplayer.activities.SenderActivity.mReservation;

public class SendingDetailsActivity extends AppCompatActivity implements ServiceConnection {

    FilesShareService filesShareService;
    boolean bounded = false;
    RecyclerView sharingFileRV;
    private String PATH_OF_SELECTED_FILE_PERF = "PATH_PREF_NAME";
    private String PATH_PREF_NAME = "HereFileStored";
    SharedPreferences preferences;
    private Handler mHandler = new Handler();
    SharingFileAdapter sharingFileAdapter;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_details);
        sharingFileRV = findViewById(R.id.sendingFileRV);
        preferences = getSharedPreferences(PATH_OF_SELECTED_FILE_PERF, MODE_PRIVATE);
        intent = new Intent(this, FilesShareService.class);
        intent.putExtra("sendingFiles", "sentIt");
        startService(intent);
        Gson gson = new Gson();
        String jsonText = preferences.getString(PATH_PREF_NAME, null);
        ShareFiles[] text = gson.fromJson(jsonText, ShareFiles[].class);
        ArrayList<ShareFiles> videoFiles = new ArrayList<ShareFiles>(Arrays.asList(text));
        sharingFileAdapter = new SharingFileAdapter(this, videoFiles);
        sharingFileRV.setAdapter(sharingFileAdapter);
        sharingFileRV.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(intent,this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        FilesShareService.MyBinder b = (FilesShareService.MyBinder) binder;
        filesShareService = b.getService();
        bounded = true;
        if (filesShareService != null) {
            SendingDetailsActivity.this.runOnUiThread(updater);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        filesShareService = null;
        bounded = false;
    }

    Runnable updater = new Runnable() {
        @Override
        public void run() {
            int progress = (int) FileTxThread.currentSize;
            updateProgress(0, progress);
            mHandler.postDelayed(updater, 1000);
        }
    };
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle("Do you want to exit ?");
        builder.setMessage("Connection Will be lost... ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (bounded)
                {
                    unbindService(SendingDetailsActivity.this);
                    bounded = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (statusCheck()) {
                            if (mReservation != null)
                                mReservation.close();
                        }
                    }
                    else
                    {
                        WifiManager manager = (WifiManager)
                                getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (manager != null)
                        {
                            if (manager.isWifiEnabled())
                            {
                                manager.setWifiEnabled(false);
                            }
                        }
                        if (ApManager.isApOn(SendingDetailsActivity.this))
                            ApManager.configApState(SendingDetailsActivity.this);
                    }
                    Intent intent = new Intent(SendingDetailsActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                FileTxThread.currentSize = 0;
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(updater);
        if (bounded) {
            unbindService(this);
            bounded = false;
        }
    }

    void updateProgress(int position, int progress)
    {
        SharingFileAdapter.ShareFileHolder fileHolder =
                (SharingFileAdapter.ShareFileHolder) sharingFileRV.findViewHolderForAdapterPosition(position);
        if (fileHolder != null) {
            fileHolder.customProgress.setProgress(progress);
            fileHolder.currentFileSize.setText(String.valueOf(progress / (1024*1024)) + "MB");
            int currentSizeOfFile = fileHolder.customProgress.getMax();
            if (currentSizeOfFile == progress) {
                fileHolder.status.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(updater);
            }
        }
    }
    public boolean statusCheck() {
        final LocationManager manager = (LocationManager)
                getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileTxThread.currentSize = 0;
    }
}