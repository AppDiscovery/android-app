package com.appdiscovery.app;

import com.appdiscovery.app.services.LanServerAvailabilityMonitor;

public class Config {
    private static final Config ourInstance = new Config();

    public static Config getInstance() {
        return ourInstance;
    }

//    public String centralServerAddr = "http://[2001:da8:270:2020:f816:3eff:fee8:8f96]:5901";
//    public String repoServerAddr = "http://[2001:da8:270:2020:f816:3eff:fee8:8f96]:5900";
//    public String lanRepoServerAddr = "http://[2001:da8:270:2020:f816:3eff:fee8:8f96]:5900";

    public String centralServerAddr = "http://47.101.202.129:889";
    public String repoServerAddr = "http://47.101.202.129:888";
    public String canonicalRepoServerAddr = "http://47.101.202.129:888";
    public String lanRepoServerAddr = "http://47.101.202.129:888";

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
