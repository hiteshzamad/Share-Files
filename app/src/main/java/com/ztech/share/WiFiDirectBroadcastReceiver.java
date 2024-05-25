package com.ztech.share;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import androidx.core.app.ActivityCompat;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final Activity activity;
    private final WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private final WifiP2pManager.PeerListListener peerListListener;

    WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Activity activity,
                                WifiP2pManager.ConnectionInfoListener connectionInfoListener,
                                WifiP2pManager.PeerListListener peerListListener) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.connectionInfoListener = connectionInfoListener;
        this.peerListListener = peerListListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            boolean isWifiOn = false;
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                isWifiOn = true;
            if (activity instanceof ActivitySender)
                ((ActivitySender) activity).setWifiOn(isWifiOn);
            else if (activity instanceof ActivityReceiver)
                ((ActivityReceiver) activity).setWifiOn(isWifiOn);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null && peerListListener != null) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                manager.requestPeers(channel, peerListListener);
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager != null && connectionInfoListener != null) {
                manager.requestConnectionInfo(channel, connectionInfoListener);
            }
        }
        WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        if (device==null)
            return;
        if (activity instanceof ActivitySender) {
            ((ActivitySender)activity).setFinalDeviceName(device.deviceName);
        }else if (activity instanceof ActivityReceiver){
            ((ActivityReceiver)activity).setFinalDeviceName(device.deviceName);
        }
    }
}
