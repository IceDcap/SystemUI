package com.amigo.navi.keyguard.skylight;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;
import com.android.keyguard.R;

public class SkylightActivity extends Activity {

    private static final String LOG_TAG="SkylightActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.skylightTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skylight_activity_layout);
        DebugLog.d(LOG_TAG, "SkylightActivity  onCreate");
        KeyguardViewHostManager viewmanager = KeyguardViewHostManager.getInstance();
        if (SkylightUtil.getIsHallOpen(getApplicationContext())) {
            DebugLog.d(LOG_TAG, "SkylightActivity  hallOpen finish ");
            finish();
            overridePendingTransition(0, 0);
        } else {
            DebugLog.d(LOG_TAG, "SkylightActivity  viewmanager is null?  "+(viewmanager==null));
            /**
             * why viewmanager maybe null??
             */
            if (viewmanager != null) {
                viewmanager.notifySkylightActivityCreated(this);
            }

        }

    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            DebugLog.d(LOG_TAG, "SkylightActivity onTouchEvent");
            finish();
            overridePendingTransition(0, 0);
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
