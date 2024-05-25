package com.ztech.share;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

class SocketConnection {

    private final String ipAddress;
    private final int port;
    private boolean isCancelled;
    private boolean isSending;
    private boolean isFile;

    private String receivedData;
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedOutputStream bufferedOutputStream;
    private BufferedInputStream bufferedInputStream;
    private final ListenerProgress listenerProgress;

    SocketConnection(String ipAddress, int port, ListenerProgress listenerProgress) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.listenerProgress = listenerProgress;
        isCancelled = false;
    }

    void openServerConnection() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.setSoTimeout(6000);
        serverSocket.bind(new InetSocketAddress(port));
        socket = serverSocket.accept();
    }

    void openClientConnection() throws IOException {
        socket = new Socket();
        socket.bind(null);
        socket.connect((new InetSocketAddress(ipAddress, port)), 3000);
    }

    boolean sendFile(String filePath) throws IOException {
        setTransfer(true,true);
        bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        return copy(bufferedInputStream,bufferedOutputStream);
    }

    boolean receiveFile(String filePath) throws IOException {
        setTransfer(false,true);
        bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        return copy(bufferedInputStream,bufferedOutputStream);
    }

    boolean sendData(String data) throws IOException {
        setTransfer(true,false);
        bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(data.getBytes()));
        return copy(bufferedInputStream,bufferedOutputStream);
    }

    String receiveData() throws IOException {
        setTransfer(false,false);
        bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new ByteArrayOutputStream());
        copy(bufferedInputStream,bufferedOutputStream);
        return receivedData;
    }

    private boolean copy(InputStream inputStream, OutputStream outputStream) {
        byte[] bytes = new byte[1024 * 32];
        try {
            long size = 0;
            StringBuilder stringBuilder = new StringBuilder();
            int bytesRead = inputStream.read(bytes, 0, bytes.length);
            while (bytesRead > -1) {
                if (isCancelled)
                    return false;
                if (listenerProgress != null && isFile && isSending) {
                    size += bytesRead;
                    listenerProgress.updateSendProgress(size);
                }
                else if (listenerProgress != null && isFile) {
                    size += bytesRead;
                    listenerProgress.updateReceiveProgress(size);
                }
                outputStream.write(bytes, 0, bytesRead);
                outputStream.flush();
                if (!isSending && !isFile)
                    stringBuilder.append(new String(bytes,0,bytesRead));
                bytesRead = inputStream.read(bytes, 0, bytes.length);
            }
            receivedData = stringBuilder.toString();
            if (isSending)
                socket.shutdownOutput();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    void closeConnection(){
        if (bufferedOutputStream!=null){
            try {
                bufferedOutputStream.close();
                bufferedOutputStream = null;
            } catch (IOException ignored) {
            }
        }
        if (socket!=null){
            try {
                socket.close();
                socket = null;
            } catch (IOException ignored) {
            }
        }
        if (serverSocket!=null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException ignored) {
            }
        }
    }

    private void setTransfer(boolean isSending, boolean isFile){
        this.isSending = isSending;
        this.isFile = isFile;
    }

    void setCancelled() {
        isCancelled = true;
    }
}
