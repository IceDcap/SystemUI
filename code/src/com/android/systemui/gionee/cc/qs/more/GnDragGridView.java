package com.android.systemui.gionee.cc.qs.more;

import java.util.ArrayList;
import java.util.Collection;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.gionee.cc.qs.GnQSTile.ExtendCallback;
import com.android.systemui.gionee.cc.qs.GnQSTileView;
import com.android.systemui.gionee.cc.qs.tiles.GnMoreTile;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;


public class GnDragGridView extends ViewGroup {
    
    private static final String TAG = "GnDragGridView";
    
    private static final String TILES_SETTING = "sysui_qs_tiles";
    
    private Context mContext;
    private ArrayList<TileRecord> mRecords = new ArrayList<TileRecord>();
    private ArrayList<TileRecord> mBackUpRecords;

    private TextView mShowLabel;
    private TextView mHideLabel;
    
    private boolean isDraging = false;
    
    private boolean isAnimating = false;
    
    private TileRecord mDragRecord = null;
    
    private GnMoreTile mMoreTile;
    
    private Vibrator mVibrator;
    
    private Rect mTouchFrame;
    
    private int mDragPosition;
    private int mCurPosition;
    
    private int mColumns;
    private int mCellWidth;
    private int mCellHeight;
    private int mCellPaddingTop;
    private int mCellPaddingTitle;
    private int mPanelPaddingLeft;
    private int mTileExtra;
    
//    private Handler mHandler = new Handler();
    private final H mHandler = new H();
    private Runnable mLongClickRunnable = new Runnable() {
        
        @Override
        public void run() {
            Log.d(TAG, "isDraging = true;");
            isDraging = true;
            mVibrator.vibrate(100);
            if (isPortrait()) {
                mMoreTile.setVisibleState(true, false);
            }
            requestLayout();
        }
    };
    
    /** 
     * item发生变化回调的接口 
     */  
    private OnChanageListener mChanageListener;
    
    /** 
     * 设置回调接口 
     * @param onChanageListener 
     */  
    public void setOnChangeListener(OnChanageListener onChanageListener){  
        mChanageListener = onChanageListener;  
    }
    
    public GnDragGridView(Context context) {
        this(context, null);
    }

