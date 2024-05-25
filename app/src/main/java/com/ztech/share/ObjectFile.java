package com.ztech.share;

import android.graphics.drawable.Drawable;

public class ObjectFile {
    private String size;
    private String filename;
    private Drawable drawable;
    private boolean checkable;
    private boolean directory;
    private boolean checked;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    boolean isCheckable() {
        return checkable;
    }

    void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    boolean isDirectory() {
        return directory;
    }

    void setDirectory(boolean directory) {
        this.directory = directory;
    }

    String getFilename() {
        return filename;
    }

    void setFilename(String filename) {
        this.filename = filename;
    }

    Drawable getDrawable() {
        return drawable;
    }

    void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    boolean isChecked() {
        return checked;
    }

    void setChecked(boolean checked) {
        this.checked = checked;
    }

}
