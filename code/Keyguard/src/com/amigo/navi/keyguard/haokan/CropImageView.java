
package com.amigo.navi.keyguard.haokan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropImageView extends View {

    private static final int RACT_WIDTH = 8;
    private static final int SHADOW = 0xb0000000;
    private static final int LINE_COLOR = 0xffff9000;

    private static final int MODE_X_BACK = 101;
    private static final int MODE_Y_BACK = 102;
    private static final int MODE_ALL_BACK = 100;

    private static final float CROP_EAGE = 60;
    private static final float CROP_FLING = 20;

    private float mMinX = 0;
    private float mMaxX = 0;
    private float mMinY = 0;
    private float mMaxY = 0;
    private float mK = 0;
    private float mChange = 0;

    private float mRecordMinX;
    private float mRecordMaxX;
    private float mRecordMinY;
    private float mRecordMaxY;

    private float mMinMoveX;
    private float mMinMoveY;
    private float mMaxMoveX;
    private float mMaxMoveY;
    private float mMinsX;
    private float mMaxsX;
    private float mMinsY;
    private float mMaxsY;

    private boolean mIsLeftTopX = false;
    private boolean mIsRightTopX = false;
    private boolean mIsLeftTopY = false;
    private boolean mIsLeftBottomY = false;
    private boolean mIsLeftBottomX = false;
    private boolean mIsRightBottomX = false;
    private boolean mIsRightTopY = false;
    private boolean mIsRightBottomY = false;
    private boolean mIsMove = false;
    private Paint mPaint = new Paint();

    public CropImageView(Context context) {
        super(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void touchDown(float x, float y) {
        boolean inX = x > (mMinX - CROP_FLING) && x < (mMinX + CROP_FLING);
        boolean outX = x > (mMaxX - CROP_FLING) && x < (mMaxX + CROP_FLING);
        boolean inY = y > (mMinY - CROP_FLING) && y < (mMinY + CROP_FLING);
        boolean outY = y > (mMaxY - CROP_FLING) && y < (mMaxY + CROP_FLING);
        if (inX && y <= (mMaxY + mMinY) / 2) {
            mIsLeftTopX = true;
            return;
        }
        if (outX && y > (mMaxY + mMinY) / 2) {
            mIsRightBottomX = true;
            return;
        }

        if (inY && x > (mMaxX + mMinX) / 2) {
            mIsRightTopY = true;
            return;
        }
        if (outY && x <= (mMaxX + mMinX) / 2) {
            mIsLeftBottomY = true;
            return;
        }

        if (inX && y > (mMaxY + mMinY) / 2) {
            mIsLeftBottomX = true;
            return;
        }
        if (outX && y <= (mMaxY + mMinY) / 2) {
            mIsRightTopX = true;
            return;
        }

        if (inY && x <= (mMaxX + mMinX) / 2) {
            mIsLeftTopY = true;
            return;
        }
        if (outY && x > (mMaxX + mMinX) / 2) {
            mIsRightBottomY = true;
            return;
        }

        if ((mMinX + CROP_FLING) <= x && x <= (mMaxX - CROP_FLING)
                && (mMinY + CROP_FLING) <= y && y <= (mMaxY - CROP_FLING)) {
            mIsMove = true;
            mMinMoveX = x - mMinX;
            mMaxMoveX = mMaxX - x;
            mMinMoveY = y - mMinY;
            mMaxMoveY = mMaxY - y;
            return;
        }
    }

    private void updateLeftTopX(float x) {
        mMinY += (x - mMinX) * mK;
        mMinX = x;
        checkMode();
    }

    private void updateLeftTopY(float y) {
        mMinX += (y - mMinY) * (1 / mK);
        mMinY = y;
        checkMode();
    }

    private void updateRightBottomX(float x) {
        mMaxY += (x - mMaxX) * mK;
        mMaxX = x;
        checkMode();
    }

    private void updateRightBottomY(float y) {
        mMaxX += (y - mMaxY) * (1 / mK);
        mMaxY = y;
        checkMode();
    }

    private void updateRightTopY(float y) {
        mMaxX += (mMinY - y) * (1 / mK);
        mMinY = y;
        checkMode();
    }

    private void updateRightTopX(float x) {
        mMinY += (mMaxX - x) * mK;
        mMaxX = x;
        checkMode();
    }

    private void updateLeftBottomY(float y) {
        mMinX += (mMaxY - y) * (1 / mK);
        mMaxY = y;
        checkMode();
    }

    private void updateLeftBottomX(float x) {
        mMaxY += (mMinX - x) * mK;
        mMinX = x;
        checkMode();
    }

    private void checkMode() {
        if (mMinX >= mMinsX && mMaxX <= mMaxsX && mMinY >= mMinsY && mMaxY <= mMaxsY) {
            if (mMinY + CROP_EAGE <= mMaxY - CROP_EAGE || mMinX + CROP_EAGE <= mMaxX - CROP_EAGE) {
                if ((mMaxY - mMinY) <= mChange) {
                    invalidate();
                }
            } else {
                setBack(MODE_ALL_BACK);
            }
        } else {
            setBack(MODE_ALL_BACK);
        }
    }

    private void updateMove(float x, float y) {
        mMinY = y - mMinMoveY;
        mMaxY = y + mMaxMoveY;
        mMinX = x - mMinMoveX;
        mMaxX = x + mMaxMoveX;
        if (mMinX < mMinsX || mMaxX > mMaxsX) {
            setBack(MODE_X_BACK);
        }
        if (mMinY < mMinsY || mMaxY > mMaxsY) {
            setBack(MODE_Y_BACK);
        }
        if (mMinX >= mMinsX && mMaxX <= mMaxsX && mMinY >= mMinsY && mMaxY <= mMaxsY) {
            if (mMinX + CROP_FLING < mMaxX - CROP_FLING && mMinY + CROP_FLING < mMaxY - CROP_FLING) {
                invalidate();
            }
        } else {
            setBack(MODE_ALL_BACK);
        }
    }

    private void TouchMove(float x, float y) {
        mRecordMinX = mMinX;
        mRecordMaxX = mMaxX;
        mRecordMinY = mMinY;
        mRecordMaxY = mMaxY;
        if (mIsLeftTopX) {
            updateLeftTopX(x);
        }
        if (mIsLeftTopY) {
            updateLeftTopY(y);
        }
        if (mIsRightBottomX) {
            updateRightBottomX(x);
        }
        if (mIsRightBottomY) {
            updateRightBottomY(y);
        }
        if (mIsRightTopY) {
            updateRightTopY(y);
        }
        if (mIsRightTopX) {
            updateRightTopX(x);
        }
        if (mIsLeftBottomY) {
            updateLeftBottomY(y);
        }
        if (mIsLeftBottomX) {
            updateLeftBottomX(x);
        }
        if (mIsMove) {
            updateMove(x, y);
        }
    }

    private void reset() {
        mIsLeftTopX = false;
        mIsRightTopX = false;
        mIsLeftTopY = false;
        mIsLeftBottomY = false;
        mIsLeftBottomX = false;
        mIsRightBottomX = false;
        mIsRightTopY = false;
        mIsRightBottomY = false;
        mIsMove = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                TouchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                reset();
                break;
            default:                             
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(SHADOW);
        mPaint.setAntiAlias(true);

        canvas.drawRect(mMinsX, mMinsY, mMinX, mMaxsY, mPaint);
        canvas.drawRect(mMinX, mMinsY, mMaxsX, mMinY, mPaint);
        canvas.drawRect(mMinX, mMaxY, mMaxsX, mMaxsY, mPaint);
        canvas.drawRect(mMaxX, mMinY, mMaxsX, mMaxY, mPaint);

        mPaint.setStrokeWidth(1f);
        mPaint.setColor(LINE_COLOR);

        canvas.drawLine(mMinX, mMinY, mMaxX, mMinY, mPaint);
        canvas.drawLine(mMaxX, mMaxY, mMaxX, mMinY, mPaint);
        canvas.drawLine(mMinX, mMinY, mMinX, mMaxY, mPaint);
        canvas.drawLine(mMaxX, mMaxY, mMinX, mMaxY, mPaint);
        canvas.save();
        canvas.restore();
        float middleX = ((mMinX + mMaxX) / 2);
        float middleY = ((mMinY + mMaxY) / 2);

        canvas.drawCircle(mMinX, middleY, RACT_WIDTH, mPaint);
        canvas.drawCircle(middleX, mMinY, RACT_WIDTH, mPaint);
        canvas.drawCircle(mMaxX, middleY, RACT_WIDTH, mPaint);
        canvas.drawCircle(middleX, mMaxY, RACT_WIDTH, mPaint);

        canvas.drawCircle(mMinX, mMinY, RACT_WIDTH, mPaint);
        canvas.drawCircle(mMinX, mMaxY, RACT_WIDTH, mPaint);
        canvas.drawCircle(mMaxX, mMinY, RACT_WIDTH, mPaint);
        canvas.drawCircle(mMaxX, mMaxY, RACT_WIDTH, mPaint);
    }

    public float getXmin() {
        return mMinX;
    }

    public float getXmax() {
        return mMaxX;
    }

    public float getYmin() {
        return mMinY;
    }

    public float getYmax() {
        return mMaxY;
    }

    private void setBack(int mode) {
        switch (mode) {
            case MODE_X_BACK:
                mMinX = mRecordMinX;
                mMaxX = mRecordMaxX;
                break;
            case MODE_Y_BACK:
                mMinY = mRecordMinY;
                mMaxY = mRecordMaxY;
                break;
            case MODE_ALL_BACK:
                mMinX = mRecordMinX;
                mMaxX = mRecordMaxX;
                mMinY = mRecordMinY;
                mMaxY = mRecordMaxY;
                break;
            default:                             
                break;
        }
    }

    public void initXY(float left, float right, float top, float bottom) {
        mMinsX = mMinX = left;
        mMaxsX = mMaxX = right;
        mMinsY = mMinY = top;
        mMaxsY = mMaxY = bottom;
        mChange = mMaxY - mMinY;
        mK = (mMaxY - mMinY) / (mMaxX - mMinX);
    }

    public void setFullOrSingScreen(float left, float right, float top, float bottom) {
        mMinX = left;
        mMaxX = right;
        mMinY = top;
        mMaxY = bottom;
        mChange = mMaxY - mMinY;
        mK = (mMaxY - mMinY) / (mMaxX - mMinX);
        invalidate();
    }
}

