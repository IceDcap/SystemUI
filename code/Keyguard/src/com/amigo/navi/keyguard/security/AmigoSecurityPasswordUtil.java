package com.amigo.navi.keyguard.security;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class AmigoSecurityPasswordUtil {

	 public static final String LOG_TAG="AmigoSecurityPasswordUtil";
	 private boolean isSecurityPasswordSupport=false;
	 
	 
	 private  static AmigoSecurityPasswordUtil  instance;
	 
	 public static AmigoSecurityPasswordUtil  getInstance(){
		 if(instance==null){
			 instance=new AmigoSecurityPasswordUtil();
		 }
		 
		 return instance;
	 }
	 

	 private  AmigoSecurityPasswordUtil() {
		 isSecurityPasswordSupport();
	 }


	private void  isSecurityPasswordSupport(){
	        try {
	            Class<?> mClass = Class.forName("android.util.AmigoSecurityPassWord");
	            Method mMethod = mClass.getMethod("isSecurityPasswordEnable", Context.class); 
	            isSecurityPasswordSupport = true;
	            Log.d(LOG_TAG, "isSecurityPasswordSupport:isSecurityPasswordSupport "+ isSecurityPasswordSupport);
	        } catch (ClassNotFoundException e) {
	            Log.d(LOG_TAG, "ClassNotFoundException: "+ e);
	            isSecurityPasswordSupport = false;
	        } catch (NoSuchMethodException ex) {
	            Log.d(LOG_TAG, "NoSuchMethodException: "+ ex);
	            isSecurityPasswordSupport = false;
	        } 
	    }


	public boolean  getSecurityPasswordSupport() {
		return this.isSecurityPasswordSupport;
	}
   
	

}
