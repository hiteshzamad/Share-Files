package com.ztech.share;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigInteger;

public class FragmentSearch extends Fragment {
    private View view01,view02;
    private Button button01,button02;
    private ActivitySender activity;
    private ImageView imageView;
    private TextView textView,textView02;
    private RippleView rippleView;
    private RecyclerView recyclerView;
    private AdapterDeviceItem adapter;
    private ObjectDevice[] objectDevices;
    private View sliderView;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        sliderView = view.findViewById(R.id.search_slider_layout);
        recyclerView = view.findViewById(R.id.search_recyclerView01);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new AdapterDeviceItem(objectDevices,null);
        recyclerView.setAdapter(adapter);
        textView = view.findViewById(R.id.search_textView);
        textView02 = view.findViewById(R.id.search_textView02);
        imageView = view.findViewById(R.id.search_imageView);
        rippleView = view.findViewById(R.id.search_rippleView);
        setUpSetting(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (ActivitySender) getActivity();
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

    void setDeviceItems(ObjectDevice[] tempObjectDevices) {
        this.objectDevices = tempObjectDevices;
        adapter = new AdapterDeviceItem(objectDevices, new AdapterDeviceItem.OnClickListener() {
            @Override
            public void onClick(int position) {
                activity.connect(objectDevices[position].getAddress());
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        button01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null)
                    activity.configureWifi();
            }
        });
        button02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null)
                    activity.configureLocation();
            }
        });
    }

    void showLocationSetting(){
        view02.setVisibility(View.VISIBLE);
    }

    void showWifiSetting(){
        view01.setVisibility(View.VISIBLE);
    }

    void hideWifiSetting(){
        view01.setVisibility(View.GONE);
    }

    void hideLocationSetting(){
        view02.setVisibility(View.GONE);
    }

    void allSet(String deviceName){
        sliderView.setVisibility(View.VISIBLE);
        hideWifiSetting();
        hideLocationSetting();
        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        textView.setText(deviceName.substring(0,1));
        imageView.setColorFilter(Color.parseColor(getRandomHexString(deviceName)), PorterDuff.Mode.SRC_ATOP);
        rippleView.startAnimation(getRandomHexString(deviceName));
        textView02.setText(deviceName);
        textView02.setVisibility(View.VISIBLE);
    }

    void notSet(){
        sliderView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        textView02.setVisibility(View.GONE);
        rippleView.stopAnimation();
    }

    void setUpSetting(View view){
        view01 = view.findViewById(R.id.search_include01);
        view02 = view.findViewById(R.id.search_include02);
        view01.findViewById(R.id.item_setting_imageView01).
                setBackground(ContextCompat.getDrawable(activity,R.drawable.ic_baseline_wifi_24));
        view02.findViewById(R.id.item_setting_imageView01).
                setBackground(ContextCompat.getDrawable(activity,R.drawable.ic_baseline_my_location_24));
        String TURN_WIFI_ON = "Turn On Wifi";
        String TURN_LOCATION_ON = "Turn On Location (Wifi Mode)";
        ((TextView)view01.findViewById(R.id.item_setting_textView01)).
                setText(TURN_WIFI_ON);
        ((TextView)view02.findViewById(R.id.item_setting_textView01)).
                setText(TURN_LOCATION_ON);
        button01 = view01.findViewById(R.id.item_setting_button01);
        button02 = view02.findViewById(R.id.item_setting_button01);
    }
}

