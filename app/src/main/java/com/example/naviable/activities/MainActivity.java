package com.example.naviable.activities;

//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//
//
//}

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageButton;

import com.example.naviable.NaviableApplication;
import com.example.naviable.R;
import com.example.naviable.navigation.EdgeInfo;
import com.example.naviable.navigation.Graph;
import com.example.naviable.navigation.MapNode;
import com.example.naviable.navigation.Navigator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView searchBarDestTextView;
    private TextView searchBarSourceTextView;
    private Button goButton;
    private NaviableApplication app;
    private final int ZOOM_OUT_FACTOR=5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Get the navigator for the map to use
        try {
            InputStream nodesInput = getAssets().open("nodes.json");
            Graph graph = new Graph(nodesInput);
            Navigator navigator = new Navigator(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        app = NaviableApplication.getInstance();

        searchBarDestTextView = (TextView) findViewById(R.id.search_bar_dest_text_view);
        searchBarSourceTextView = (TextView) findViewById(R.id.search_bar_source_text_view);
        searchBarSourceTextView.setVisibility(View.GONE);
        goButton = (Button) findViewById(R.id.go_button);
        goButton.setVisibility(View.GONE);

        // todo: use "if" to check if dest is set - if so, show the source search
//        searchBarDestTextView.setVisibility(View.GONE);

        searchBarDestTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToSearchActivity(NaviableApplication.SEARCH_TYPE.DESTINATION);
            }
        });

        app.getChosenDestinationLiveDataPublic().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String observedDestination) {
                if(!observedDestination.isEmpty()){
                    searchBarDestTextView.setText(observedDestination);
                    searchBarSourceTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        searchBarSourceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToSearchActivity(NaviableApplication.SEARCH_TYPE.SOURCE);
            }
        });

        app.getChosenSourceLiveDataPublic().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String observedSource) {
                if(!observedSource.isEmpty()){
                    searchBarSourceTextView.setText(observedSource);
                    goButton.setVisibility(View.VISIBLE);
                }
            }
        });

        app.getCampusChosenLiveDataPublic().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                updateMapLocation();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMapLocation();
    }

    private void moveToSearchActivity(NaviableApplication.SEARCH_TYPE type){
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        boolean searchTypeIsDestinationSearch = type.equals(NaviableApplication.SEARCH_TYPE.DESTINATION);
        intent.putExtra("searchTypeIsDestinationSearch", searchTypeIsDestinationSearch);
        startActivity(intent);
    }

    // changes which campus we focus on in the map
    public void updateMapLocation(){
        LatLng  campus = app.getDB().getCampus();
//        mMap.addMarker(new MarkerOptions()
//                .position(campus));
        // todo - *note*: zoom level is between 2.0 and 21.0
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campus, 18.5f));
    }
}
