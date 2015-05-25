package com.android.systemui.gionee.cc.qs;

import android.content.Context;
import android.widget.ImageView;

import com.android.systemui.R;

public class GnQSBoolTileView extends GnQSTileView {

    public GnQSBoolTileView(Context context) {
        super(context);
    }

    @Override
    protected void handleStateChanged(GnQSTile.State state) {
        super.handleStateChanged(state);

        GnQSTile.BooleanState booleanState = (GnQSTile.BooleanState) state;
        if (mIcon instanceof ImageView) {
            if (booleanState.value) {
                mIcon.setBackgroundResource(R.drawable.gn_ic_qs_tile_bg_enable);
            } else {
                mIcon.setBackgroundResource(R.drawable.gn_ic_qs_tile_bg_disable);
            }
        }
        
        if (booleanState.value) {
            mLabel.setTextColor(mContext.getResources().getColor(R.color.gn_qs_tile_on));
        } else {
            mLabel.setTextColor(mContext.getResources().getColor(R.color.gn_qs_tile_off));
        }
    }

}