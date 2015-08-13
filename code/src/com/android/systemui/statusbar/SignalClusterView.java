/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.systemui.statusbar;

import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.gionee.statusbar.GnNetworkType;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SecurityController;

import java.util.ArrayList;
import java.util.List;
import android.os.SystemProperties;
import com.android.systemui.gionee.GnFeatureOption;

// Intimately tied to the design of res/layout/signal_cluster_view.xml
public class SignalClusterView
        extends LinearLayout
        implements NetworkControllerImpl.SignalCluster,
        SecurityController.SecurityControllerCallback {

    static final String TAG = "SignalClusterView";
    static final boolean DEBUG = true;//Log.isLoggable(TAG, Log.DEBUG);

    NetworkControllerImpl mNC;
    SecurityController mSC;

    private boolean mNoSimsVisible = false;
    private boolean mVpnVisible = false;
    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0;
    private boolean mIsAirplaneMode = false;
    private int mAirplaneIconId = 0;
    private int mAirplaneContentDescription;
    private String mWifiDescription;
    private ArrayList<PhoneState> mPhoneStates = new ArrayList<PhoneState>();
    
    private boolean mIsEmergency = false;

    ViewGroup mWifiGroup;
    ImageView mWifi, mAirplane, mNoSim1, mNoSim2, mVpn;
    View mWifiAirplaneSpacer;
    View mWifiSignalSpacer;
    LinearLayout mMobileSignalGroup;

    private int mWideTypeIconStartPadding;
    private int mSecondaryTelephonyPadding;
    private int mEndPadding;
    private int mEndPaddingNothingVisible;
    private int mSlotCount;
    
    public SignalClusterView(Context context) {
        this(context, null);
    }

    public SignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNetworkController(NetworkControllerImpl nc) {
        if (DEBUG) Log.d(TAG, "NetworkController=" + nc);
        mNC = nc;
        mSlotCount = mNC.gnGetPhoneCount();
    }

    public void setSecurityController(SecurityController sc) {
        if (DEBUG) Log.d(TAG, "SecurityController=" + sc);
        mSC = sc;
        mSC.addCallback(this);
        mVpnVisible = mSC.isVpnEnabled();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWideTypeIconStartPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.wide_type_icon_start_padding);
        mSecondaryTelephonyPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.secondary_telephony_padding);
        mEndPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.signal_cluster_battery_padding);
        mEndPaddingNothingVisible = getContext().getResources().getDimensionPixelSize(
                R.dimen.no_signal_cluster_battery_padding);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mVpn            = (ImageView) findViewById(R.id.vpn);
        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mAirplane       = (ImageView) findViewById(R.id.airplane);
        mNoSim1         = (ImageView) findViewById(R.id.no_sims_1);
        mNoSim2         = (ImageView) findViewById(R.id.no_sims_2);
        mWifiAirplaneSpacer =         findViewById(R.id.wifi_airplane_spacer);
        mWifiSignalSpacer =           findViewById(R.id.wifi_signal_spacer);
        mMobileSignalGroup = (LinearLayout) findViewById(R.id.mobile_signal_group);
        for (PhoneState state : mPhoneStates) {
            mMobileSignalGroup.addView(state.mMobileGroup);
        }

        apply();
    }

    @Override
    protected void onDetachedFromWindow() {
        mVpn            = null;
        mWifiGroup      = null;
        mWifi           = null;
        mAirplane       = null;
        mMobileSignalGroup.removeAllViews();
        mMobileSignalGroup = null;

        super.onDetachedFromWindow();
    }

    // From SecurityController.
    @Override
    public void onStateChanged() {
        post(new Runnable() {
            @Override
            public void run() {
                mVpnVisible = mSC.isVpnEnabled();
                apply();
            }
        });
    }

    @Override
    public void setWifiIndicators(boolean visible, int strengthIcon, String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiDescription = contentDescription;

        apply();
    }

    @Override
    public void setMobileDataIndicators(boolean visible, int strengthIcon, int typeIcon,
            String contentDescription, String typeContentDescription, boolean isTypeIconWide,
            int subId) {
        /// M: "Add getState". @{
        PhoneState state = getState(subId);
        if (state == null) {
            Log.d(TAG, "setMobileDataIndicators(" + subId + "), subId = " +
            subId + " is not exist");
            return;
        }
        /// M: "Add getState". @} 
        state.mMobileVisible = visible;
        state.mMobileStrengthId = strengthIcon;
        state.mMobileTypeId = typeIcon;
        state.mMobileDescription = contentDescription;
        state.mMobileTypeDescription = typeContentDescription;
        state.mIsMobileTypeIconWide = isTypeIconWide;
        apply();
    }

    @Override
    public void setNoSims(boolean show) {
        mNoSimsVisible = show;
    }

    @Override
    public void setSubs(List<SubscriptionInfo> subs) {
        // Clear out all old subIds.
        mPhoneStates.clear();
        if (mMobileSignalGroup != null) {
            mMobileSignalGroup.removeAllViews();
        }
        final int n = subs.size();
        for (int i = 0; i < n; i++) {
            //inflatePhoneState(subs.get(i).getSubscriptionId());
        	inflatePhoneState(subs.get(i).getSimSlotIndex());
        }
    }
    
    /// M: "Add getState". @{
    private PhoneState getState(int subId) {
        for (PhoneState state : mPhoneStates) {
            if (state.mSubId == subId) {
                return state;
            }
        }
        return null;
    }
    /// M: "Add getState". @}

    private PhoneState getOrInflateState(int subId) {
        for (PhoneState state : mPhoneStates) {
            if (state.mSubId == subId) {
                return state;
            }
        }
        return inflatePhoneState(subId);
    }

    private PhoneState inflatePhoneState(int subId) {
        PhoneState state = new PhoneState(subId, mContext);
        if (mMobileSignalGroup != null) {
            mMobileSignalGroup.addView(state.mMobileGroup);
        }
        mPhoneStates.add(state);
        return state;
    }

    @Override
    public void setIsAirplaneMode(boolean is, int airplaneIconId, int contentDescription) {
        mIsAirplaneMode = is;
        mAirplaneIconId = airplaneIconId;
        mAirplaneContentDescription = contentDescription;

        apply();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mWifiVisible && mWifiGroup != null && mWifiGroup.getContentDescription() != null)
            event.getText().add(mWifiGroup.getContentDescription());
        for (PhoneState state : mPhoneStates) {
            state.populateAccessibilityEvent(event);
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        if (mWifi != null) {
            mWifi.setImageDrawable(null);
        }

        for (PhoneState state : mPhoneStates) {
            if (state.mMobile != null) {
                state.mMobile.setImageDrawable(null);
            }
            if (state.mMobileType != null) {
                state.mMobileType.setImageDrawable(null);
            }
        }

        if(mAirplane != null) {
            mAirplane.setImageDrawable(null);
        }

        apply();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    // Run after each indicator change.
    private void apply() {
        if (mWifiGroup == null) return;

        mVpn.setVisibility(mVpnVisible ? View.VISIBLE : View.GONE);
        if (DEBUG) Log.d(TAG, String.format("vpn: %s", mVpnVisible ? "VISIBLE" : "GONE"));
        if (mWifiVisible) {
            mWifi.setImageResource(mWifiStrengthId);
            mWifiGroup.setContentDescription(mWifiDescription);
            mWifiGroup.setVisibility(View.VISIBLE);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Log.d(TAG,
                String.format("wifi: %s sig=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId));

        boolean anyMobileVisible = false;
        int firstMobileTypeId = 0;
        for (PhoneState state : mPhoneStates) {
            if (state.apply(anyMobileVisible)) {
                if (!anyMobileVisible) {
                    firstMobileTypeId = state.mMobileTypeId;
                    anyMobileVisible = true;
                }
            }
        }

        if (mIsAirplaneMode) {
            mAirplane.setImageResource(mAirplaneIconId);
            mAirplane.setContentDescription(mAirplaneContentDescription != 0 ?
                    mContext.getString(mAirplaneContentDescription) : null);
            mAirplane.setVisibility(View.VISIBLE);
        } else {
            mAirplane.setVisibility(View.GONE);
        }

        if (mIsAirplaneMode && mWifiVisible) {
            mWifiAirplaneSpacer.setVisibility(View.VISIBLE);
        } else {
            mWifiAirplaneSpacer.setVisibility(View.GONE);
        }

        if (((anyMobileVisible && firstMobileTypeId != 0) || mNoSimsVisible) && mWifiVisible) {
            mWifiSignalSpacer.setVisibility(View.VISIBLE);
        } else {
            mWifiSignalSpacer.setVisibility(View.GONE);
        }

        if(GnFeatureOption.GN_CTCC_SUPPORT && (mNoSim1 != null && mNoSim2 != null)) {
        	if(mNoSimsVisible) {
        		mNoSim1.setImageResource(R.drawable.gn_stat_sys_no_sims);
        		mNoSim2.setImageResource(R.drawable.gn_stat_sys_no_sims);
        		mNoSim1.setVisibility(View.VISIBLE);
        		mNoSim2.setVisibility(View.VISIBLE);
        	}
        	
        	if(mIsAirplaneMode) {
        		mNoSim1.setVisibility(View.GONE);
        		mNoSim2.setVisibility(View.GONE);
        	}
        }
        
        if (mNoSimsVisible) {
        	mWifiSignalSpacer.setVisibility(View.GONE);
        }
        
        if (!GnFeatureOption.GN_CTCC_SUPPORT && mNoSimsVisible && !mWifiVisible && !mIsAirplaneMode) {
        	Log.i(TAG,"setVisibility:" + (mNoSimsVisible && !mWifiVisible && !mIsAirplaneMode));
        	setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
        }
        //mNoSims.setVisibility(mNoSimsVisible ? View.VISIBLE : View.GONE);

        boolean anythingVisible = mNoSimsVisible || mWifiVisible || mIsAirplaneMode
                || anyMobileVisible || mVpnVisible;
        if(!GnFeatureOption.GN_CTCC_SUPPORT) {
        	setPaddingRelative(0, 0, anythingVisible ? mEndPadding : mEndPaddingNothingVisible, 0);
        }
    }

    private class PhoneState {
        private final int mSubId;
        private boolean mMobileVisible = false;
        private int mMobileStrengthId = 0, mMobileTypeId = 0;
        private boolean mIsMobileTypeIconWide;
        private String mMobileDescription, mMobileTypeDescription;
        private int mNetworkTypeId;
        private int mMobileInOutId;
        private ViewGroup mNetworkTypeGroup;
        private boolean mIsConnect = false;

        private ViewGroup mMobileGroup;
        private ImageView mMobile, mMobileType;
        private ImageView mNetworkType, mMobileInOut, mSlotIndicator;
        
        private ViewGroup mNetworkSignalGroup;
        private ViewGroup mNetworkSignalGroup_CT;
        private ImageView mMobile_top_CT, mMobile_buttom_CT, mMobileType_CT;
        private int mMobileStrengthIdCT = 0;
        private GnNetworkType mGnNetworkType;
        private boolean mIsRoaming;
        private boolean mIsDataConnect = false;

        public PhoneState(int subId, Context context) {
            ViewGroup root = (ViewGroup) LayoutInflater.from(context)
                    .inflate(R.layout.mobile_signal_group, null);
            setViews(root);
            mSubId = subId;
        }

        public void setViews(ViewGroup root) {
            mMobileGroup    = root;
            mMobile         = (ImageView) root.findViewById(R.id.mobile_signal);
            mMobileType     = (ImageView) root.findViewById(R.id.mobile_type);
            mNetworkType	= (ImageView) root.findViewById(R.id.network_type);
            mMobileInOut	= (ImageView) root.findViewById(R.id.mobile_inout);
            mSlotIndicator	= (ImageView) root.findViewById(R.id.mobile_slot_indicator);
            mNetworkTypeGroup = (ViewGroup) root.findViewById(R.id.network_type_combo);
            
            mMobile_top_CT 			= (ImageView) root.findViewById(R.id.mobile_signal_top);
            mMobile_buttom_CT 		= (ImageView) root.findViewById(R.id.mobile_signal_buttom);
            mMobileType_CT 			= (ImageView) root.findViewById(R.id.mobile_type_ct);
            mNetworkSignalGroup 	= (ViewGroup) root.findViewById(R.id.network_signal_combo);
            mNetworkSignalGroup_CT 	= (ViewGroup) root.findViewById(R.id.network_signal_combo_ct);
        }

        public boolean apply(boolean isSecondaryIcon) {
            if (DEBUG) Log.d(TAG,"apply(), mMobileVisible = " + mMobileVisible + ", mIsAirplaneMode = " + mIsAirplaneMode
                            + ", mIsEmergency = " + mIsEmergency);
            if (mMobileVisible && !mIsAirplaneMode && !mIsEmergency) {
            	if (GnFeatureOption.GN_CTCC_SUPPORT) {
            		mSlotIndicator.setVisibility(View.GONE);
                    if (DEBUG) Log.d(TAG,"apply(), CTCC, mSubId = " + mSubId);
            		if(mSubId == 0) {
            			if(mMobileStrengthId != 0 && mMobileStrengthIdCT != 0) {
            				mMobile_top_CT.setImageResource(mMobileStrengthIdCT);
            				mMobile_buttom_CT.setImageResource(mMobileStrengthId);
            				mMobileType_CT.setImageResource(mMobileTypeId);
            				mNetworkSignalGroup_CT.setVisibility(View.VISIBLE);
            				mNetworkSignalGroup.setVisibility(View.GONE);
                            if (DEBUG) Log.d(TAG,"apply(), CTCC, mMobileStrengthIdCT = " + mMobileStrengthIdCT);
            			} else {
            				mMobile_top_CT.setImageResource(0);
            				mMobile_buttom_CT.setImageResource(0);
            				mMobileType_CT.setImageResource(0);
            				mMobileType.setImageResource(mMobileTypeId);
            				mMobile.setImageResource(mMobileStrengthId);
            				mNetworkSignalGroup_CT.setVisibility(View.GONE);
            				mNetworkSignalGroup.setVisibility(View.VISIBLE);
                            if (DEBUG) Log.d(TAG,"apply(), CTCC, mMobileStrengthId = " + mMobileStrengthId);
						}
            		} else {
                        if (DEBUG) Log.d(TAG,"apply(), CTCC, slot wrong : " + mSubId);
            			mMobile.setImageResource(mMobileStrengthId);
                        mMobileType.setImageResource(mMobileTypeId);
                        mMobileGroup.setContentDescription(mMobileTypeDescription
                                + " " + mMobileDescription);
					}
            		if(mNetworkTypeId != 0 && mMobileInOutId != 0) {
            			if(mSubId == 0) {
            				mNetworkType.setImageResource(gnGetNetworkTypeIconCT(0, mGnNetworkType));
            			} else if(mSubId == 1) {
            				mNetworkType.setImageResource(gnGetNetworkTypeIconCT(1, mGnNetworkType));
						}
            		} else {
						mNetworkType.setImageResource(0);
					}
				} else {
					mSlotIndicator.setImageResource(getSlotIndicator(mSubId));
					mMobile.setImageResource(mMobileStrengthId);
					mMobileType.setImageResource(gnGetMotileTypeIcon(mIsRoaming, mGnNetworkType));
					mMobileGroup.setContentDescription(mMobileTypeDescription
							+ " " + mMobileDescription);
					mNetworkType.setImageResource(mIsDataConnect ? getNetworkTypeIcon(mGnNetworkType) : 0);
				}
            	//mNetworkType.setImageResource(mNetworkTypeId);
            	mMobileInOut.setImageResource(mMobileInOutId);
                mMobileGroup.setVisibility(View.VISIBLE);
            } else {
                mMobileGroup.setVisibility(View.GONE);
            }

            // When this isn't next to wifi, give it some extra padding between the signals.
            mMobileGroup.setPaddingRelative(isSecondaryIcon ? mSecondaryTelephonyPadding : 0,
                    0, 0, 0);
            //Gionee <fangjian> <2015-07-07> modify for CR01503190 begin
            //mMobile.setPaddingRelative(mIsMobileTypeIconWide ? mWideTypeIconStartPadding : 0,
            //        0, 0, 0);
            mMobile.setPaddingRelative(mWideTypeIconStartPadding,0,0,0);
            //Gionee <fangjian> <2015-07-07> modify for CR01503190 end

            if (DEBUG) Log.d(TAG, String.format("mobile: %s sig=%d mIsAirPlaneMode=%s netType=%d mobileType=%d inout=%d mIsDataConnect =%s mSubId=%d",
                        (mMobileVisible ? "VISIBLE" : "GONE"), mMobileStrengthId, mIsAirplaneMode, mNetworkTypeId, mMobileTypeId, mMobileInOutId, mIsDataConnect, mSubId));

            mMobileType.setVisibility(mMobileTypeId != 0 ? View.VISIBLE : View.GONE);
            mNetworkTypeGroup.setVisibility(mWifiVisible ? View.GONE : View.VISIBLE);

            return mMobileVisible;
        }

        public void populateAccessibilityEvent(AccessibilityEvent event) {
            if (mMobileVisible && mMobileGroup != null
                    && mMobileGroup.getContentDescription() != null) {
                event.getText().add(mMobileGroup.getContentDescription());
            }
        }
    }
    
    private int getSlotIndicator(int slotId) {
    	if(mSlotCount < 2) {
    		return 0;
    	}
		if (slotId == 0) {
			return R.drawable.gn_sim_indicator_1;
		} else if(slotId == 1) {
			return R.drawable.gn_sim_indicator_2;
		}
		return 0;
	}

	@Override
	public void setNetworkType(int networkType, int subId) {
		PhoneState state = getState(subId);
        if (state == null) {
            Log.d(TAG, "setNetworkType(" + subId + "), subId = " + subId + " is not exist");
            return;
        }
		state.mNetworkTypeId = networkType;
		apply();
	}

	@Override
	public void setMobileInout(boolean visible, int mobileInOut, int subId) {
		// TODO Auto-generated method stub
		PhoneState state = getOrInflateState(subId);
		state.mIsConnect = visible;
		if(visible) {
			state.mMobileInOutId = mobileInOut;
		} else {
			state.mMobileInOutId = 0;
		}
		apply();
	}
	@Override
	public void gnSetMobileSignalCT(boolean visible, int strengthIconCT, int strengthIcon, int typeIcon, int subId) {
        Log.d(TAG, "gnSetMobileSignalCT(), visible= " + visible + ", strengthIconCT = " + strengthIconCT
                    + ", strengthIcon = " + strengthIcon + ", typeIcon = " + typeIcon + ", subId = " + subId);
		PhoneState state = getOrInflateState(subId);
		state.mMobileStrengthIdCT = strengthIconCT;
		state.mMobileStrengthId = strengthIcon;
		state.mMobileVisible = visible;
		state.mMobileTypeId = typeIcon;
		apply();
	}

	@Override
	public void gnSetSimUnavail(boolean visible, boolean isEmergency,
			int unAvailResId, int subId) {
        Log.d(TAG, "gnSetSimUnavail(), visible= " + visible + ", unAvailResId = " + unAvailResId);
		mIsEmergency = isEmergency;
		if(mNoSim1 == null && mNoSim2 == null)
			return;
		
		if(subId == 1) {
			mNoSim2.setImageResource(unAvailResId);
			mNoSim2.setVisibility(visible ? View.VISIBLE : View.GONE);
		} else if(subId == 0) {
			mNoSim1.setImageResource(unAvailResId);
			mNoSim1.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void gnSetNetworkType(GnNetworkType networkType, boolean isRoaming, boolean isDataConnect, int subId) {
		Log.d(TAG, "gnSetNetworkType(" + subId + "), NetworkType= " + networkType);
        PhoneState state = getOrInflateState(subId);
        state.mGnNetworkType = networkType;
        state.mIsRoaming = isRoaming;
        state.mIsDataConnect = isDataConnect;
	}
	
	private int gnGetMotileTypeIcon(boolean isRoaming, GnNetworkType networkType) {
		if(isRoaming) {
			return R.drawable.gn_stat_sys_mobile_roam;
		}
        if (networkType == GnNetworkType.Type_G) {
            return R.drawable.gn_stat_sys_mobile_type_g;
        } else if (networkType == GnNetworkType.Type_E) {
            return R.drawable.gn_stat_sys_mobile_type_e;
        } else if (networkType == GnNetworkType.Type_3G) {
            return R.drawable.gn_stat_sys_mobile_type_3g;
        } else if (networkType == GnNetworkType.Type_4G) {
            return R.drawable.gn_stat_sys_mobile_type_4g;
        } else if (networkType == GnNetworkType.Type_1X) {
            return R.drawable.gn_stat_sys_mobile_type_1x;
        } else if (networkType == GnNetworkType.Type_1X3G) {
            return R.drawable.gn_stat_sys_mobile_type_3g;
        } else if (networkType == GnNetworkType.Type_H) {
			return R.drawable.gn_stat_sys_mobile_type_h;
		} else {
            return 0;
        }
    }
	
	private int gnGetNetworkTypeIconCT(int slotId, GnNetworkType networkType) {
		if(slotId == 1) {
			return R.drawable.gn_stat_sys_network_type_2g;
		} else if (slotId == 0) {
			if(networkType == GnNetworkType.Type_G 
					|| networkType == GnNetworkType.Type_E 
					|| networkType == GnNetworkType.Type_1X) {
				return R.drawable.gn_stat_sys_network_type_2g;
			} else if(networkType == GnNetworkType.Type_3G 
					|| networkType == GnNetworkType.Type_1X3G
					|| networkType == GnNetworkType.Type_H) {
				return R.drawable.gn_stat_sys_network_type_3g;
			} else if (networkType == GnNetworkType.Type_4G) {
				return R.drawable.gn_stat_sys_network_type_4g;
			}
		}
		return 0;
	}
	
	private int getNetworkTypeIcon(GnNetworkType networkType) {
		if (networkType == GnNetworkType.Type_G) {
            return R.drawable.gn_stat_sys_network_type_g;
        } else if (networkType == GnNetworkType.Type_E) {
            return R.drawable.gn_stat_sys_network_type_e;
        } else if (networkType == GnNetworkType.Type_3G) {
            return R.drawable.gn_stat_sys_network_type_3g;
        } else if (networkType == GnNetworkType.Type_4G) {
            return R.drawable.gn_stat_sys_network_type_4g;
        } else if (networkType == GnNetworkType.Type_1X) {
            return R.drawable.gn_stat_sys_network_type_1x;
        } else if (networkType == GnNetworkType.Type_1X3G) {
            return R.drawable.gn_stat_sys_network_type_3g;
        } else if (networkType == GnNetworkType.Type_H) {
			return R.drawable.gn_stat_sys_network_type_h;
		} else {
            return 0;
        }
	}
}

