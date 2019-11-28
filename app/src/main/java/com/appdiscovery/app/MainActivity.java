package com.appdiscovery.app;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.util.Log;

import com.appdiscovery.app.services.LocationUtils;
import com.appdiscovery.app.services.DigitalSignature;
import com.appdiscovery.app.services.DiscoverApp;
import com.appdiscovery.app.services.LanServerAvailabilityMonitor;
import com.appdiscovery.app.services.LocationWatcher;
import com.appdiscovery.app.services.WidgetAlarmService;
import com.appdiscovery.app.services.WifiStateReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

// AS默认 Activity 继承自 AppCompatActivity
// ActivityCompat.OnRequestPermissionsResultCallback 处理运行时权限
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton mEditUserProfileBtn;
    private WebApp[] webapps = new WebApp[0];
    public static String activeAppName = "";
    private WifiStateReceiver mWifiStateReceiver;



    public MainActivity() {
        // 严苛模式线程策略检测
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        LanServerAvailabilityMonitor.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // savedInstanceState 保存 Activity 的状态
        super.onCreate(savedInstanceState);
        // TODO: Read DigitalSignature
        DigitalSignature.init(this);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.apps_list);
        // Item 的改变不会影响 RecyclerView 宽高的时设置为 true 避免重新计算大小
        mRecyclerView.setHasFixedSize(true);
        setListView(webapps);
        mLocationWatcher.start();
        WebApp.setContext(this);
        AppListAdapter.setContext(this);
        mEditUserProfileBtn = findViewById(R.id.edit_user_profile_btn);
        mEditUserProfileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditUserProfileActivity.class);
            startActivity(intent);
        });
        WidgetAlarmService.start(this);


        final Intent intent = new Intent(this, AppsWidgetProvider.class);
        intent.setAction("UPDATE_WIDGET");
        sendBroadcast(intent);


//        if(mWifiStateReceiver == null) {
//            mWifiStateReceiver = new WifiStateReceiver(this::discoverAppByLan);
//        }
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        registerReceiver(mWifiStateReceiver, filter);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                Log.d("SSID",wifiInfo.getSSID());
                Location location = LocationUtils.getInstance().getLocations(MainActivity.this);
                Log.i("Tobin", String.format("Lat: %f, Log: %f", location.getLatitude(), location.getLongitude()));

                discoverAppByLocation(location);
                discoverAppByLan(wifiInfo.getSSID());

                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
    }


//    protected void onStop() {
//        unregisterReceiver(mWifiStateReceiver);
//        super.onStop();
//    }


    // TODO: Read LocationWatcher
    private LocationWatcher mLocationWatcher = new LocationWatcher(this, this::discoverAppByLocation);

    private void setListView(WebApp[] webapps) {
        Log.d("SetListView", "Called");
        MainActivity.this.webapps = webapps;
        mAdapter = new AppListAdapter(webapps, MainActivity.this.onListItemClick);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void discoverAppByLocation(Location location) {
        // this.discoverAppByLan();
        DiscoverApp.byLocation(location, webapps -> {
            for (WebApp webapp : webapps) {
                new Thread(() -> {
                    try {
                        webapp.download();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            ArrayList<WebApp> mergedWebApps = new ArrayList<>();
            for (WebApp app : MainActivity.this.webapps) {
                if (app.distance_in_m < 0) {
                    mergedWebApps.add(app);
                }
            }
            mergedWebApps.addAll(Arrays.asList(webapps));
            setListView(mergedWebApps.toArray(new WebApp[mergedWebApps.size()]));
        });
    }

    public void discoverAppByLan(String ssid) {
        DiscoverApp.byLan(webapps -> {
            for (WebApp webapp : webapps) {
                new Thread(() -> {
                    try {
                        webapp.download();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            ArrayList<WebApp> mergedWebApps = new ArrayList<>(Arrays.asList(webapps));
            if(!ssid.equals("\"Merseyside\"")) {
                mergedWebApps = new ArrayList<>();
            }
            for (WebApp app : MainActivity.this.webapps) {
                if (app.distance_in_m >= 0) {
                    mergedWebApps.add(app);
                }
            }
            setListView(mergedWebApps.toArray(new WebApp[mergedWebApps.size()]));
        });
    }

    private View.OnClickListener onListItemClick = (view -> {
        int itemPosition = mRecyclerView.getChildLayoutPosition(view);
        WebApp webapp = webapps[itemPosition];
        try {
            activeAppName = webapp.name;
            webapp.launch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 初次通过地理位置权限请求
        if (requestCode == LocationWatcher.REQUEST_PERMISSION_LOCATION_STATE) {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            assert locationManager != null;
            discoverAppByLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
    }
}
