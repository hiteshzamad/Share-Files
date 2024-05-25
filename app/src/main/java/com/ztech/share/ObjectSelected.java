package com.ztech.share;

import android.graphics.drawable.Drawable;

public class ObjectSelected {
    private final Drawable image;
    private String name;
    private String path;
    private final FileType fileType;

    enum FileType{
        APP,FOLDER,FILE,EXTRA
    }

    public ObjectSelected(Drawable image, String name, String path, FileType fileType) {
        this.image = image;
        this.name = name;
        this.path = path;
        this.fileType = fileType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Drawable getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileType getFileType() {
        return fileType;
    }

}
