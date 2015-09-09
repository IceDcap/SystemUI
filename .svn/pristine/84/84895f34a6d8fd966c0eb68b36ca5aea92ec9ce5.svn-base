/**
  File Description:Animation to scan
  Author:baorui
  Create Date:2014-01-15
  Change List:
 */

package com.android.systemui.recent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.systemui.R;

public class GnScanView extends ImageView {

    public static final int START_ANGLE = 270;
    private final float SCAN_STROKE_WIDTH = getResources().getDimension(R.dimen.gn_scan_strokewidth);
    private final float SCAN_INNER_CIRCLE = getResources().getDimension(R.dimen.gn_scan_innercircle);
    private final float SCAN_CENTER_R = SCAN_STROKE_WIDTH + SCAN_INNER_CIRCLE;

    private Paint mPaint;
    private int mStartAngle = 0;
    private int mAngle = 0;
    private RectF mOval;

    // private final int[] SCAN_COLORS_ARRAY = getResources().getIntArray(R.array.scan_color_shade_array);

    public int getStartAngle() {
        return mStartAngle;
    }
    
    public int getAngle() {
        return mAngle;
    }

    public void setAngle(int angle) {
        mStartAngle = START_ANGLE;
        mAngle = angle;
        postInvalidate();
    }
    
    public void setAngle(int startAngle, int angle) {
        mStartAngle = startAngle;
        mAngle = angle;
        postInvalidate();
    }

    public GnScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mOval, mStartAngle, mAngle, false, mPaint);
    }

    private void initData() {
        mPaint = new Paint();

        float mCenterX = SCAN_CENTER_R;//getDrawable().getIntrinsicWidth() / 2;
        float mCenterY = SCAN_CENTER_R;//getDrawable().getIntrinsicHeight() / 2;

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(SCAN_STROKE_WIDTH);
        mPaint.setColor(0xffffffff);

        // Shader shader = new SweepGradient(mCenterX, mCenterY, SCAN_COLORS_ARRAY, null);
        // mPaint.setShader(shader);

        mOval = new RectF();
        mOval.left = mCenterX - (SCAN_INNER_CIRCLE + SCAN_STROKE_WIDTH / 2);
        mOval.right = mCenterX + (SCAN_INNER_CIRCLE + SCAN_STROKE_WIDTH / 2);
        mOval.top = mCenterY - (SCAN_INNER_CIRCLE + SCAN_STROKE_WIDTH / 2);
        mOval.bottom = mCenterY + (SCAN_INNER_CIRCLE + SCAN_STROKE_WIDTH / 2);
    }
}
