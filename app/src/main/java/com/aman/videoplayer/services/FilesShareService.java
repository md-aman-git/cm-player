package com.aman.videoplayer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aman.videoplayer.modals.ShareFiles;
import com.aman.videoplayer.utils.ClientRxThread;
import com.aman.videoplayer.utils.ServerSocketThread;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

import static com.aman.videoplayer.activities.SenderActivity.mReservation;

public class FilesShareService extends Service {

    ServerSocket serverSocket;
    ServerSocketThread serverSocketThread;
    private final IBinder mBinder = new MyBinder();
    static final int SocketServerPORT = 4444;
    private String PATH_OF_SELECTED_FILE_PERF = "PATH_PREF_NAME";
    private String PATH_PREF_NAME = "HereFileStored";
    SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(PATH_OF_SELECTED_FILE_PERF,
                Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public FilesShareService getService() {
            return FilesShareService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String intentData = intent.getStringExtra("sendingFiles");
        String ipAdd = intent.getStringExtra("ipAddress");
        Log.e("Servicing", "Yes");
        if (intentData != null && intentData.equals("sentIt")) {
            Gson gson = new Gson();
            String jsonText = preferences.getString(PATH_PREF_NAME, null);
            ShareFiles[] text = gson.fromJson(jsonText, ShareFiles[].class);
            ArrayList<ShareFiles> shareFiles = new ArrayList<ShareFiles>(Arrays.asList(text));
            String myPath = shareFiles.get(0).getPath();
            char type = shareFiles.get(0).getType();
            String title = shareFiles.get(0).getTitle();
            String fileName = shareFiles.get(0).getFileName();
            if (!title.equals(""))
            {
                serverSocketThread = new ServerSocketThread(myPath,
                        SocketServerPORT, type, title);
                serverSocketThread.start();
            }
            else
            {
                serverSocketThread = new ServerSocketThread(myPath,
                        SocketServerPORT, type, fileName);
                serverSocketThread.start();
            }
            Log.e("Sending", "Yes");
        }
        else if (intentData != null && intentData.equals("receiveIt")) {
            if (ipAdd != null) {
                ClientRxThread clientRxThread =
                        new ClientRxThread(
                                ipAdd,
                                SocketServerPORT);
                clientRxThread.start();
                Log.e("Receiving", "Yes");
            }
        }
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (mReservation != null)
        {
            mReservation.close();
        }
    }

}
