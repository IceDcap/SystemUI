
package com.amigo.navi.keyguard.haokan.entity;

import java.io.Serializable;

public class EventLogger implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String dateTime;
    
    private int imgId;
    
    private int typeId;
    
    private int event;
    
    private int count;
    
    private int value;
    
    private String urlPv;
    
    
    public EventLogger(String dateTime, int event,int value) {
        super();
         
        this.dateTime = dateTime;
        this.imgId = -1;
        this.typeId = -1;
        this.event = event;
        this.count = 1;
        this.value = value;
    }
    
    public EventLogger(String dateTime, int imgId, int typeId, int event, int count) {
        super();
         
        this.dateTime = dateTime;
        this.imgId = imgId;
        this.typeId = typeId;
        this.event = event;
        this.count = count;
    }
    

    
    public EventLogger(String dateTime, int imgId, int typeId, int event, int count,int value, String urlPv) {
        super();
         
        this.dateTime = dateTime;
        this.imgId = imgId;
        this.typeId = typeId;
        this.event = event;
        this.count = count;
        this.value = value;
        this.urlPv = urlPv;
    }
    
    public EventLogger(String dateTime, int imgId, int typeId, int event, int count,String urlPv) {
        super();
         
        this.dateTime = dateTime;
        this.imgId = imgId;
        this.typeId = typeId;
        this.event = event;
        this.count = count;
        this.urlPv = urlPv;
    }
  
    

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "dateTime : " + dateTime + " | imgId = " + imgId + ", typeId = " + typeId
                + ", event = " + event + ", value=" + value;
    }

    public String getUrlPv() {
        return urlPv;
    }

    public void setUrlPv(String urlPv) {
        this.urlPv = urlPv;
    }
    
    

}
