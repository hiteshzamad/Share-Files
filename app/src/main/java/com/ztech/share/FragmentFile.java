package com.ztech.share;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ztech.share.ActivitySelect;
import com.ztech.share.AdapterFileItem;
import com.ztech.share.AdapterFilePointer;
import com.ztech.share.ObjectFile;
import com.ztech.share.R;
import com.ztech.share.Sort;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Stack;

public class FragmentFile extends Fragment implements AdapterFilePointer.OnClickListener, AdapterFileItem.OnClickListener {

    private Sort sort;
    private Fragment fragment;
    private AdapterFileItem adapterFileItem;
    private AdapterFilePointer adapterFilePointer;
    private ActivitySelect activity;
    private RecyclerView recyclerView,recyclerView02;
    private TextView textView01;
    private Stack<String> pathStack;
    private ObjectFile[] objectFiles;
    private String currentPath;
    private boolean isBusy;
    private Stack<Integer> lastLocationIntStack;
    private Integer lastLocationInt;

    private AlertDialog alertDialog;

    public FragmentFile() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file,container,false);
        recyclerView = view.findViewById(R.id.file_recyclerView01);
        recyclerView02 = view.findViewById(R.id.file_recyclerView02);
        textView01 = view.findViewById(R.id.file_textView01);
        recyclerView.setHasFixedSize(true);
        recyclerView02.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView02.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastLocationInt = 0;
        lastLocationIntStack = new Stack<>();
        sort = Sort.NAME_ASCENDING;
        pathStack = new Stack<>();
        activity = (ActivitySelect) getActivity();
        fragment = this;
        isBusy = false;
        pathStack.add("Root");
    }

    @Override
    public void onClickDestination(int position) {
        if (!isBusy) {
            isBusy = true;
            int len = pathStack.size();
            for (int i = 0; i < len - 1 - position; i++) {
                String[] path = currentPath.split("/");
                currentPath = currentPath.substring(0, currentPath.length() - path[path.length-1].length() - 1);
                pathStack.pop();
                lastLocationInt = lastLocationIntStack.pop();
            }
            if (position == 0) {
                lastLocationIntStack = new Stack<>();
                lastLocationIntStack.add(0);
                pathStack = new Stack<>();
                pathStack.add("ROOT");
            }
            backGroundThread();
        }
    }

    @Override
    public void onClick(int position) {
        if (!isBusy) {
            isBusy = true;
            if (pathStack.size() == 1) {
                pathStack.push(objectFiles[position].getFilename());
                if (recyclerView!=null && recyclerView.getLayoutManager()!=null)
                    lastLocationIntStack.push(((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
                currentPath = objectFiles[position].getSize();
                backGroundThread();
            } else {
                String fileName = objectFiles[position].getFilename();
                if (objectFiles[position].isDirectory()) {
                    if (!objectFiles[position].isChecked()) {
                        pathStack.push(fileName);
                        if (recyclerView!=null && recyclerView.getLayoutManager()!=null)
                            lastLocationIntStack.push(((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
                        currentPath = currentPath + fileName + "/";
                        backGroundThread();
                    } else {
                        Toast.makeText(getContext(), "Folder is selected", Toast.LENGTH_SHORT).show();
                        isBusy = false;
                    }
                }
                isBusy = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onCheck(int position, boolean isChecked) {
        objectFiles[position].setChecked(isChecked);
        if (isChecked) {
            if (!objectFiles[position].isDirectory()) {
                activity.addToFileStack(currentPath + objectFiles[position].getFilename());
            }
            else {
                activity.addToFolderStack(currentPath + objectFiles[position].getFilename());
            }
        }
        else {
            if (!objectFiles[position].isDirectory())
                activity.removeFromFileStack(currentPath + objectFiles[position].getFilename());
            else
                activity.removeFromFolderStack(currentPath + objectFiles[position].getFilename());
        }
    }

    boolean onBackPressed(){
        if (pathStack.size()>2 && !isBusy){
            isBusy = true;
            String[] path = currentPath.split("/");
            currentPath = currentPath.substring(0, currentPath.length() - path[path.length-1].length() - 1);
            pathStack.pop();
            lastLocationInt = lastLocationIntStack.pop();
            backGroundThread();
            return true;
        }
        else if (pathStack.size()==2 && !isBusy){
            isBusy = true;
            pathStack.pop();
            lastLocationInt = lastLocationIntStack.pop();
            backGroundThread();
            return true;
        }
        else return false;
    }

    private void backGroundThread(){
        dialogRefresh();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (pathStack.size()>1) {
                    setFileList();
                }
                else
                    setRoot();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (objectFiles == null) {
                            textView01.setText(R.string.empty_folder);
                            textView01.setVisibility(View.VISIBLE);
                        } else {
                            textView01.setVisibility(View.GONE);
                        }
                        adapterFileItem = new AdapterFileItem((AdapterFileItem.OnClickListener) fragment, objectFiles, getContext(), currentPath);
                        recyclerView.setAdapter(adapterFileItem);
                        if (lastLocationInt != null  && lastLocationInt != -1){
                            if (recyclerView.getLayoutManager()!=null)
                                recyclerView.getLayoutManager().scrollToPosition(lastLocationInt);
                        }
                        lastLocationInt = 0;
                        adapterFilePointer = new AdapterFilePointer(pathStack, (AdapterFilePointer.OnClickListener) fragment);
                        recyclerView02.setAdapter(adapterFilePointer);
                        if (recyclerView02.getLayoutManager()!=null)
                            recyclerView02.getLayoutManager().scrollToPosition(pathStack.size()-1);
                        isBusy = false;
                        if (alertDialog!=null)
                            alertDialog.cancel();
                    }
                });
            }
        });
        thread.start();
    }

    void refresh(){
        if (objectFiles==null || recyclerView==null || adapterFileItem==null) {
            backGroundThread();
            return;
        }
        for (ObjectFile file: objectFiles) {
            if (file != null &&
                    ( activity.checkFileSelected( currentPath + file.getFilename()) ||
                            activity.checkFolderSelected( currentPath + file.getFilename())  ))
                file.setChecked(true);
            else if (file != null)
                file.setChecked(false);
        }
        if (!recyclerView.isComputingLayout())
            adapterFileItem.notifyDataSetChanged();
    }

    void selectAll(){
        if (pathStack.size()<=1 || objectFiles==null || adapterFileItem ==null || recyclerView ==null)
            return;
        dialogRefresh();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectFile f: objectFiles){
                    File temp = new File(currentPath+f.getFilename()+"/");
                    if (!temp.isDirectory()){
                        activity.addToFileStack(currentPath+f.getFilename());
                    }else {
                        activity.addToFolderStack(currentPath+f.getFilename());
                    }
                    f.setChecked(true);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterFileItem.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    void deselectAll(){
        if (pathStack.size()<=1 || objectFiles==null || adapterFileItem ==null || recyclerView ==null)
            return;
        dialogRefresh();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ObjectFile f: objectFiles){
                    File temp = new File(currentPath+f.getFilename()+"/");
                    if (!temp.isDirectory()){
                        activity.removeFromFileStack(currentPath+f.getFilename());
                    }
                    else {
                        activity.removeFromFolderStack(currentPath+f.getFilename());
                    }
                    f.setChecked(false);
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog!=null)
                            alertDialog.cancel();
                        if (!recyclerView.isComputingLayout()) {
                            adapterFileItem.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
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

    private void setFileList() {
        File file = new File(currentPath);
        if (!file.exists())
            return;
        String[] strings = file.list();
        ObjectFile[] objectFiles = null;
        int k = 0;
        if (strings != null && strings.length > 0) {
            for (String s:strings){
                if (!(new File(currentPath+s)).isHidden())
                    k++;
            }
            objectFiles = new ObjectFile[k];
            sorting(strings);
        }
        if (k==0)
            objectFiles = null;
        int j = 0;
        for (int i = 0; objectFiles != null && i < strings.length ; i++) {
            if (!(new File(currentPath + strings[i])).isHidden()) {
                objectFiles[j] = new ObjectFile();
                File tempFile = new File(currentPath + strings[i]+"/");
                objectFiles[j].setFilename(getNameFromLocation(strings[i]));
                objectFiles[j].setDirectory(tempFile.isDirectory());
                objectFiles[j].setCheckable(true);
                objectFiles[j].setChecked(activity.checkFileSelected(currentPath + strings[i])||
                        activity.checkFolderSelected(currentPath + strings[i]));
                if (objectFiles[j].isDirectory()) {
                    objectFiles[j].setDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_folder_24dp));
                    objectFiles[j].setSize("");
                }
                else {
                    objectFiles[j].setDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_file));
                    objectFiles[j].setSize(getProperUnit(tempFile.length()));
                }
                j++;
            }
        }
        this.objectFiles = objectFiles;
    }

    private void sorting(String[] strings){
        switch (sort) {
            case NAME_ASCENDING:
                Arrays.sort(strings, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });
                break;
            case NAME_DESCENDING:
                Arrays.sort(strings, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2) * -1;
                    }
                });
                break;
            case MODIFIED_ASCENDING:
                Arrays.sort(strings, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        File file01 = new File(currentPath + o1);
                        File file02 = new File(currentPath + o2);
                        if (!file01.exists() || !file02.exists())
                            return o1.compareToIgnoreCase(o2);
                        if (file01.lastModified() > file02.lastModified())
                            return 1;
                        else
                            return -1;
                    }
                });
                break;
            case MODIFIED_DESCENDING:
                Arrays.sort(strings, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        File file01 = new File(currentPath + o1);
                        File file02 = new File(currentPath + o2);
                        if (!file01.exists() || !file02.exists())
                            return o1.compareToIgnoreCase(o2);
                        if (file01.lastModified() > file02.lastModified())
                            return -1;
                        else
                            return 1;
                    }
                });
                break;
            case SIZE_ASCENDING:
                Arrays.sort(strings, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        File file01 = new File(currentPath + o1);
                        File file02 = new File(currentPath + o2);
                        if (!file01.exists() || !file02.exists())
                            return o1.compareToIgnoreCase(o2);
                        if (file01.isDirectory() && file02.isDirectory())
                            return o1.compareToIgnoreCase(o2);
                        else if (file01.isDirectory())
                            return 1;
                        else if (file02.isDirectory())
                            return -1;
                        else if (file01.length() > file02.length())
                            return 1;
                        else
                            return -1;
                    }
                });
                break;
            case SIZE_DESCENDING:
                Arrays.sort(strings, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        File file01 = new File(currentPath + o1);
                        File file02 = new File(currentPath + o2);
                        if (!file01.exists() || !file02.exists())
                            return o1.compareToIgnoreCase(o2);
                        if (file01.isDirectory() && file02.isDirectory())
                            return o1.compareToIgnoreCase(o2);
                        else if (file01.isDirectory())
                            return -1;
                        else if (file02.isDirectory())
                            return 1;
                        else if (file01.length() > file02.length())
                            return -1;
                        else
                            return 1;
                    }
                });
                break;
        }
    }

    private void setRoot(){
        String[] root = getStoragePath();
        if (root.length > 0){
            objectFiles = new ObjectFile[root.length];
            for(int i = 0; i< objectFiles.length; i++){
                objectFiles[i] = new ObjectFile();
                objectFiles[i].setSize(root[i]);
                objectFiles[i].setDrawable(activity.getDrawable(R.drawable.ic_sd_storage_24dp));
                objectFiles[i].setFilename("External Storage" );
                objectFiles[i].setDirectory(true);
                objectFiles[i].setChecked(false);
                objectFiles[i].setCheckable(false);
            }
            objectFiles[0].setDrawable(activity.getDrawable(R.drawable.ic_storage_24dp));
            objectFiles[0].setFilename("Internal Storage");
        }
    }

    private String getProperUnit(long size){
        if (size<1000) {
            return size+" B";
        }else if (size<1000000) {
            Double d = size/1000.0;
            String t = String.format(Locale.ENGLISH,"%.2f", d);
            return t +" KB";
        }else if (size<1000000000) {
            Double d = size/1000000.0;
            String t = String.format(Locale.ENGLISH,"%.2f", d);
            return t +" MB";
        }else {
            Double d = size / 1000000000.0;
            String t = String.format(Locale.ENGLISH, "%.2f", d);
            return t + " GB";
        }
    }

    private String getNameFromLocation(String path){
        String[] temp = path.split("/");
        return temp[temp.length-1];
    }

    private String[] getStoragePath() {
        File[] files = activity.getExternalFilesDirs(null);
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
    }

    private String[] stackToStringArray(Stack<String> s){
        String[] strings = new String[s.size()];
        for (int i=0;i<strings.length;i++){
            strings[i] = s.get(i);
        }
        return strings;
    }

    public void startSorting(Sort sort) {
        this.sort = sort;
        backGroundThread();
    }
}
