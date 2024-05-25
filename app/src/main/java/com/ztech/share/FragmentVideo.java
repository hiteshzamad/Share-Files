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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentVideo extends Fragment {

    private AlertDialog alertDialog;
    private Sort sort;

    public FragmentVideo() {
        // Required empty public constructor
    }

    private ActivitySelect activity;
    private RecyclerView recyclerView;
    private ObjectVideo[] objectVideo;
    private AdapterVideoItem adapterVideoItem;

    private final AdapterVideoItem.OnClickListener onClickListener = new AdapterVideoItem.OnClickListener() {
        @Override
        public void onCheck(int position, boolean isChecked) {
            objectVideo[position].setChecked(isChecked);
            if (isChecked)
                activity.addToExtraStack(objectVideo[position].getLocation());
            else
                activity.removeFromExtraStack(objectVideo[position].getLocation());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        recyclerView = view.findViewById(R.id.video_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),4));
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
        if (objectVideo == null || recyclerView == null || adapterVideoItem == null)
            return;
        dialogRefresh();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectVideo objectVideo1: objectVideo){
                    activity.addToExtraStack(objectVideo1.getLocation());
                    objectVideo1.setChecked(true);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterVideoItem.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    void deselectAll(){
        if (objectVideo == null || recyclerView == null || adapterVideoItem == null)
            return;
        dialogRefresh();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectVideo objectVideo1: objectVideo){
                    activity.removeFromExtraStack(objectVideo1.getLocation());
                    objectVideo1.setChecked(false);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterVideoItem.notifyDataSetChanged();
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
                String[] projection = new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};
                try {
                    cursor = activity.getApplicationContext().getContentResolver().query(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
                }catch (Exception e){
                    cursor = activity.getApplicationContext().getContentResolver().query(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
                }
                if (cursor != null) {
                    objectVideo = new ObjectVideo[cursor.getCount()];
                    int i = 0;
                    while (cursor.moveToNext()) {
                        int idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                        int nameColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                        if (idColumn == -1 || nameColumn == -1)
                            return;
                        objectVideo[i] = new ObjectVideo(cursor.getString(nameColumn), false,cursor.getLong(idColumn));
                        if (activity.checkExtraSelected(cursor.getString(nameColumn)))
                            objectVideo[i].setChecked(true);
                        i++;
                    }
                    cursor.close();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        adapterVideoItem = new AdapterVideoItem(objectVideo, onClickListener, getContext());
                        recyclerView.setAdapter(adapterVideoItem);
                    }
                });
            }
        });
        thread.start();
    }

    private String sorting(){
        switch(sort){
            case NAME_ASCENDING:
                return MediaStore.Video.Media.DISPLAY_NAME + " ASC";
            case NAME_DESCENDING:
                return MediaStore.Video.Media.DISPLAY_NAME + " DESC";
            case SIZE_ASCENDING:
                return MediaStore.Video.Media.SIZE + " ASC";
            case SIZE_DESCENDING:
                return MediaStore.Video.Media.SIZE + " DESC";
            case MODIFIED_ASCENDING:
                return MediaStore.Video.Media.DATE_MODIFIED + " ASC";
            case MODIFIED_DESCENDING:
                return MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        }
        return "";
    }


    void refresh(){
        if (objectVideo ==null || recyclerView==null || adapterVideoItem==null) {
            backGroundThread();
            return;
        }
        for (ObjectVideo objectVideo1 : objectVideo) {
            if (objectVideo1 != null && activity.checkExtraSelected(objectVideo1.getLocation()))
                objectVideo1.setChecked(true);
            else if (objectVideo1 != null)
                objectVideo1.setChecked(false);
        }
        if (!recyclerView.isComputingLayout())
            adapterVideoItem.notifyDataSetChanged();

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



