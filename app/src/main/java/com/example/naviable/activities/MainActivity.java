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
import android.widget.TextView;
import android.widget.ImageButton;

import com.example.naviable.NaviableApplication;
import com.example.naviable.R;
import com.example.naviable.navigation.Direction;
import com.example.naviable.navigation.Edge;
import com.example.naviable.navigation.Graph;
import com.example.naviable.navigation.MapNode;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView searchBarDestTextView;
    private TextView searchBarSourceTextView;
    private NaviableApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        //todo: delete
        MapNode node1 = new MapNode("Harman", 31.77654, 35.19621, false);
        MapNode node2 = new MapNode("Popik", 31.77735, 35.19608, false);
        Direction dir = new Direction("do something", "RIGHT");
        ArrayList<Direction> directions = new ArrayList<>();
        directions.add(dir);
        Edge edge = new Edge(node1, node2, directions);

        ArrayList<Edge> edges = new ArrayList<Edge>();
        ArrayList<MapNode> nodes = new ArrayList<MapNode>();
        edges.add(edge);
        nodes.add(node1);
        nodes.add(node2);
        Graph g = new Graph(nodes, edges);

        Gson gson = new Gson();
        String data = gson.toJson(g);
        String resourceName = "com/example/naviable/activities/graph.txt";
        InputStream path = MainActivity.class.getResourceAsStream(resourceName);

        resourceName = "graph.txt";
        FileOutputStream fos;
        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(resourceName));
            fos = new FileOutputStream(resourceName, true);
            FileWriter writer = new FileWriter(fos.getFD());
            writer.write(data);
            writer.close();

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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void moveToSearchActivity(NaviableApplication.SEARCH_TYPE type){
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        boolean searchTypeIsDestinationSearch = type.equals(NaviableApplication.SEARCH_TYPE.DESTINATION);
        intent.putExtra("searchTypeIsDestinationSearch", searchTypeIsDestinationSearch);
        startActivity(intent);
    }
}
