package com.amigo.navi.keyguard.skylight;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.R ;

public class ImageTextButton extends LinearLayout {

	private final static String TAG = "skylight_ImageTextButton"; 
	private ImageView mImageView;
	private TextView mTextView;

	public ImageTextButton(Context context) {
		super(context);
		this.setClickable(true);
	}

	public ImageTextButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.skylight_img_text_bt, this, true);

		mImageView = (ImageView) findViewById(R.id.imageView);
		mTextView = (TextView) findViewById(R.id.textView);
		mImageView.setImageResource(R.drawable.skylight_missed_sms);

	}



	public void setImageView(int resourceId) {
		mImageView.setImageResource(resourceId);
	}

	public void setTextView(String text) {
		mTextView.setText(text);
	}

	public void setTextSize(Float textSize) {
		mTextView.setTextSize(textSize);
	}

	public void setTextColor(int color) {
		mTextView.setTextColor(color);
	}


}
