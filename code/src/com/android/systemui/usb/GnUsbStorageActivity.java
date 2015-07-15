package com.android.systemui.usb;

import java.util.List;

import amigo.app.AmigoActivity;
import amigo.app.AmigoAlertDialog;
import amigo.app.AmigoProgressDialog;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.StorageEventListener;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import java.util.List;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.hardware.usb.UsbManager;
import android.widget.ImageView;
import amigo.widget.AmigoSwitch;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;
import android.os.SystemProperties;
//Gionee <zengxh> <2013-12-09> add for CR00972244 begin
import android.telephony.TelephonyManager;
//Gionee <zengxh> <2013-12-09> add for CR00972244 end
import com.android.systemui.R;

import amigo.changecolors.ChameleonColorManager;

public class GnUsbStorageActivity extends AmigoActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, DialogInterface.OnDismissListener {

    private static final String TAG = "GnUsbStorageActivity";
    private static final int CONTENT = 1;
    private static final int DLG_USB_STORAGE_OPENING = 2;
    private static final int DLG_ERROR_SHARING = 3;
    private static final int DLG_ADB_WARN = 4;
    private static final int DLG_NO_SDCARD = 5;

    private AmigoSwitch mPtpCheck;
    private AmigoSwitch mDebugCheck;
    private AmigoSwitch mUsbCheck;
    private ImageView mChargeButton;
    private ImageView mPtpButton;
    private ImageView mUsbButton;
    private View mChargeView;
    private View mPtpView;
    private View mUsbView;
    private View mDebugView;
    private View mMtpHelpView;
    private ImageView mDiverView;
    private boolean mIsPtpOpened = false;
    private boolean mIsChargeOpened = false;
    private boolean mDebugState;
    private boolean mUsbStorageOpened = false;
    private boolean mDestroyed;
    private boolean mHasCheck = false;
    private boolean mSettingUMS = false;
    private boolean mIsUMSSwitching = false;
    private int mAllowedShareNum = 0;
    TelephonyManager mTelephonyManager;
    private StorageManager mStorageManager = null;
    private UsbManager mUsbManager;

    static final boolean localLOGV = true;
    
