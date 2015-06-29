/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/

package com.android.systemui.gionee.cc.qs.brightness;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.systemui.R;

/** A dialog that provides controls for adjusting the screen brightness. */
public class GnBrightnessDialog extends Activity {

    private GnBrightnessController mBrightnessController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Window window = getWindow();

        window.setGravity(Gravity.TOP);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.gn_qs_brightness_dialog);

        final ImageView icon = (ImageView) findViewById(R.id.brightness_icon);
        final ImageView moreIcon = (ImageView) findViewById(R.id.more_icon);
        final GnToggleSlider slider = (GnToggleSlider) findViewById(R.id.brightness_slider);
        mBrightnessController = new GnBrightnessController(this, icon, moreIcon, slider);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBrightnessController.registerCallbacks();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBrightnessController.unregisterCallbacks();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }
}
