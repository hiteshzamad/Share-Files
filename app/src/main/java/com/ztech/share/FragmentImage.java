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

public class FragmentImage extends Fragment {

    private AlertDialog alertDialog;
    private Sort sort;

    public FragmentImage() {
        // Required empty public constructor
    }

    private ActivitySelect activity;
    private RecyclerView recyclerView;
    private ObjectImage[] objectImage;
    private AdapterImageItem adapterImageItem;

    private final AdapterImageItem.OnClickListener onClickListener = new AdapterImageItem.OnClickListener() {
        @Override
        public void onCheck(int position, boolean isChecked) {
            objectImage[position].setChecked(isChecked);
            if (isChecked)
                activity.addToExtraStack(objectImage[position].getLocation());
            else
                activity.removeFromExtraStack(objectImage[position].getLocation());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        recyclerView = view.findViewById(R.id.image_recyclerView);
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
        if (objectImage == null || recyclerView == null || adapterImageItem == null)
            return;
        dialogRefresh();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectImage objectImage1: objectImage){
                    activity.addToExtraStack(objectImage1.getLocation());
                    objectImage1.setChecked(true);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterImageItem.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    void deselectAll(){
        if (objectImage == null || recyclerView == null || adapterImageItem == null)
            return;
        dialogRefresh();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectImage objectImage1: objectImage){
                    activity.removeFromExtraStack(objectImage1.getLocation());
                    objectImage1.setChecked(false);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterImageItem.notifyDataSetChanged();
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
                String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
                try {
                    cursor = activity.getApplicationContext().getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
                }catch (Exception e){
                    cursor = activity.getApplicationContext().getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
                }
                if (cursor != null) {
                    objectImage = new ObjectImage[cursor.getCount()];
                    int i = 0;
                    while (cursor.moveToNext()) {
                        int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        int nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        if (idColumn == -1 || nameColumn == -1)
                            return;
                        objectImage[i] = new ObjectImage(cursor.getString(nameColumn), false,cursor.getLong(idColumn));
                        if (activity.checkExtraSelected(cursor.getString(nameColumn)))
                            objectImage[i].setChecked(true);
                        i++;
                    }
                    cursor.close();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        adapterImageItem = new AdapterImageItem(objectImage, onClickListener, getContext());
                        recyclerView.setAdapter(adapterImageItem);
                    }
                });
            }
        });
        thread.start();
    }

    private String sorting(){
        switch(sort){
            case NAME_ASCENDING:
                return MediaStore.Images.Media.DISPLAY_NAME + " ASC";
            case NAME_DESCENDING:
                return MediaStore.Images.Media.DISPLAY_NAME + " DESC";
            case SIZE_ASCENDING:
                return MediaStore.Images.Media.SIZE + " ASC";
            case SIZE_DESCENDING:
                return MediaStore.Images.Media.SIZE + " DESC";
            case MODIFIED_ASCENDING:
                return MediaStore.Images.Media.DATE_MODIFIED + " ASC";
            case MODIFIED_DESCENDING:
                return MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        }
        return "";
    }
    
    void refresh(){
        if (objectImage==null || recyclerView==null || adapterImageItem==null) {
            backGroundThread();
            return;
        }
        for (ObjectImage image : objectImage) {
            if (image != null && activity.checkExtraSelected(image.getLocation()))
                image.setChecked(true);
            else if (image != null)
                image.setChecked(false);
        }
        if (!recyclerView.isComputingLayout())
            adapterImageItem.notifyDataSetChanged();

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


