package com.aman.videoplayer.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketThread extends Thread {

    int SocketServerPORT;
    ServerSocket serverSocket;
    String path, fileTitle;
    char type;
    public ServerSocketThread(String path, int SocketPort, char type, String fileTitle)
    {
        this.path = path;
        SocketServerPORT = SocketPort;
        this.type = type;
        this.fileTitle = fileTitle;
    }

    @Override
    public void run() {
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(SocketServerPORT);

            while (true) {
                socket = serverSocket.accept();
                FileTxThread fileTxThread = new FileTxThread(socket, path,
                        type, fileTitle);
                fileTxThread.start();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    int getIpAddressOfServer()
    {
        return serverSocket.getLocalPort();
    }

}
