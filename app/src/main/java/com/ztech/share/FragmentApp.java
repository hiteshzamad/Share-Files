package com.ztech.share;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ztech.share.ActivitySelect;
import com.ztech.share.AdapterAppItem;
import com.ztech.share.ObjectApp;
import com.ztech.share.R;
import com.ztech.share.Sort;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FragmentApp extends Fragment {

    private Sort sort;

    public FragmentApp() {
        // Required empty public constructor
    }

    private ActivitySelect activity;
    private RecyclerView recyclerView;
    private ObjectApp[] objectApp;
    private AdapterAppItem adapterAppItem;

    private AlertDialog alertDialog;

    private final AdapterAppItem.OnClickListener onClickListener = new AdapterAppItem.OnClickListener() {
        @Override
        public void onCheck(int position, boolean isChecked) {
            objectApp[position].setChecked(isChecked);
            if (isChecked)
                activity.addToAppStack(objectApp[position].getName(),objectApp[position].getLocation());
            else
                activity.removeFromAppStack(objectApp[position].getName(),objectApp[position].getLocation());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app, container, false);
        recyclerView = view.findViewById(R.id.app_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),5));
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sort = Sort.NAME_ASCENDING;
        activity = (ActivitySelect) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    void selectAll(){
        if (objectApp == null || recyclerView==null || adapterAppItem == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectApp objectApp1: objectApp){
                    activity.addToAppStack(objectApp1.getName(),objectApp1.getLocation());
                    objectApp1.setChecked(true);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterAppItem.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    void deselectAll(){
        if (objectApp == null || recyclerView==null || adapterAppItem == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectApp objectApp1: objectApp){
                    activity.removeFromAppStack(objectApp1.getName(),objectApp1.getLocation());
                    objectApp1.setChecked(false);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterAppItem.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    private void backGroundThread(){
        dialogRefresh();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                PackageManager packageManager = activity.getPackageManager();
                List<ApplicationInfo> packages =  packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                int k=0;
                for (ApplicationInfo applicationInfo:packages){
                    if (isNotSystemPackage(applicationInfo))
                        k++;
                }
                objectApp = new ObjectApp[k];
                k=0;
                for (ApplicationInfo applicationInfo:packages) {
                    if (isNotSystemPackage(applicationInfo)) {
                        objectApp[k] = new ObjectApp(packageManager.getApplicationLabel(applicationInfo).toString(),
                                applicationInfo.sourceDir,applicationInfo.loadIcon(packageManager));
                        if (activity.checkAppLocationSelected(objectApp[k].getLocation()))
                            objectApp[k].setChecked(true);
                        k++;
                    }
                }
                sorting(objectApp);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        adapterAppItem = new AdapterAppItem(objectApp, onClickListener);
                        recyclerView.setAdapter(adapterAppItem);
                    }
                });
            }
        });
        thread.start();
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

    private void sorting(ObjectApp[] objectApp){
        switch (sort) {
            case NAME_ASCENDING:
                Arrays.sort(objectApp, new Comparator<ObjectApp>() {
                    @Override
                    public int compare(ObjectApp o1, ObjectApp o2) {
                        return (o1.getName()).compareToIgnoreCase(o2.getName());
                    }
                });
                break;
            case NAME_DESCENDING:
                Arrays.sort(objectApp, new Comparator<ObjectApp>() {
                    @Override
                    public int compare(ObjectApp o1, ObjectApp o2) {
                        return (o1.getName()).compareToIgnoreCase(o2.getName()) * -1;
                    }
                });
                break;
            case MODIFIED_ASCENDING:
                Arrays.sort(objectApp, new Comparator<ObjectApp>() {
                    @Override
                    public int compare(ObjectApp o1, ObjectApp o2) {
                        File file01 = new File(o1.getLocation());
                        File file02 = new File(o2.getLocation());
                        if (!file01.exists() || !file02.exists())
                            return (o1.getName()).compareToIgnoreCase(o2.getName());
                        if (file01.lastModified() > file02.lastModified())
                            return  1;
                        else
                            return  -1;
                    }
                });
                break;
            case MODIFIED_DESCENDING:
                Arrays.sort(objectApp, new Comparator<ObjectApp>() {
                    @Override
                    public int compare(ObjectApp o1, ObjectApp o2) {
                        File file01 = new File(o1.getLocation());
                        File file02 = new File(o2.getLocation());
                        if (!file01.exists() || !file02.exists())
                            return (o1.getName()).compareToIgnoreCase(o2.getName());
                        if (file01.lastModified() > file02.lastModified())
                            return  -1;
                        else
                            return 1;
                    }
                });
                break;
            case SIZE_ASCENDING:
                Arrays.sort(objectApp, new Comparator<ObjectApp>() {
                    @Override
                    public int compare(ObjectApp o1, ObjectApp o2) {
                        File file01 = new File(o1.getLocation());
                        File file02 = new File(o2.getLocation());
                        if (!file01.exists() || !file02.exists())
                            return (o1.getName()).compareToIgnoreCase(o2.getName());
                        if (file01.isDirectory() && file02.isDirectory())
                            return (o1.getName()).compareToIgnoreCase(o2.getName());
                        else if (file01.isDirectory())
                            return  -1;
                        else if (file02.isDirectory())
                            return  1;
                        else if (file01.length() > file02.length())
                            return  1;
                        else
                            return -1;
                    }
                });
                break;
            case SIZE_DESCENDING:
                Arrays.sort(objectApp, new Comparator<ObjectApp>() {
                    @Override
                    public int compare(ObjectApp o1, ObjectApp o2) {
                        File file01 = new File(o1.getLocation());
                        File file02 = new File(o2.getLocation());
                        if (!file01.exists() || !file02.exists())
                            return (o1.getName()).compareToIgnoreCase(o2.getName());
                        if (file01.isDirectory() && file02.isDirectory())
                            return (o1.getName()).compareToIgnoreCase(o2.getName());
                        else if (file01.isDirectory())
                            return 1;
                        else if (file02.isDirectory())
                            return -1;
                        else if (file01.length() > file02.length())
                            return -1;
                        else
                            return 1;
                    }
                });
                break;
        }
    }

    private boolean isNotSystemPackage(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);
    }

    void refresh(){
        if (objectApp==null || recyclerView==null || adapterAppItem==null) {
            backGroundThread();
            return;
        }
        for (ObjectApp app : objectApp) {
            if (app != null && activity.checkAppSelected(app.getName()))
                app.setChecked(true);
            else if (app != null)
                app.setChecked(false);
        }
        if (!recyclerView.isComputingLayout())
            adapterAppItem.notifyDataSetChanged();
    }

    public void startSorting(Sort sort) {
        this.sort = sort;
        backGroundThread();
    }
}

