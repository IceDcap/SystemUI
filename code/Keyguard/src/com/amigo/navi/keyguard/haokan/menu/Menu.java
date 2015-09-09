package com.amigo.navi.keyguard.haokan.menu;

import com.amigo.navi.keyguard.haokan.Common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import com.android.keyguard.R;

public class Menu {

	public static final float DEFAULT_FROM_DEGREES = -28.0f;

	public static final float DEFAULT_TO_DEGREES = -152.0f;

	int widthPixels;
	int heightPixels;
	int mRadiusMax;
	int mHomeButtonSize;
	int mRadiusNormal;
	int mEdgeDistance;
	Rect mainRect;
	int mChildSize;
	int infozoneHeight;
	int mTopDistance;
	float mFromDegrees;
	float mToDegrees;
	int mRadius;

	int edgeDistance;
	Context mContext;

	float mTranslationX;
	float mTranslationY;

	public Menu(Context context) {
		this.mContext = context;
		mChildSize = getResources()
				.getDimensionPixelSize(R.dimen.menuChildSize);
		mRadiusMax = getResources().getDimensionPixelSize(
				R.dimen.haokan_arcmenu_radius_max);
		mHomeButtonSize = getResources().getDimensionPixelSize(
				R.dimen.haokan_arcmenu_home_size);
		mRadiusNormal = getResources().getDimensionPixelSize(
				R.dimen.haokan_arcmenu_radius);
		widthPixels = Common.getScreenWidth(context);
		heightPixels = Common.getScreenHeight(context);

		mEdgeDistance = (int) (mRadiusNormal * Math.cos(Math
				.toRadians(DEFAULT_FROM_DEGREES)));

		mTopDistance = (int) (mRadiusMax * Math.sin(Math.toRadians(25)))
				+ mChildSize / 2;
		mainRect = new Rect(mEdgeDistance, 0, widthPixels - mEdgeDistance,
				heightPixels);

		infozoneHeight = getResources().getDimensionPixelSize(
				R.dimen.ketguard_infozone_height);

	}

	private Resources getResources() {
		return mContext.getResources();
	}

	public boolean requestLayout(float x, float y) {

		float fromDegrees;
		float toDegrees;
		int radius;

		if (y >= heightPixels - infozoneHeight) {
			return false;
		}

		if (mainRect.contains((int) x, (int) y)) {
			if (y > mRadiusNormal) {
				fromDegrees = -152;
				toDegrees = -28;
			} else {
				fromDegrees = 28;
				toDegrees = 152;
			}
			radius = mRadiusNormal;

		} else if (x < mEdgeDistance) {
			if (y <= mTopDistance) {
				return false;
			}

			if (y > heightPixels / 3) {
				fromDegrees = -62;
				toDegrees = 62;
			} else {

				fromDegrees = -32;
				toDegrees = 80;
			}
			radius = mRadiusMax;
		} else {

			if (y <= mTopDistance) {
				return false;
			}

			if (y > heightPixels / 3) {
				fromDegrees = 118;
				toDegrees = 242;
			} else {
				fromDegrees = 100;
				toDegrees = 212;
			}
			radius = mRadiusMax;
		}
		setRadiusAndDegrees(radius, fromDegrees, toDegrees);
		setTranslationX(x - widthPixels / 2);
		setTranslationY(y - heightPixels / 2);
		return true;
	}

	public void setTranslationX(float translationX) {
		mTranslationX = translationX;
	}

	public void setTranslationY(float translationY) {
		mTranslationY = translationY;
	}

	public void setRadiusAndDegrees(int radius, float fromDegrees,
			float toDegrees) {
		if (mFromDegrees == fromDegrees && mToDegrees == toDegrees
				&& radius == mRadius) {
			return;
		}
		this.mRadius = radius;
		this.mFromDegrees = fromDegrees;
		this.mToDegrees = toDegrees;

	}

	public int getWidthPixels() {
		return widthPixels;
	}

	public void setWidthPixels(int widthPixels) {
		this.widthPixels = widthPixels;
	}

	public int getHeightPixels() {
		return heightPixels;
	}

	public void setHeightPixels(int heightPixels) {
		this.heightPixels = heightPixels;
	}

}
