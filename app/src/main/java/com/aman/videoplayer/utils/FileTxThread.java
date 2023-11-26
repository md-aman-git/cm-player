package com.aman.videoplayer.utils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class FileTxThread extends Thread {
    Socket socket;
    String path, fileTitle;
    File file;
    char type;
    public static long currentSize = 0;
    FileTxThread(Socket socket, String path, char type, String fileTitle) {
        this.socket = socket;
        this.path = path;
        this.type = type;
        this.fileTitle = fileTitle;
    }

    @Override
    public void run() {
        file = new File(path);
        byte[] myByteArray = new byte[16 * 1024];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        OutputStream os;
        DataOutputStream dos = null;
        try {
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            if (fileTitle != null)
                dos.writeUTF(fileTitle);
            else
                dos.writeUTF("base.apk");
            dos.writeLong(file.length());
            dos.writeChar(type);
            int read;
            while ((read = dis.read(myByteArray)) != -1) {
                dos.write(myByteArray, 0, read);
                currentSize += read;
            }
            dos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.flush();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String getIpOfReceiver()
    {
        return String.valueOf(socket.getInetAddress());
    }
}
