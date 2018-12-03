package com.amazonaws.mchp.awsprovisionkit.model;


/**
 * Created by Administrator on 2016/12/2.
 */

public class idevice {

    protected static final String LOGTAG="Apptest/ces/control" ;


    private String deviceName;
    private int deviceIcon;
    private String macAddr;
    private String devType;

    public String getDeviceName() {
        return deviceName;
    }

    public int getDevIcon() {
        return deviceIcon;
    }
    //-- setter
    public  idevice setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }
    public  idevice setDevType(String devType) {
        this.devType = devType;
        return this;
    }

    public  String getDevType() {
        return this.devType;
    }

    public  idevice setMacAddr(String macAddr) {
        this.macAddr = macAddr;
        return this;
    }
    public String getMacAddr() {
        return macAddr;
    }

    public  idevice setDevIcon(int deviceIcon) {
        this.deviceIcon = deviceIcon;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if ( o == null || !(o instanceof  idevice)) {
            return false;
        }
        return macAddr.equals(((idevice)o).getMacAddr());
    }


}
