package com.ztech.share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class AdapterSectionPagerFTPHelp extends FragmentStateAdapter {

    private static final int NUM_PAGES = 4;

    private ViewPager2 viewPager2;
    private AlertDialog alertDialog;

    public AdapterSectionPagerFTPHelp(@NonNull FragmentActivity fragmentActivity, ViewPager2 viewPager2, AlertDialog alertDialog) {
        super(fragmentActivity);
        this.viewPager2 = viewPager2;
        this.alertDialog = alertDialog;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new FragmentFTPHelp(R.layout.fragment_ftp_help01,viewPager2,alertDialog);
            case 1:
                return new FragmentFTPHelp(R.layout.fragment_ftp_help02,viewPager2,alertDialog);
            case 2:
                return new FragmentFTPHelp(R.layout.fragment_ftp_help03,viewPager2,alertDialog);
            case 3:
                return new FragmentFTPHelp(R.layout.fragment_ftp_help04,viewPager2,alertDialog);
        }
        return new FragmentFTPHelp(R.layout.fragment_ftp_help01,viewPager2,alertDialog);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
