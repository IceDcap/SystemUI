package com.android.systemui.gionee.cc.qs.tiles;

import android.content.Intent;

import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.GnQSTile.BooleanState;

public class GnCalculateTile extends GnQSTile<GnQSTile.BooleanState> {
    
    private final static String CALCULATOR_PKG = "com.android.calculator2";
    private final static String CALCULATOR_CLS = "com.android.calculator2.Calculator";

    public GnCalculateTile(Host host, String spec) {
        super(host, spec);
    }

    @Override
    public void setListening(boolean listening) {
        
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnCalculateTile");
        Intent intent = new Intent();
        intent.setClassName(CALCULATOR_PKG, CALCULATOR_CLS);
        mHost.startSettingsActivity(intent);
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = false;
        state.visible = true;
        state.label = mContext.getString(R.string.gn_qs_calculator);
        state.iconId = R.drawable.gn_ic_qs_calculator;
        state.contentDescription = mContext.getString(R.string.gn_qs_calculator);
    }

}
