package com.gtx.cooliris.utils;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkHelper
{
	/**
	 * Whether the network (WIFI/2-3G) is valid or not.
	 * 
	 * @param context The application environment.
	 * 
	 * @return If network is valid, return true; otherwise, return false.
	 */   
    public static boolean isNetworkValid(Context context)
    {
        if (null != context)
        {
            ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            
            if (null != info && info.isConnected())
            {
                return true;
            }
        }
        
        return false;
    }
    
	/**
	 * Whether the WIFI network is valid or not.
	 * 
	 * @param context The application environment.
	 * 
	 * @return If WIFI network is valid, return true; otherwise, return false.
	 */
	public static boolean isWifiValid(Context context)
	{
		if (null != context)
		{
			ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connectivity.getActiveNetworkInfo();

			if (null == info /*|| !connectivity.getBackgroundDataSetting()*/) 
			{
				return false;
			}

			if (ConnectivityManager.TYPE_WIFI == info.getType())
			{
				return info.isConnected();
			}	
		}

		return false;
	}
	
	public static boolean isMobileNetwork(Context context)
    {
        if (null != context)
        {
            ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivity.getActiveNetworkInfo();

            if (null == info) 
            {
                return false;
            }

            if (ConnectivityManager.TYPE_MOBILE == info.getType())
            {
                return info.isConnected();
            }   
        }

        return false;
    }

	/**
	 * Called to try to connect the specified wifi network.
	 * 
	 * @param context     The Context.
	 * @param strWifiSSID The wifi SSID.
	 * @param strPassword The password.
	 * 
	 * @return The value indicates the wifi signal about the specified SSID is in the terminal monitoring range or not.
	 */
	public static boolean wifiConnection(Context context, String strWifiSSID, String strPassword)
	{
	    boolean isConnection = false;
	    WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	    String strQuotationSSID = "\"" + strWifiSSID + "\"";

	    WifiInfo wifiInfo = wifi.getConnectionInfo();
        if (null != wifiInfo && (strWifiSSID.equals(wifiInfo.getSSID()) || strQuotationSSID.equals(wifiInfo.getSSID())))
        {
            isConnection = true;
        }
        else
        {
            List<ScanResult> scanResults = wifi.getScanResults();
            if (null != scanResults && 0 != scanResults.size())
            {
                for (int nAllIndex = scanResults.size() - 1; nAllIndex >= 0; nAllIndex --)
                {
                    String strScanSSID = scanResults.get(nAllIndex).SSID;
                    if (strWifiSSID.equals(strScanSSID) || strQuotationSSID.equals(strScanSSID))
                    {
                        WifiConfiguration config = new WifiConfiguration();
                        config.SSID = strQuotationSSID;
                        config.preSharedKey = "\"" + strPassword + "\"";
                        config.status = WifiConfiguration.Status.ENABLED;

                        int nAddWifiId = wifi.addNetwork(config);
                        isConnection = wifi.enableNetwork(nAddWifiId, false);
                        break;
                    }
                }
            }
        }

	    return isConnection;
	}

	/**
     * Get WifiManager.
     * 
     * @param context The Context.
     * @return The WifiManager.
     */
    public static WifiManager getWifiManager(Context context)
    {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        return wifiManager;
    }
    
    /**
     * Get the current network information.
     * 
     * @param context The Context.
     * @return The network information.
     */
    public static NetworkInfo getNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return networkInfo;
    }
    
    /**
     * Get the wifi enable state.
     * 
     * @param context The Context.
     * 
     * @return The wifi state.
     */
    public static int getWifiState(Context context)
    {
        WifiManager wifi = getWifiManager(context);

        if(null == wifi)
        {
            return WifiManager.WIFI_STATE_UNKNOWN;
        }

        return wifi.getWifiState();
    }
    
    /**
     * Get the wifi connectivity state.
     * 
     * @param context The Context.
     * 
     * @return The connectivity state.
     */
    public static DetailedState getConnectivityState(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(null == networkInfo)
        {
            return DetailedState.FAILED;
        }

        return networkInfo.getDetailedState();
    }
}
