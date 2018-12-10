package com.gstarcad.andy.unrarsample;

import android.app.Application;

public class MyApplication extends Application{

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static MyApplication getContext() {
        return mInstance;
    }
}