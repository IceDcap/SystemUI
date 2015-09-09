package com.amigo.navi.keyguard.haokan.analysis;

import com.amigo.navi.keyguard.haokan.entity.EventLogger;

import java.util.ArrayList;
import java.util.List;

public class MessageModel {

	public String jsonData;
	
	public ArrayList<Integer> ids = null;
	
	private List<EventLogger> listPv = null;
	
	public MessageModel(){
		 
	}

    public MessageModel(String data, ArrayList<Integer> idList) {
        super();
        this.jsonData = data;
        this.ids = idList;
    }
	
    public MessageModel(String data, ArrayList<Integer> idList,List<EventLogger> listPv) {
        super();
        this.jsonData = data;
        this.ids = idList;
        this.listPv = listPv;
    }

    public List<EventLogger> getListPv() {
        return listPv;
    }

    public void setListPv(List<EventLogger> listPv) {
        this.listPv = listPv;
    }
    
    
	
}
