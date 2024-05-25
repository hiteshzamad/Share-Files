package com.ztech.share;

public class ObjectDevice {
    private final String deviceName;
    private final String address;
    private final String colorString ;

    public ObjectDevice(String deviceName, String address, String colorString) {
        this.deviceName = deviceName;
        this.address = address;
        this.colorString = colorString;
    }

    String getColorString() {
        return colorString;
    }

    String getDeviceName() {
        return deviceName;
    }

    String getAddress() {
        return address;
    }
}
