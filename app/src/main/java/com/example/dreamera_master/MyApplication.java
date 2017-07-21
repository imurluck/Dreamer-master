package com.example.dreamera_master;

import android.app.Application;
import android.content.Context;

/**
 * Created by Zhangzongxiang on 2017/5/10.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
    }

    public static Context getContext() {
        return context;
    }
}
