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

import com.ztech.share.ActivityReceiver;
import com.ztech.share.R;
import com.ztech.share.RippleView;

import java.math.BigInteger;

public class FragmentDiscover extends Fragment {

    private View view01,view02;
    private Button button01,button02;
    private ActivityReceiver activity;
    private ImageView imageView;
    private TextView textView,textView02;
    private RippleView rippleView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        textView = view.findViewById(R.id.discover_textView_rippleText);
        textView02 = view.findViewById(R.id.discover_textView_deviceName);
        imageView = view.findViewById(R.id.discover_imageView);
        rippleView = view.findViewById(R.id.discover_rippleView);
        bindSettingView(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (ActivityReceiver) getActivity();
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

    private String getRandomHexColorString(@NonNull String s) {
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

    public void showLocationSetting(){
        view02.setVisibility(View.VISIBLE);
    }

    void showWifiSetting(){
        view01.setVisibility(View.VISIBLE);
    }

    void hideWifiSetting(){
        view01.setVisibility(View.GONE);
    }

    public void hideLocationSetting(){
        view02.setVisibility(View.GONE);
    }

    void makeRippleVisible(@NonNull String deviceName){
        hideWifiSetting();
        hideLocationSetting();
        textView.setText(String.valueOf(Character.toUpperCase(deviceName.charAt(0))));
        textView02.setText(deviceName);
        imageView.setColorFilter(Color.parseColor(getRandomHexColorString(deviceName)), PorterDuff.Mode.SRC_ATOP);
        rippleView.startAnimation(getRandomHexColorString(deviceName));
        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        textView02.setVisibility(View.VISIBLE);
    }

    void makeRippleInvisible(){
        rippleView.stopAnimation();
        imageView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        textView02.setVisibility(View.GONE);
    }

    void bindSettingView(View view){
        String TURN_WIFI_ON = "Turn On Wifi";
        String TURN_LOCATION_ON = "Turn On Location (Wifi Mode)";view01 = view.findViewById(R.id.discover_include01);
        view02 = view.findViewById(R.id.discover_include02);
        view01.findViewById(R.id.item_setting_imageView01).setBackground(ContextCompat.getDrawable(activity,R.drawable.ic_baseline_wifi_24));
        view02.findViewById(R.id.item_setting_imageView01).setBackground(ContextCompat.getDrawable(activity,R.drawable.ic_baseline_my_location_24));
        ((TextView)view01.findViewById(R.id.item_setting_textView01)).setText(TURN_WIFI_ON);
        ((TextView)view02.findViewById(R.id.item_setting_textView01)).setText(TURN_LOCATION_ON);
        button01 = view01.findViewById(R.id.item_setting_button01);
        button02 = view02.findViewById(R.id.item_setting_button01);
    }
}
