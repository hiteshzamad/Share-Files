package com.ztech.share;

public class ObjectAudio {
    private final String location;
    private boolean checked;
    private long id;

    public ObjectAudio(String location, boolean checked, long id) {
        this.location = location;
        this.checked = checked;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}