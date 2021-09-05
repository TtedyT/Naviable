package com.example.naviable;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NaviableApplication extends Application {
    private static NaviableApplication naviableApplication = null;

    private final MutableLiveData<String> chosenDestinationMutableLiveData = new MutableLiveData<>();
    private final LiveData<String> chosenDestinationLiveDataPublic = chosenDestinationMutableLiveData;

    private final MutableLiveData<String> chosenSourceMutableLiveData = new MutableLiveData<>();
    private final LiveData<String> chosenSourceLiveDataPublic = chosenSourceMutableLiveData;

    public static enum SEARCH_TYPE {
        SOURCE,
        DESTINATION
    }
    private DB db;

    @Override
    public void onCreate() {
        naviableApplication = this;
        this.db = new DB(this);
        super.onCreate();
    }

    public static NaviableApplication getInstance(){
        if(naviableApplication == null){
            naviableApplication = new NaviableApplication();
        }
        return naviableApplication;
    }

    public DB getDB(){
        return naviableApplication.db;
    }

    // todo: continue from here
    public LiveData<String> getChosenDestinationLiveDataPublic(){
        return chosenDestinationLiveDataPublic;
    }

    public void setSearchDestination(String chosenDestination){
        chosenDestinationMutableLiveData.postValue(chosenDestination);
        System.out.println("dest setted");
    }

    public LiveData<String> getChosenSourceLiveDataPublic(){
        return  chosenSourceLiveDataPublic;
    }

    public void setSearchSource(String chosenSource){
        chosenSourceMutableLiveData.postValue(chosenSource);
        System.out.println("source setted");
    }
}
