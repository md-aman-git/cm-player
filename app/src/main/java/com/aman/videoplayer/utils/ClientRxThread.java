package com.aman.videoplayer.utils;

import android.os.Environment;
import android.util.Log;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientRxThread extends Thread {
    String dstAddress;
    int dstPort;
    public static long currentFileReceived = 0;
    public static long totalFileToReceive = 0;
    public static String currentFileName = "Identifying File Name";
    Socket socket = null;

    public ClientRxThread(String address, int port) {
        dstAddress = address;
        dstPort = port;
    }

    @Override
    public void run() {

        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            socket = new Socket(dstAddress, dstPort);
            InputStream in = null;
            int bufferSize;
            try {
                bufferSize = socket.getReceiveBufferSize();
                in = socket.getInputStream();
                DataInputStream clientData = new DataInputStream(in);
                currentFileName = clientData.readUTF();
                totalFileToReceive = clientData.readLong();
                char type = clientData.readChar();
                String fileName;
                switch (type)
                {
                    case 'v':
                        fileName = "/CM PLAYER/Video";
                        break;
                    case 'm':
                        fileName = "/CM PLAYER/Audio";
                        break;
                    case 'a':
                        fileName = "/CM PLAYER/Apps";
                        break;
                    case 'd':
                        fileName = "/CM PLAYER/Documents";
                        break;
                    default:
                        fileName = "/CM PLAYER/Others";
                }
                File file = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + fileName);
                if (!file.exists())
                    file.mkdirs();
                Log.e("FileName - FileSize ", currentFileName + " - " + totalFileToReceive);
                OutputStream output = new FileOutputStream(new File(file, currentFileName));
                byte[] buffer = new byte[bufferSize];
                int read;
                while ((read = clientData.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    currentFileReceived += read;
                }
                output.flush();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
