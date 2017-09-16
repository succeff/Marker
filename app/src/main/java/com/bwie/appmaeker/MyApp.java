package com.bwie.appmaeker;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by peng on 2017/9/14.
 */

public class MyApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
//        SDKInitializer.initialize(getApplicationContext());
    }
}
