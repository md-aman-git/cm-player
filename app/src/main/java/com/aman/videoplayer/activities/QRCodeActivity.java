package com.aman.videoplayer.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.aman.videoplayer.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static com.aman.videoplayer.fragments.SenderFragment.hotspotName;
import static com.aman.videoplayer.fragments.SenderFragment.hotspotPass;

public class QRCodeActivity extends AppCompatActivity {

    ImageView qr_code_activity;
    String originalSsId, duplicateSsId = "AndroidShare_1234", password;
    private int mInterval = 1000;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_code);
        qr_code_activity = findViewById(R.id.qr_code_activity);
        mHandler = new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if (hotspotName != null && hotspotPass != null)
            {
                duplicateSsId = hotspotName;
                password = hotspotPass;
                qrCodeBitmap();
            }
        }
        else
        {
            boolean setOrNot = setHotspotName(duplicateSsId, this);
            if (!setOrNot)
            {
                setHotspotName(duplicateSsId, this);
            }
            qrCodeBitmap();
        }
    }
    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 300, 300, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public boolean setHotspotName(String newName, Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            Method getConfigMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifiManager);
            if (wifiConfig != null) {
                originalSsId = wifiConfig.SSID;
                password = wifiConfig.preSharedKey;
                wifiConfig.SSID = newName;
            }
            Method setConfigMethod = wifiManager.getClass()
                    .getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setConfigMethod.invoke(wifiManager, wifiConfig);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void qrCodeBitmap()
    {
        try {
            Bitmap bitmap = encodeAsBitmap(duplicateSsId + password);
            qr_code_activity.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
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

    private void updateStatus() {
        if (getClientList())
        {
            Intent intent = new Intent(this, SendingDetailsActivity.class);
            startActivity(intent);
        }
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRepeatingTask();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            setHotspotName(originalSsId, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRepeatingTask();
    }

    public boolean getClientList() {
        int macCount = 0;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                String mac = splitted[3];
                System.out.println("Mac : Outside If "+ mac );
                if (mac.matches("..:..:..:..:..:..")) {
                    macCount++;
                    if (macCount > 0)
                    {
                        return true;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
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