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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.naviable.InstructionsAdapter;
import com.example.naviable.NaviableApplication;
import com.example.naviable.R;
import com.example.naviable.navigation.Direction;
import com.example.naviable.navigation.Navigator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView searchBarDestTextView;
    private TextView searchBarSourceTextView;
    private Button goButton;
    private Button doneNavigationButton;
    private NaviableApplication app;
    private ConstraintLayout constraintLayout;
    private List<Direction> directions;
    private final int ZOOM_OUT_FACTOR = 5;
    private RecyclerView recyclerViewInstructions;
    private TextView showNavigationSrcDest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        Drawable searchBackground = ContextCompat.getDrawable(this,
                R.drawable.rounded_rectangle_view_search_background);

        FloatingActionButton qrButton = findViewById(R.id.qr_scan_button);
        qrButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, CodeScannerActivity.class);
            startActivity(intent);
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        app = NaviableApplication.getInstance();

        searchBarDestTextView = findViewById(R.id.search_bar_dest_text_view);
        searchBarSourceTextView = findViewById(R.id.search_bar_source_text_view);
        recyclerViewInstructions = findViewById(R.id.directions_recycler_view);
        constraintLayout = findViewById(R.id.search_constraint_layout);
        goButton = findViewById(R.id.go_button);
        hideSearch();

        Button goButton = findViewById(R.id.go_button);
        goButton.setEnabled(false);
        doneNavigationButton = findViewById(R.id.done_navigation_botton);
        showNavigationSrcDest = findViewById(R.id.show_navigation_src_dest_text_view);
        showNavigationSrcDest.setVisibility(View.GONE);
        recyclerViewInstructions.setVisibility(View.GONE);
        doneNavigationButton.setVisibility(View.GONE);
        doneNavigationButton.setOnClickListener(view -> {
            showHomeUI();
            // todo: make the correct views visible/invisible
        });

        // InstructionsAdapter instructionsAdapter = new InstructionsAdapter(this, null);
        Navigator finalNavigator = app.getDB().getNavigator();
        goButton.setOnClickListener(view -> {
            String src = searchBarSourceTextView.getText().toString();
            String dest = searchBarDestTextView.getText().toString();
            if (src.equals(dest)) {
                Toasty.info(this, "Start and destination are the same.",
                        Toast.LENGTH_SHORT, true).show();
            } else {
                if(directions!=null)
                {
                    System.out.println("bla1: " + directions.size());
                }
                directions = finalNavigator.getDirections(dest, src);
                System.out.println("bla2: " + directions.size());
                if (directions.isEmpty()) {
                    Toasty.info(this, "No accessible route found.",
                            Toast.LENGTH_SHORT, true).show();
                } else {
                    //instructionsAdapter.setInstructions(directions);
                    //instructionsAdapter.notifyDataSetChanged();
                    InstructionsAdapter instructionsAdapter = new InstructionsAdapter(this, directions);
                    // todo : causes memory allocation problems ( clicking go multiple times)
                    recyclerViewInstructions.setAdapter(instructionsAdapter);
                    recyclerViewInstructions.setLayoutManager(new LinearLayoutManager(this));

                    showNavigationSrcDest.setVisibility(View.VISIBLE);
                    showNavigationSrcDest.setText(src+" -> "+dest);

                    hideSearch();
                    searchBarDestTextView.setVisibility(View.GONE);

                    recyclerViewInstructions.setVisibility(View.VISIBLE);
                    doneNavigationButton.setVisibility(View.VISIBLE);
                }
            }

        });

        searchBarDestTextView.setOnClickListener(view ->
                moveToSearchActivity(NaviableApplication.SEARCH_TYPE.DESTINATION));

        app.getChosenDestinationLiveDataPublic().observe(this, observedDestination -> {
            if (!observedDestination.isEmpty()) {
                searchBarDestTextView.setText(observedDestination);
                searchBarSourceTextView.setVisibility(View.VISIBLE);
                goButton.setVisibility(View.VISIBLE);
                constraintLayout.setBackground(searchBackground);
                tryEnableButton();
            }
        });

        searchBarSourceTextView.setOnClickListener(view ->
                moveToSearchActivity(NaviableApplication.SEARCH_TYPE.SOURCE));

        app.getChosenSourceLiveDataPublic().observe(this, observedSource -> {
            if (!observedSource.isEmpty()) {
                searchBarSourceTextView.setText(observedSource);
                tryEnableButton();
            }
        });

        app.getCampusChosenLiveDataPublic().observe(this, s -> updateMapLocation());
    }


    private void tryEnableButton() {
        if (searchBarDestTextView.getText().toString().isEmpty() ||
                searchBarSourceTextView.getText().toString().isEmpty()) {
            goButton.setEnabled(false);

        } else {
            goButton.setEnabled(true);
        }
    }

    private void hideSearch() {
        searchBarSourceTextView.setVisibility(View.GONE);
        goButton.setVisibility(View.GONE);
        constraintLayout.setBackgroundColor(0x00ffffff);
    }

    private void showHomeUI() {
        searchBarDestTextView.setVisibility(View.VISIBLE);
        recyclerViewInstructions.setVisibility(View.GONE);
        doneNavigationButton.setVisibility(View.GONE);
        searchBarDestTextView.setText("");
        searchBarSourceTextView.setText("");


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * <p>
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMapLocation();
    }

    private void moveToSearchActivity(NaviableApplication.SEARCH_TYPE type) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        boolean searchTypeIsDestinationSearch = type.equals(NaviableApplication.SEARCH_TYPE.DESTINATION);
        intent.putExtra("searchTypeIsDestinationSearch", searchTypeIsDestinationSearch);
        startActivity(intent);
    }

    // changes which campus we focus on in the map
    public void updateMapLocation() {
        LatLng campus = app.getDB().getCampus();
//        mMap.addMarker(new MarkerOptions()
//                .position(campus));
        // todo - *note*: zoom level is between 2.0 and 21.0
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campus, 18.5f));
    }
}
