package com.ztech.share;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.ztech.share.R;

public class FragmentFTPHelp extends Fragment {

    private int view;
    private ViewPager2 viewPager2;
    private AlertDialog alertDialog;
    private Button button;

    public FragmentFTPHelp(int view, ViewPager2 viewPager2, AlertDialog alertDialog) {
        this.view = view;
        this.viewPager2 = viewPager2;
        this.alertDialog = alertDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view1 =  inflater.inflate(view,container,false);
        button = view1.findViewById(R.id.fragment_ftp_help_button);
        return view1;
    }

    @Override
    public void onResume() {
        super.onResume();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (view){
                    case R.layout.fragment_ftp_help01:
                        viewPager2.setCurrentItem(1,true);
                        break;
                    case R.layout.fragment_ftp_help02:
                        viewPager2.setCurrentItem(2,true);
                        break;
                    case R.layout.fragment_ftp_help03:
                        viewPager2.setCurrentItem(3,true);
                        break;
                    case R.layout.fragment_ftp_help04:
                        alertDialog.cancel();
                        break;
                }
            }
        });
    }
}
