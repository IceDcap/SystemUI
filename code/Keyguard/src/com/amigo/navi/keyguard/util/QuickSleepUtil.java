package com.amigo.navi.keyguard.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.amigo.navi.keyguard.AmigoKeyguardPage;
import com.amigo.navi.keyguard.DebugLog;
import com.android.keyguard.R;

public class QuickSleepUtil {
    private static final String TAG = "QuickSleepUtil";
	
    private static final int DOUBLE_TAP_MIN_TIME=100;
    private static final int DOUBLE_TAP_MAX_TIME=350;
    private static int sDoubleTapAreaRadius=-1;
	
	
	private static int sEventCount=0;
    private static long mLastClickTime=0;
    private static float mPreTapX=0;
    private static float mPreTapY=0;
    
    
    private static final int FIRST_DOWN_ACTION=1;
    private static final int FIRST_UP_ACTION=2;
    private static final int SECOND_DOWN_ACTION=3;
    private static final int SECOND_UP_ACTION=4;

    public static void gotoSleepIfDoubleTap(Context context, MotionEvent event,AmigoKeyguardPage mainCellLayout) {
        if(isTouchInvalidArea(event, mainCellLayout)){
            return;
        }
        if(sDoubleTapAreaRadius == -1){
        	sDoubleTapAreaRadius = context.getResources().getDimensionPixelSize(R.dimen.double_tap_area_radius);
        	if(DebugLog.DEBUG) DebugLog.d(TAG, "gotoSleepIfDoubleTap sDoubleTapAreaRadius:"+sDoubleTapAreaRadius);
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            if (action == MotionEvent.ACTION_DOWN && sEventCount % 2 !=0) {
                sEventCount--;
            }
            sEventCount++;
        } 
        if(sEventCount==SECOND_DOWN_ACTION){//second down action
            long timeInterval=SystemClock.elapsedRealtime()-mLastClickTime;
            boolean isInDoubleTapArea=isDoubleTapDistanceShortEnough(event);
            if(timeInterval > DOUBLE_TAP_MAX_TIME||!isInDoubleTapArea){
                sEventCount=1;
            }
        }
        if(sEventCount==FIRST_DOWN_ACTION){//first down action
            mLastClickTime = SystemClock.elapsedRealtime();
        }
        
        if(DebugLog.DEBUG) DebugLog.d(TAG, "gotoSleepIfDoubleTap  sEventCount: "+sEventCount+"  interval: "+(SystemClock.elapsedRealtime()-mLastClickTime)+"  preDownX: "+mPreTapX);
        if (sEventCount % 2 == 0) {//up action
            float downX = event.getX();
            float downY = event.getY();
            if (sEventCount % 4 == 0) {// second up action
                long clickTime = SystemClock.elapsedRealtime();
                long timeInterval = Math.abs(clickTime - mLastClickTime);
                boolean isDoubleTap = (timeInterval < DOUBLE_TAP_MAX_TIME && timeInterval > DOUBLE_TAP_MIN_TIME);
                boolean isDoubleTapDistanceShort =isDoubleTapDistanceShortEnough(event);
                if(DebugLog.DEBUG) DebugLog.d(TAG, "isDoubleTap: " + isDoubleTap + "  isInDoubleTapArea: " + isDoubleTapDistanceShort);
                if (isDoubleTap && isDoubleTapDistanceShort) {
                	if(DebugLog.DEBUG) DebugLog.d(TAG, "gotoSleepIfDoubleTap()  mLastClickTime: " + mLastClickTime + "  clickTime:"
                                + clickTime);
                        PowerManager pw = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        pw.goToSleep(SystemClock.uptimeMillis());
                } 
                mLastClickTime = clickTime;
                sEventCount = 0;
            }
            mPreTapX = downX;
            mPreTapY = downY;
        }
    }
    
    private static boolean isDoubleTapDistanceShortEnough(MotionEvent event){
        float doubleTapDistance = (float) Math.sqrt(Math.pow(event.getX() - mPreTapX, 2)
                + Math.pow(event.getY() - mPreTapY, 2));
        boolean isDoubleTapDistanceShort = doubleTapDistance < sDoubleTapAreaRadius;
        return isDoubleTapDistanceShort;
    }
    
    private static boolean isTouchInvalidArea(MotionEvent event,AmigoKeyguardPage mainCellLayout){
        boolean isInvalidArea=false;
        Rect notificationRect=mainCellLayout.getNotificationContentRect();
        if(DebugLog.DEBUG) DebugLog.d(TAG, "notificationRect: "+notificationRect.toString());
        float x = event.getX();
        float y = event.getY();
        boolean isInNotificationRect = (x > notificationRect.left && x < notificationRect.right) && (y > notificationRect.top && y < notificationRect.bottom);
        if(isInNotificationRect){
            isInvalidArea=true;
        }
        return isInvalidArea;
    }
    
    
    
}
