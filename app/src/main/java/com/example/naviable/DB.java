package com.example.naviable;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.naviable.navigation.Graph;
import com.example.naviable.navigation.Navigator;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class DB {
    private final SharedPreferences sp;
    private final SharedPreferences spRecentSearchedLocations;


    private static final LatLng givatRamCampus = new LatLng(31.776581574592292, 35.1981551984834);
    private static final LatLng mountScopusCampus = new LatLng(31.792265890048565, 35.24341248840097);
    private static final LatLng einKeremCampus = new LatLng(31.765301216056226, 35.14982248131393);
    private static final LatLng rehovotCampus = new LatLng(31.90506819982093, 34.80482152595895);
    private static final int RECENT_LOCATIONS_MAX_SIZE = 5;
    private static HashMap<String, LatLng> campuses;
    private Navigator navigator;
    private ArrayList<String> locations;

    private LinkedBlockingQueue<String> recentLocations;


    /**
     * base on the python file in assets:
     * 'straight',
     * 'right',
     * 'left',
     * 'elevator'
     */
    private static Map<String, Integer> path_map = new HashMap<>();

    private void initMapWithPaths() {
        path_map.put("straight", R.drawable.ic_baseline_straight_24);
        path_map.put("right", R.drawable.ic_turn_left_24);
        path_map.put("left", R.drawable.ic_turn_right_24);
        path_map.put("elevator", R.drawable.ic_baseline_elevator_24);
    }

    public int getImagePathFromMap(String type) {
        return path_map.get(type);
    }

    public DB(Context context) {
        initMapWithPaths();
        sp = context.getSharedPreferences("db", Context.MODE_PRIVATE);
        spRecentSearchedLocations = context.getSharedPreferences("recentSearchedLocations", Context.MODE_PRIVATE);

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
        recentLocations = new LinkedBlockingQueue<>();
        fetchRecentSearches();
    }

    public void setCampus(String campus) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("campus", campus);
        editor.apply();

        NaviableApplication.getInstance().setCampus(campus);
    }

    public LatLng getCampus() {
        String campusName = sp.getString("campus", "undefined");
        if (campusName.equals("undefined")) {
            // default
            campusName = "Givat Ram Campus";
        }
        LatLng campus = campuses.get(campusName);
        return campus;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }

    public void saveSpinnerChosenOption(int optionIdx) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("spinnerChosenOption", optionIdx);
        editor.apply();
    }

    public int getSpinnerChosenOption() {
        return sp.getInt("spinnerChosenOption", 0);
    }

    public void addRecentLocation(String location) {
        recentLocations.remove(location); // removes the location if already exist to push it as last
        if (recentLocations.size() == RECENT_LOCATIONS_MAX_SIZE) {
            // element wasnt in and reached full capacity
            recentLocations.poll();
        }
        recentLocations.add(location);
        saveRecentsToSp();
    }

    public Object[] getRecentLocationsStaticArray() {
        return recentLocations.toArray();
    }

    private void saveRecentsToSp() {
        Gson gson = new Gson();
        SharedPreferences.Editor editor = spRecentSearchedLocations.edit();
        String recentLocationsStringRepre = gson.toJson(this.recentLocations);
        editor.putString("recentLocationsKey", recentLocationsStringRepre);
        editor.apply();
    }

    private void fetchRecentSearches() {
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedBlockingQueue<String>>() {
        }.getType();
        String recentLocationsStringRepre = spRecentSearchedLocations.getString("recentLocationsKey", "");
        if (!recentLocationsStringRepre.isEmpty()) {
            this.recentLocations = gson.fromJson(recentLocationsStringRepre, type);
        }
    }

}
