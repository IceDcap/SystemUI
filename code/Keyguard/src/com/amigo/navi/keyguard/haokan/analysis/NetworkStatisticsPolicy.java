package com.amigo.navi.keyguard.haokan.analysis;

import com.amigo.navi.keyguard.network.connect.NetWorkUtils;

import junit.framework.Assert;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Process;
import android.util.Log;


public class NetworkStatisticsPolicy {
	
	private static long beginMillis = -1;
	private static long endMillis = -1;
	
	private static int sNetworkType = NetWorkUtils.NETWORK_TYPE_NULL;
	private static long beginThroughput = -1;
	private static long endThroughput = -1;
	
	public static void onNetworkAccessBegin(Context context) {
		beginMillis = System.currentTimeMillis();
		beginThroughput = getCurrentDataThroughput();
		sNetworkType = NetWorkUtils.getNetworkType(context);
	}
	
	public static void onNetworkAccessEnd(Context context, boolean accessDone) {
		if(accessDone && sNetworkType != NetWorkUtils.NETWORK_TYPE_NULL) {
			onEventNetworkAccessSuccessCount();
			
			endMillis = System.currentTimeMillis();
			int millisCost = (int) (endMillis - beginMillis);
			Log.d("DEBUG_STATISTICS_NETWORK_ACCESS", "millis cost: " + millisCost);
			if(beginMillis > 0 && millisCost > 0) {
				onEventNetworkAccessMillisCost(millisCost);
			}
			
			endThroughput = getCurrentDataThroughput();
			int throughput = (int) (endThroughput - beginThroughput);
			Log.d("DEBUG_STATISTICS_NETWORK_ACCESS", "throughput cost: " + throughput);
			if(throughput >= 0) {
				onEventNetworkAccessThroughput(throughput);
			}
		}
		
		onEventNetworkAccessTotalCount();
		
		resetValues();
	}
	
	private static void resetValues() {
		beginMillis = -1;
		endMillis = -1;
		beginThroughput = -1;
		endThroughput = -1;
		sNetworkType = NetWorkUtils.NETWORK_TYPE_NULL;
	}
	
	private static void onEventNetworkAccessTotalCount() {
		int eventId = getNetworkAccessEventId(sNetworkType, 0);
		HKAgent.onEventNetworkAccess(eventId, 1);
	}
	
	private static void onEventNetworkAccessSuccessCount() {
		int eventId = getNetworkAccessEventId(sNetworkType, 1);
		HKAgent.onEventNetworkAccess(eventId, 1);
	}
	
	private static void onEventNetworkAccessMillisCost(int millisCost) {
		int eventId = getNetworkAccessEventId(sNetworkType, 2);
		HKAgent.onEventNetworkAccess(eventId, millisCost); 
	}
	
	private static void onEventNetworkAccessThroughput(int throughput) {
		int eventId = getNetworkAccessEventId(sNetworkType, 3);
		HKAgent.onEventNetworkAccess(eventId, throughput); 
	}
	
	// eventType = (0, 1, 2, 3) (total count, success count, millis cost, throughput)
	private static int getNetworkAccessEventId(int networkType, int eventType) {
		int startPositon = Event.NETWORK_REQUEST_WEEKLY_TOTAL_COUNT;
		int endPosition = Event.NETWORK_REQUEST_WEEKLY_TOTAL_COUNT;
		int eventId = startPositon + eventType;
		if(networkType == NetWorkUtils.NETWORK_TYPE_WIFI) {
			endPosition = Event.NETWORK_REQUEST_WEEKLY_TOTAL_COUNT_WIFI;
		} else if(networkType == NetWorkUtils.NETWORK_TYPE_4G) {
			endPosition = Event.NETWORK_REQUEST_WEEKLY_TOTAL_COUNT_4G;
		} else if(networkType == NetWorkUtils.NETWORK_TYPE_3G) {
			endPosition = Event.NETWORK_REQUEST_WEEKLY_TOTAL_COUNT_3G;
		} else if(networkType == NetWorkUtils.NETWORK_TYPE_2G) {
			endPosition = Event.NETWORK_REQUEST_WEEKLY_TOTAL_COUNT_2G;
		}
		
		return eventId + (endPosition - startPositon);
	}
	
	private static long getCurrentDataThroughput() {
		int uid = Process.myUid();
		long rx = TrafficStats.getUidRxBytes(uid);
		long tx = TrafficStats.getUidTxBytes(uid);
		
		long result = TrafficStats.UNSUPPORTED;
		if(rx != TrafficStats.UNSUPPORTED && tx != TrafficStats.UNSUPPORTED) {
			result = rx + tx;
		}
		
		return result;
	}
	
}
