package com.appdiscovery.app.services;

import android.location.Location;
import android.util.Log;

import com.appdiscovery.app.Config;
import com.appdiscovery.app.WebApp;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DiscoverApp {
    public static boolean isLanAvaliable = false;

    public static void setLanAvaliable(String ssid) {
        if(ssid.length() == 0) {
            isLanAvaliable = false;
        } else {
            isLanAvaliable = true;
        }
    }

    public static void byLocation(Location location, Consumer<WebApp[]> callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Config.getRepoServerAddr() + "/app/discover?lat=" + location.getLatitude() + "&lng=" + location.getLongitude())
                .get()
                .build();
        try {
            Log.d("Discover by Location:", "success");
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            Gson gson = new Gson();
            callback.accept(gson.fromJson(jsonData, WebApp[].class));
        } catch (SocketTimeoutException e) {
            if (LanServerAvailabilityMonitor.lanAvailable) {
                // LAN server not available, retry without it
                LanServerAvailabilityMonitor.lanAvailable = false;
                byLocation(location, callback);
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void byLan(Consumer<WebApp[]> callback) {
        // if(!isLanAvaliable) return;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Config.getInstance().lanRepoServerAddr + "/app/lan-discover")
                .get()
                .build();
        try {
            Log.d("Discover by Lan:", "success");
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            Gson gson = new Gson();
            callback.accept(gson.fromJson(jsonData, WebApp[].class));
        } catch (IOException ignored) {
        }
    }
}
