package com.ztech.share;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ztech.share.ActivitySelect;
import com.ztech.share.AdapterAudioItem;
import com.ztech.share.ObjectAudio;
import com.ztech.share.R;
import com.ztech.share.Sort;

public class FragmentAudio extends Fragment {

    private AlertDialog alertDialog;
    private Sort sort;

    public FragmentAudio() {
        // Required empty public constructor
    }

    private ActivitySelect activity;
    private RecyclerView recyclerView;
    private ObjectAudio[] objectAudio;
    private AdapterAudioItem adapterAudioItem;

    private final AdapterAudioItem.OnClickListener onClickListener = new AdapterAudioItem.OnClickListener() {
        @Override
        public void onCheck(int position, boolean isChecked) {
            objectAudio[position].setChecked(isChecked);
            if (isChecked)
                activity.addToExtraStack(objectAudio[position].getLocation());
            else
                activity.removeFromExtraStack(objectAudio[position].getLocation());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio, container, false);
        recyclerView = view.findViewById(R.id.audio_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sort = Sort.MODIFIED_DESCENDING;
        activity = (ActivitySelect) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    void selectAll(){
        if (objectAudio == null || recyclerView == null || adapterAudioItem == null)
            return;
        dialogRefresh();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectAudio objectAudio1: objectAudio){
                    activity.addToExtraStack(objectAudio1.getLocation());
                    objectAudio1.setChecked(true);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterAudioItem.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    void deselectAll(){
        if (objectAudio == null || recyclerView == null || adapterAudioItem == null)
            return;
        dialogRefresh();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectAudio objectAudio1: objectAudio){
                    activity.removeFromExtraStack(objectAudio1.getLocation());
                    objectAudio1.setChecked(false);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterAudioItem.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    private void backGroundThread() {
        dialogRefresh();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String sortOrder = sorting();
                Cursor cursor;
                String[] projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA};
                try {
                    cursor = activity.getApplicationContext().getContentResolver().query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
                }catch (Exception e){
                    cursor = activity.getApplicationContext().getContentResolver().query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
                }
                if (cursor != null) {
                    objectAudio = new ObjectAudio[cursor.getCount()];
                    int i = 0;
                    while (cursor.moveToNext()) {
                        int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                        int nameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                        if (idColumn == -1 || nameColumn == -1)
                            return;
                        objectAudio[i] = new ObjectAudio(cursor.getString(nameColumn), false,cursor.getLong(idColumn));
                        if (activity.checkExtraSelected(cursor.getString(nameColumn)))
                            objectAudio[i].setChecked(true);
                        i++;
                    }
                    cursor.close();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        adapterAudioItem= new AdapterAudioItem(objectAudio, onClickListener, getContext());
                        recyclerView.setAdapter(adapterAudioItem);
                    }
                });
            }
        });
        thread.start();
    }

    private String sorting(){
        switch(sort){
            case NAME_ASCENDING:
                return MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
            case NAME_DESCENDING:
                return MediaStore.Audio.Media.DISPLAY_NAME + " DESC";
            case SIZE_ASCENDING:
                return MediaStore.Audio.Media.SIZE + " ASC";
            case SIZE_DESCENDING:
                return MediaStore.Audio.Media.SIZE + " DESC";
            case MODIFIED_ASCENDING:
                return MediaStore.Audio.Media.DATE_MODIFIED + " ASC";
            case MODIFIED_DESCENDING:
                return MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        }
        return "";
    }

    void refresh(){
        if (objectAudio==null || recyclerView==null || adapterAudioItem==null) {
            backGroundThread();
            return;
        }
        for (ObjectAudio objectAudio1 : objectAudio) {
            if (objectAudio1 != null && activity.checkExtraSelected(objectAudio1.getLocation()))
                objectAudio1.setChecked(true);
            else if (objectAudio1 != null)
                objectAudio1.setChecked(false);
        }
        if (!recyclerView.isComputingLayout())
            adapterAudioItem.notifyDataSetChanged();

    }

    public void startSorting(Sort sort) {
        this.sort = sort;
        backGroundThread();
    }

    private void dialogRefresh(){
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.dialog_refreshing, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setView(view);
        alertDialog = alertBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

}



