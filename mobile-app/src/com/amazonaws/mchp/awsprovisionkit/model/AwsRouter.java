package com.amazonaws.mchp.awsprovisionkit.model;


/**
 * Created by Administrator on 2016/12/2.
 */

public class AwsRouter {
    private String deviceName;
    private String macAddr;
    private String devType;
    private String thingName;

    public String getDeviceName() {
        return deviceName;
    }
    public String getMacAddr(){return macAddr;}
    public String getDevType(){return devType;}
    public String getThingName(){return thingName;}

    public AwsRouter setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public AwsRouter setMacAddr(String macAddr) {
        this.macAddr = macAddr;
        return this;
    }

    public AwsRouter setDevType(String devType) {
        this.devType = devType;
        return this;
    }

    public AwsRouter setThingName(String thingName) {
        this.thingName = thingName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if ( o == null || !(o instanceof  idevice)) {
            return false;
        }
        if(deviceName == null &&  ((idevice) o).getDeviceName() == null ) {
            return true;
        }
        if(deviceName != null) {
            return deviceName.equals(((idevice)o).getDeviceName());
        }
        return false;
    }
}
