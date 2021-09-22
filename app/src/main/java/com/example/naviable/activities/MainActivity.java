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
import androidx.constraintlayout.solver.widgets.analyzer.Direct;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.naviable.InstructionsAdapter;
import com.example.naviable.NaviableApplication;
import com.example.naviable.R;
import com.example.naviable.navigation.Direction;
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
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView searchBarDestTextView;
    private TextView searchBarSourceTextView;
    private Button goButton;
    private NaviableApplication app;
    private final int ZOOM_OUT_FACTOR=5;
    private RecyclerView recyclerViewInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConstraintLayout searchLayout = (ConstraintLayout) findViewById(R.id.search_constraint_layout);
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

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


        recyclerViewInstructions = (RecyclerView) findViewById(R.id.directions_recycler_view);
//        ArrayList<String> temporaryDirectionsForDebug = new ArrayList<>();
//        temporaryDirectionsForDebug.add("direction 1");
//        temporaryDirectionsForDebug.add("direction 2");
//        temporaryDirectionsForDebug.add("direction 3");
//        temporaryDirectionsForDebug.add("direction 4");
//        temporaryDirectionsForDebug.add("direction 5");
//        temporaryDirectionsForDebug.add("direction 6");



        Button goButton = findViewById(R.id.go_button);
        Navigator finalNavigator = app.getDB().getNavigator();
        goButton.setOnClickListener(view -> {
            String src = searchBarSourceTextView.getText().toString();
            String dest = searchBarDestTextView.getText().toString();
            if(src.equals(dest)){
                Toasty.info(this, "Start and destination are the same.", Toast.LENGTH_SHORT, true).show();
            }
            else {
                List<Direction> directions = finalNavigator.getDirections(dest, src);
                if(directions.isEmpty()){
                    Toasty.info(this, "No accessible route found.", Toast.LENGTH_SHORT, true).show();
                }
                else {
                    // Log.i("MainActivity", "onCreate: printing directions..");
                    InstructionsAdapter instructionsAdapter = new InstructionsAdapter(this, directions);
                    recyclerViewInstructions.setAdapter(instructionsAdapter);
                    recyclerViewInstructions.setLayoutManager(new LinearLayoutManager(this));
//                    for (Direction dir : directions) {
//                        // Log.i("MainActivity", "direction: " + dir.getDescription());
//
//                    }
                }
            }

        });

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
                    goButton.setVisibility(View.VISIBLE);
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
