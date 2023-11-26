package com.aman.videoplayer.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.aman.videoplayer.utils.ApManager;
import com.aman.videoplayer.CustomDialogClass;
import com.aman.videoplayer.activities.QRCodeActivity;
import com.aman.videoplayer.R;

import static com.aman.videoplayer.activities.SenderActivity.mReservation;

public class SenderFragment extends Fragment implements View.OnClickListener {

    private static final int LOCATION_CODE = 10;
    View view;
    Button hotspotOnBtn, sendNowBtn;
    private int mInterval = 500; // 5 seconds by default, can be changed later
    private Handler mHandler;
    public static String hotspotName;
    public static String hotspotPass;
    Intent intent;

    public SenderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sender, container, false);
        hotspotOnBtn = view.findViewById(R.id.hotsPotOn);
        sendNowBtn = view.findViewById(R.id.sendSelectedFile);
        hotspotOnBtn.setOnClickListener(this);
        mHandler = new Handler();
        startRepeatingTask();
        return view;
    }

    @Override
    public void onClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (statusCheck()) {
                if (mReservation == null)
                    turnOnHotspot();
                else
                    turnOffHotspot();
            }
        }
        else
        {
            onOffBelowO();
        }
    }
    void onOffBelowO()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSystemWritePermission()) {
                if (getContext() != null)
                {
                    WifiManager manager = (WifiManager) getContext().
                            getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (manager != null)
                    {
                        if (manager.isWifiEnabled())
                        {
                            manager.setWifiEnabled(false);
                        }
                    }
                    ApManager.configApState(getContext());
                    if (ApManager.isApOn(getContext()))
                    {
                        hotspotOnBtn.setText("Hotspot Off");
                    }
                    else
                    {
                        hotspotOnBtn.setText("Hotspot On");
                    }
                }
            } else {
                openAndroidPermissionsMenu();
                CustomDialogClass dialogClass = new CustomDialogClass(getContext(), intent);
                dialogClass.setCancelable(false);
                dialogClass.show();
            }
        }
        else
        {
            if (getContext() != null)
            {
                WifiManager manager = (WifiManager) getContext().
                        getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (manager != null)
                {
                    if (manager.isWifiEnabled())
                    {
                        manager.setWifiEnabled(false);
                    }
                }
                ApManager.configApState(getContext());
                if (ApManager.isApOn(getContext()))
                {
                    hotspotOnBtn.setText("Hotspot Off");
                }
                else
                {
                    hotspotOnBtn.setText("Hotspot On");
                }
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot() {
        WifiManager manager = (WifiManager) getActivity().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
            return;
        }
        if (mReservation != null)
        {
            turnOffHotspot();
            hotspotOnBtn.setText("Hotspot On");
        }
        else
        {
            manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

                @Override
                public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                    super.onStarted(reservation);
                    Log.d("TAG", "Wifi Hotspot is on now");
                    mReservation = reservation;
                    hotspotName = mReservation.getWifiConfiguration().SSID;
                    hotspotPass = mReservation.getWifiConfiguration().preSharedKey;
                    hotspotOnBtn.setText("Hotspot Off");
                    Log.e("TAG", hotspotName + "--" + hotspotPass);
                }

                @Override
                public void onStopped() {
                    super.onStopped();
                    Log.d("TAG", "onStopped: ");
                }

                @Override
                public void onFailed(int reason) {
                    super.onFailed(reason);
                    Toast.makeText(getContext(),
                            "Please Off Hotspot First", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "onFailed: ");
                }
            }, new Handler());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                statusCheck();
            }
        }
    }
    public boolean statusCheck() {
        final LocationManager manager = (LocationManager) getActivity().
                getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        else
        {
            return true;
        }
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        builder.setMessage("Your GPS is off enable it.")
                .setTitle("Turn On Location.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings
                                .ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRepeatingTask();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("Resumed", "Yes");
        startRepeatingTask();
    }

    void updateStatus()
    {
        Log.e("Running", "UiThread");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if (mReservation != null)
            {
                sendNowBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), QRCodeActivity.class);
                        startActivity(intent);
                    }
                });
                sendNowBtn.setBackgroundResource(R.drawable.send_receive_background);
            }
            else
            {
                sendNowBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(),
                                "Please Enable Hotspot", Toast.LENGTH_SHORT).show();
                    }
                });
                sendNowBtn.setBackgroundColor(Color.parseColor("#00000000"));
            }
        }
        else
        {
            if (ApManager.isApOn(getContext()))
            {
                sendNowBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), QRCodeActivity.class);
                        startActivity(intent);
                    }
                });
                sendNowBtn.setBackgroundResource(R.drawable.send_receive_background);
            }
            else
            {
                sendNowBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(),
                                "Please Enable Hotspot", Toast.LENGTH_SHORT).show();
                    }
                });
                sendNowBtn.setBackgroundColor(Color.parseColor("#00000000"));
            }
        }
    }
    private void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
            mReservation = null;
            hotspotOnBtn.setText("Hotspot On");
        }
    }

    private boolean checkSystemWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(getContext());
        }
        return false;
    }

    private void openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            if (getContext() != null)
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
        }
    }
}