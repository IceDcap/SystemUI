package com.android.systemui.gionee.cc.camera;
/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
import com.android.systemui.gionee.cc.util.GnAppConstants;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class GnNotificationHandleActivity extends Activity{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent intent=getIntent();
            Uri uri=intent.getParcelableExtra(GnAppConstants.BLIND_SHOOT_URI_INTENT_KEY);
            Intent activityIntent = new Intent(Intent.ACTION_VIEW);
            activityIntent.setDataAndType(uri, "image/*");
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
            /*KeyguardViewManager instance = KeyguardViewManager.getInstance();
            if (instance != null) {
                instance.hideShortcut(true);
            }*/
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        finish();
        
    }

}
