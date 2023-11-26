package com.aman.videoplayer.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aman.videoplayer.utils.ApManager;
import com.aman.videoplayer.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class QRScanFragment extends Fragment {

    View view;
    SurfaceView surfaceView = null;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    Button btnAction;

    public QRScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_q_r_scan, container, false);
        txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        surfaceView = view.findViewById(R.id.surfaceView);
        btnAction = view.findViewById(R.id.btnAction);
        return view;
    }

    private void initialiseDetectorsAndSources() {

        //Toast.makeText(getContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        if (getContext() != null)
        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        if (surfaceView != null)
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getContext(),
                        "Barcode Scanner Stopped...",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                            {
                                if (getContext() != null) {
                                    if (ApManager.isApOn(getContext()))
                                        ApManager.configApState(getContext());
                                }
                            }
                            if (getContext() != null) {
                                WifiManager wifiManager = (WifiManager) getContext()
                                        .getApplicationContext()
                                        .getSystemService(Context.WIFI_SERVICE);
                                if (!wifiManager.isWifiEnabled()) {
                                    wifiManager.setWifiEnabled(true);
                                }
                            }
                            txtBarcodeValue.removeCallbacks(null);
                            txtBarcodeValue.setText(barcodes.valueAt(0).displayValue);
                            connectWifi(barcodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });
    }
    void connectWifi(String information)
    {
        if (getActivity() != null) {
            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", information.substring(0, 17));
            wifiConfig.preSharedKey = String.format("\"%s\"", information.substring(17));
            int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            if (getFragmentManager() != null)
                getFragmentManager().beginTransaction()
                        .replace(R.id.receiverFrameLayout, new ReceivingFilesDetailsFragment())
                        .commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    public void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                initialiseDetectorsAndSources();
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