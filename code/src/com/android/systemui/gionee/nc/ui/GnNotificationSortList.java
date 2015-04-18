package com.android.systemui.gionee.nc.ui;

import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.systemui.gionee.nc.GnNotificationService.NotificationType;

public class GnNotificationSortList extends ViewGroup {
	private static final String TAG = "GnNotificationSortList";
	
	HashMap<View, GnNotificationSortViewEntry> mNotificationSortList = new HashMap<>();
	private int mItemHeight = -1;
	public GnNotificationSortList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Add view entry to {@code HashMap<View, GnNotificationSortViewEntry>}
	 * @param view it's the key to get entry
	 * @param entry GnNotificationSortViewEntry which relate notification with view
	 * @return void
	 * */
	public void saveEntry(View view, GnNotificationSortViewEntry entry){
		mItemHeight = view.getMeasuredHeight();
		RelativeLayout.LayoutParams parentLayoutParam = (RelativeLayout.LayoutParams) getLayoutParams();
		parentLayoutParam.height += mItemHeight;
		mNotificationSortList.put(view, entry);
	}
	
	/**
	 * Add view entry to {@code HashMap<View, GnNotificationSortViewEntry>}
	 * @param view it's the key to get entry
	 * @param appInfo ApplicationInfo
	 * @param type NotificationType
	 * @return void
	 * */
	public void saveEntry(View view, ApplicationInfo appInfo, NotificationType type){
		GnNotificationSortViewEntry entry = new GnNotificationSortViewEntry(appInfo, type);
		saveEntry(view, entry);
	}
	
	/**
	 * delete view from group, add adjust group's height
	 * @param view child in the group
	 * @return void
	 * */
	public void deleteEntry(View view){
		mNotificationSortList.remove(view);
		mItemHeight = view.getMeasuredHeight();
		RelativeLayout.LayoutParams parentLayoutParam = (RelativeLayout.LayoutParams) getLayoutParams();
		parentLayoutParam.height -= mItemHeight;
		removeView(view);
	}
	
	/**
	 * get entry which keeps notification information by view
	 * @param view Key value in the map
	 * @return GnNotificationSortViewEntry
	 * */
	public GnNotificationSortViewEntry getEntry(View view){
		return mNotificationSortList.get(view);
	}
	
	/**
	 * update views in the group
	 * */
	public void updateViews(){
		removeAllViews();
		Iterator<View> iterator = mNotificationSortList.keySet().iterator();
		while(iterator.hasNext()){
			View view = iterator.next();
			if (view.getParent() == null) {
				addView(view);
			}
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		int top = 0;
		Log.v(TAG, "ChildCount:"+count+ " mNotificationSortList size:"+mNotificationSortList.size());
		for (int i = 0; i < count; i++) {
			View v = getChildAt(i);
			int height = v.getMeasuredHeight();
			v.layout(l, top, r, top+height);
			top += height;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int count = getChildCount();
		for (int index = 0; index < count; index++) {
			View child = getChildAt(index);
			child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		}
	}
}
