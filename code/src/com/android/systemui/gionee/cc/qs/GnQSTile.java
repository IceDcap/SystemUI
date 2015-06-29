/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.android.systemui.gionee.cc.GnControlCenterPanel.OnDrawerCloseListener;
import com.android.systemui.gionee.cc.qs.GnQSTile.State;
import com.android.systemui.gionee.cc.qs.policy.GnBluetoothController;
import com.android.systemui.gionee.cc.qs.policy.GnLocationController;
import com.android.systemui.gionee.cc.qs.policy.GnMobileDataController;
import com.android.systemui.gionee.cc.qs.policy.GnNextAlarmController;
import com.android.systemui.gionee.cc.qs.policy.GnRotationLockController;
import com.android.systemui.gionee.cc.qs.policy.GnWifiController;
import com.android.systemui.statusbar.policy.Listenable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Base quick-settings tile, extend this to create a new tile.
 *
 * State management done on a looper provided by the host.  Tiles should update state in
 * handleUpdateState.  Callbacks affecting state should use refreshState to trigger another
 * state update pass on tile looper.
 */
public abstract class GnQSTile<TState extends State> implements Listenable {
    protected final String TAG = "GnQSTile." + getClass().getSimpleName();
    protected static final boolean DEBUG = true;//Log.isLoggable("GnQSTile", Log.DEBUG);
    
    public static final int STATE_TYPE_BOOLEAN = 0;
    public static final int STATE_TYPE_ANIMBOOLEAN = 1;
    public static final int STATE_TYPE_SIGNAL = 2;

    protected final Host mHost;
    protected final Context mContext;
    protected final H mHandler;
    protected final Handler mUiHandler = new Handler(Looper.getMainLooper());
    protected final String mSpec;

    private ArrayList<Callback> mCallback = new ArrayList<Callback>();
    public final TState mState = newTileState();
    private final TState mTmpState = newTileState();
    private boolean mAnnounceNextStateChange;

    abstract protected TState newTileState();
    abstract protected void handleClick();
    abstract protected void handleLongClick();
    abstract protected void handleUpdateState(TState state, Object arg);
    

    protected GnQSTile(Host host, String spec) {
        mHost = host;
        mSpec = spec;
        mContext = host.getContext();
        mHandler = new H(host.getLooper());
    }

    public boolean supportsDualTargets() {
        return false;
    }
    
    public int supportsStateType() {
        return STATE_TYPE_BOOLEAN;
    }
    
    public boolean supportsLongClick() {
        return false;
    }

    public Host getHost() {
        return mHost;
    }
    
    public String getSpec() {
        return mSpec;
    }
    
    public GnQSTileView createTileView(Context context, int type) {
        switch (type) {
            case STATE_TYPE_ANIMBOOLEAN:                
                return new GnQSAnimTileView(context);
            case STATE_TYPE_BOOLEAN:
                return new GnQSBoolTileView(context);
            default:
                return new GnQSBoolTileView(context);
        }
    }
    
    public void setCallback(Callback callback) {
        mHandler.obtainMessage(H.SET_CALLBACK, callback).sendToTarget();
    }
    
    public void click() {
        mHandler.sendEmptyMessage(H.CLICK);
    }

    public void secondaryClick() {
        mHandler.sendEmptyMessage(H.SECONDARY_CLICK);
    }

    public void showDetail(boolean show) {
        mHandler.obtainMessage(H.SHOW_DETAIL, show ? 1 : 0, 0).sendToTarget();
    }

    public final void refreshState() {
        refreshState(null);
    }

    protected final void refreshState(Object arg) {
        mHandler.obtainMessage(H.REFRESH_STATE, arg).sendToTarget();
    }

    public void userSwitch(int newUserId) {
        mHandler.obtainMessage(H.USER_SWITCH, newUserId, 0).sendToTarget();
    }

    public void fireToggleStateChanged(boolean state) {
        mHandler.obtainMessage(H.TOGGLE_STATE_CHANGED, state ? 1 : 0, 0).sendToTarget();
    }

    public void fireScanStateChanged(boolean state) {
        mHandler.obtainMessage(H.SCAN_STATE_CHANGED, state ? 1 : 0, 0).sendToTarget();
    }

