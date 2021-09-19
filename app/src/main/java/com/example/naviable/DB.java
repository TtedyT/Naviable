package com.example.naviable;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.naviable.navigation.Graph;
import com.example.naviable.navigation.Navigator;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DB {
    private final SharedPreferences sp;

//    public String[] campusesNames = new String[] {
//            "Givat Ram Campus",
//            "Mount Scopus Campus",
//            "Ein Kerem Campus",
//            "Rehovot Campus"
//    };

    // todo: move to firebase
    private static LatLng givatRamCampus = new LatLng(31.776581574592292, 35.1981551984834);
    private static LatLng mountScopusCampus = new LatLng(31.792265890048565, 35.24341248840097);
    private static LatLng einKeremCampus = new LatLng(31.765301216056226, 35.14982248131393);
    private static LatLng rehovotCampus = new LatLng(31.90506819982093, 34.80482152595895);
    private static HashMap<String, LatLng> campuses;
    private Navigator navigator;
    private List<String> locations;

    public DB(Context context){
        sp = context.getSharedPreferences("db", Context.MODE_PRIVATE);

        // todo: remove once we have firebase
        campuses = new HashMap<>();
        // todo: note that copied from strings.xml if changes there change here too
        campuses.put("Givat Ram Campus", givatRamCampus);
        campuses.put("Mount Scopus Campus", mountScopusCampus);
        campuses.put("Ein Kerem Campus", einKeremCampus);
        campuses.put("Rehovot Campus", rehovotCampus);
        // Get the navigator for the map to use
        navigator = null;
        try {
            InputStream nodesInput = context.getAssets().open("nodes.json");
            Graph graph = new Graph(nodesInput);
            navigator = new Navigator(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
        locations = navigator.getLocations();
    }

    public void setCampus(String campus){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("campus", campus);
        editor.apply();

        NaviableApplication.getInstance().setCampus(campus);
    }

    public LatLng getCampus(){
        String campusName = sp.getString("campus", "undefined");
        if(campusName.equals("undefined")){
            // default
            campusName = "Givat Ram Campus";
        }
        LatLng campus = campuses.get(campusName);
        return campus;
    }

    public Navigator getNavigator(){
        return navigator;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void saveSpinnerChosenOption(int optionIdx){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("spinnerChosenOption", optionIdx);
        editor.apply();
    }

    public int getSpinnerChosenOption(){
        return sp.getInt("spinnerChosenOption", 0);
    }

//    public String[] getCampusesNames(){
//        return campusesNames;
//    }
}