    public GnDragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        final Resources res = mContext.getResources();
        mColumns = res.getInteger(R.integer.gn_quick_settings_num_columns);
        mCellWidth = res.getDimensionPixelSize(R.dimen.gn_qs_tile_width);
        mCellHeight = res.getDimensionPixelSize(R.dimen.gn_qs_tile_height);
        mCellPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_more_tile_padding_top);
        mCellPaddingTitle = res.getDimensionPixelSize(R.dimen.gn_qs_more_tile_padding_title);
        mPanelPaddingLeft = res.getDimensionPixelSize(R.dimen.gn_qs_panel_padding_left);
        
        mShowLabel = new TextView(context);
        mShowLabel.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        mShowLabel.setSingleLine();
        mShowLabel.setPadding(mPanelPaddingLeft, 0, 0, 0);
        mShowLabel.setText(mContext.getString(R.string.gn_qs_more_show_title));
        mShowLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimensionPixelSize(R.dimen.gn_qs_more_title_text_size));
        addView(mShowLabel);
        
        mHideLabel = new TextView(context);
        mHideLabel.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        mHideLabel.setSingleLine();
        mHideLabel.setPadding(mPanelPaddingLeft, 0, 0, 0);
        mHideLabel.setText(mContext.getString(R.string.gn_qs_more_hide_title));
        mHideLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimensionPixelSize(R.dimen.gn_qs_more_title_text_size));
        addView(mHideLabel);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Resources res = mContext.getResources();
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mColumns = res.getInteger(R.integer.gn_quick_settings_num_columns);
            mCellPaddingTitle = res.getDimensionPixelSize(R.dimen.gn_qs_more_tile_padding_title);
            mCellPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_more_tile_padding_top);
        } else {
            mColumns = res.getInteger(R.integer.gn_quick_settings_num_columns_land);
            mCellPaddingTitle = res.getDimensionPixelSize(R.dimen.gn_qs_more_tile_padding_title_land);
            mCellPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_more_tile_padding_top_land);
        }
        
        hideMoreView();
        
        mHideLabel.setText(R.string.gn_qs_more_hide_title);
        mShowLabel.setText(R.string.gn_qs_more_show_title);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int witch = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
        int labelHeight = getLableHeight(); 
        mShowLabel.measure(widthMeasureSpec, exactly(labelHeight));
        mHideLabel.measure(widthMeasureSpec, exactly(labelHeight));
        
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) continue;
            record.tileView.setDual(false);
            View child = record.tileView;
            child.measure(exactly(mCellWidth), exactly(mCellHeight));
        }
        
        setMeasuredDimension(witch, height);
    }

    private int getLableHeight() {
        int labelHeight = mContext.getResources().getDimensionPixelSize(R.dimen.gn_qs_more_title_height);
        if (isLandscape()) {
            labelHeight = mContext.getResources().getDimensionPixelSize(R.dimen.gn_qs_more_title_height_land);
        }
        return labelHeight;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int hideTitleTop = getHideTitleTop();
        mShowLabel.layout(0, 0, mShowLabel.getMeasuredWidth(), mShowLabel.getMeasuredHeight());
        mHideLabel.layout(0, hideTitleTop, mHideLabel.getMeasuredWidth(), 
                hideTitleTop + mHideLabel.getMeasuredHeight());
        
        int row = 0;
        int col = 0;
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) {
                Log.d(TAG, "gone " + record.tile.getClass().getSimpleName());
                continue;
            }
            
            layoutTile(record.tileView, col, row);
            
            col ++;
            if (col >= mColumns) {
                col = 0;
                row ++;
            }
        }
    }

    private int getHideTitleTop() {
        if (isLandscape()) {
            return mShowLabel.getMeasuredHeight() + mCellHeight + mCellPaddingTop + mCellPaddingTitle;
        } else {
            return mShowLabel.getMeasuredHeight() + 2 * (mCellHeight + mCellPaddingTop) + mCellPaddingTitle;
        }
    }

    private boolean isLandscape() {
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    private boolean isPortrait() {
        return !isLandscape();
    }
    
    private void layoutTile(View view, int col, int row) {
        TileRect rect = calculatePosition(view, col, row);
        view.layout(rect.left, rect.top, rect.right, rect.bottom);
    }
    
    private class TileRect {
        int left;
        int right;
        int top;
        int bottom;
    }
    
    private void updateTilePosition(final View view, final int position) {
        int col = position % mColumns;
        int row = position / mColumns;
        Log.d(TAG, "position= " + position + "  col = " + col + "  row = " + row);

        final TileRect rect = calculatePosition(view, col, row);
        Rect frame = new Rect();
        view.getHitRect(frame);
        
        TranslateAnimation translate = new TranslateAnimation(0, rect.left-frame.left, 0, rect.top - frame.top);
        translate.setDuration(100);
        translate.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.layout(rect.left, rect.top, rect.right, rect.bottom);
                isAnimating = false;
            }
        });
        view.startAnimation(translate);
    }

    private TileRect calculatePosition(View view, final int col, final int row) {
        TileRect rect = new TileRect();
        int width = getWidth() - 2 * mPanelPaddingLeft;
        int extra = (width - view.getMeasuredWidth() * mColumns) / (mColumns - 1);
        rect.left = col * view.getMeasuredWidth() + extra * col + mPanelPaddingLeft;
        rect.top = getTileTop(view, row);
        rect.right = rect.left + view.getMeasuredWidth();
        rect.bottom = rect.top + view.getMeasuredHeight();
        
        mTileExtra = extra;
        
        return rect;
    }

    private int getTileTop(View view, final int row) {
        int top = row * (view.getMeasuredHeight() + mCellPaddingTop) 
                + mShowLabel.getMeasuredHeight() + mCellPaddingTitle;
        
        if (isLandscape()) {
            if (row >= 1) {
                top = top + mHideLabel.getMeasuredHeight() + mCellPaddingTitle;
            }
        } else {
            if (row >= 2) {
                top = top + mHideLabel.getMeasuredHeight() + mCellPaddingTitle;
            }
        }
        return top;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                mDragPosition = -1;
                mDragPosition = pointToPosition(x, y);
                Log.d(TAG, "position = " + mDragPosition);

                if (mDragPosition != -1) {
                    mDragRecord = mRecords.get(mDragPosition);
                    mCurPosition = mDragPosition;
                    mHandler.postDelayed(mLongClickRunnable, 500);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDragPosition != -1
                        && !isTouchInItem(mDragRecord.tileView, (int) ev.getX(), (int) ev.getY())) {
                    Log.d(TAG, "action move remove mLongClickRunnable");
                    removeDragingState();
                    mDragPosition = -1;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "action up remove mLongClickRunnable");
                mHandler.removeCallbacks(mLongClickRunnable);
                if (!mMoreTile.mState.clickable) {
                    mMoreTile.setVisibleState(false, false);
                }
                requestLayout();
                if (isDraging) {
                    isDraging = false;
                    return true;
                }
                break;
            default:
                break;
        }

        Log.d(TAG, "isDraging = " + isDraging);
        if (isDraging) {
            return true;
        } else {
            // super.onInterceptTouchEvent(ev);
            return false;
        }
    }

    private void removeDragingState() {
        mHandler.removeCallbacks(mLongClickRunnable);
        mMoreTile.setVisibleState(false, false);
        isDraging = false;
        requestLayout();
    }

    private int pointToPosition(int x, int y) {
        int position = -1;
        
        for (int i=0; i<mRecords.size(); i++) {
            
            if (i == mDragPosition) {
                continue;
            }
            
            if (i == 7) {
                continue;
            }
            
            Rect frame = mTouchFrame;
            if (frame == null) {
                frame = new Rect();
                mTouchFrame = frame;
            }
            
            final View child = mRecords.get(i).tileView;
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    position = i;
                    removeView(child);
                    addView(child);
                    break;
                }
            }
        }
        
        return position;
    }
    
    private int getToPosition(int x, int y) {
        int position = -1;
        
        for (int i=0; i<mRecords.size(); i++) {
            if (i == mCurPosition) {
                continue;
            }

            Rect frame = mTouchFrame;
            if (frame == null) {
                frame = new Rect();
                mTouchFrame = frame;
            }
            
            final View child = mRecords.get(i).tileView;
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (i < mCurPosition) {
                    frame = new Rect(frame.left - mTileExtra, frame.top, frame.right, frame.bottom);
                } else {
                    frame = new Rect(frame.left, frame.top, frame.right + mTileExtra, frame.bottom);
                }
                if (frame.contains(x, y)) {
                    position = i;
                    break;
                }
            }
        }
        
        return position;
    }
    
    private boolean isTouchInItem(View dragView, int x, int y){
        int leftOffset = dragView.getLeft();
        int topOffset = dragView.getTop();
        if(x < leftOffset || x > leftOffset + dragView.getWidth()){
            return false;
        }
        
        if(y < topOffset || y > topOffset + dragView.getHeight()){
            return false;
        }
        
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isDraging && mDragPosition != -1) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    View child = mDragRecord.tileView;
                    int tileW = child.getMeasuredWidth();
                    int tileH = child.getMeasuredHeight();
                    child.layout(x - tileW / 2, y - tileH / 2, x + tileW / 2, y + tileH / 2);
                    
                    int newPosition = getToPosition(x, y);
                    Log.d(TAG, " newPosition = " + newPosition + "  mCurPosition = " + mCurPosition);
                    if (newPosition != -1 && newPosition != 7 && !isAnimating) {
                        isAnimating = true;
                        int invisibleCount = 0;
                        int index;
                        if (newPosition < mCurPosition) {
                            for (int i=newPosition; i<mCurPosition; i++) {
                                if (mRecords.get(i).tileView.getVisibility() != VISIBLE) {
                                    invisibleCount ++;
                                    continue;
                                }
                                
                                if (i == 7) {
                                    continue;
                                }
                                
                                if (mRecords.get(i) == mDragRecord) {
                                    continue;
                                }
                                
                                index = i - invisibleCount;
                                if (isLandscape()) {
                                    if (index > 7 && newPosition > 7) {
                                        index --;
                                    }
                                    updateTilePosition(mRecords.get(i).tileView, index + 1);
                                } else {
                                    if (index == 6 && newPosition < 7) {
                                        updateTilePosition(mRecords.get(i).tileView, index + 2);
                                    } else {
                                        updateTilePosition(mRecords.get(i).tileView, index + 1);
                                    }
                                }
                            }
                        } else {
                            for (int i=mCurPosition + 1; i<=newPosition; i++) {
                                if (mRecords.get(i).tileView.getVisibility() != VISIBLE) {
                                    invisibleCount ++;
                                    continue;
                                }
                                
                                if (i == 7) {
                                    continue;
                                }
                                
                                if (mRecords.get(i) == mDragRecord) {
                                    continue;
                                }
                                
                                index = i - invisibleCount;
                                if (isLandscape()) {
                                    if ((index > 7 && newPosition < 7) || (mCurPosition > 7 )) {
                                        index --;
                                    }
                                    updateTilePosition(mRecords.get(i).tileView, index - 1);
                                } else {
                                    if (index == 8 && newPosition > 7) {
                                        updateTilePosition(mRecords.get(i).tileView, index - 2);
                                    } else {
                                        updateTilePosition(mRecords.get(i).tileView, index - 1);
                                    }
                                }
                            }
                        }
                        updateRecords(mCurPosition, newPosition);
                        mCurPosition = newPosition;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                int endPosition = getToPosition((int) event.getX(), (int) event.getY());
                Log.d(TAG, "UP  endPosition = " + endPosition);
                if (endPosition == 7) {
                    mCurPosition = -1;
                }
                if (!mMoreTile.mState.clickable) {
                    mMoreTile.setVisibleState(false, false);
                }
                isDraging = false;
                mDragPosition = -1;
                mHandler.postDelayed(new Runnable() {
                    
                    @Override
                    public void run() {
                        requestLayout();
                    }
                }, 100);
                break;
            default:
                break;
        }
        return true;//super.onTouchEvent(event);
    }
    
    private void updateRecords(int from, int to) {
        
        Log.d(TAG, "updateRecords  from = " + from + "  to = " + to);
        
        TileRecord moreTile = mRecords.get(7);
        TileRecord record = mRecords.get(from);
        
        mRecords.remove(from);
        mRecords.add(to, record);
        mRecords.remove(moreTile);
        mRecords.add(7, moreTile);
    }

    public void setTiles(Collection<GnQSTile<?>> collection) {
        
        for (TileRecord record : mRecords) {
            removeView(record.tileView);
        }
        
        mRecords.clear();
        addTiles(collection);
        
        mBackUpRecords = (ArrayList<TileRecord>) mRecords.clone();
        
        refreshAllTiles();
    }
    
    private void refreshAllTiles() {
        for (TileRecord r : mRecords) {
            r.tile.refreshState();
        }
    }

    private void addTiles(Collection<GnQSTile<?>> collection) {
        for (GnQSTile<?> tile : collection) {
            addTile(tile);
            if (tile.getSpec().equals("more")) {
                mMoreTile = (GnMoreTile) tile;
            }
        }
    }

    private void addTile(GnQSTile<?> tile) {
        final TileRecord r = addTileView(tile);
        mRecords.add(r);
    }

    private TileRecord addTileView(GnQSTile<?> tile) {
        final TileRecord r = new TileRecord();
        r.tile = tile;
        r.tileView = tile.createTileView(mContext, tile.supportsStateType());
        r.tileView.setVisibility(VISIBLE);

        Log.d(TAG, "add view name " + r.tile.getClass().getSimpleName() + " view " + r.tileView + " visible = " + tile.mState.visible);
        
        final ExtendCallback callback = new GnQSTile.ExtendCallback() {
            @Override
            public void onStateChanged(GnQSTile.State state) {
                int visibility = state.visible ? VISIBLE : GONE;
                if (isPortrait() && r.tile.getSpec().equals("more")) {
                    visibility = state.visible ? VISIBLE : INVISIBLE;
                }
                
                Log.d(TAG, "name " + r.tile.getClass().getSimpleName() + " view " + r.tileView + " visible = " + visibility);
                
                setTileVisibility(r.tileView, visibility);
                r.tileView.onStateChanged(state);
            }
        };
        r.tile.setExtendCallback(callback);
        
        final View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r.tile.click();
            }
        };

        r.tileView.init(click);
        r.tile.refreshState();
        callback.onStateChanged(r.tile.getState());
        addView(r.tileView);
        return r;
    }
    
    private void setTileVisibility(View v, int visibility) {
        mHandler.obtainMessage(H.SET_TILE_VISIBILITY, visibility, 0, v).sendToTarget();
    }
    
    private class H extends Handler {
        
        private static final int SET_TILE_VISIBILITY = 1;
        
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SET_TILE_VISIBILITY) {
                handleSetTileVisibility((View)msg.obj, msg.arg1);
            }
        }
    }
    
    private void handleSetTileVisibility(View v, int visibility) {
        if (visibility == v.getVisibility()) return;
        Log.d(TAG, " v = " + v + " visible = " + visibility);
        v.setVisibility(visibility);
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }
    
    public interface OnChanageListener {
        public void onChange(int from, int to);
    }
    
    private static final class TileRecord {
        GnQSTile<?> tile;
        GnQSTileView tileView;
    }

    public void onTilesChanged() {
        String tileList = "";
        for (TileRecord record : mRecords) {
            tileList = tileList + record.tile.getSpec() + ",";
        }
        Log.d(TAG, "tileList = " + tileList);
        
        Secure.putString(mContext.getContentResolver(), TILES_SETTING, tileList);
        
        mBackUpRecords.clear();
        mBackUpRecords = (ArrayList<TileRecord>) mRecords.clone();
    }

    public void recovery() {
        mRecords.clear();
        mRecords = (ArrayList<TileRecord>) mBackUpRecords.clone();
        requestLayout();
    }

    public void hideMoreView() {
        mMoreTile.setVisibleState(false, false);
    }

    public void showMoreView() {
        Log.d(TAG, "showMoreView");
        mMoreTile.setVisibleState(true, true);
    }

}
