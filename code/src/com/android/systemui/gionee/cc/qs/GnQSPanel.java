/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.systemui.R;
import com.android.systemui.gionee.cc.qs.GnQSTile.State;
import com.android.systemui.gionee.cc.qs.brightness.GnBrightnessController;
import com.android.systemui.gionee.cc.qs.brightness.GnToggleSlider;

import java.util.ArrayList;
import java.util.Collection;

/** View that represents the quick settings tile panel. **/
public class GnQSPanel extends ViewGroup {
    
    private final String TAG = "GnQSPanel";
    
    private final Context mContext;
    private final ArrayList<TileRecord> mRecords = new ArrayList<TileRecord>();
    private final View mBrightnessSlider;
    private final H mHandler = new H();

    private int mColumns;
    private int mCellWidth;
    private int mCellHeight;
    private int mCellPaddingTop;
    private int mBrightnessPaddingTop;
    private int mBrightnessTilePaddingLeft;
    private int mBrightnessTilePaddingRight;
    private int mPanelPaddingTop;
    private int mPanelPaddingLeft;
    private boolean mListening;

    private GnBrightnessController mBrightnessController;
    private GnQSTileHost mHost;
    
    
    public GnQSPanel(Context context) {
        this(context, null);
    }

    public GnQSPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mBrightnessSlider = LayoutInflater.from(context).inflate(
                R.layout.gn_qs_brightness_dialog, this, false);
        addView(mBrightnessSlider);

        updateResources();

