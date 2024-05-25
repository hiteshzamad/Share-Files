package com.ztech.share;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.lang.reflect.Method;

public class ActivityReceiver extends AppCompatActivity
        implements WifiP2pManager.ConnectionInfoListener {

    private static final String DEVICE_NAME = "device_name";
    private static final String SHARED_PREFERENCE = "shared_preference_android_share";
    private static final String APP_STORAGE_LOCATION = "app_storage_location";

    private boolean connect;
    private boolean isSet;
    private boolean isGroupOwner;
    private String ipAddress;
    private Context context;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WiFiDirectBroadcastReceiver receiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        context = (Context) this;
        setSupportActionBar((Toolbar) findViewById(R.id.receiver_toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        }
        fragmentDeviceInfoTransaction();
        initializeWifiDirect();
        makeDir();
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
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_discover");
                if (fragment instanceof FragmentDiscover) {
                    ipAddress = info.groupOwnerAddress.getHostAddress();
                    isGroupOwner = info.isGroupOwner;
                    vibrate();
                    fragmentTransferTransaction();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_transfer");
        if ((fragment instanceof FragmentTransfer)) {
            ((FragmentTransfer) fragment).onBackPressed();
        } else
            super.onBackPressed();
    }

    private void initializeWifiDirect() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (wifiP2pManager != null) {
            channel = wifiP2pManager.initialize(this, getMainLooper(), null);
            receiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this, this, null);
        } else
            Toast.makeText(context, "Error while setting up wifi direct", Toast.LENGTH_SHORT).show();
    }

    void setWifiOn(boolean wifiOn) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_discover");
        if (fragment instanceof FragmentDiscover) {
            if (wifiOn && isLocationOn()) {
                isSet = true;
                resetWifiDirect();
                String deviceName = setDeviceName();
                discoverPeers();
                ((FragmentDiscover) fragment).makeRippleVisible(deviceName);
            } else {
                isSet = false;
                ((FragmentDiscover) fragment).makeRippleInvisible();
                if (!wifiOn) {
                    ((FragmentDiscover) fragment).showWifiSetting();
                } else {
                    ((FragmentDiscover) fragment).hideWifiSetting();
                }
                if (!isLocationOn()) {
                    ((FragmentDiscover) fragment).showLocationSetting();
                } else {
                    ((FragmentDiscover) fragment).hideLocationSetting();
                }
            }
        }
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

    private void fragmentTransferTransaction() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FragmentTransfer fragmentTransfer = new FragmentTransfer();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isServer", isGroupOwner);
        bundle.putBoolean("isSender", false);
        bundle.putString("ipAddress", ipAddress);
        bundle.putStringArray("Files", null);
        fragmentTransfer.setArguments(bundle);
        fragmentTransaction.replace(R.id.receiver_fragment_container, fragmentTransfer, "tag_transfer");
        fragmentTransaction.commit();
    }

    private void fragmentDeviceInfoTransaction() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FragmentDiscover fragmentDiscover = new FragmentDiscover();
        fragmentTransaction.replace(R.id.receiver_fragment_container, fragmentDiscover, "tag_discover");
        fragmentTransaction.commit();
    }

    private void cancelConnect() {
        wifiP2pManager.cancelConnect(channel, null);
    }

    private void stopDiscoverPeers() {
        wifiP2pManager.stopPeerDiscovery(channel, null);
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

    private void disconnect(){
        wifiP2pManager.removeGroup(channel, null);
    }

    private void resetWifiDirect(){
        cancelConnect();
        disconnect();
        stopDiscoverPeers();
        connect = true;
    }

    private void makeDir(){
        File myDirectory = new File(getAppStorageLocation() + getString(R.string.app_name));
        if(!myDirectory.exists()) {
            if (!myDirectory.mkdirs())
                Toast.makeText(this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
        }
    }

    private String getInternalPath() {
        try {
            File file = getExternalFilesDir(null);
            String state = Environment.getExternalStorageState(file);
            if (file == null)
                return "/storage/emulated/0/";
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                String[] temp = file.getPath().split("/");
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < temp.length - 4; j++)
                    stringBuilder.append(temp[j]).append("/");
                return stringBuilder.toString();
            }
        }catch (Exception ignored){
        }
        return "/storage/emulated/0/";
    }

    private String getAppStorageLocation() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(APP_STORAGE_LOCATION, getInternalPath());
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

    private String setDeviceName() {
        String deviceName = getDeviceName() ;
        try {
            String deviceEditedName = "Receiver "+ deviceName ;
            if (deviceEditedName.length()>32)
                deviceEditedName = deviceEditedName.substring(0,32) ;
            deviceName = deviceEditedName.substring(9);
            Method method = wifiP2pManager.getClass().getMethod("setDeviceName", WifiP2pManager.Channel.class,
                    String.class, WifiP2pManager.ActionListener.class);
            method.invoke(wifiP2pManager, channel, deviceEditedName, null);
        } catch (Exception ignored) {

        }
        return deviceName;
    }

    private void vibrate(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            else
                vibrator.vibrate(500);
        }
    }

    public void setFinalDeviceName(String deviceName) {
        if (isSet) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag_discover");
            if (fragment instanceof FragmentDiscover) {
                String[] strings = deviceName.split("Receiver ");
                if (strings.length>1)
                    ((FragmentDiscover) fragment).makeRippleVisible(strings[1]);
                else
                    ((FragmentDiscover) fragment).makeRippleVisible(deviceName);
            }
        }
    }
}
