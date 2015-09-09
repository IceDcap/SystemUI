package com.android.systemui.usb;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class GnMtpHelpOsList extends ViewGroup {

	public GnMtpHelpOsList(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		int top = 0;
		for (int i = 0; i < count; i++) {
			View v = getChildAt(i);
			int height = v.getMeasuredHeight();
			v.layout(l, top, r, top+height);
			top += height;
		}
	}

}
