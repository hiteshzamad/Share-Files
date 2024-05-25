package com.ztech.share;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

public class ActivitySender extends AppCompatActivity
        implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener {

    private static final String DEVICE_NAME = "device_name";
    private static final String SHARED_PREFERENCE = "shared_preference_android_share";

    private String[] extras;
    private String[] files;
    private String[] folders;
    private String[] apps;
    private String[] appLocation;
    private String ipAddress;
    private boolean isGroupOwner;
    private boolean isConnecting;
    private boolean connect;
    private boolean isSet;

    private Context context;
    private AlertDialog alertDialog;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WiFiDirectBroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private void initializeWifiDirect() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (wifiP2pManager != null) {
            channel = wifiP2pManager.initialize(this, getMainLooper(), null);
            receiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this, this, this);
        } else
            Toast.makeText(context, "Error while setting wifi direct", Toast.LENGTH_SHORT).show();
    }

    void configureWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return;
        boolean isHotspotOn = false;
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            Object o = method.invoke(wifiManager, (Object[]) null);
            if (o instanceof Integer) {
                int actualState = (Integer) o;
                if (actualState != 11)
                    isHotspotOn = true;
            }
        } catch (Exception ignored) {

        }
        if ((!wifiManager.isWifiEnabled() && !(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING)) || isHotspotOn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                startActivity(panelIntent);
            } else
                wifiManager.setWifiEnabled(true);
        }
    }

    void configureLocation() {
        if (!isLocationOn()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private boolean isLocationOn() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return locationManager.isLocationEnabled();
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        Toolbar toolbar = findViewById(R.id.sender_toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            extras = intent.getExtras().getStringArray("Extras");
            files = intent.getExtras().getStringArray("Files");
            folders = intent.getExtras().getStringArray("Folders");
            apps = intent.getExtras().getStringArray("Apps");
            appLocation = intent.getExtras().getStringArray("AppLocation");
        }
        isSet = false;
        context = this;
        connect = false;
        isConnecting = false;
        fragmentSearchTransaction();
        initializeWifiDirect();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        resetWifiDirect();
        super.onDestroy();
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info != null && connect) {
            if (info.groupFormed) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_search");
                if (fragment instanceof FragmentSearch) {
                    ipAddress = info.groupOwnerAddress.getHostAddress();
                    isGroupOwner = info.isGroupOwner;
                    if (alertDialog != null)
                        alertDialog.cancel();
                    isConnecting = false;
                    vibrate();
                    fragmentTransferTransaction();
                }
            }
        }
    }

    private void stopDiscoverPeers() {
        wifiP2pManager.stopPeerDiscovery(channel, null);
    }

    private void cancelConnect() {
        wifiP2pManager.cancelConnect(channel, null);
    }

    private void discoverPeers() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        wifiP2pManager.discoverPeers(channel, null);
    }

    void connect(String address) {
        isConnecting = true;
        dialog();
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        wifiP2pManager.connect(channel, config, null);
    }

    private void disconnect(){
        wifiP2pManager.removeGroup(channel, null);
    }

    private void resetWifiDirect(){
        stopDiscoverPeers();
        cancelConnect();
        disconnect();
        connect = true;
    }

    void setWifiOn(boolean wifiOn) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_search");
        if (fragment instanceof FragmentSearch) {
            if (wifiOn && isLocationOn()) {
                isSet = true;
                resetWifiDirect();
                String deviceName = setDeviceName();
                discoverPeers();
                ((FragmentSearch) fragment).allSet(deviceName);
            }
            else {
                isSet = false;
                ((FragmentSearch) fragment).notSet();
                if (!wifiOn) {
                    ((FragmentSearch) fragment).showWifiSetting();
                } else {
                    ((FragmentSearch) fragment).hideWifiSetting();
                }
                if (!isLocationOn()) {
                    ((FragmentSearch) fragment).showLocationSetting();
                } else {
                    ((FragmentSearch) fragment).hideLocationSetting();
                }
            }
        }
    }

    private void fragmentSearchTransaction(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FragmentSearch fragmentSearch = new FragmentSearch();
        fragmentTransaction.replace(R.id.sender_fragment_container, fragmentSearch,"tag_search");
        fragmentTransaction.commit();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        ObjectDevice[] devices = null;
        if (peers != null && peers.getDeviceList().size() != 0){
            int size = peers.getDeviceList().size();
            Collection<WifiP2pDevice> collection = peers.getDeviceList();
            ArrayList<WifiP2pDevice> wifiP2pDevices = new ArrayList<>(collection);
            int deviceCount = 0;
            for (int i=0;i<size;i++){
                if (wifiP2pDevices.get(i).deviceName.contains("Receiver")){
                    deviceCount++;
                }
            }
            devices = new ObjectDevice[deviceCount];
            deviceCount = 0;
            for (int i=0;i<size;i++){
                if (wifiP2pDevices.get(i).deviceName.contains("Receiver")) {
                    String[] temp = wifiP2pDevices.get(i).deviceName.split("Receiver");
                    String devName = temp[temp.length-1].trim();
                    devices[deviceCount] = new ObjectDevice(devName,
                            wifiP2pDevices.get(i).deviceAddress,
                            getRandomHexString(devName));
                    deviceCount++;
                }
            }
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_search");
        if (fragment instanceof FragmentSearch) {
            ((FragmentSearch) fragment).setDeviceItems(devices);
        }
    }


    private String getRandomHexString(String s) {
        String[] colors = {"#fc2403","#2803fc","#c906a2","#19c716",
                "#ed2843","#4ca3dd","#f707b3","#66ab11"};
        try {
            String code = String.valueOf(new BigInteger(s.getBytes()));
            long along = Long.parseLong(code.substring(code.length() - 10));
            int random = (int) (along % 8);
            return colors[random];
        }catch (Exception e){
            return "#1569ca";
        }
    }

    private String setDeviceName() {
        String deviceName = getDeviceName() ;
        try {
            String deviceEditedName = "Sender "+ deviceName ;
            if (deviceEditedName.length()>32)
                deviceEditedName = deviceEditedName.substring(0,32) ;
            deviceName = deviceEditedName.substring(7);
            Method method = wifiP2pManager.getClass().getMethod("setDeviceName", WifiP2pManager.Channel.class,
                    String.class, WifiP2pManager.ActionListener.class);
            method.invoke(wifiP2pManager, channel, deviceEditedName, null);
        } catch (Exception ignored) {

        }
        return deviceName;
    }

    private String getActualDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer))
            return (model);
        return (manufacturer) + " " + model;
    }

    private String getDeviceName(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String deviceName = sharedPreferences.getString(DEVICE_NAME, getActualDeviceName());
        if (deviceName.length()>20)
            deviceName = deviceName.substring(0,20);
        return deviceName;
    }

    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySender.this);
        LayoutInflater layoutInflater = LayoutInflater.from(ActivitySender.this);
        View view = layoutInflater.inflate(R.layout.dialog_connecting,null);
        builder.setView(view);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (isConnecting){
                    resetWifiDirect();
                    discoverPeers();
                    if (alertDialog!=null)
                        alertDialog.cancel();
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_search");
                    if (fragment instanceof FragmentSearch) {
                        ((FragmentSearch) fragment).setDeviceItems(null);
                    }
                    isConnecting = false;
                }
            }
        });
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void fragmentTransferTransaction(){
        FragmentTransaction fragmentTransaction= getSupportFragmentManager().beginTransaction();
        FragmentTransfer fragmentTransfer = new FragmentTransfer();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isServer",isGroupOwner);
        bundle.putBoolean("isSender",true);
        bundle.putString("ipAddress",ipAddress);
        bundle.putStringArray("Files",files);
        bundle.putStringArray("Folders",folders);
        bundle.putStringArray("Apps",apps);
        bundle.putStringArray("AppLocation",appLocation);
        bundle.putStringArray("Extras",extras);
        fragmentTransfer.setArguments(bundle);
        fragmentTransaction.replace(R.id.sender_fragment_container, fragmentTransfer,"tag_transfer");
        fragmentTransaction.commit();
    }

    private void vibrate (){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            else
                vibrator.vibrate(500);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_transfer");
        if ((fragment instanceof FragmentTransfer)) {
            ((FragmentTransfer) fragment).onBackPressed();
        }
        else
            super.onBackPressed();
    }

    public void setFinalDeviceName(String deviceName) {
        if (isSet) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_search");
            if (fragment instanceof FragmentDiscover) {
                String[] strings = deviceName.split("Sender ");
                if (strings.length>1)
                    ((FragmentDiscover) fragment).makeRippleVisible(strings[1]);
                else
                    ((FragmentDiscover) fragment).makeRippleVisible(deviceName);
            }
        }
    }
}
