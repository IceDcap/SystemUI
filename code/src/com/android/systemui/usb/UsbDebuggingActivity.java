/*
 * Copyright (C) 2012 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.systemui.usb;

//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
import amigo.app.AmigoActivity;
import amigo.app.AmigoAlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import amigo.widget.AmigoCheckBox;
import amigo.widget.AmigoTextView;
import amigo.changecolors.ChameleonColorManager;

import com.android.systemui.R;

public class UsbDebuggingActivity extends AmigoActivity
                                  implements DialogInterface.OnClickListener {
    private static final String TAG = "UsbDebuggingActivity";

    private AmigoCheckBox mAlwaysAllow;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private String mKey;
    private String mFingerprints;
    private static final int DEBUG_DIALOG = 1;

    @Override
    public void onCreate(Bundle icicle) {
    	setTheme(R.style.GnAlertDialogLight);
        super.onCreate(icicle);
        ChameleonColorManager.getInstance().onCreate(this);

        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            mDisconnectedReceiver = new UsbDisconnectedReceiver(this);
        }

        Intent intent = getIntent();
        mFingerprints = intent.getStringExtra("fingerprints");
        mKey = intent.getStringExtra("key");

        if (mFingerprints == null || mKey == null) {
            finish();
            return;
        }

        showDialog(DEBUG_DIALOG);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

	@Override
	protected AmigoAlertDialog onCreateDialog(int id) {
		AmigoAlertDialog dialog = null;
		LayoutInflater inflater = getLayoutInflater();
		AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
		builder.setTitle(R.string.usb_debugging_title);
		View view = inflater.inflate(R.layout.gn_usb_debug_confirm, null);
		builder.setNegativeButton(android.R.string.cancel, this);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setView(view);
		dialog = builder.create();

		AmigoTextView confirmText = (AmigoTextView) view.findViewById(R.id.confirm_text);
		confirmText.setText(getString(R.string.usb_debugging_message, mFingerprints));
		mAlwaysAllow = (AmigoCheckBox) view.findViewById(R.id.alwaysUse);
//		mAlwaysAllow.setText(getString(R.string.usb_debugging_always));
		return dialog;
	}
    
    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final AmigoActivity mActivity;
        public UsbDisconnectedReceiver(AmigoActivity activity) {
            mActivity = activity;
        }

        @Override
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (!UsbManager.ACTION_USB_STATE.equals(action)) {
                return;
            }
            boolean connected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
            if (!connected) {
                mActivity.finish();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_STATE);
        registerReceiver(mDisconnectedReceiver, filter);
    }

    @Override
    protected void onStop() {
        if (mDisconnectedReceiver != null) {
            unregisterReceiver(mDisconnectedReceiver);
        }
        super.onStop();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        boolean allow = (which == AmigoAlertDialog.BUTTON_POSITIVE);
        boolean alwaysAllow = allow && mAlwaysAllow.isChecked();
        try {
            IBinder b = ServiceManager.getService(USB_SERVICE);
            IUsbManager service = IUsbManager.Stub.asInterface(b);
            if (allow) {
                service.allowUsbDebugging(alwaysAllow, mKey);
            } else {
                service.denyUsbDebugging();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to notify Usb service", e);
        }
        finish();
    }
}
