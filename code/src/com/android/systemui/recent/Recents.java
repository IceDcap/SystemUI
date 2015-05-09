/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recent;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import com.android.systemui.R;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.AlternateRecentsComponent;


public class Recents extends SystemUI implements RecentsComponent {
    private static final String TAG = "Recents";
    private static final boolean DEBUG = true;

    // Which recents to use
    boolean mUseAlternateRecents = false;//true;
    boolean mBootCompleted = false;
    static AlternateRecentsComponent sAlternateRecents;

    /** Returns the Recents component, creating a new one in-process if necessary. */
    public static AlternateRecentsComponent getRecentsComponent(Context context,
            boolean forceInitialize) {
        if (sAlternateRecents == null) {
            sAlternateRecents = new AlternateRecentsComponent(context);
            if (forceInitialize) {
                sAlternateRecents.onStart();
                sAlternateRecents.onBootCompleted();
            }
        }
        return sAlternateRecents;
    }

    @Override
    public void start() {
        if (mUseAlternateRecents) {
            if (sAlternateRecents == null) {
                sAlternateRecents = getRecentsComponent(mContext, false);
            }
            sAlternateRecents.onStart();
        }

        putComponent(RecentsComponent.class, this);
    }

    @Override
    protected void onBootCompleted() {
        if (mUseAlternateRecents) {
            if (sAlternateRecents != null) {
                sAlternateRecents.onBootCompleted();
            }
        }
        mBootCompleted = true;
    }

    @Override
    public void showRecents(boolean triggeredFromAltTab, View statusBarView) {
        if (mUseAlternateRecents) {
            sAlternateRecents.onShowRecents(triggeredFromAltTab);
        }
    }

    @Override
    public void hideRecents(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        if (mUseAlternateRecents) {
            sAlternateRecents.onHideRecents(triggeredFromAltTab, triggeredFromHomeKey);
        } else {
            Intent intent = new Intent(RecentsActivity.CLOSE_RECENTS_INTENT);
            intent.setPackage("com.android.systemui");
            sendBroadcastSafely(intent);

            RecentTasksLoader.getInstance(mContext).cancelPreloadingFirstTask();
        }
    }

