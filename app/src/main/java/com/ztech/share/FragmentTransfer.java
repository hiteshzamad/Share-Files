package com.ztech.share;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Stack;

public class FragmentTransfer extends Fragment implements ListenerDataTransfer, ListenerProgress {

    private static final String FILE_TRANSFERRED = "file_transferred";
    private static final String DATA_TRANSFERRED = "data_transferred";
    private static final String SHARED_PREFERENCE = "shared_preference_android_share";

    public FragmentTransfer() {
        // Required empty public constructor
    }

    private boolean completed;
    private boolean isSender;
    private boolean isServer;
    private boolean started;
    private int port;
    private int currentFile;
    private int currentTransferItem;
    private long size;
    private long folderSizeDone;
    private long lastFileSize;
    private long lastFile;
    private long lastTime;
    private long timeStarted;
    private long lastSizeDone;

    private String currentFolderBase;
    private String ipAddress;
    private String[] extras;
    private String[] files;
    private String[] folders;
    private String[] appName;
    private String[] appLocation;
    private Stack<String> folderStack;
    private Stack<Long> folderFileSizeStack;

    private Activity activity;
    private RecyclerView recyclerView;
    private ProgressBar progressBar01;
    private TextView textView01, textView02, textView03, textView04;
    private DataTransferTask dataTransferTask;
    private ObjectTransfer[] objectTransfers;
    private AdapterTransferItem adapter;

