package com.appdiscovery.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

import com.appdiscovery.app.NetworkConfig;
import java.util.function.Consumer;


public class WifiStateReceiver extends BroadcastReceiver {
    private final Consumer<Integer> onWifiStateChangeCallback;

    public WifiStateReceiver(Consumer<Integer> onWifiStateChangeCallback){
        this.onWifiStateChangeCallback = onWifiStateChangeCallback;
        String url1 = NetworkConfig.centralServerAddr;
        String url2 = NetworkConfig.lanRepoServerAddr;
        String url3 = NetworkConfig.repoServerAddr;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if(wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if(wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                Log.d("WifiStateReceiver", "ssid: " + String.valueOf(ssid));
                // if(!ssid.equals("<unknown ssid>")) {
                //if(ssid.equals("\"AndroidWifi\"")) {
                if(ssid.equals("\"Merseyside\"")) {
                    onWifiStateChangeCallback.accept(1);
                    // DiscoverApp.setLanAvaliable(ssid);
                    return;
                }
            }
        }

        // DiscoverApp.setLanAvaliable("");
    }
}

