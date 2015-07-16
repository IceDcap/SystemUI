/*
*
* MODULE DESCRIPTION
* add by huangwt for Android L at 20141210.
* 
*/
package com.android.systemui.gionee.cc.qs.tiles;
//add by y3_gionee hangh for GNNCR00010562 start
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
//add by y3_gionee hangh for GNNCR00010562 end
import com.android.systemui.gionee.cc.qs.GnQSTile;
import com.android.systemui.screenshot.GnSnapshotService;
import com.android.systemui.R;
import com.android.systemui.gionee.GnYouJu;
//add by y3_gionee hangh for GNNCR00010562 start
import android.view.WindowManagerGlobal;
import android.view.WindowManager.LayoutParams;
import amigo.app.AmigoAlertDialog;
//add by y3_gionee hangh for GNNCR00010562 end
public class GnScreenShotTile extends GnQSTile<GnQSTile.BooleanState> {

    private static final Intent GNSCREEN_SHOT = new Intent("gn.intent.action.SELECT_SHOT");
    //add by y3_gionee hangh for GNNCR00010562 start
    private AmigoAlertDialog mScreenShotAlertDialog = null;   
    private static final int SCREENSHOT_SWITCH_DIALOG_LONG_TIMEOUT = 4000;
    private static ServiceConnection mScreenShotServiceConn;
    private final Object mScreenshotLock = new Object();
    private boolean mListening;
    ////add by y3_gionee hangh for GNNCR00010562 end
    private boolean mLongScreenShotSupport = false;    
    public GnScreenShotTile(Host host, String spec) {
        super(host, spec);
        mLongScreenShotSupport = supportLongScreenShot();
    }

    @Override
	public void setListening(boolean listening) {
//add by y3_gionee hangh for CR01490367 start
    	if (mListening == listening) return;
        mListening = listening;
        if (listening) {
        	final IntentFilter filter = new IntentFilter();
    		filter.addAction(Intent.ACTION_SCREEN_OFF);
    		mContext.registerReceiver(mReceiver, filter);
        }else {
            mContext.unregisterReceiver(mReceiver);
        }
//add by y3_gionee hangh for CR01490367 end		
	}

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
	protected void handleClick() {
		// modify by y3_gionee hangh for GNNCR00010562 start
		if (mLongScreenShotSupport) {
			showScreenShotSwitchDialog();
		} else {
			GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnScreenShotTile");
			mHost.collapsePanels();

			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					GnSnapshotService.getService(mContext).takeScreenShot();
					mHost.startSettingsActivity(GNSCREEN_SHOT);
				}
			}, 500);
		}
		// modify by y3_gionee hangh for GNNCR00010562 end
		// mHost.startSettingsActivity(GNSCREEN_SHOT, 500);
	}
