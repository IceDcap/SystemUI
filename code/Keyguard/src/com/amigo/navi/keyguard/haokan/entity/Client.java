
package com.amigo.navi.keyguard.haokan.entity;

public class Client {

    private String deviceName = android.os.Build.MODEL;

    private String imei;

    private String iccid;

    private String mobileNumber;
    /**
     * string手机尺寸 (宽x高)
     */
    private String screenSize;
    /**
     * 设备mac地址
     */
    private String mac;

    /**
     * number操作系统类型 （1、Android手机2、安卓电视3、IPhone 4、iPad 5安卓pad ）
     */
    private int os = 1;

    /**
     * number性别(1：男，2：女，默认为0:未知)
     */
    private int sex = 0;
    /**
     * string出生日期（yyyy-MM-dd）
     */
    private String birthday;
    private String province;
    private String city;
    
    

    public Client(String imei, String iccid, String mobileNumber,
            String screenSize) {
        super();
        this.imei = imei;
        this.iccid = iccid;
        this.mobileNumber = mobileNumber;
        this.screenSize = screenSize;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getImei() {
        imei = "008600215556761";
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getOs() {
        return os;
    }

    public void setOs(int os) {
        this.os = os;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}
