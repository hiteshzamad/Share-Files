package com.ztech.share;

import androidx.annotation.NonNull;

public class ObjectTransfer {
    private final String BREAKER2 = "-@.6&9#!";
    private String fileName;
    private String realName;
    private String status;
    private String location;
    private String[] files;
    private long[] filesSize;
    private int percent;
    private long size;
    private boolean isFolder;

    @NonNull
    @Override
    public String toString() {
        String BREAKER = "-@.1&.#!";
        return fileName + BREAKER + location + BREAKER + status + BREAKER + size + BREAKER + isFolder + BREAKER
                + getFileLength() + BREAKER + arrayToString() + BREAKER + arraySizeToString();
    }


    public long[] getFilesSize() {
        return filesSize;
    }

    public void setFilesSize(long[] filesSize) {
        this.filesSize = filesSize;
    }

    String[] getFiles() {
        return files;
    }

    void setFiles(String[] files) {
        this.files = files;
    }

    boolean isFile() {
        return !isFolder;
    }

    void setFolder(boolean folder) {
        isFolder = folder;
    }

    String getLocation() {
        return location;
    }

    void setLocation(String location) {
        this.location = location;
    }

    void setStatus(String status) {
        this.status = status;
    }

    long getSize() {
        return size;
    }

    void setSize(long size) {
        this.size = size;
    }

    String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    String getStatus() {
        return status;
    }

    int getPercent() {
        return percent;
    }

    void setPercent(int percent) {
        this.percent = percent;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    private String arrayToString(){
        StringBuilder stringBuilder = new StringBuilder();
        if (files!=null){
            for (int i=0;i<files.length;i++) {
                if (i!=0)
                    stringBuilder.append(BREAKER2);
                stringBuilder.append(files[i]);
            }
        }
        else
            stringBuilder.append("EMPTY");
        return stringBuilder.toString();
    }
    private int getFileLength(){
        if (files!=null)
            return files.length;
        return 0;
    }

    private String arraySizeToString(){
        StringBuilder stringBuilder = new StringBuilder();
        if (filesSize!=null){
            for (int i=0;i<filesSize.length;i++) {
                if (i!=0)
                    stringBuilder.append(BREAKER2);
                stringBuilder.append(filesSize[i]);

            }
        }
        else
            stringBuilder.append("EMPTY");
        return stringBuilder.toString();
    }
}
