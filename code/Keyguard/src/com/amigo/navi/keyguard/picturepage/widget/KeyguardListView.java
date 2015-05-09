package com.amigo.navi.keyguard.picturepage.widget;


import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.KWDataCache;

import android.content.Context;
import android.util.AttributeSet;

public class KeyguardListView extends HorizontalListView {
    private static final String TAG = "KeyguardListView";
    private Context mContext;
    public KeyguardListView(Context context) {
        super(context);
        init(context);
    }
    
    public KeyguardListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public KeyguardListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        mScreenWid = KWDataCache.getScreenWidth(getResources());
        mScreenHei = KWDataCache.getAllScreenHeigt(mContext);
        mChildWidth = mScreenWid;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }
    
    public Object getCurrentItem(){
        if(getPage() >= 0 && mAdapter.getCount() > 0){
            return mAdapter.getItem(getPage());
        }
      return null;
    }
    
}
