package com.example.naviable;

import android.content.Context;
import android.content.SharedPreferences;

public class DB {
    private final SharedPreferences sp;

    public DB(Context context){
        sp = context.getSharedPreferences("db", Context.MODE_PRIVATE);
    }

    public void setCampus(String campus){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("campus", campus);
        editor.apply();
    }

    public String getCampus(){
        return sp.getString("campus", "undefined");
    }

    //todo: possibly add more options
}