//add by y3_gionee hangh for GNNCR00010562 start
	private void showScreenShotSwitchDialog() {
		
		AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(mContext,
                R.style.GnScreenShotAlertDialogDark);
        View view = LayoutInflater.from(mContext).inflate(R.layout.quick_settings_screenshot_switch_dialog, null);
        builder.setView(view);
        if(mScreenShotAlertDialog != null){
        	mScreenShotAlertDialog.dismiss();
        }else{
        	 mScreenShotAlertDialog = builder.create();
        }
        mScreenShotAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
        mScreenShotAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        if (!mScreenShotAlertDialog.isShowing()) {
			try {
				WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
			} catch (RemoteException e) {
			}
			mHost.collapsePanels();
			mScreenShotAlertDialog.show();
			//dismissScreenShotSwitchDialog(SCREENSHOT_SWITCH_DIALOG_LONG_TIMEOUT);
		}
        
		View normalScreenshot = (View) mScreenShotAlertDialog.findViewById(R.id.normal_screenshot);
		View longScreenshot = (View) mScreenShotAlertDialog.findViewById(R.id.long_screenshot);
		View partScreenshot = (View) mScreenShotAlertDialog.findViewById(R.id.part_screenshot);
		TextView normalScreenshotText = (TextView) normalScreenshot.findViewById(R.id.normal_screenshot_text);
		normalScreenshotText.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelSize(R.dimen.gn_qs_tile_text_size));
		normalScreenshotText.setText(mContext.getString(R.string.gn_qs_normal_screen_shot));
		TextView longScreenshotText = (TextView) longScreenshot.findViewById(R.id.long_screenshot_text);
		longScreenshotText.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelSize(R.dimen.gn_qs_tile_text_size));
		longScreenshotText.setText(mContext.getString(R.string.gn_qs_long_screen_shot));
		TextView partScreenshotText = (TextView) partScreenshot.findViewById(R.id.part_screenshot_text);
		partScreenshotText.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelSize(R.dimen.gn_qs_tile_text_size));
		partScreenshotText.setText(mContext.getString(R.string.gn_qs_part_screen_shot));
		normalScreenshot.setOnClickListener(mScreenShotSwitchListener);
		longScreenshot.setOnClickListener(mScreenShotSwitchListener);
		partScreenshot.setOnClickListener(mScreenShotSwitchListener);
		
	}

	private View.OnClickListener mScreenShotSwitchListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.normal_screenshot:
				normalScreenShot();
				break;
			case R.id.long_screenshot:
				longScreenShot();
				break;
			case R.id.part_screenshot:
				partScreenShot();
				break;

			default:
				break;
			}
			if (mScreenShotAlertDialog != null) {
				mScreenShotAlertDialog.dismiss();
			}
		}
	};

	private void normalScreenShot() {
		mHandler.postDelayed(mNormalScreenshotRunnable, 200);
	}
	private void longScreenShot(){
		Intent intent = new Intent();
		intent.setClassName("com.gionee.longscreenshot", "com.gionee.longscreenshot.ScreenShotService");
		mContext.startService(intent);
	}

	private final Runnable mNormalScreenshotRunnable = new Runnable() {
		@Override
		public void run() {
			takeScreenshot();
		}
	};

	private void takeScreenshot() {
		ComponentName cn = new ComponentName("com.android.systemui",
				"com.android.systemui.screenshot.TakeScreenshotService");
		Intent intent = new Intent();
		intent.setComponent(cn);
		mScreenShotServiceConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				synchronized (mScreenshotLock) {

					Messenger messenger = new Messenger(service);
					Message msg = Message.obtain(null, 1);
					final ServiceConnection myConn = this;
					Handler h = new Handler(mHandler.getLooper()) {
						@Override
						public void handleMessage(Message msg) {
							synchronized (mScreenshotLock) {
								mContext.unbindService(mScreenShotServiceConn);
								mScreenShotServiceConn = null;
							}
						}
					};
					msg.replyTo = new Messenger(h);
					msg.arg1 = msg.arg2 = 0;
					try {
						messenger.send(msg);
					} catch (RemoteException e) {
					}
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
		};
		mContext.bindService(intent, mScreenShotServiceConn,
				Context.BIND_AUTO_CREATE);
	}

	private void partScreenShot() {
		GnYouJu.onEvent(mContext, "Amigo_SystemUI_CC", "GnScreenShotTile");
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				GnSnapshotService.getService(mContext).takeScreenShot();
				mHost.startSettingsActivity(GNSCREEN_SHOT);
			}
		}, 500);
	}

//add by y3_gionee hangh for GNNCR00010562 end
    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = false;
        state.visible = true;
        if (mLongScreenShotSupport){
        	state.label = mContext.getString(R.string.gn_qs_super_screen_shot);
        	state.contentDescription = mContext.getString(R.string.gn_qs_super_screen_shot);
        	} else{
        	state.label = mContext.getString(R.string.gn_qs_screen_shot);
        	state.contentDescription = mContext.getString(R.string.gn_qs_screen_shot);
        	}
        state.iconId = R.drawable.gn_ic_qs_screenshot;

    }
    
    private boolean supportLongScreenShot() {
		try {
			PackageManager pm = mContext.getPackageManager();
			ApplicationInfo info = pm.getApplicationInfo("com.gionee.longscreenshot", 0);
			return info.enabled;
		} catch (NameNotFoundException e) {
			Log.d(TAG, "not supportLongScreenShot");
			e.printStackTrace();
			return false;
		}
	}

	//add by y3_gionee hangh for CR01490367 start    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	//AlertDialog alertDialog = null;
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                if(mScreenShotAlertDialog != null && mScreenShotAlertDialog.isShowing()){
                	mScreenShotAlertDialog.dismiss();
                }
            }
        }
    };
 //add by y3_gionee hangh for CR01490367 end   
}