    @Override
    public void toggleRecents(Display display, int layoutDirection, View statusBarView) {
        if (mUseAlternateRecents) {
            // Launch the alternate recents if required
            sAlternateRecents.onToggleRecents();
            return;
        }

        if (DEBUG) Log.d(TAG, "toggle recents panel");
        try {
            TaskDescription firstTask = RecentTasksLoader.getInstance(mContext).getFirstTask();

            Intent intent = new Intent(RecentsActivity.TOGGLE_RECENTS_INTENT);
            intent.setClassName("com.android.systemui",
                    "com.android.systemui.recent.RecentsActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            if (firstTask == null) {
                startRecentActivityDiractly(intent);
            } else {
                startRecentActivityByAnim(display, statusBarView, firstTask, intent);
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to launch RecentAppsIntent", e);
        }
    }

    private void startRecentActivityDiractly(Intent intent) {
        if (RecentsActivity.forceOpaqueBackground(mContext)) {
            ActivityOptions opts = ActivityOptions.makeCustomAnimation(mContext,
                    R.anim.recents_launch_from_launcher_enter,
                    R.anim.recents_launch_from_launcher_exit);
            mContext.startActivityAsUser(intent, opts.toBundle(), new UserHandle(
                    UserHandle.USER_CURRENT));
        } else {
            // The correct window animation will be applied via the activity's style
            mContext.startActivityAsUser(intent, new UserHandle(
                    UserHandle.USER_CURRENT));
        }
    }

    private void startRecentActivityByAnim(Display display, View statusBarView,
            TaskDescription firstTask, Intent intent) {
        Log.d(TAG, "go to recents panel from = " + firstTask.packageName);
        Bitmap first = null;
        if (firstTask.getThumbnail() instanceof BitmapDrawable) {
            first = ((BitmapDrawable) firstTask.getThumbnail()).getBitmap();
        } else {
            first = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Drawable d = RecentTasksLoader.getInstance(mContext).getDefaultThumbnail();
            d.draw(new Canvas(first));
        }
        
        final Resources res = mContext.getResources();
        float thumbWidth = res
                .getDimensionPixelSize(com.android.internal.R.dimen.thumbnail_width);
        float thumbHeight = res
                .getDimensionPixelSize(com.android.internal.R.dimen.thumbnail_height);
        if (first == null) {
            throw new RuntimeException("Recents thumbnail is null");
        }
        if (first.getWidth() != thumbWidth || first.getHeight() != thumbHeight) {
            first = Bitmap.createScaledBitmap(first, (int) thumbWidth, (int) thumbHeight,
                    true);
            if (first == null) {
                throw new RuntimeException("Recents thumbnail is null");
            }
        }

        int x, y;
        int statusBarHeight = res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
        int thumbTopMargin = res.getDimensionPixelSize(R.dimen.gn_recent_thumbnail_margin_top);
        int thumbPaddingLeft = res.getDimensionPixelSize(R.dimen.gn_recent_thumbnail_padding_left);
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);

        int count = RecentTasksLoader.getInstance(mContext).getTasksCount();
        if (count > 1) {
            x = (int) ((int) ((dm.widthPixels - thumbWidth) / 2f) - thumbWidth - thumbPaddingLeft * 2);
        } else {
            x = (int) ((dm.widthPixels - thumbWidth) / 2f);
        }
        y = thumbTopMargin + statusBarHeight;
        
        ActivityOptions opts = ActivityOptions.makeThumbnailScaleDownAnimation(
                statusBarView,
                first, x, y,
                new ActivityOptions.OnAnimationStartedListener() {
                    public void onAnimationStarted() {
                        Intent intent =
                                new Intent(RecentsActivity.WINDOW_ANIMATION_START_INTENT);
                        intent.setPackage("com.android.systemui");
                        sendBroadcastSafely(intent);
                    }
                });
        intent.putExtra(RecentsActivity.WAITING_FOR_WINDOW_ANIMATION_PARAM, true);
        startActivitySafely(intent, opts.toBundle());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (mUseAlternateRecents) {
            sAlternateRecents.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void preloadRecents() {
        if (mUseAlternateRecents) {
            sAlternateRecents.onPreloadRecents();
        } else {
            Intent intent = new Intent(RecentsActivity.PRELOAD_INTENT);
            intent.setClassName("com.android.systemui",
                    "com.android.systemui.recent.RecentsPreloadReceiver");
            sendBroadcastSafely(intent);

            RecentTasksLoader.getInstance(mContext).preloadFirstTask();
        }
    }

    @Override
    public void cancelPreloadingRecents() {
        if (mUseAlternateRecents) {
            sAlternateRecents.onCancelPreloadingRecents();
        } else {
            Intent intent = new Intent(RecentsActivity.CANCEL_PRELOAD_INTENT);
            intent.setClassName("com.android.systemui",
                    "com.android.systemui.recent.RecentsPreloadReceiver");
            sendBroadcastSafely(intent);

            RecentTasksLoader.getInstance(mContext).cancelPreloadingFirstTask();
        }
    }

    @Override
    public void showNextAffiliatedTask() {
        if (mUseAlternateRecents) {
            sAlternateRecents.onShowNextAffiliatedTask();
        }
    }

    @Override
    public void showPrevAffiliatedTask() {
        if (mUseAlternateRecents) {
            sAlternateRecents.onShowPrevAffiliatedTask();
        }
    }

    @Override
    public void setCallback(Callbacks cb) {
        if (mUseAlternateRecents) {
            sAlternateRecents.setRecentsComponentCallback(cb);
        }
    }

    /**
     * Send broadcast only if BOOT_COMPLETED
     */
    private void sendBroadcastSafely(Intent intent) {
        if (!mBootCompleted) return;
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
    }

    /**
     * Start activity only if BOOT_COMPLETED
     */
    private void startActivitySafely(Intent intent, Bundle opts) {
        if (!mBootCompleted) return;
        mContext.startActivityAsUser(intent, opts, new UserHandle(UserHandle.USER_CURRENT));
    }
}
