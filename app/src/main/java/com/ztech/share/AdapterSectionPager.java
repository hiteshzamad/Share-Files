package com.ztech.share;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdapterSectionPager extends FragmentStateAdapter {

    private FragmentFile fragmentFile;
    private FragmentApp fragmentApp;
    private FragmentImage fragmentImage;
    private FragmentVideo fragmentVideo;
    private FragmentAudio fragmentAudio;
    private static final int NUM_PAGES = 5;

    AdapterSectionPager(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public FragmentFile getFragmentFile() {
        return fragmentFile;
    }

    public FragmentApp getFragmentApp() {
        return fragmentApp;
    }

    public FragmentImage getFragmentImage() {
        return fragmentImage;
    }

    public FragmentVideo getFragmentVideo() {
        return fragmentVideo;
    }

    public FragmentAudio getFragmentAudio() {
        return fragmentAudio;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            fragmentFile = new FragmentFile();
            return fragmentFile;
        }
        else if (position == 1){
            fragmentApp = new FragmentApp();
            return fragmentApp;
        }
        else if (position == 2){
            fragmentImage = new FragmentImage();
            return fragmentImage;
        }
        else if (position == 3){
            fragmentVideo = new FragmentVideo();
            return fragmentVideo;
        }
        else {
            fragmentAudio = new FragmentAudio();
            return fragmentAudio;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}