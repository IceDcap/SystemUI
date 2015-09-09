package com.amigo.navi.keyguard.network;

import com.amigo.navi.keyguard.DebugLog;
import com.amigo.navi.keyguard.settings.KeyguardSettings;

import amigo.app.AmigoAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.keyguard.R;


public class NetworkRemind {
	
	private static final String TAG="NetworkRemind";
	private Context mContext;
    private boolean isAlert = true;
    private boolean connectNet = false;
    private AmigoAlertDialog networkDialog;
    private static final String NETWORK_IAALART="network_isAlert";
    private static final String NETWORK_DOWNL = "network_download_";
    private static NetworkRemind instance;
    private ClickContinueCallback mClickContinueCallback;
    
    public static NetworkRemind  getInstance(Context context){
    	if(instance==null){
    		new NetworkRemind(context);
    	}
    	
    	return instance;
    }
	
	private NetworkRemind(Context context) {
		mContext=context;
		instance=this;
	}



	public  void alertDialog() {
		
		if(networkDialog != null && networkDialog.isShowing()){
			return;
		}
		if(DebugLog.DEBUG)DebugLog.d(TAG, "NetWork....alertDialog");
		View dialogView=LayoutInflater.from(mContext).inflate(R.layout.net_dialog, null);
		final CheckBox cbDontShowAgain = (CheckBox) dialogView.findViewById(R.id.no_point);
		final TextView dialogInfoTitle=(TextView)dialogView.findViewById(R.id.dialog_information_title);
		dialogInfoTitle.setVisibility(View.GONE);
		final TextView dialogInfoContent=(TextView)dialogView.findViewById(R.id.dialog_Allinformation_content);
		dialogInfoContent.setText(R.string.network_dialog_Allinformation);
		networkDialog=new AmigoAlertDialog.Builder(mContext, AmigoAlertDialog.THEME_AMIGO_LIGHT)
		.setTitle(R.string.dialog_information)
 	    .setView(dialogView)
		.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(cbDontShowAgain.isChecked()){
					isAlert = false;
				}
				else{
					isAlert = true;
				}
				KeyguardSettings.setBooleanSharedConfig(mContext, NETWORK_IAALART, isAlert);
				dialog.dismiss();
				if(mClickContinueCallback!=null){
					mClickContinueCallback.clickContinue();
				}
			}
		}).setNegativeButton(R.string.dialog_cancle, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
					isAlert = true;
					KeyguardSettings.setBooleanSharedConfig(mContext, NETWORK_IAALART, isAlert);				
					dialog.dismiss();
			}
		}).create();
		WindowManager.LayoutParams params = networkDialog.getWindow()
		        .getAttributes();
		params.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
		networkDialog.getWindow().setAttributes(params);

		networkDialog.show();
		networkDialog.setCancelable(false);
		networkDialog.setCanceledOnTouchOutside(false);
	}
	
    public void dismissDialog() {
        if (networkDialog != null) {
            if (networkDialog.isShowing()) {
                networkDialog.dismiss();
            }
        }
    }
	    
	
	public boolean needShowDialog(){
		return KeyguardSettings.getBooleanSharedConfig(mContext, NETWORK_IAALART, true);
	}
	
	public void registeContinueCallback(ClickContinueCallback clickContinueCallback){
		mClickContinueCallback=clickContinueCallback;
	}
	public interface ClickContinueCallback{
		void clickContinue();
	}
	
}