    public void destroy() {
        mHandler.sendEmptyMessage(H.DESTROY);
    }

    public TState getState() {
        return mState;
    }

    // call only on tile worker looper
    private void handleSetCallback(Callback callback) {
        mCallback.add(callback);
        handleRefreshState(null);
    }
    
    protected void handleSecondaryClick() {
        // optional
    }

    protected void handleRefreshState(Object arg) {
        handleUpdateState(mTmpState, arg);
        final boolean changed = mTmpState.copyTo(mState);
        if (changed) {
            handleStateChanged();
        }
    }

    private void handleStateChanged() {
        for (Callback callback : mCallback) {
            callback.onStateChanged(mState);
        }
                
        boolean delayAnnouncement = shouldAnnouncementBeDelayed();
        mAnnounceNextStateChange = mAnnounceNextStateChange && delayAnnouncement;
    }

    protected boolean shouldAnnouncementBeDelayed() {
        return false;
    }

    protected String composeChangeAnnouncement() {
        return null;
    }

    protected void handleUserSwitch(int newUserId) {
        handleRefreshState(null);
    }

    protected void handleDestroy() {
        setListening(false);
        mCallback.clear();
    }

    protected final class H extends Handler {
        private static final int SET_CALLBACK = 1;
        private static final int CLICK = 2;
        private static final int LONG_CLICK = 3;
        private static final int SECONDARY_CLICK = 4;
        private static final int REFRESH_STATE = 5;
        private static final int SHOW_DETAIL = 6;
        private static final int USER_SWITCH = 7;
        private static final int TOGGLE_STATE_CHANGED = 8;
        private static final int SCAN_STATE_CHANGED = 9;
        private static final int DESTROY = 10;

        private H(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String name = null;
            try {
                if (msg.what == SET_CALLBACK) {
                    name = "handleSetCallback";
                    handleSetCallback((GnQSTile.Callback)msg.obj);
                } else if (msg.what == CLICK) {
                    name = "handleClick";
                    mAnnounceNextStateChange = true;
                    handleClick();
                } else if (msg.what == LONG_CLICK) {
                    name = "handleLongClick";
                    handleLongClick();
                } else if (msg.what == SECONDARY_CLICK) {
                    name = "handleSecondaryClick";
                    handleSecondaryClick();
                } else if (msg.what == REFRESH_STATE) {
                    name = "handleRefreshState";
                    handleRefreshState(msg.obj);
                } else if (msg.what == USER_SWITCH) {
                    name = "handleUserSwitch";
                    handleUserSwitch(msg.arg1);
                } else if (msg.what == DESTROY) {
                    name = "handleDestroy";
                    handleDestroy();
                } else {
                    throw new IllegalArgumentException("Unknown msg: " + msg.what);
                }
            } catch (Throwable t) {
                final String error = "Error in " + name;
                Log.w(TAG, error, t);
                mHost.warn(error, t);
            }
        }
    }

    public interface Callback {
        void onStateChanged(State state);
    }
    
    public interface Host {
        void startSettingsActivity(Intent intent);
        void startSettingsActivity(Intent intent, int delay);
        void warn(String message, Throwable t);
        void collapsePanels();
        Looper getLooper();
        Context getContext();
        Collection<GnQSTile<?>> getTiles();
        void setCallback(Callback callback);
        GnBluetoothController getBluetoothController();
        GnLocationController getLocationController();
        GnRotationLockController getRotationLockController();
        GnWifiController getGnWifiController();
        GnMobileDataController getGnMobileDataController();
        GnNextAlarmController getGnNextAlarmController();

        public interface Callback {
            void onTilesChanged();
        }

    }

    public static class State {
        public boolean visible;
        public int iconId;
        public Drawable icon;
        public String label;
        public String contentDescription;
        public String dualLabelContentDescription;
        public boolean autoMirrorDrawable = true;
        public boolean clickable = true;