        mBrightnessController = new GnBrightnessController(getContext(),
                (ImageView) findViewById(R.id.brightness_icon),
                (GnToggleSlider) findViewById(R.id.brightness_slider));
    }

    public void setHost(GnQSTileHost qsh) {
        mHost = qsh;
    }

    public GnQSTileHost getHost() {
        return mHost;
    }

    public void updateResources() {
        final Resources res = mContext.getResources();
        final int columns = Math.max(1, res.getInteger(R.integer.gn_quick_settings_num_columns));
        mCellHeight = res.getDimensionPixelSize(R.dimen.gn_qs_tile_height);
        mCellWidth = res.getDimensionPixelSize(R.dimen.gn_qs_tile_width);
        
        updateDimens(res.getConfiguration());
        
        if (mColumns != columns) {
            mColumns = columns;
            postInvalidate();
        }

        if (mListening) {
            refreshAllTiles();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateDimens(newConfig);
        setPadding(mPanelPaddingLeft, 0, mPanelPaddingLeft, 0);
    }

    private void updateDimens(Configuration newConfig) {
        Resources res = mContext.getResources();
        
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPanelPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_panel_padding_top_land);
            mCellPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_tile_padding_top_land);
            mPanelPaddingLeft = res.getDimensionPixelSize(R.dimen.gn_qs_panel_padding_left_land);
            mBrightnessTilePaddingLeft = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_tile_padding_left_land);
            mBrightnessTilePaddingRight = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_tile_padding_right_land);
            mBrightnessPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_padding_top_land);
        } else {
            mPanelPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_panel_padding_top);
            mCellPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_tile_padding_top);
            mPanelPaddingLeft = res.getDimensionPixelSize(R.dimen.gn_qs_panel_padding_left);
            mBrightnessTilePaddingLeft = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_tile_padding_left);
            mBrightnessTilePaddingRight = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_tile_padding_right);
            mBrightnessPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_padding_top);
        }
    }

    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        for (TileRecord r : mRecords) {
            r.tile.setListening(mListening);
        }

        if (mListening) {
            refreshAllTiles();
        }
        if (listening) {
            mBrightnessController.registerCallbacks();
        } else {
            mBrightnessController.unregisterCallbacks();
        }
    }

    private void refreshAllTiles() {
        for (TileRecord r : mRecords) {
            r.tile.refreshState();
        }
    }

    private void setTileVisibility(View v, int visibility) {
        mHandler.obtainMessage(H.SET_TILE_VISIBILITY, visibility, 0, v).sendToTarget();
    }

    private void handleSetTileVisibility(View v, int visibility) {
        if (visibility == v.getVisibility()) return;
        v.setVisibility(visibility);
    }

    public void setTiles(Collection<GnQSTile<?>> collection) {
        for (TileRecord record : mRecords) {
            removeView(record.tileView);
        }
        mRecords.clear();
        for (GnQSTile<?> tile : collection) {
            addTile(tile);
        }
    }

    private void addTile(final GnQSTile<?> tile) {
        final TileRecord r = addTileView(tile);
        mRecords.add(r);
    }

    private TileRecord addTileView(final GnQSTile<?> tile) {
        final TileRecord r = new TileRecord();
        r.tile = tile;
        r.tileView = tile.createTileView(mContext, tile.supportsStateType());
        r.tileView.setVisibility(VISIBLE);

        final GnQSTile.Callback callback = new GnQSTile.Callback() {
            @Override
            public void onStateChanged(State state) {
                int visibility = state.visible ? VISIBLE : GONE;
                setTileVisibility(r.tileView, visibility);
                r.tileView.onStateChanged(state);
            }
        };
        r.tile.setCallback(callback);
        
        final View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r.tile.click();
            }
        };
        r.tileView.setClickListener(click);
                
        if (r.tile.supportsLongClick()) {
            final View.OnLongClickListener longClick = new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    r.tile.handleLongClick();
                    return true;
                }
            };
            r.tileView.setLongClickListener(longClick);
        }
        
        r.tile.setListening(mListening);
        callback.onStateChanged(r.tile.getState());
        r.tile.refreshState();
        addView(r.tileView);
        return r;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        mBrightnessSlider.measure(exactly(width - mBrightnessTilePaddingLeft - mBrightnessTilePaddingRight),
                MeasureSpec.UNSPECIFIED);

        int r = -1;
        int c = -1;
        int rows = 0;
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) continue;
            // wrap to next column if we've reached the max # of columns
            // also don't allow dual + single tiles on the same row
            if (r == -1 || c == (mColumns - 1)) {
                r++;
                c = 0;
            } else {
                c++;
            }
            record.row = r;
            record.col = c;
            rows = r + 1;
        }

        for (TileRecord record : mRecords) {
            record.tileView.setDual(record.tile.supportsDualTargets());
            if (record.tileView.getVisibility() == GONE) continue;
            final int cw = mCellWidth;
            final int ch = mCellHeight;
            record.tileView.measure(exactly(cw), exactly(ch));
        }
        
        final int h = getRowTop(rows);
        final int brightnessHeight = mBrightnessSlider.getMeasuredHeight();
        setMeasuredDimension(width, h + brightnessHeight + mBrightnessPaddingTop);
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout");
        layoutBrightnessSlider();

        final int w = getWidth() - mPanelPaddingLeft * 2;
        boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        int count = 0;
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) {
                Log.d(TAG, "onLayout gone : " + record.tile.getClass().getSimpleName());
                continue;
            }
            count ++;
            if (count > 8) {
                // layout out of the view
                record.tileView.layout(0, 0, 0, 0);
                continue;
            }
            
            final int cols = getColumnCount(record.row);
            final int cw = mCellWidth;
            final int extra = (w - cw * cols) / (cols - 1);
            int left = record.col * cw + record.col * extra + mPanelPaddingLeft;
            final int top = getRowTop(record.row);
            int right;
            int tileWith = record.tileView.getMeasuredWidth();
            if (isRtl) {
                right = w - left;
                left = right - tileWith;
            } else {
                right = left + tileWith;
            }
            record.tileView.layout(left, top, right, top + record.tileView.getMeasuredHeight());
        }
    }

    private void layoutBrightnessSlider() {
        mBrightnessSlider.layout(mBrightnessTilePaddingLeft, getBrightnessViewTop(),
                mBrightnessTilePaddingLeft + mPanelPaddingLeft + mBrightnessSlider.getMeasuredWidth(), 
                getBrightnessViewTop() + mBrightnessSlider.getMeasuredHeight());
    }

    private int getRowTop(int row) {
        if (row <= 0) {
            return mPanelPaddingTop;
        } else if (row > 2) {
            row = 2;
        }
        
        return row * mCellHeight + mCellPaddingTop + mPanelPaddingTop;
    }
    
    private int getBrightnessViewTop() {
        return (mCellHeight) * 2 + mCellPaddingTop + mBrightnessPaddingTop + mPanelPaddingTop;
    }

    private int getColumnCount(int row) {
        /*int cols = 0;
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) continue;
            if (record.row == row) cols++;
        }
        return cols;*/
        return mColumns;
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

    private static final class TileRecord {
        GnQSTile<?> tile;
        GnQSTileView tileView;
        int row;
        int col;
    }

}
