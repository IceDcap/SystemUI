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
import com.android.systemui.gionee.cc.GnControlCenterPanel;
import com.android.systemui.gionee.cc.GnQSTileHost;
import com.android.systemui.gionee.cc.qs.brightness.GnBrightnessController;
import com.android.systemui.gionee.cc.qs.brightness.GnToggleSlider;
import com.android.systemui.gionee.cc.qs.tiles.GnSettingTile;

import java.util.ArrayList;
import java.util.Collection;

/** View that represents the quick settings tile panel. **/
public class GnQSPanel extends ViewGroup {
    
    private final String TAG = "GnQSPanel";
    
    private static final int QS_COLUMN_COUNT = 5;

    private final Context mContext;
    private final ArrayList<TileRecord> mRecords = new ArrayList<TileRecord>();
    private final View mBrightnessSlider;
    private final H mHandler = new H();

    private int mColumns;
    private int mCellWidth;
    private int mCellHeight;
    private int mCellPaddingTop;
    private int mBrightnessPaddingTop;
    private int mBrightnessPaddingBottom;
    private int mPanelPadding;
    private boolean mListening;

    private GnControlCenterPanel mGnControlCenterPanel;
    private GnBrightnessController mBrightnessController;
    private GnQSTileHost mHost;
    
    private TileRecord mSettingTileRecord;

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
        setPadding(mPanelPadding, 0, mPanelPadding, 0);
    }

    private void updateDimens(Configuration newConfig) {
        Resources res = mContext.getResources();
        
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mCellPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_tile_padding_top_land);
            mPanelPadding = res.getDimensionPixelSize(R.dimen.gn_qs_panel_padding_left_land);
            mBrightnessPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_padding_top_land);
            mBrightnessPaddingBottom = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_padding_bottom_land);
        } else {
            mCellPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_tile_padding_top);
            mPanelPadding = res.getDimensionPixelSize(R.dimen.gn_qs_panel_padding_left);
            mBrightnessPaddingTop = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_padding_top);
            mBrightnessPaddingBottom = res.getDimensionPixelSize(R.dimen.gn_qs_brightness_padding_bottom);
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
        Log.d(TAG, "addTileView   name = " + tile.getClass().getSimpleName() 
                + "  visible = " + tile.mState.visible);
        
        final TileRecord r = new TileRecord();
        r.tile = tile;
        r.tileView = tile.createTileView(mContext, mGnControlCenterPanel);
        r.tileView.setVisibility(tile.mState.visible ? View.VISIBLE : View.GONE);

        final GnQSTile.Callback callback = new GnQSTile.Callback() {
            @Override
            public void onStateChanged(GnQSTile.BooleanState state) {
                Log.d(TAG, "onStateChanged  name = " + r.tile.getClass().getSimpleName() 
                        + " visible = " + state.visible);
                int visibility = state.visible ? VISIBLE : GONE;
                setTileVisibility(r.tileView, visibility);
                r.tileView.onStateChanged(state);
            }

            @Override
            public void onAnnouncementRequested(CharSequence announcement) {
                announceForAccessibility(announcement);
            }
        };
        r.tile.setCallback(callback);
        
        final View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r.tile.click();
            }
        };
        
        final View.OnClickListener clickSecondary = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r.tile.secondaryClick();
            }
        };
        
        final View.OnLongClickListener longClick = new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                r.tile.handleLongClick();
                return true;
            }
        };
        
        r.tileView.init(click, clickSecondary, longClick);
        r.tile.setListening(mListening);
        callback.onStateChanged(r.tile.getState());
        r.tile.refreshState();
        addView(r.tileView);
        return r;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        mBrightnessSlider.measure(exactly(width - mCellWidth - mPanelPadding * 2), MeasureSpec.UNSPECIFIED);
        mSettingTileRecord.tileView.measure(exactly(mCellWidth), exactly(mCellWidth));
        mSettingTileRecord.tileView.setDual(mSettingTileRecord.tile.supportsDualTargets());

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
        setMeasuredDimension(width, h + brightnessHeight + mBrightnessPaddingTop + mBrightnessPaddingBottom);
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutBrightnessSlider();
        layoutSettingView();

        final int w = getWidth() - mPanelPadding * 2;
        boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        int count = 0;
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) continue;
            count ++;
            if (count > 10) {
                // layout out of the view
                record.tileView.layout(0, 0, 0, 0);
                continue;
            }
            
            final int cols = getColumnCount(record.row);
            final int cw = mCellWidth;
            final int extra = (w - cw * cols) / (cols - 1);
            int left = record.col * cw + record.col * extra + mPanelPadding;
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
        mBrightnessSlider.layout(mPanelPadding, getBrightnessViewTop(), mPanelPadding + mBrightnessSlider.getMeasuredWidth(),
                getBrightnessViewTop() + mBrightnessSlider.getMeasuredHeight());
    }

    private void layoutSettingView() {
        int left = mBrightnessSlider.getMeasuredWidth() + mPanelPadding;
        int top = getBrightnessViewTop();
        int right = left + mSettingTileRecord.tileView.getMeasuredWidth();
        int bottom = top + mSettingTileRecord.tileView.getMeasuredHeight();
        
        mSettingTileRecord.tileView.layout(left, top, right, bottom);
    }

    private int getRowTop(int row) {
        if (row <= 0) {
            return 0;
        } else if (row > 2) {
            row = 2;
        }
        
        return row * mCellHeight + mCellPaddingTop;
    }
    
    private int getBrightnessViewTop() {
        return (mCellHeight) * 2 + mCellPaddingTop + mBrightnessPaddingTop;
    }

    private int getColumnCount(int row) {
        /*int cols = 0;
        for (TileRecord record : mRecords) {
            if (record.tileView.getVisibility() == GONE) continue;
            if (record.row == row) cols++;
        }
        return cols;*/
        return QS_COLUMN_COUNT;
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

    public void addSettingTile() {
        mSettingTileRecord = addTileView(new GnSettingTile(mHost));
    }

    public void setControlCenterPanel(GnControlCenterPanel panel) {
        mGnControlCenterPanel = panel;
    }
}
