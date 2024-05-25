package com.ztech.share;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ActivityFTP extends AppCompatActivity {

    private static final String STOP_FTP = "Stop FTP";
    private static final String START_FTP = "Start FTP";
    private static final String DEVICE_NAME = "device_name";
    private static final String NEW_USER = "new_user";
    private static final String SHARED_PREFERENCE = "shared_preference_android_share";
    private static final String CHANNEL_ID = "FTP";

    private boolean isAnonymousLoginEnabled ;
    private boolean isStarted;
    private long lastClickTime;
    private String UserName, Password, IpAddress;

    private Button button01;
    private TextView textViewInfo, textView01, textView02;
    private View view;
    private RadioGroup radioGroup;
    private RadioButton radioButton01, radioButton02;
    private EditText editText01, editText02;
    private Button button02;
    private AlertDialog alertDialog;
    private ActivityFTP activityFTP;
    private FtpServer ftpServer;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);
        button01 = findViewById(R.id.ftp_button01);
        textViewInfo = findViewById(R.id.ftp_textView01);
        view = findViewById(R.id.ftp_includeLayout01);
        bottomSheetBehavior = BottomSheetBehavior.from(view);
        bindDialogView(view);
        activityFTP = this;
        setSupportActionBar((Toolbar)findViewById(R.id.ftp_toolbar));
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        isAnonymousLoginEnabled = true;
        isStarted = false;
        lastClickTime = 0;
        IpAddress = "";
        if (isNewUser())
            showHelpLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(activityFTP);
        menuInflater.inflate(R.menu.menu_ftp,menu);
        menu.getItem(0).setShowAsAction(ActionMenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_ftp_help)
            showHelpLayout();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStarted)
            button01.setText(STOP_FTP);
        else {
            textViewInfo.setText("");
            button01.setText(START_FTP);
        }
        button01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 500)
                    return;
                lastClickTime = SystemClock.elapsedRealtime();
                if (!isStarted) {
                    if (isHotspotOn()){
                        IpAddress = "192.168.43.1";
                    } else if (isWifiConnected() ){
                        IpAddress = getIpAddress();
                    } else {
                        Toast.makeText(ActivityFTP.this,"Device not connected to wifi network",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (IpAddress.compareTo("0.0.0.0")==0 || IpAddress.compareTo("")==0 ){
                        Toast.makeText(ActivityFTP.this,"Device not connected to wifi network",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (isAnonymousLoginEnabled) {
                        radioGroup.check(R.id.dialog_ftp_radioButton01);
                        hideView();
                    }
                    else {
                        radioGroup.check(R.id.dialog_ftp_radioButton02);
                        showView();
                    }
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else {
                    closeFTPServer();
                    isStarted = false;
                }
            }
        });

        button02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnonymousLoginEnabled){
                    if (editText01.getText().toString().compareTo("")==0) {
                        Toast.makeText(ActivityFTP.this, "Username cannot be empty", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (editText02.getText().toString().compareTo("")==0) {
                        Toast.makeText(ActivityFTP.this, "Password cannot be empty", Toast.LENGTH_LONG).show();
                        return;
                    }
                    UserName = editText01.getText().toString();
                    Password = editText02.getText().toString();
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                openFTPServer();
            }
        });

        radioButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAnonymousLoginEnabled = true;
                hideView();
            }
        });

        radioButton02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAnonymousLoginEnabled = false;
                showView();
            }
        });
    }

    @Override
    protected void onDestroy() {
        closeFTPServer();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        if (isStarted)
            dialogStop();
        else
            super.onBackPressed();
    }

    private boolean isNewUser(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE,MODE_PRIVATE);
        boolean newUser = sharedPreferences.getBoolean(NEW_USER,true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NEW_USER,false);
        editor.apply();
        return newUser;
    }

    private void showHelpLayout() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activityFTP);
                LayoutInflater layoutInflater = LayoutInflater.from(activityFTP);
                View view = layoutInflater.inflate(R.layout.layout_ftp_help,null);
                ViewPager2 viewPager2 = view.findViewById(R.id.layout_ftp_help_viewPager2);
                builder.setView(view);
                alertDialog = builder.create();
                alertDialog.show();
                AdapterSectionPagerFTPHelp adapterSectionPagerFTPHelp = new AdapterSectionPagerFTPHelp(activityFTP,viewPager2,alertDialog);
                viewPager2.setAdapter(adapterSectionPagerFTPHelp);
            }
        },250);
    }

    private void closeFTPServer(){
        cancelNotification();
        try {
            if (ftpServer!=null)
                ftpServer.stop();
        }catch (Exception ignored){
        }
        isStarted = false;
        button01.setText(START_FTP);
        textViewInfo.setText("");
    }

    private void openFTPServer(){
        try {
            FtpServerFactory serverFactory = new FtpServerFactory();
            ListenerFactory factory = new ListenerFactory();
            int PORT;
            do {
                PORT = getRandomPort();
            } while (isPortInUse(IpAddress, PORT));
            factory.setPort(PORT);
            serverFactory.addListener("default", factory.createListener());
            ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
            connectionConfigFactory.setAnonymousLoginEnabled(isAnonymousLoginEnabled);
            serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
            serverFactory.getUserManager().save(getBaseUser());
            ftpServer = serverFactory.createServer();
            ftpServer.start();
            isStarted = true;
            button01.setText(STOP_FTP);
            String data ;
            if (!isAnonymousLoginEnabled)
                data = "Ftp Started \nUrl : ftp://" + IpAddress + ":" + PORT + "\nUsername : " + UserName + "\nPassword : " + Password;
            else
                data = "Ftp Started \nUrl : ftp://" + IpAddress + ":" + PORT + "\n";
            textViewInfo.setText(data);
            String ftpAddress = "ftp://" + IpAddress + ":" + PORT ;
            issueNotification(ftpAddress);
        }catch (Exception ignored){
        }
    }

    private void hideView(){
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        editText01.setVisibility(View.GONE);
        editText02.setVisibility(View.GONE);
        textView01.setVisibility(View.GONE);
        textView02.setVisibility(View.GONE);
    }

    private void showView(){
        editText01.setVisibility(View.VISIBLE);
        editText02.setVisibility(View.VISIBLE);
        textView01.setVisibility(View.VISIBLE);
        textView02.setVisibility(View.VISIBLE);
        editText01.setText(getUserName());
        String password = "Password";
        editText02.setText(password);
    }

    private BaseUser getBaseUser(){
        BaseUser user = new BaseUser();
        if (isAnonymousLoginEnabled){
            user.setName("anonymous");
        }
        else {
            user.setName(UserName);
            user.setPassword(Password);
        }
        user.setHomeDirectory(getBaseFolder());
        return user;
    }

    private void issueNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel();
        }
        Intent intent = new Intent(this, ActivityFTP.class);
        intent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP | Intent. FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("FTP Started")
                .setContentText(message)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorMain))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager!=null)
        notificationManager.notify(1, notification.build());
    }

    private void cancelNotification(){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager!=null)
            notificationManager.cancel(1);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void makeNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.channel_name_ftp), NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager!=null)
            notificationManager.createNotificationChannel(channel);
    }

    private boolean isPortInUse(String address, int portNumber) {
        try {
            (new Socket(address, portNumber)).close();
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }

    private String getBaseFolder(){
        File file = getExternalFilesDir(null);
        String basePath = "/storage/emulated/0/";
        if (file != null) {
            String[] temp = file.getPath().split("/");
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < temp.length - 4; j++)
                stringBuilder.append(temp[j]).append("/");
            basePath = stringBuilder.toString();
        }
        return basePath;
    }

    private int getRandomPort(){
        return (int)(Math.random()*(65536 - 49152 )) + 49151 ;
    }

    private String getIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        if (wifiManager==null)
            return "";
        return intToINetAddress(wifiManager.getDhcpInfo().ipAddress);
    }

    private String intToINetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes).toString().substring(1);
        } catch (UnknownHostException e) {
            return "";
        }
    }

    private String getActualDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer))
            return (model);
        return (manufacturer) + " " + model;
    }

    private String getUserName(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DEVICE_NAME, getActualDeviceName());
    }

    private boolean isHotspotOn() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
                method.setAccessible(true);
                Integer state = (Integer) method.invoke(wifiManager, (Object[]) null);
                if (state != null)
                    return state == 13;
            }
        }catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean isWifiConnected(){
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi != null)
                    return mWifi.isConnected();
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    private void dialogStop(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activityFTP);
        alertBuilder.setMessage("Do you want to stop ftp server ?");
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activityFTP.finish();
            }
        });
        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void bindDialogView(View view){
        radioGroup = view.findViewById(R.id.dialog_ftp_radioGroup01);
        radioButton01 = view.findViewById(R.id.dialog_ftp_radioButton01);
        radioButton02 = view.findViewById(R.id.dialog_ftp_radioButton02);
        textView01 = view.findViewById(R.id.dialog_ftp_textView01);
        textView02 = view.findViewById(R.id.dialog_ftp_textView02);
        editText01 = view.findViewById(R.id.dialog_ftp_editText01);
        editText02 = view.findViewById(R.id.dialog_ftp_editText02);
        button02 = view.findViewById(R.id.dialog_ftp_button);
    }
}