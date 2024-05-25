package com.ztech.share;

import android.os.AsyncTask;

import java.io.IOException;

public class DataTransferTask extends AsyncTask<String, Integer, Response> {

    private final int port;
    private final boolean isServer;
    private final boolean isSending;
    private final boolean isData;
    private final String ipAddress;
    private SocketConnection socketConnection;
    private final ListenerDataTransfer listenerDataTransfer;
    private String dataReceived = "";
    private Response response;
    private final ListenerProgress listenerProgress;

    DataTransferTask(boolean isServer, boolean isSending, boolean isData, String ipAddress,
                     int port, ListenerDataTransfer listenerDataTransfer, ListenerProgress listenerProgress) {
        this.isServer = isServer;
        this.isSending = isSending;
        this.isData = isData;
        this.ipAddress = ipAddress;
        this.port = port;
        this.listenerDataTransfer = listenerDataTransfer;
        this.listenerProgress = listenerProgress;
    }

    @Override
    protected Response doInBackground(String... data) {
        boolean success = false;
        while (!success) {
            if (isCancelled()) {
                if (socketConnection!=null) {
                    socketConnection.setCancelled();
                    socketConnection.closeConnection();
                }
                return Response.CANCELLED;
            }
            socketConnection = new SocketConnection(ipAddress, port, listenerProgress);
            try {
                if (isServer)
                    socketConnection.openServerConnection();
                else
                    socketConnection.openClientConnection();

                if (isSending && isData) {
                    if (socketConnection.sendData(data[0])) {
                        success = true;
                        response = Response.DATA_SEND_SUCCESSFUL;
                    } else
                        response = Response.DATA_SEND_FAILED;
                }
                else if (isSending) {
                    if (socketConnection.sendFile(data[0])){
                        if (socketConnection.receiveData().compareTo("")!=0) {
                            response = Response.FILE_SEND_SUCCESSFUL;
                            success = true;
                        }
                        else
                            response = Response.FILE_SEND_FAILED;
                    } else
                        response = Response.FILE_SEND_FAILED;
                }
                else if (isData) {
                    dataReceived = socketConnection.receiveData();
                    if (dataReceived!=null && dataReceived.compareTo("") != 0) {
                        response = Response.DATA_RECEIVED_SUCCESSFUL;
                        success = true;
                    } else
                        response = Response.DATA_RECEIVED_FAILED;
                }
                else {
                    if (socketConnection.receiveFile(data[0])){
                        if (socketConnection.sendData("File Received")) {
                            response = Response.FILE_RECEIVED_SUCCESSFUL;
                            dataReceived = data[0];
                            success = true;
                        }
                        else
                            response = Response.FILE_RECEIVED_FAILED;
                    } else
                        response = Response.FILE_RECEIVED_FAILED;
                }
            } catch (IOException ignored) {
            } finally {
                if (socketConnection != null)
                    socketConnection.closeConnection();
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);
        listenerDataTransfer.onProgressUpdateComplete(response,dataReceived);
    }
}
