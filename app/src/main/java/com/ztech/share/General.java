package com.ztech.share;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.Locale;
import java.util.Stack;

public class General {

    static final String DEVICE_NAME = "device_name";
    static final String APP_STORAGE_LOCATION = "app_storage_location";
    static final String SHARED_PREFERENCE = "shared_preference_android_share";

    static String[] stackToStringArray(Stack<String> stringStack){
        String[] strings = new String[stringStack.size()];
        for (int i=0;i<strings.length;i++){
            strings[i] = stringStack.get(i);
        }
        return strings;
    }

    static String convertSizeToUnit(long size){
        if (size<1000) {
            return size+" B";
        }else if (size<1000000L) {
            Double d = size/1000.0;
            String t = String.format(Locale.ENGLISH,"%.2f", d);
            return t +" KB";
        }else if (size<1000000000L) {
            Double d = size/1000000.0;
            String t = String.format(Locale.ENGLISH,"%.2f", d);
            return t +" MB";
        }else if (size<1000000000000L){
            Double d = size/1000000000.0;
            String t = String.format(Locale.ENGLISH,"%.2f", d);
            return t +" GB";
        }else {
            Double d = size/1000000000000.0;
            String t = String.format(Locale.ENGLISH,"%.2f", d);
            return t +" TB";
        }
    }

    static String getDefaultDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.contains(manufacturer))
            return (model);
        return manufacturer + " " + model;
    }

    static String getDefaultAppStorageLocation(File[] files){
        String[] storagePath = getStoragePath(files);
        if(storagePath == null)
            return "";
        return storagePath[0];
    }

    static String getEditableAppStorageLocation(SharedPreferences sharedPreferences, File[] files) {
        return sharedPreferences.getString(APP_STORAGE_LOCATION, getDefaultAppStorageLocation(files));
    }

    static void setEditableAppStorageLocation(SharedPreferences sharedPreferences, String appStorageLocation) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(APP_STORAGE_LOCATION,appStorageLocation);
        editor.apply();
    }

    static void setEditableDeviceName(SharedPreferences sharedPreferences, String deviceName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_NAME,deviceName);
        editor.apply();
    }

    static String getEditableDeviceName(SharedPreferences sharedPreferences){
        return sharedPreferences.getString(DEVICE_NAME, getDefaultDeviceName());
    }

    static String[] getStoragePath(File[] files) {
        try {
            Stack<String> stringStack = new Stack<>();
            for (File file : files) {
                String state = Environment.getExternalStorageState(file);
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    String[] temp = file.getPath().split("/");
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 0; j < temp.length - 4; j++)
                        stringBuilder.append(temp[j]).append("/");
                    stringStack.push(stringBuilder.toString());
                }
            }
            return stackToStringArray(stringStack);
        }catch (Exception e){
            return null;
        }
    }

}
