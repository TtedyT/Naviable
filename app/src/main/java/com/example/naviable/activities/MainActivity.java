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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private Drawable searchBackground;
    private GoogleMap mMap;
    private TextView searchBarDestTextView;
    private TextView searchBarSourceTextView;
    private Button goButton;
    private Button doneNavigationButton;
    private Navigator navigator;
    private NaviableApplication app;
    private ConstraintLayout constraintLayoutSearch;
    private final int ZOOM_OUT_FACTOR = 5;
    private RecyclerView recyclerViewInstructions;
    private Marker srcMarker;
    private Marker destMarker;
    private Polyline pathPolyline;
    private FloatingActionButton qrButton;
    private TextView showNavigationSrcDest;
    private ArrayList<Marker> categoryMarkers;
    private BottomNavigationView bottomNav;
    private MenuItem currentNavMenuItem;
    private boolean navBarFlag = false;

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

        qrButton = findViewById(R.id.qr_scan_button);
        qrButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, CodeScannerActivity.class);
            startActivity(intent);
        });
        qrButton.setVisibility(View.GONE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        initVars();

        Button goButton = findViewById(R.id.go_button);
        goButton.setEnabled(false);
        doneNavigationButton = findViewById(R.id.done_navigation_botton);
        showNavigationSrcDest = findViewById(R.id.show_navigation_src_dest_text_view);
        recyclerViewInstructions.setVisibility(View.GONE);
        doneNavigationButton.setVisibility(View.GONE);
        doneNavigationButton.setOnClickListener(view -> {
            showHomeUI();
            // todo: make the correct views visible/invisible
        });


        navigator = app.getDB().getNavigator();
        goButton.setOnClickListener(view -> {
            String src = searchBarSourceTextView.getText().toString();
            String dest = searchBarDestTextView.getText().toString();
            if (src.equals(dest)) {
                Toasty.info(this, "Start and destination are the same.",
                        Toast.LENGTH_SHORT, true).show();
            } else {
                List<Direction> directions = navigator.getDirections(src, dest);
                if (directions.isEmpty()) {
                    Toasty.info(this, "No accessible route found.",
                            Toast.LENGTH_SHORT, true).show();
                } else {

                    InstructionsAdapter instructionsAdapter = new InstructionsAdapter(this, directions);
                    // todo : causes memory allocation problems ( clicking go multiple times)
                    showNavigationSrcDest.setText(src + " -> " + dest);
                    recyclerViewInstructions.setAdapter(instructionsAdapter);
                    recyclerViewInstructions.setLayoutManager(new LinearLayoutManager(this));
                    hideSearch();
                    searchBarDestTextView.setVisibility(View.GONE);

                    recyclerViewInstructions.setVisibility(View.VISIBLE);
                    doneNavigationButton.setVisibility(View.VISIBLE);

                    ArrayList<LatLng> path = navigator.getPathLatLng(src, dest);
                    pathPolyline = drawPathOnMap(path);
                }
            }

        });

        bottomNav = findViewById(R.id.bottom_navigation_bar);
        bottomNav.setOnItemSelectedListener(navListener);
        uncheckAllMenuItems();

        searchBarDestTextView.setOnClickListener(view ->
                moveToSearchActivity(NaviableApplication.SEARCH_TYPE.DESTINATION));

        app.getChosenDestinationLiveDataPublic().observe(this, observedDestination -> {
            if (!observedDestination.isEmpty()) {
                onDestChangedAction(observedDestination);
            }
        });

        searchBarSourceTextView.setOnClickListener(view ->
                moveToSearchActivity(NaviableApplication.SEARCH_TYPE.SOURCE));

        app.getChosenSourceLiveDataPublic().observe(this, observedSource -> {
            if (!observedSource.isEmpty()) {
                onSrcChangedAction(observedSource);
            }
        });

        app.getCampusChosenLiveDataPublic().observe(this, s -> updateMapLocation());
    }

    private void deletePathFromMap() {
        pathPolyline.remove();
    }

    private Polyline drawPathOnMap(ArrayList<LatLng> path) {
        PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(10);
        return mMap.addPolyline(opts);
    }

    private void initVars() {
        searchBackground = ContextCompat.getDrawable(this,
                R.drawable.rounded_rectangle_view_search_background);
        app = NaviableApplication.getInstance();
        searchBarDestTextView = findViewById(R.id.search_bar_dest_text_view);
        searchBarSourceTextView = findViewById(R.id.search_bar_source_text_view);
        recyclerViewInstructions = findViewById(R.id.directions_recycler_view);
        constraintLayoutSearch = findViewById(R.id.search_constraint_layout);
        goButton = findViewById(R.id.go_button);
        doneNavigationButton = findViewById(R.id.done_navigation_botton);
        hideSearch();
    }

    private void onSrcChangedAction(String observedSource) {
        searchBarSourceTextView.setText(observedSource);
        LatLng sourceCoordinate = navigator.getCoordinate(observedSource);
        if (srcMarker != null) {
            srcMarker.remove();
        }
        srcMarker = mMap.addMarker(new MarkerOptions().position(sourceCoordinate)
                .title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(183)));
        tryEnableButton();
    }

    /**
     * Run this function every time a change is detected to dest search view
     *
     * @param observedDestination new destination entered
     */
    private void onDestChangedAction(String observedDestination) {
        searchBarDestTextView.setText(observedDestination);
        searchBarSourceTextView.setVisibility(View.VISIBLE);
        qrButton.setVisibility(View.VISIBLE);
        goButton.setVisibility(View.VISIBLE);
        constraintLayoutSearch.setBackground(searchBackground);
        LatLng destCoordinate = navigator.getCoordinate(observedDestination);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destCoordinate, 17.5f));
        if (destMarker != null) {
            destMarker.remove();
        }
        destMarker = mMap.addMarker(new MarkerOptions().position(destCoordinate).title(observedDestination));

        tryEnableButton();
    }

    /**
     * try enable go button if source and dest are set, keep disabled otherwise.
     */
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
        qrButton.setVisibility(View.GONE);
        goButton.setVisibility(View.GONE);
        constraintLayoutSearch.setBackgroundColor(0x00ffffff);
    }

    private void showHomeUI() {
        srcMarker.remove();
        destMarker.remove();

        searchBarDestTextView.setVisibility(View.VISIBLE);
        recyclerViewInstructions.setVisibility(View.GONE);
        doneNavigationButton.setVisibility(View.GONE);
        searchBarDestTextView.setText("");
        searchBarSourceTextView.setText("");
        deletePathFromMap();
    }

    private NavigationBarView.OnItemSelectedListener navListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (categoryMarkers == null) {
                categoryMarkers = new ArrayList<>();
            }
            clearCategoryMarkers();

            if (item == currentNavMenuItem & navBarFlag) {
                // Deselect item
                uncheckAllMenuItems();
                currentNavMenuItem = item;
                navBarFlag = false;

                return false;
            }
            else {
                ArrayList<String> locations = new ArrayList<>();
                switch (item.getItemId()) {
                    case R.id.toilets:
                        locations = new ArrayList<String>(app.getDB().getToiletLocations());
                        break;
                    case R.id.restaurants:
                        locations = new ArrayList<String>(app.getDB().getRestaurantLocations());
                        break;
                    case R.id.cafes:
                        locations = new ArrayList<String>(app.getDB().getCafeLocations());
                        break;
                    case R.id.libraries:
                        locations = new ArrayList<String>(app.getDB().getLibraryLocations());
                        break;
                }

                for (String locationName : locations){
                    LatLng locationCoordinate = navigator.getCoordinate(locationName);
                    categoryMarkers.add(mMap.addMarker(new MarkerOptions().position(locationCoordinate)
                            .title(locationName).icon(BitmapDescriptorFactory.defaultMarker(183))));
                }

                currentNavMenuItem = item;
                navBarFlag = true;

                return true;
            }
        }
    };

    private void uncheckAllMenuItems() {
        Menu menu = bottomNav.getMenu();
        menu.setGroupCheckable(0, true, false);
        for (int i = 0; i < menu.size(); i++){
            menu.getItem(i).setChecked(false);
        }
        menu.setGroupCheckable(0, true, true);
    }

    private void clearCategoryMarkers() {
        for (int i = 0; i < categoryMarkers.size(); i++) {
            categoryMarkers.get(i).remove();
        }
        categoryMarkers.removeAll(categoryMarkers);
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
        // note: zoom level is between 2.0 and 21.0
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campus, 17.25f));
    }

}
