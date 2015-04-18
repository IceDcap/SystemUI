package com.android.systemui.gionee.nc.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.android.systemui.statusbar.ExpandableView;

public class GnNotficationHandlerView extends ExpandableView {

	public GnNotficationHandlerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void performRemoveAnimation(long duration,
			float translationDirection, Runnable onFinishedRunnable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void performAddAnimation(long delay, long duration) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

}
