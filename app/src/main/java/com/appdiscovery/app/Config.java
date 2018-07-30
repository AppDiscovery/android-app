package com.appdiscovery.app;

import com.appdiscovery.app.services.LanServerAvailabilityMonitor;

public class Config {
    private static final Config ourInstance = new Config();

    public static Config getInstance() {
        return ourInstance;
    }

    public String centralServerAddr = "http://192.168.99.100:889";

    public String repoServerAddr = "http://192.168.99.100:888";
    public String canonicalRepoServerAddr = "http://192.168.99.100:888";
    public String lanRepoServerAddr = "http://192.168.99.100:888";

    public static String getRepoServerAddr(boolean bypassLan) {
        if (LanServerAvailabilityMonitor.lanAvailable && !bypassLan) {
            return getInstance().lanRepoServerAddr;
        } else {
            return getInstance().repoServerAddr;
        }
    }

    public static String getRepoServerAddr() {
        return getRepoServerAddr(false);
    }

    private Config() {
    }
}