    private static final int UPDATE_CHARGE_BUTTON = 0;
    // New request to add MTP help page
    private static final int UPDATE_MTP_HELP_VIEW = 1;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CHARGE_BUTTON:
                    final String currentFunction = getCurrentFunction();
                    if (mChargeButton != null) {
                        if (!mDebugState && !mSettingUMS && !mIsPtpOpened
                                && UsbManager.USB_FUNCTION_MASS_STORAGE.equals(currentFunction)) {
                            mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_sel_unpress);
                            mIsChargeOpened = true;
                        } else {
                            mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_unpress);
                            mIsChargeOpened = false;
                        }
                    }
                    break;
                // New request to add MTP help page
                case UPDATE_MTP_HELP_VIEW:
                	updateMtpView();
                	break;
                default:
                    break;
            }
        };
    };
    
    void updateMtpView() {
        if (mIsMtpOpened == true) {
        	mMtpHelpView.setVisibility(View.VISIBLE);
        } else {
        	mMtpHelpView.setVisibility(View.GONE);
        }
    }

    // UI thread
    private Handler mUIHandler;

    // thread for working with the storage services, which can be slow
    private Handler mAsyncStorageHandler;

    /** Used to detect when the USB cable is unplugged, so we can call finish() */
    private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_STATE)) {
                handleUsbStateChanged(intent);
            }
        }
    };

    private StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            final boolean on = newState.equals(Environment.MEDIA_SHARED)
            		// GIONEE <wujj> <2015-07-15> add for CR01516872 begin
            		&& isUsbCDRomSupport();
            		//GIONEE <wujj> <2015-07-15> add for CR01516872 end
            Log.d(TAG, "onStorageStateChanged - on: " + on + ", mSettingUMS: " + mSettingUMS + ", path: "
                    + path + ", oldState: " + oldState + ", newState: " + newState);

            if (mSettingUMS) {
                StorageVolume[] volumes = mStorageManager.getVolumeList();
                Log.d(TAG, "onStorageStateChanged - [UMS Enabled] volumes.length: " + volumes.length
                        + ", path: " + path + ", volumes state: " + newState + ", mAllowedShareNum: "
                        + mAllowedShareNum);

                boolean haveShared = false;
                for (int i = 0; i < volumes.length; i++) {
                    if (Environment.MEDIA_SHARED.equals(mStorageManager.getVolumeState(volumes[i]
                            .getPath()))) {
                        haveShared = true;
                        break;
                    }
                }
                Log.d(TAG, "onStorageStateChanged - haveShared: " + haveShared);
                switchDisplay(haveShared);
                
            } else {
                Log.d(TAG, "onStorageStateChanged - [UMS Disable] mSettingUMS: " + mSettingUMS + ", on: "
                        + on);
                switchDisplay(on);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.GnAlertDialogLight);
        super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
        
        if (mStorageManager == null) {
            mStorageManager = ( StorageManager ) getSystemService(Context.STORAGE_SERVICE);
            if (mStorageManager == null) {
                Log.w(TAG, "Failed to get StorageManager");
            }
        }
        mUsbManager = ( UsbManager ) getSystemService(Context.USB_SERVICE);
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        mUIHandler = new Handler();

        HandlerThread thr = new HandlerThread("SystemUI UsbStorageActivity");
        thr.start();
        mAsyncStorageHandler = new Handler(thr.getLooper());

        this.showDialog(CONTENT);
        // createDialog();
    }

    private String getCurrentFunction() {
        String functions = android.os.SystemProperties.get("sys.usb.config", "none");
        Log.d(TAG, "current function: " + functions);
        return functions;
    }

    private static boolean containsFunction(String functions, String function) {
        int index = functions.indexOf(function);

        if (index < 0)
            return false;
        if (index > 0 && functions.charAt(index - 1) != ',')
            return false;
        int charAfter = index + function.length();
        if (charAfter < functions.length() && functions.charAt(charAfter) != ',')
            return false;
        return true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        mStorageManager.registerListener(mStorageListener);
        registerReceiver(mUsbStateReceiver, new IntentFilter(UsbManager.ACTION_USB_STATE));
        super.onResume();
        updateState();
    }
    
    private void updateState(){
        String currentFunction = getCurrentFunction();
        if (mPtpButton != null) {
            if (containsFunction(currentFunction, UsbManager.USB_FUNCTION_PTP)) {
                mPtpButton.setBackgroundResource(R.drawable.gn_ic_usb_ptp_sel_unpress);
                mIsPtpOpened = true;
            } else {
                mPtpButton.setBackgroundResource(R.drawable.gn_ic_usb_ptp_unpress);
                mIsPtpOpened = false;
            }
        }

        // New request to add MTP help page
        if (mUsbButton != null) {
        	if (containsFunction(currentFunction, UsbManager.USB_FUNCTION_MTP)) {
        		mUsbButton.setBackgroundResource(R.drawable.gn_ic_usb_storage_sel_unpress);
        		mIsMtpOpened = true;
        	} else {
        		mUsbButton.setBackgroundResource(R.drawable.gn_ic_usb_storage_unpress);
        		mIsMtpOpened = false;
        	}
        	mHandler.sendEmptyMessage(UPDATE_MTP_HELP_VIEW);
        }
        
        mDebugState = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) != 0;
        boolean flagOfShownDebugView = Settings.Secure.getInt(getContentResolver(), "real_debug_state", 0) == 0;
        if (mDebugState) {
            mDebugCheck.setChecked(true);
        } else {
            mDebugCheck.setChecked(false);
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                mSettingUMS = checkUmsEnabled();
                mHandler.sendEmptyMessage(UPDATE_CHARGE_BUTTON);
            }
            
        });

        mHasCheck = false;

        try {
            mAsyncStorageHandler.post(new Runnable() {
                @Override
                public void run() {
                    switchDisplay(checkUmsEnabled());
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Failed to read UMS enable state", ex);
        }
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(mUsbStateReceiver);
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
        
        mAsyncStorageHandler.getLooper().quit();
        mDestroyed = true;
    }

    private void handleUsbStateChanged(Intent intent) {
        boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
        boolean isUMSmode = intent.getExtras().getBoolean(UsbManager.USB_FUNCTION_MASS_STORAGE);

        Log.d(TAG, "handleUsbStateChanged - connected:  " + connected + ", isUMSmode: " + isUMSmode);
        if (!connected || !isUMSmode) {
            /**
             * If the USB cable was unplugged when UMS was enabled, set the UMS enable flag to
             * false
             */
            if (mSettingUMS) {
                mSettingUMS = false;
                Log.d(TAG, "handleUsbStateChanged - [Unplug when UMS enabled] connected:  " + connected);
            }
            // It was disconnected from the plug, so finish
            if (!connected) {
                this.finish();
            }
        }
    }

    private IMountService getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        }
        return null;
    }

    private void switchDisplay(final boolean usbStorageInUse) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                switchDisplayAsync(usbStorageInUse);
            }
        });
    }

    private void switchDisplayAsync(boolean usbStorageInUse) {
        if (usbStorageInUse  || isMtpModeDefault()) {
            Log.d(TAG, "switchDisplayAsync - [Mount] usbStorageInUse:  " + usbStorageInUse);
            mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_unpress);
            mPtpButton.setBackgroundResource(R.drawable.gn_ic_usb_ptp_unpress);
            mIsPtpOpened = false;
            mIsChargeOpened = false;
            mUsbButton.setBackgroundResource(R.drawable.gn_ic_usb_storage_sel_unpress);
            mUsbStorageOpened = true;
            mIsUMSSwitching = false;
        } else {
            Log.d(TAG, "switchDisplayAsync - [Unmount] usbStorageInUse:  " + usbStorageInUse);
            mUsbButton.setBackgroundResource(R.drawable.gn_ic_usb_storage_unpress);
            mUsbStorageOpened = false;
            if(!mIsPtpOpened && !mDebugState && !mSettingUMS){
                mIsChargeOpened = true;
                mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_sel_unpress);
            }
        }
    }

    @Override
    protected AmigoAlertDialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = this.getLayoutInflater();
        AmigoAlertDialog dialog = null;

        switch (id) {
            case CONTENT:
                AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this,
                        AmigoAlertDialog.THEME_AMIGO_LIGHT);
                builder.setTitle(getResources().getString(R.string.gn_usb_connection));
                View view = inflater.inflate(R.layout.gn_usb_storage_activity_layout, null);
                builder.setView(view);
                builder.setOnDismissListener(this);
                dialog = builder.create();

                sendBroadcast();

                notifyPhoneStatusBar();
                
                mChargeView = ( View ) view.findViewById(R.id.view_charge);
                mChargeButton = ( ImageView ) view.findViewById(R.id.button_charge);
                mChargeView.setOnClickListener(this);

                mPtpView = ( View ) view.findViewById(R.id.view_ptp);
                mPtpButton = ( ImageView ) view.findViewById(R.id.button_ptp);
                mPtpView.setOnClickListener(this);

                mUsbView = ( View ) view.findViewById(R.id.view_usb);
                mUsbButton = ( ImageView ) view.findViewById(R.id.button_usb);
                mUsbView.setOnClickListener(this);

                mDebugCheck = ( AmigoSwitch ) view.findViewById(R.id.check_debug);
                mDebugCheck.setOnCheckedChangeListener(this);

                mDebugView = ( View ) view.findViewById(R.id.layout_debug);

                mMtpHelpView = view.findViewById(R.id.mtp_help_view);
                mMtpHelpView.setOnClickListener(this);
            	mMtpHelpView.setVisibility(View.GONE);
                break;

            case DLG_ADB_WARN:
                AmigoAlertDialog adbWarnAlertDialog = new AmigoAlertDialog.Builder(this,
                        AmigoAlertDialog.THEME_AMIGO_FULLSCREEN)
                        .setMessage(getResources().getString(R.string.gn_adb_warning_message))
                        .setTitle(R.string.gn_adb_warning_title)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mDebugCheck.setChecked(false);
                            }
                        }).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            	if(!ActivityManager.isUserAMonkey()) {
                            		Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
                            		mDebugState = true;
                            		mDebugCheck.setChecked(true);
                            	}
                            }
                        }).create();
                adbWarnAlertDialog.setCancelable(true);
                adbWarnAlertDialog.setCanceledOnTouchOutside(false);
                return adbWarnAlertDialog;

            case DLG_USB_STORAGE_OPENING:
                AmigoProgressDialog pd = new AmigoProgressDialog(this);
                pd.setMessage(getString(R.string.gn_usb_switch_info));
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                return pd;

        }
        return dialog;
    }

    private void sendBroadcast() {
        final String ACTION_NAVIL_LOSE_FOCUS = "android.intent.action.NAVIL_LOSE_FOCUS";

        Intent intent = new Intent(ACTION_NAVIL_LOSE_FOCUS);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        this.sendBroadcast(intent);
    }

    private void notifyPhoneStatusBar() {
    	Intent intent = new Intent("gn.intent.action.ON_USB_CONNECT");
    	intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
    	this.sendBroadcast(intent);
    }
    
    private void scheduleShowDialog(final int id) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mDestroyed) {
                    removeDialog(id);
                    showDialog(id);
                }
            }
        });
    }

    private void switchUsbMassStorage(final boolean on) {
        // things to do elsewhere
        mAsyncStorageHandler.post(new Runnable() {
            @Override
            public void run() {
                if (on) {
                    mSettingUMS = true;
                    mStorageManager.enableUsbMassStorage();
                } else {
                    mSettingUMS = false;
                    mStorageManager.disableUsbMassStorage();
                }
            }
        });
    }

    private void checkStorageUsers() {
        mAsyncStorageHandler.post(new Runnable() {
            @Override
            public void run() {
                checkStorageUsersAsync();
            }
        });
    }

    private void checkStorageUsersAsync() {
        IMountService ims = getMountService();
        if (ims == null) {
            // Display error dialog
            scheduleShowDialog(DLG_ERROR_SHARING);
            return;
        }
        String extStoragePath = Environment.getExternalStorageDirectory().toString();
        boolean showDialog = false;
        try {
            int[] stUsers = ims.getStorageUsers(extStoragePath);
            if (stUsers != null && stUsers.length > 0) {
                showDialog = true;
            } else {
                // List of applications on sdcard.
                ActivityManager am = ( ActivityManager ) getSystemService(Context.ACTIVITY_SERVICE);
                List<ApplicationInfo> infoList = am.getRunningExternalApplications();
                if (infoList != null) {
                    Log.d(TAG, "checkStorageUsersAsync - infoList.size(): " + infoList.size());
                } else {
                    Log.d(TAG, "checkStorageUsersAsync - [NO EXT RUNNING APPS]");
                }
            }
        } catch (RemoteException e) {
            // Display error dialog
            scheduleShowDialog(DLG_ERROR_SHARING);
        }
        if (showDialog) {
            Log.d(TAG, "checkStorageUsersAsync - [SHOW DIALOG] showDialog: " + showDialog);
            mHasCheck = false;
            switchUsbMassStorage(true);
        } else {
            Log.d(TAG, "checkStorageUsersAsync - [NO DIALOG] showDialog: " + showDialog);
            if (localLOGV)
                Log.i(TAG, "Enabling UMS");
            switchUsbMassStorage(true);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // TODO Auto-generated method stub
        this.finish();
    }
    
    // New request to add MTP help page
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (mIsUMSSwitching || v == null) {
            return;
        }
        
        int id = v.getId();
        switch (id) {
        	case R.id.view_ptp:
        		if (mUsbButton != null) {
        			switchToPtpMode();
        		}
				break;
			case R.id.view_charge:
				switchToChargeOnlyMode();
				break;
			case R.id.view_usb:
				if (true) {
	        		switchToMtpMode();
	        	} else {
	        		switchToUsbMassStorageMode();
	        	}
				break;
			case R.id.mtp_help_view:
				Intent intent = new Intent("gn.intent.action.MTP_HELP");
				this.startActivity(intent);
				break;
			default:
				break;
		}
        this.finish();
    }
    
    private void switchToChargeOnlyMode() {
    	if(ActivityManager.isUserAMonkey()) {
    		return;
    	}
    	if (!mIsChargeOpened) {
            // setUmsFunction();
            if (checkUmsEnabled()) {
                //switchUsbMassStorage(false);
                mStorageManager.disableUsbMassStorage();
                mSettingUMS = false;
            } else {
                setUmsFunction();
            }
            mUsbButton.setBackgroundResource(R.drawable.gn_ic_usb_storage_unpress);
            mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_sel_unpress);
            mPtpButton.setBackgroundResource(R.drawable.gn_ic_usb_ptp_unpress);
            mIsPtpOpened = false;
            mIsChargeOpened = true;

            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
            mDebugCheck.setChecked(false);
            mDebugState = false;

        } else {
            mIsChargeOpened = true;
            mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_sel_unpress);
            setUmsFunction();
        }
    }
    private void switchToPtpMode(){
    	if (!mIsPtpOpened) {
            if(mSettingUMS){
                //switchUsbMassStorage(false);
                mStorageManager.disableUsbMassStorage();
                mSettingUMS = false;
            }
            mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, true);
            mUsbButton.setBackgroundResource(R.drawable.gn_ic_usb_storage_unpress);
            mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_unpress);
            mPtpButton.setBackgroundResource(R.drawable.gn_ic_usb_ptp_sel_unpress);
            mIsPtpOpened = true;
            mIsChargeOpened = false;
        } else {
            setUmsFunction();
            mPtpButton.setBackgroundResource(R.drawable.gn_ic_usb_ptp_unpress);
            mIsPtpOpened = false;
            if (!mDebugState && !mSettingUMS) {
                mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_sel_unpress);
                mIsChargeOpened = true;
            } else {
                mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_unpress);
                mIsChargeOpened = false;
            }
        }
    }
    
    /**
     * This function is extracted from onClik() in the mUsbView branch.
     * */
	public void switchToUsbMassStorageMode() {
	   if (!checkUmsEnabled()) {
	       String externalStorageState = Environment.getExternalStorageState();
		   if (externalStorageState.equals(Environment.MEDIA_REMOVED)
	           || externalStorageState.equals(Environment.MEDIA_BAD_REMOVAL)
		       || externalStorageState.equals(Environment.MEDIA_CHECKING)) {
			   scheduleShowDialog(DLG_NO_SDCARD);
		       return;
		   }
           mIsUMSSwitching = true;
		   if (mIsPtpOpened) {
		       Log.d(TAG, "mIsPtpOpened is true!");
		       setUmsFunction();
		       mPtpButton.setBackgroundResource(R.drawable.gn_ic_usb_ptp_unpress);
		       mIsPtpOpened = false;
		   }
	       Toast.makeText(GnUsbStorageActivity.this, R.string.gn_usb_switch_info,
	               Toast.LENGTH_LONG).show();
	       mSettingUMS = true;
	       mStorageManager.enableUsbMassStorage();
	   } else {
	       if (localLOGV)
	           Log.i(TAG, "Disabling UMS");
	       switchUsbMassStorage(false);
	   }
	   
	   //scheduleShowDialog(DLG_USB_STORAGE_OPENING);
	}
    private boolean mIsMtpOpened = false;
	/**
	 * Set current mode to MTP.
	 * */
    private void switchToMtpMode() {
    	String currentFunctions = getCurrentFunction();
    	if (!checkUmsEnabled() && !containsFunction(currentFunctions, UsbManager.USB_FUNCTION_MTP)) {
	       String externalStorageState = Environment.getExternalStorageState();
	       if (externalStorageState.equals(Environment.MEDIA_REMOVED)
	               || externalStorageState.equals(Environment.MEDIA_BAD_REMOVAL)
	               || externalStorageState.equals(Environment.MEDIA_CHECKING)) {
	           scheduleShowDialog(DLG_NO_SDCARD);
	           return;
	       }
	       mIsUMSSwitching = true;
	       if (mIsPtpOpened) {
	           Log.d(TAG, "mIsPtpOpened is true!");
	           mIsPtpOpened = false;
	       }
	       StringBuilder builder = new StringBuilder();
	       builder.append(UsbManager.USB_FUNCTION_MTP);
	       if (isUsbCDRomSupport()) {
		       builder.append(",");
		       builder.append(UsbManager.USB_FUNCTION_MASS_STORAGE);
	       }
	       String targetFunction = builder.toString();
	       mUsbManager.setCurrentFunction(targetFunction, true);
	       Log.v(TAG, "switchToMtpMode: setTargetFunction to ["+targetFunction+"]");
	       mUsbButton.setBackgroundResource(R.drawable.gn_ic_usb_storage_sel_unpress);
	       mPtpButton.setBackgroundResource(R.drawable.gn_ic_usb_ptp_unpress);
	       mSettingUMS = false;
	       mIsChargeOpened = false;
	       mIsMtpOpened = true;
    	} else {
    		setUmsFunction();
    		mStorageManager.disableUsbMassStorage();
    		mSettingUMS = false;
    		mIsMtpOpened = false;
    	}
    }

    // GIONEE <wujj> <2015-07-15> modify begin
    // In GBL8609 OTA project, CDRom is not support, so need to disable mass_storage function.
    // see init.qcom.usb.rc, there is no configuration for mtp,mass_storage
	private void setUmsFunction() {
		mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MASS_STORAGE,true);
	}

	private boolean checkUmsEnabled() {
		if(!isUsbCDRomSupport())
			return false;
		return mStorageManager.isUsbMassStorageEnabled();
	}
	
	private boolean isUsbCDRomSupport() {
		String buildVersion = SystemProperties.get("ro.gn.gnprojectid", null);
		if ("CBL8609".equals(buildVersion)) {
			return false;
		}
		return true;
	}
	// GIONEE <wujj> <2015-07-15> modify end
	
    /**
     * Check whether MTP is the default Usb mode.Normally the usb functions is in mtp,adb style.
     * The current default one is the first part of the string.
     * @param
     * @return boolean value that indicating current function is MTP or not
     * */
    private boolean isMtpModeDefault(){
    	if (false)
    		return false;
    	
    	String functions = getCurrentFunction();
    	if (functions != null) {
    		String[] split = functions.split(",");
    		if (split != null && UsbManager.USB_FUNCTION_MTP.equals(split[0])) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        // Show toast message if Bluetooth is not allowed in airplane mode
    	boolean isUserAMonkey = ActivityManager.isUserAMonkey();
        Log.d(TAG, "onCheckChanged" + " ActivityManager.isUserAMonkey() = " + isUserAMonkey);
        if (v == mDebugCheck && !isUserAMonkey) {
            if (!isChecked) {
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
                mDebugState = false;

                if (!mIsPtpOpened && !checkUmsEnabled() && !mIsMtpOpened) {
                    mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_sel_unpress);
                    mIsChargeOpened = true;
                }
            } else {
                if (mIsChargeOpened) {
                    mIsChargeOpened = false;
                    mChargeButton.setBackgroundResource(R.drawable.gn_ic_usb_charge_unpress);
                    setUmsFunction();
                }
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
                mDebugState = true;
            }
        }
    }
}
