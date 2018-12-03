package com.amazonaws.mchp.awsprovisionkit.model;

/**
 * Created by Administrator on 2016/12/20.
 */

public class AwsZeroTouchProvisionKitDevice {


    private String thingID;
    private String deviceName;



    public AwsZeroTouchProvisionKitDevice setThingID(String thingID) {
        this.thingID = thingID;
        return this;
    }

    public AwsZeroTouchProvisionKitDevice setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public String getThingID() {
        return thingID;
    }

    public String getDeviceName() {
        return deviceName;
    }

}
