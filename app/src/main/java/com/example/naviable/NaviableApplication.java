package com.example.naviable;

import android.app.Application;

public class NaviableApplication extends Application {
    private static NaviableApplication naviableApplication = null;
    private DB db;

    @Override
    public void onCreate() {
        naviableApplication = this;
        this.db = new DB(this);
        super.onCreate();
    }

    public static NaviableApplication getInstance(){
        return naviableApplication;
    }

    public DB getDB(){
        return naviableApplication.db;
    }

}
