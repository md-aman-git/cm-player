package com.aman.videoplayer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.videoplayer.utils.ClientRxThread;
import com.aman.videoplayer.services.FilesShareService;
import com.aman.videoplayer.R;
import com.aman.videoplayer.modals.ReceiveFilesModal;
import com.aman.videoplayer.adapters.ReceivingFileAdapter;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class ReceivingFilesDetailsFragment extends Fragment implements ServiceConnection {

    View view;
    TextView textView;
    private Handler mHandler;
    String ip_address;
    boolean serviceStarted = false;
    RecyclerView receiverDetailsRV;
    FilesShareService filesShareService;
    boolean bounded = false;
    ReceivingFileAdapter receivingFileAdapter;
    int currentSizeOfFile = 0;
    Intent intent;
    boolean once = false;
    ArrayList<ReceiveFilesModal> receiveFile;
    public ReceivingFilesDetailsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_receiving_files_details,
                container, false);
        receiverDetailsRV = view.findViewById(R.id.receivingRV);
        textView = view.findViewById(R.id.textView);
        receiveFile = new ArrayList<>();
        mHandler = new Handler();
        startRepeatingTask();
        return view;
    }
    public String getHotspotAdress(){
        final WifiManager manager = (WifiManager)getContext().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        int ipAddress = dhcp.gateway;
        ipAddress = (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ?
                Integer.reverseBytes(ipAddress) : ipAddress;
        byte[] ipAddressByte = BigInteger.valueOf(ipAddress).toByteArray();
        try {
            InetAddress myAddr = InetAddress.getByAddress(ipAddressByte);
            return myAddr.getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            Log.e("Wifi Class", "Error getting Hotspot IP address ", e);
        }
        return "null";
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (!serviceStarted)
                    updateStatus();
            } finally {
                if (serviceStarted)
                {
                    stopRepeatingTask();
                }
                // 5 seconds by default, can be changed later
                int mInterval = 2000;
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
        startRepeatingTask();
    }

    void updateStatus()
    {
        ip_address = getHotspotAdress();
        if (!ip_address.equals("null")) {
            textView.setText(ip_address);
            stopRepeatingTask();
            if (!serviceStarted)
            {
                intent = new Intent(getContext(), FilesShareService.class);
                intent.putExtra("sendingFiles", "receiveIt");
                intent.putExtra("ipAddress", ip_address);
                if (getContext() != null)
                    getContext().startService(intent);
                serviceStarted = true;
            }
        }
        if (getContext() != null && intent != null)
            getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        FilesShareService.MyBinder b = (FilesShareService.MyBinder) binder;
        filesShareService = b.getService();
        bounded = true;
        if (filesShareService != null) {
            if (getActivity() != null)
                getActivity().runOnUiThread(updater);
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
            int progress = (int) ClientRxThread.currentFileReceived;
            updateProgress(0, progress);
            mHandler.postDelayed(updater, 1000);
        }
    };
    void updateProgress(int position, int progress)
    {
        if (!once && ClientRxThread.totalFileToReceive != 0)
        {
            receiveFile.clear();
            ReceiveFilesModal receiveFilesModal =
                    new ReceiveFilesModal(ClientRxThread.currentFileName,
                    (int) ClientRxThread.totalFileToReceive);
            receiveFile.add(receiveFilesModal);
            receivingFileAdapter = new ReceivingFileAdapter(getContext(), receiveFile);
            receiverDetailsRV.setAdapter(receivingFileAdapter);
            receiverDetailsRV.setLayoutManager(new LinearLayoutManager(getContext(),
                    RecyclerView.VERTICAL, false));
            once = true;
        }
        ReceivingFileAdapter.ReceiveFileHolder fileHolder =
                (ReceivingFileAdapter.ReceiveFileHolder) receiverDetailsRV
                        .findViewHolderForAdapterPosition(position);
        if (fileHolder != null) {
            fileHolder.customProgress.setProgress(progress);
            fileHolder.currentFileSize.setText(String.valueOf(progress / (1024*1024)) + "MB");
            currentSizeOfFile = fileHolder.customProgress.getMax();
            if (currentSizeOfFile == progress && currentSizeOfFile != 0) {
                fileHolder.status.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(updater);
            }
        }
    }
}