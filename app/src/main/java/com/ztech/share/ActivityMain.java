package com.ztech.share;

import static com.ztech.share.General.DEVICE_NAME;
import static com.ztech.share.General.SHARED_PREFERENCE;
import static com.ztech.share.General.getDefaultDeviceName;
import static com.ztech.share.General.getEditableAppStorageLocation;
import static com.ztech.share.General.getEditableDeviceName;
import static com.ztech.share.General.getStoragePath;
import static com.ztech.share.General.setEditableAppStorageLocation;
import static com.ztech.share.General.setEditableDeviceName;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class ActivityMain extends AppCompatActivity {

    private static final int NAVIGATION_VIEW_DELAY_TIME = 200;
    private static final int ON_CLICK_TIME_DIFFERENCE = 500;
//    private static final String FILE_TRANSFERRED = "file_transferred";
//    private static final String DATA_TRANSFERRED = "data_transferred";
//    private static final String DEVICE_NAME = "device_name";
    private static final int MULTIPLE_PERMISSIONS = 1;

    private long lastClickTime;
    private int newActivity;
    private Button button01,button02;
    private LinearLayout linearLayout;
    private TextView textView03;
    private Context context;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AlertDialog alertDialogEditDeviceName, alertDialogAbout, alertDialogStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariables();
        bindView();
        initAlertDialogEditDeviceName();
        initAlertDialogAppStorageLocation();
        initAlertDialogAbout();
//        initAds();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(context);
        menuInflater.inflate(R.menu.menu_main,menu);
        menu.getItem(0).setIcon(ContextCompat.getDrawable(context,R.drawable.ic_ftp));
        menu.getItem(0).setShowAsAction(ActionMenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_main_ftp) {
            if (SystemClock.elapsedRealtime() - lastClickTime < ON_CLICK_TIME_DIFFERENCE)
                return super.onOptionsItemSelected(item);
            lastClickTime = SystemClock.elapsedRealtime();
            newActivity = 4;
            if (isPermissionNeeded())
                getRemainingPermissions();
            else
                startActivity(new Intent(context, ActivityFTP.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        drawerLayout.openDrawer(GravityCompat.START);
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMenuItemContent();
        setDeviceNameTextView();
        button01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < ON_CLICK_TIME_DIFFERENCE)
                    return;
                newActivity = 1;
                lastClickTime = SystemClock.elapsedRealtime();
                if (isPermissionNeeded())
                    getRemainingPermissions();
                else
                    startActivity(new Intent(context,ActivitySelect.class));
            }
        });
        button02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < ON_CLICK_TIME_DIFFERENCE)
                    return;
                newActivity = 2;
                lastClickTime = SystemClock.elapsedRealtime();
                if (isPermissionNeeded())
                    getRemainingPermissions();
                else
                    startActivity(new Intent(context,ActivityReceiver.class));
            }
        });
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < ON_CLICK_TIME_DIFFERENCE)
                    return;
                lastClickTime = SystemClock.elapsedRealtime();
                alertDialogEditDeviceName.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i : grantResults){
                if (i == -1) {
                    Toast.makeText(context,"Permission Not Granted",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (newActivity == 1)
                startActivity(new Intent(context,ActivitySelect.class));
            else if (newActivity == 2)
                startActivity(new Intent(context,ActivityReceiver.class));
            else if (newActivity == 4)
                startActivity(new Intent(context,ActivityFTP.class));
        }
    }

    private void initVariables(){
        context = this;
    }

//    private void initAds() {
//        MobileAds.initialize(context, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//            }
//        });
//        AdView adView = findViewById(R.id.main_adView01);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);
//    }


    private void bindView(){
        drawerLayout = findViewById(R.id.main_drawerLayout);
        navigationView = findViewById(R.id.main_navigationView);
        View view = findViewById(R.id.main_include);
        button01 = view.findViewById(R.id.main_button01);
        button02 = view.findViewById(R.id.main_button02);
        textView03 = view.findViewById(R.id.main_textView03);
        linearLayout = view.findViewById(R.id.main_layout_deviceName);
        Toolbar toolbar = view.findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_main_about) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertDialogAbout.show();
                        }
                    }, NAVIGATION_VIEW_DELAY_TIME);
                }
                else if (item.getItemId() == R.id.menu_main_storage_location) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertDialogStorage.show();
                        }
                    }, NAVIGATION_VIEW_DELAY_TIME);
                }
                return false;
            }
        });
    }

    private void initAlertDialogEditDeviceName(){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_device_name, null);
        final EditText editText = view.findViewById(R.id.dialog_device_name_editText01);
        editText.requestFocus();
        editText.setText(getEditableDeviceName(getSharedPreferences(SHARED_PREFERENCE,Context.MODE_PRIVATE)));
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setView(view);
        alertBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String deviceName = editText.getText().toString();
                if(deviceName.compareTo("")!=0 && deviceName.length() > 0){
                    if (deviceName.length()>20)
                        deviceName = deviceName.substring(0,20);
                    setEditableDeviceName(getSharedPreferences(SHARED_PREFERENCE,Context.MODE_PRIVATE),deviceName);
                    textView03.setText(deviceName);
                    Toast.makeText(context,"Device Name Updated", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(context,"Name Cannot Be Blank", Toast.LENGTH_SHORT).show();
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogEditDeviceName = alertBuilder.create();
        alertDialogEditDeviceName.setCancelable(false);
    }

    private void initAlertDialogAbout(){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_about, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setView(view);
        alertDialogAbout = alertBuilder.create();
    }

    private void initAlertDialogAppStorageLocation() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_app_storage_location, null);
        final RadioGroup radioGroup = view.findViewById(R.id.dialog_app_storage_location_radio_group);
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        final String[] storagePath = getStoragePath(getExternalFilesDirs(null));
        if (storagePath==null)
            return;
        String appStorageLocation = getEditableAppStorageLocation(getSharedPreferences(SHARED_PREFERENCE,Context.MODE_PRIVATE), getExternalFilesDirs(null));
        RadioButton[] radioButton = new RadioButton[storagePath.length];
        for(int i=0; i<storagePath.length; i++){
            radioButton[i]  = new RadioButton(context);
            radioButton[i].setId(i);
            radioButton[i].setText(storagePath[i]);
            if(appStorageLocation.compareTo(storagePath[i]) == 0)
                radioButton[i].setChecked(true);
            radioGroup.addView(radioButton[i]);
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setView(view);
        alertBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setEditableAppStorageLocation(getSharedPreferences(SHARED_PREFERENCE,Context.MODE_PRIVATE),storagePath[radioGroup.getCheckedRadioButtonId()]);
                setMenuItemContent();
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogStorage = alertBuilder.create();
    }

    private void addPermissionToListIfNotGranted(String permission , List<String> listPermissionsNeeded){
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(permission);
    }

    private void getRemainingPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        addPermissionToListIfNotGranted(Manifest.permission.ACCESS_NETWORK_STATE,listPermissionsNeeded);
        addPermissionToListIfNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE,listPermissionsNeeded);
        addPermissionToListIfNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,listPermissionsNeeded);
        addPermissionToListIfNotGranted(Manifest.permission.ACCESS_FINE_LOCATION,listPermissionsNeeded);
        addPermissionToListIfNotGranted(Manifest.permission.ACCESS_WIFI_STATE,listPermissionsNeeded);
        addPermissionToListIfNotGranted(Manifest.permission.CHANGE_WIFI_STATE,listPermissionsNeeded);
        addPermissionToListIfNotGranted(Manifest.permission.INTERNET,listPermissionsNeeded);
        addPermissionToListIfNotGranted(Manifest.permission.VIBRATE,listPermissionsNeeded);
        if (listPermissionsNeeded.size()>0) {
            String[] strings = new String[listPermissionsNeeded.size()];
            for (int i=0;i<listPermissionsNeeded.size();i++){
                strings[i] = listPermissionsNeeded.get(i);
            }
            ActivityCompat.requestPermissions((Activity) context, strings, MULTIPLE_PERMISSIONS);
        }
    }

    private boolean isPermissionNeeded(){
        int GRANTED = PackageManager.PERMISSION_GRANTED;
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != GRANTED) ||
                (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != GRANTED) ||
                (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != GRANTED) ||
                (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != GRANTED) ||
                (ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) != GRANTED) ||
                (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) != GRANTED) ||
                (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != GRANTED) ||
                (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != GRANTED);
    }

    private void setMenuItemContent(){
        Menu menu = navigationView.getMenu();
        String[] storagePath = getStoragePath(getExternalFilesDirs(null));
        if (storagePath==null)
            return;
        StatFs statFsInternal = new StatFs(storagePath[0]);
        String freeStorage1 = General.convertSizeToUnit(statFsInternal.getAvailableBlocksLong() * statFsInternal.getBlockSizeLong());
        menu.getItem(0).setTitle("Internal Storage" + "\n" + freeStorage1 + " " + "Free");
        if (storagePath.length>1){
            StatFs stat2 = new StatFs(storagePath[1]);
            String freeStorage2 = General.convertSizeToUnit(stat2.getAvailableBlocksLong() * stat2.getBlockSizeLong());
            menu.getItem(1).setTitle("SD Card Storage" + "\n" + freeStorage2 + " " + "Free");
            menu.getItem(1).setVisible(true);
        }
        String appStorageLocation = getEditableAppStorageLocation(getSharedPreferences(SHARED_PREFERENCE,Context.MODE_PRIVATE), getExternalFilesDirs(null));
        int i;
        for (i = 0;i<storagePath.length;i++){
            if (storagePath[i].compareTo(appStorageLocation)==0)
                break;
        }
        if (i==storagePath.length)
            setEditableAppStorageLocation(getSharedPreferences(SHARED_PREFERENCE,Context.MODE_PRIVATE),storagePath[0]);
        menu.getItem(2).setTitle("Storage Location" + "\n" + getEditableAppStorageLocation(
                getSharedPreferences(SHARED_PREFERENCE,Context.MODE_PRIVATE), getExternalFilesDirs(null)) + getString(R.string.app_name));
    }

    private void setDeviceNameTextView(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String deviceName = sharedPreferences.getString(DEVICE_NAME, getDefaultDeviceName());
        if(deviceName == null)
            deviceName = "Device";
        if (deviceName.length()>20)
            deviceName = deviceName.substring(0,20);
        setEditableDeviceName(getSharedPreferences(SHARED_PREFERENCE,Context.MODE_PRIVATE),deviceName);
        textView03.setText(deviceName);
        textView03.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_24dp, 0);
    }

}