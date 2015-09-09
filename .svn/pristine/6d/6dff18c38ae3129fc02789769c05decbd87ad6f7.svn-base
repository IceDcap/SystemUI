/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.amigo.navi.keyguard.fingerprint;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KeyguardViewHostManager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;



import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AmigoFingerService extends Service {
    static final String TAG = "AmigoFingerService";
    static final String PERMISSION = android.Manifest.permission.CONTROL_KEYGUARD;



    @Override
    public void onCreate() {

    }

    @Override
    public IBinder onBind(Intent intent) {
    	if(DebugLog.DEBUG){
    		DebugLog.i(TAG, "onBind");
    	}
        return mBinder;
    }

    void checkPermission() {
        // Avoid deadlock by avoiding calling back into the system process.
        if (Binder.getCallingUid() == Process.SYSTEM_UID) return;

        // Otherwise,explicitly check for caller permission ...
        if (getBaseContext().checkCallingOrSelfPermission(PERMISSION) != PERMISSION_GRANTED) {
            Log.w(TAG, "Caller needs permission '" + PERMISSION + "' to call " + Debug.getCaller());
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid()
                    + ", must have permission " + PERMISSION);
        }
    }

    private final IAmigoFingerService.Stub mBinder = new IAmigoFingerService.Stub() {

          public void blackScreenFingerSuccess(){
        	  if(DebugLog.DEBUG){
          		DebugLog.i(TAG, "blackScreenFingerSuccess");
          	}
        	  checkPermission();	  
        	  KeyguardViewHostManager.getInstance().unlockByBlackScreenFingerIdentify();
          }

		@Override
		public boolean openFingerPrintOrNot() throws RemoteException {
			if(DebugLog.DEBUG){
          		DebugLog.i(TAG, "openFingerPrintOrNot");
          	}
        	  checkPermission();	
        	 return  KeyguardViewHostManager.getInstance().openFingerPrintOrNot();
		}
    };
}