        public boolean copyTo(State other) {
            if (other == null) throw new IllegalArgumentException();
            if (!other.getClass().equals(getClass())) throw new IllegalArgumentException();
            final boolean changed = other.visible != visible
                    || other.clickable != clickable
                    || other.iconId != iconId
                    || !Objects.equals(other.icon, icon)
                    || !Objects.equals(other.label, label)
                    || !Objects.equals(other.contentDescription, contentDescription)
                    || !Objects.equals(other.autoMirrorDrawable, autoMirrorDrawable)
                    || !Objects.equals(other.dualLabelContentDescription,
                    dualLabelContentDescription);
            other.visible = visible;
            other.clickable = clickable;
            other.iconId = iconId;
            other.icon = icon;
            other.label = label;
            other.contentDescription = contentDescription;
            other.dualLabelContentDescription = dualLabelContentDescription;
            other.autoMirrorDrawable = autoMirrorDrawable;
            return changed;
        }

        @Override
        public String toString() {
            return toStringBuilder().toString();
        }

        protected StringBuilder toStringBuilder() {
            final StringBuilder sb = new StringBuilder(  getClass().getSimpleName()).append('[');
            sb.append("visible=").append(visible);
            sb.append(",iconId=").append(iconId);
            sb.append(",icon=").append(icon);
            sb.append(",label=").append(label);
            sb.append(",contentDescription=").append(contentDescription);
            sb.append(",dualLabelContentDescription=").append(dualLabelContentDescription);
            sb.append(",autoMirrorDrawable=").append(autoMirrorDrawable);
            return sb.append(']');
        }
    }

    public static class BooleanState extends State {
        public boolean value;

        @Override
        public boolean copyTo(State other) {
            final BooleanState o = (BooleanState) other;
            final boolean changed = super.copyTo(other) || o.value != value;
            o.value = value;
            return changed;
        }

        @Override
        protected StringBuilder toStringBuilder() {
            final StringBuilder rt = super.toStringBuilder();
            rt.insert(rt.length() - 1, ",value=" + value);
            return rt;
        }
    }
    
    public static class AnimBooleanState extends State {
        public boolean value;
        public boolean animating;

        @Override
        public boolean copyTo(State other) {
            final AnimBooleanState o = (AnimBooleanState) other;
            final boolean changed = super.copyTo(other) || o.value != value
                    || o.animating != animating;
            o.value = value;
            o.animating = animating;
            return changed;
        }

        @Override
        protected StringBuilder toStringBuilder() {
            final StringBuilder rt = super.toStringBuilder();
            rt.insert(rt.length() - 1, ",value=" + value);
            rt.insert(rt.length() - 1, ",animating=" + animating);
            return rt;
        }
    }

    public static final class SignalState extends State {
        public boolean enabled;
        public boolean connected;
        public boolean activityIn;
        public boolean activityOut;
        public int overlayIconId;
        public boolean filter;
        public boolean isOverlayIconWide;

        @Override
        public boolean copyTo(State other) {
            final SignalState o = (SignalState) other;
            final boolean changed = o.enabled != enabled
                    || o.connected != connected || o.activityIn != activityIn
                    || o.activityOut != activityOut
                    || o.overlayIconId != overlayIconId
                    || o.isOverlayIconWide != isOverlayIconWide;
            o.enabled = enabled;
            o.connected = connected;
            o.activityIn = activityIn;
            o.activityOut = activityOut;
            o.overlayIconId = overlayIconId;
            o.filter = filter;
            o.isOverlayIconWide = isOverlayIconWide;
            return super.copyTo(other) || changed;
        }

        @Override
        protected StringBuilder toStringBuilder() {
            final StringBuilder rt = super.toStringBuilder();
            rt.insert(rt.length() - 1, ",enabled=" + enabled);
            rt.insert(rt.length() - 1, ",connected=" + connected);
            rt.insert(rt.length() - 1, ",activityIn=" + activityIn);
            rt.insert(rt.length() - 1, ",activityOut=" + activityOut);
            rt.insert(rt.length() - 1, ",overlayIconId=" + overlayIconId);
            rt.insert(rt.length() - 1, ",filter=" + filter);
            rt.insert(rt.length() - 1, ",wideOverlayIcon=" + isOverlayIconWide);
            return rt;
        }
    }
}