    private String getAppFolder(){
        File file = activity.getExternalFilesDir(null);
        String basePath = "/storage/emulated/0/";
        if (file != null) {
            String[] temp = file.getPath().split("/");
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < temp.length - 4; j++)
                stringBuilder.append(temp[j]).append("/");
            basePath = stringBuilder.toString();
        }
        return basePath + getString(R.string.app_name) + "/";
    }

    private AdapterTransferItem.OnClickListener onClickListener = new AdapterTransferItem.OnClickListener() {
        @Override
        public void onClick(int position) {
            File file = new File(getAppFolder()+ objectTransfers[position].getRealName());
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String type = "application/vnd.android.package-archive";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri downloadedApk = FileProvider.getUriForFile(activity, "com.ztech.share.fileProvider", file);
                    intent.setDataAndType(downloadedApk, type);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    intent.setDataAndType(Uri.fromFile(file), type);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView = view.findViewById(R.id.transfer_recyclerview01);
        progressBar01 = view.findViewById(R.id.transfer_progressBar01);
        textView01 = view.findViewById(R.id.transfer_textView01);
        textView02 = view.findViewById(R.id.transfer_textView02);
        textView03 = view.findViewById(R.id.transfer_textView03);
        textView04 = view.findViewById(R.id.transfer_textView04);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
        makeAppFolders();
        if (getArguments() != null) {
            isServer = (boolean) getArguments().get("isServer");
            isSender = (boolean) getArguments().get("isSender");
            ipAddress = (String) getArguments().get("ipAddress");
            extras = (String[]) getArguments().get("Extras");
            files = (String[]) getArguments().get("Files");
            folders = (String[]) getArguments().get("Folders");
            appName = (String[]) getArguments().get("Apps");
            appLocation = (String[]) getArguments().get("AppLocation");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!started) {
            started = true;
            if (isSender)
                senderTask();
            else
                receiverTask();
        }
    }

    @Override
    public void onDestroy() {
        if (activity instanceof ActivitySender)
            if (dataTransferTask!=null)
                dataTransferTask.cancel(true);
        if (activity instanceof ActivityReceiver)
            if (dataTransferTask!=null)
                dataTransferTask.cancel(true);
        super.onDestroy();
    }

    private void senderTask(){
        setTransferItems();
        dataTransferTask = new DataTransferTask(isServer,isSender,true,ipAddress, port,this,this);
        dataTransferTask.execute(transferItemObjectToString());
    }

    private void receiverTask(){
        dataTransferTask = new DataTransferTask(isServer,isSender,true,ipAddress, port,this,this);
        dataTransferTask.execute("");
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
            Double d = size/1000000000.0;
            String t = String.format(Locale.ENGLISH,"%.2f", d);
            return t +" GB";
        }
    }

    private long getFileSize(String path){
        File file = new File(path);
        if (file.exists())
            return file.length();
        return 0;
    }

    private String getNameFromPath(String path){
        String[] temp = path.split("/");
        return temp[temp.length-1];
    }

    private void getFolderFiles(String path) {
        File tempFile = new File(path);
        String[] files = tempFile.list();
        if (files!=null) {
            for (String s : files) {
                File file = new File(path+s);
                if (!file.isHidden() && file.isDirectory()){
                    getFolderFiles(path+s+"/");
                }
                else if (!file.isHidden()){
                    size += file.length();
                    folderStack.add(path+s);
                    folderFileSizeStack.add(file.length());
                }
            }
        }
    }

    private String[] getFolderFilesArray(String folderPath){
        size = 0;
        getFolderFiles(folderPath+"/");
        String[] strings = null;
        if (folderStack!=null&&folderStack.size()!=0){
            strings = new String[folderStack.size()];
            for (int i=0;i<folderStack.size();i++){
                strings[i] = folderStack.elementAt(i);
            }
            folderStack = new Stack<>();
        }
        return strings;
    }

    private long[] getFolderFilesSizeArray(){
        long[] sizes = null;
        if (folderFileSizeStack!=null&&folderFileSizeStack.size()!=0){
            sizes = new long[folderFileSizeStack.size()];
            for (int i=0;i<folderFileSizeStack.size();i++){
                sizes[i] = folderFileSizeStack.elementAt(i);
            }
            folderFileSizeStack = new Stack<>();
        }
        return sizes;
    }

    private void setTransferItems() {
        int length = 0;
        if (extras!=null) length += extras.length;
        if (appName!=null) length += appName.length;
        if (files!=null) length += files.length;
        if (folders!=null) length += folders.length;
        if (length!=0) objectTransfers = new ObjectTransfer[length];
        int counter = 0;
        for (int i=0; appName!=null && i<appName.length; i++){
            objectTransfers[counter] = new ObjectTransfer();
            objectTransfers[counter].setFileName(appName[i] + ".apk");
            objectTransfers[counter].setFolder(false);
            objectTransfers[counter].setPercent(0);
            objectTransfers[counter].setLocation(appLocation[i]);
            objectTransfers[counter].setSize(getFileSize(appLocation[i]));
            objectTransfers[counter].setStatus(0 +" / "+getProperUnit(getFileSize(appLocation[i])));
            counter++;
        }
        for (int i=0; files!=null && i<files.length ; i++){
            objectTransfers[counter] = new ObjectTransfer();
            objectTransfers[counter].setFileName(getNameFromPath(files[i]));
            objectTransfers[counter].setFolder(false);
            objectTransfers[counter].setPercent(0);
            objectTransfers[counter].setLocation(files[i]);
            objectTransfers[counter].setSize(getFileSize(files[i]));
            objectTransfers[counter].setStatus(0 +" / "+getProperUnit(getFileSize(files[i])));
            counter++;
        }
        for (int i=0; folders!=null && i<folders.length; i++){
            objectTransfers[counter] = new ObjectTransfer();
            objectTransfers[counter].setFileName(getNameFromPath(folders[i]));
            objectTransfers[counter].setFolder(true);
            objectTransfers[counter].setPercent(0);
            objectTransfers[counter].setLocation(folders[i]);
            objectTransfers[counter].setFiles(getFolderFilesArray(folders[i]));
            objectTransfers[counter].setFilesSize(getFolderFilesSizeArray());
            objectTransfers[counter].setSize(size);
            objectTransfers[counter].setStatus(0 +" / "+getProperUnit(objectTransfers[counter].getSize()));
            counter++;
        }
        for (int i=0; extras!=null && i<extras.length; i++){
            objectTransfers[counter] = new ObjectTransfer();
            objectTransfers[counter].setFileName(getNameFromPath(extras[i]));
            objectTransfers[counter].setFolder(false);
            objectTransfers[counter].setPercent(0);
            objectTransfers[counter].setLocation(extras[i]);
            objectTransfers[counter].setSize(getFileSize(extras[i]));
            objectTransfers[counter].setStatus(0 +" / "+getProperUnit(getFileSize(extras[i])));
            counter++;
        }
        adapter = new AdapterTransferItem(objectTransfers,onClickListener,!isSender,false);
        recyclerView.setAdapter(adapter);
    }

    private String transferItemObjectToString(){
        String BREAKER = "1!@F-/.0";
        size = 0;
        StringBuilder stringBuilder = new StringBuilder();
        if (objectTransfers !=null){
            for (ObjectTransfer objectTransfer : objectTransfers){
                size += objectTransfer.getSize();
                stringBuilder.append(objectTransfer.toString());
                stringBuilder.append(BREAKER);
            }
            stringBuilder.append(size);
        }
        return stringBuilder.toString();
    }

    private void stringToTransferItemObjectArray(String data){
        String BREAKER = "1!@F-/.0";
        String[] strings = data.split(BREAKER);
        objectTransfers = new ObjectTransfer[strings.length-1];
        for (int i = 0; i< objectTransfers.length; i++){
            objectTransfers[i] = stringToTransferItem(strings[i]);
        }
        size = Long.parseLong(strings[strings.length-1]);
        adapter = new AdapterTransferItem(objectTransfers,onClickListener,!isSender,false);
        recyclerView.setAdapter(adapter);
    }

    private ObjectTransfer stringToTransferItem(String data){
        String BREAKER = "-@.1&.#!";
        String BREAKER2 = "-@.6&9#!";

        ObjectTransfer objectTransfer = new ObjectTransfer();
        String[] strings = data.split(BREAKER);
        objectTransfer.setFileName(strings[0]);
        objectTransfer.setLocation(strings[1]);
        objectTransfer.setStatus(strings[2]);
        objectTransfer.setSize(Long.parseLong(strings[3]));
        objectTransfer.setFolder(Boolean.parseBoolean(strings[4]));
        int fileSize = Integer.parseInt(strings[5]);
        String[] files = null;
        long[] longFilesSize = null;
        if (fileSize>0){
            files = strings[6].split(BREAKER2);
            String[] FilesSizeArray = strings[7].split(BREAKER2);
            longFilesSize = new long[fileSize];
            for (int i=0;i<fileSize;i++){
                longFilesSize[i] = Long.parseLong(FilesSizeArray[i]);
            }
        }
        objectTransfer.setFiles(files);
        objectTransfer.setFilesSize(longFilesSize);
        return objectTransfer;
    }

    private void initialize(){
        size = 0;
        completed = false;
        timeStarted = System.currentTimeMillis();
        activity = getActivity();
        currentFile = 0;
        currentTransferItem = 0;
        port = 9656;
        started = false;
        folderStack = new Stack<>();
        folderFileSizeStack = new Stack<>();
    }

    private void sendFile(String path){
        dataTransferTask = new DataTransferTask(isServer,true,false,ipAddress,port,this,this);
        dataTransferTask.execute(path);
    }

    private void receiveFile(String path){
        dataTransferTask = new DataTransferTask(isServer,false,false,ipAddress,port,this,this);
        dataTransferTask.execute(path);
    }

    private boolean checkIfPathExists(String string){
        File file = new File(getAppFolder() + getFileAppFolder( objectTransfers[currentTransferItem].getFileName()) + string);
        return file.exists();
    }

    private boolean checkIfFolderExists(String string){
        File file = new File(getAppFolder()+ "Folder/" + string);
        return file.exists();
    }

    private String changeFolderName(String string){
        int i=1;
        while (checkIfFolderExists( string+"("+i+")")) {
            i++;
        }
        return string+"("+i+")";
    }


    private void makeAppFolders(){
        String[] folders = {"Folder","File","Image","Video","Music","Document","App"};
        for (String folder : folders) {
            File file = new File(getAppFolder() + folder);
            if (!file.exists()) {
                if (!file.mkdirs())
                    Toast.makeText(activity, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makeDirectory(String string){
        File myDirectory = new File(getAppFolder() +"Folder/"+ string);
        if(!myDirectory.exists()) {
            if (!myDirectory.mkdirs())
                Toast.makeText(activity,"Something Went Wrong",Toast.LENGTH_SHORT).show();
        }
    }

    private String changeFileName(String string){
        int i;
        int t = string.lastIndexOf(".");
        if (t==-1){
            i=1;
            while (checkIfPathExists(string+"("+i+")")) {
                i++;
            }
            return string + "(" + i + ")";
        }else {
            String temp = string.substring(0,t);
            String temp2 = string.substring(t);
            i=1;
            while(checkIfPathExists(temp+"("+i+")"+temp2)){
                i++;
            }
            return temp+"("+i+")"+temp2;
        }
    }

    private int getPercent(long done, long total){
        if (total == 0)
            return 100;
        return (int) (100 * done/total);
    }

    private void setUi(final long finalSizeTotalDone){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String time = "Time "+(System.currentTimeMillis()-timeStarted)/1000 + "s";
                String data = "Data "+getProperUnit(finalSizeTotalDone)+" / "+getProperUnit(size);
                if (System.currentTimeMillis()-lastTime>1000) {
                    Double d = ((double) (finalSizeTotalDone - lastSizeDone) / ((double) 1024 * (System.currentTimeMillis() - lastTime)));
                    lastTime = System.currentTimeMillis();
                    lastSizeDone = finalSizeTotalDone;
                    String speed = "Speed " + String.format(Locale.ENGLISH,"%.2f", d) + "MB/s";
                    textView01.setText(speed);
                }
                int percentFinal = (int) ((100 *finalSizeTotalDone)/ size);
                textView02.setText(data);
                textView03.setText(time);
                textView04.setText(String.valueOf(percentFinal));
                progressBar01.setProgress(percentFinal);
                adapter.notifyDataSetChanged();
            }
        });
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


    @Override
    public void onProgressUpdateComplete(Response response, String data) {
        if (response == Response.DATA_RECEIVED_SUCCESSFUL) {
            try {
                stringToTransferItemObjectArray(data);
            }catch (Exception e){
                activity.finish();
                Toast.makeText(activity, "Error While Parsing Data", Toast.LENGTH_SHORT).show();
            }
            String[] strings = getStoragePath();
            StatFs stat = new StatFs(strings[0]);
            long storageAvailable =(stat.getAvailableBlocksLong()) * stat.getBlockSizeLong();
            if (size > 0 && size >= storageAvailable && !isSender){
                Toast.makeText(activity,"Not Enough Storage",Toast.LENGTH_LONG).show();
                return;
            }
            currentFile = 0;
            currentTransferItem = 0;
            if (stopTransfer())
                return;
            if (currentTransferItem >= objectTransfers.length) {
                onComplete();
            }
            else if (objectTransfers[currentTransferItem].isFile()){
                String temp = objectTransfers[currentTransferItem].getFileName();
                if (checkIfPathExists(objectTransfers[currentTransferItem].getFileName())) {
                    temp = changeFileName(objectTransfers[currentTransferItem].getFileName());
                }
                objectTransfers[currentTransferItem].setRealName(temp);
                receiveFile(getAppFolder()+ getFileAppFolder( objectTransfers[currentTransferItem].getFileName()) + temp);
            }
            else {
                String temp = objectTransfers[currentTransferItem].getFileName();
                if (currentFile == 0) {
                    if (checkIfFolderExists(temp)) {
                        currentFolderBase = changeFolderName(objectTransfers[currentTransferItem].getFileName());
                    }
                    else {
                        currentFolderBase = objectTransfers[currentTransferItem].getFileName();
                    }
                }
                int w = objectTransfers[currentTransferItem].getLocation().length();
                int x = objectTransfers[currentTransferItem].getFiles()[currentFile].lastIndexOf('/') + 1;
                String y = objectTransfers[currentTransferItem].getFiles()[currentFile].substring(w, x);
                makeDirectory("/" + currentFolderBase + y);
                receiveFile(getAppFolder() +"Folder/" + currentFolderBase +
                        objectTransfers[currentTransferItem].getFiles()[currentFile].substring(w));
            }
        }
        else if (response == Response.FILE_RECEIVED_SUCCESSFUL) {
            try{
                MediaScannerConnection.scanFile(getContext(),
                        new String[] { data }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
            }catch (Exception ignored){

            }
            currentFile++;
            if (stopTransfer())
                return;
            if (currentTransferItem >= objectTransfers.length) {
                onComplete();
            }
            else if (objectTransfers[currentTransferItem].isFile()){
                String temp = objectTransfers[currentTransferItem].getFileName();
                if (checkIfPathExists(objectTransfers[currentTransferItem].getFileName())) {
                    temp = changeFileName(objectTransfers[currentTransferItem].getFileName());
                }
                objectTransfers[currentTransferItem].setRealName(temp);
                receiveFile(getAppFolder() + getFileAppFolder( objectTransfers[currentTransferItem].getFileName())  + temp);
            }
            else {
                String temp = objectTransfers[currentTransferItem].getFileName();
                if (currentFile == 0) {
                    if (checkIfFolderExists(temp)) {
                        currentFolderBase = changeFolderName(objectTransfers[currentTransferItem].getFileName());
                    }
                    else {
                        currentFolderBase = objectTransfers[currentTransferItem].getFileName();
                    }
                }
                int w = objectTransfers[currentTransferItem].getLocation().length();
                int x = objectTransfers[currentTransferItem].getFiles()[currentFile].lastIndexOf('/') + 1;
                String y = objectTransfers[currentTransferItem].getFiles()[currentFile].substring(w, x);
                makeDirectory("/"+ currentFolderBase + y);
                receiveFile(getAppFolder()  + "Folder/" + currentFolderBase +
                        objectTransfers[currentTransferItem].getFiles()[currentFile].substring(w));
            }
        }

        else if (response == Response.DATA_SEND_SUCCESSFUL) {
            if (stopTransfer())
                return;
            if (currentTransferItem >= objectTransfers.length) {
                onComplete();
            }
            else if (objectTransfers[currentTransferItem].isFile()) {
                sendFile(objectTransfers[currentTransferItem].getLocation());
            }
            else if (currentTransferItem < objectTransfers.length){
                sendFile(objectTransfers[currentTransferItem].getFiles()[currentFile]);
            }
        }

        else if (response == Response.FILE_SEND_SUCCESSFUL) {
            currentFile++;
            if (stopTransfer())
                return;
            if (currentTransferItem >= objectTransfers.length) {
                onComplete();
            }
            else if (objectTransfers[currentTransferItem].isFile()) {
                sendFile(objectTransfers[currentTransferItem].getLocation());
            }
            else if (currentTransferItem < objectTransfers.length){
                sendFile(objectTransfers[currentTransferItem].getFiles()[currentFile]);
            }
        }
    }

    private static boolean isApkFile(String path) {
        try{
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("application/vnd.android.package-archive");
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isAudioFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("audio");
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isPdfFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            if(mimeType != null)
                return  mimeType.startsWith("application/pdf") ;
        }catch (Exception ignored){
        }
        return false;
    }

    private static boolean isImageFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("image");
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isTextFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("text");
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isVideoFile(String path) {
        try{
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("video");
        }catch (Exception e){
            return false;
        }
    }

    private String getFileAppFolder(String filename){
        if (filename != null) {
            if (isImageFile(filename))  return "Image/";
            if (isVideoFile(filename))  return "Video/";
            if (isAudioFile(filename))  return "Music/";
            if (isApkFile(filename))  return "App/";
            if (isTextFile(filename))  return "Document/";
            if (isPdfFile(filename))  return "Document/";
        }
        return "File/";
    }

    private boolean stopTransfer(){
        while (true){
            if (currentTransferItem < objectTransfers.length){
                if (objectTransfers[currentTransferItem].getPercent() == 100){
                    objectTransfers[currentTransferItem].setPercent(100);
                    long size = objectTransfers[currentTransferItem].getSize();
                    objectTransfers[currentTransferItem].setStatus(getProperUnit(size)+" / "+getProperUnit(size));
                    currentTransferItem++;
                    currentFile = 0;
                }
                else if (objectTransfers[currentTransferItem].getSize() == 0) {
                    objectTransfers[currentTransferItem].setPercent(100);
                    long size = objectTransfers[currentTransferItem].getSize();
                    objectTransfers[currentTransferItem].setStatus(getProperUnit(size)+" / "+getProperUnit(size));
                    currentTransferItem++;
                    currentFile = 0;
                }
                else if (!objectTransfers[currentTransferItem].isFile() &&
                        objectTransfers[currentTransferItem].getFiles()==null){
                    objectTransfers[currentTransferItem].setPercent(100);
                    long size = objectTransfers[currentTransferItem].getSize();
                    objectTransfers[currentTransferItem].setStatus(getProperUnit(size)+" / "+getProperUnit(size));
                    currentTransferItem++;
                    currentFile = 0;
                }
                else if (!objectTransfers[currentTransferItem].isFile() &&
                        currentFile >= objectTransfers[currentTransferItem].getFiles().length){
                    long size = objectTransfers[currentTransferItem].getSize();
                    objectTransfers[currentTransferItem].setStatus(getProperUnit(size)+" / "+getProperUnit(size));
                    objectTransfers[currentTransferItem].setPercent(100);
                    currentTransferItem++;
                    currentFile = 0;
                }
                else if (!objectTransfers[currentTransferItem].isFile() &&
                        objectTransfers[currentTransferItem].getFilesSize()[currentFile] == 0){
                    currentFile++;
                }
                else{
                    return false;
                }
                adapter.notifyDataSetChanged();
            }
            else {
                onComplete();
                return true;
            }
        }
    }

    @Override
    public void updateSendProgress(final long size) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentFile ==0){
                    folderSizeDone = 0;
                }
                if (currentFile != lastFile) {
                    lastFile = currentFile;
                    folderSizeDone += lastFileSize;
                }
                lastFileSize = size;
                long totalSize = objectTransfers[currentTransferItem].getSize();
                objectTransfers[currentTransferItem].setPercent(getPercent(lastFileSize + folderSizeDone,totalSize));
                objectTransfers[currentTransferItem].setStatus(getProperUnit(lastFileSize + folderSizeDone)+" / "+getProperUnit(totalSize));
                adapter.notifyDataSetChanged();
                long sizeDone = 0;
                for (int i=0;i<currentTransferItem;i++){
                    sizeDone += objectTransfers[i].getSize();
                }
                sizeDone += lastFileSize + folderSizeDone ;
                setUi(sizeDone);
            }
        });
    }

    @Override
    public void updateReceiveProgress(final long size) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentFile ==0){
                    folderSizeDone = 0;
                }
                if (currentFile != lastFile) {
                    lastFile = currentFile;
                    folderSizeDone += lastFileSize;
                }
                lastFileSize = size;
                long totalSize = objectTransfers[currentTransferItem].getSize();
                objectTransfers[currentTransferItem].setPercent(getPercent(lastFileSize + folderSizeDone,totalSize));
                objectTransfers[currentTransferItem].setStatus(getProperUnit(lastFileSize + folderSizeDone)+" / "+getProperUnit(totalSize));
                adapter.notifyDataSetChanged();
                long sizeDone = 0;
                for (int i=0;i<currentTransferItem;i++){
                    sizeDone += objectTransfers[i].getSize();
                }
                sizeDone += lastFileSize + folderSizeDone ;
                setUi(sizeDone);
            }
        });
    }

    private void onComplete(){
        completed = true;
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        int fileTransferred = sharedPreferences.getInt(FILE_TRANSFERRED,0);
        long dataTransferred = sharedPreferences.getLong(DATA_TRANSFERRED,0);
        fileTransferred +=  objectTransfers.length;
        dataTransferred += size;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(FILE_TRANSFERRED,fileTransferred);
        editor.putLong(DATA_TRANSFERRED,dataTransferred);
        editor.apply();
        vibrate();
        Toast.makeText(activity,"Transfer Completed",Toast.LENGTH_LONG).show();
        if (adapter!=null){
            adapter.setCompleted();
            if (!recyclerView.isComputingLayout())
                adapter.notifyDataSetChanged();
        }
    }

    private void vibrate(){
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            else
                vibrator.vibrate(500);
        }
    }


    private void dialogCancel(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setMessage("Do you want to cancel the transfer?");
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(activity, ActivityMain.class);
                activity.startActivity(intent);
            }
        });
        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void onBackPressed() {
        if (completed) {
            Intent intent = new Intent(activity,ActivityMain.class);
            activity.startActivity(intent);
        }
        else
            dialogCancel();
    }
}
