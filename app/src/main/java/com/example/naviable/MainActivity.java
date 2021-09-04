package com.example.naviable;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
