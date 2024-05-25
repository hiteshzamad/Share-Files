package com.ztech.share;

import android.graphics.drawable.Drawable;

public class ObjectApp {

    private final String name;
    private final String location;
    private final Drawable icon;
    private boolean checked;

    ObjectApp(String name, String location, Drawable icon) {
        this.name = name;
        this.location = location;
        this.icon = icon;
        this.checked = false;
    }

    boolean isChecked() {
        return checked;
    }

    void setChecked(boolean checked) {
        this.checked = checked;
    }

    String getName() {
        return name;
    }

    String getLocation() {
        return location;
    }

    Drawable getIcon() {
        return icon;
    }
}
