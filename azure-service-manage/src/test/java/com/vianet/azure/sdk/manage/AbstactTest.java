package com.vianet.azure.sdk.manage;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chen.rui on 6/7/2016.
 */
public class AbstactTest {

    public static final String publishsetting = "D:\\Users\\chen.rui\\Documents\\china.publishsettings";

    public static final String subId = "5bbf0cbb-647d-4bd8-b4e6-26629f109bd7";

    Configuration config = null;


    public Configuration getConfig() {
        try {
            if(config == null) {
                config = PublishSettingsLoader.createManagementConfiguration(publishsetting, subId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }


}
