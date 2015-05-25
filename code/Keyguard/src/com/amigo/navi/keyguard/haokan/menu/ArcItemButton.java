package com.amigo.navi.keyguard.haokan.menu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.keyguard.R;


public class ArcItemButton extends RelativeLayout{
	
    private TextView mTextView;
    private ImageView mImageView;
	
    private int[] titleResIds;
	 
    private int toXDelta;
    private int toYDelta;
    
    private boolean needFeekBack;
     
    private boolean itemSelected = false;
    
    
    public void setItemSelected(boolean itemSelected) {
        this.itemSelected = itemSelected;
        mImageView.setSelected(itemSelected);
        setTitle(titleResIds[itemSelected ? 1 : 0]);
    }
    

	public ArcItemButton(Context context) {
		super(context);
		
		init();
	}

	public ArcItemButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ArcItemButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.haokan_arc_item_button, this);
		
        mTextView = (TextView) findViewById(R.id.arc_item_title);
        mImageView = (ImageView) findViewById(R.id.arc_item_image);
        
	}
	
	public void setBackgroundResource(int resid) {
	    mImageView.setBackgroundResource(resid);
    }
	
	public void setTitle(String str) {
	    mTextView.setText(str);
	}
	
	public void setTitle(int textId) {
        mTextView.setText(textId);
    }
	
	public void setImageColor(int color) {
	    mImageView.setBackgroundColor(color);
	}

    public TextView getmTextView() {
        return mTextView;
    }

    public void setmTextView(TextView mTextView) {
        this.mTextView = mTextView;
    }

    public ImageView getmImageView() {
        return mImageView;
    }

    public void setmImageView(ImageView mImageView) {
        this.mImageView = mImageView;
    }


    public boolean isItemSelected() {
        return itemSelected;
    }


    public int[] getTitleResIds() {
        return titleResIds;
    }


    public void setTitleResIds(int[] titleResIds) {
        this.titleResIds = titleResIds;
    }


    public int getToXDelta() {
        return toXDelta;
    }


    public void setToXDelta(int toXDelta) {
        this.toXDelta = toXDelta;
    }


    public int getToYDelta() {
        return toYDelta;
    }


    public void setToYDelta(int toYDelta) {
        this.toYDelta = toYDelta;
    }


    public boolean isNeedFeekBack() {
        return needFeekBack;
    }


    public void setNeedFeekBack(boolean needFeekBack) {
        this.needFeekBack = needFeekBack;
    }
	
     
    
	
	
}
