package com.example.naviable.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TextToSpeech.OnInitListener {


	private static final int DEF_PEEK_HEIGHT = 240;
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
	private Marker srcMarker;
	private Marker destMarker;
	private Polyline pathPolyline;
	private ImageButton qrButton;
	private TextView showNavigationSrcDest;
	private ArrayList<Marker> categoryMarkers;
	private BottomNavigationView bottomNav;
	private MenuItem currentNavMenuItem;
	private boolean navBarFlag = false;
	private View layoutBottomSheet;
	private BottomSheetBehavior<View> sheetBehavior;
	private TextView showSrcDestTextView;
	private TextView orScanLocationTextView;
	private TextToSpeech textToSpeech;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ImageButton settingsButton = findViewById(R.id.settings_button);
		settingsButton.setOnClickListener(view -> {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		});

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

		goButton.setEnabled(false);

		navigator = app.getDB().getNavigator();
		goButton.setOnClickListener(view -> goButtonAction());

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


		if (savedInstanceState != null) {
			updateOnScreenRotation(savedInstanceState);
		}
	}

	// text to speech code start
	@Override
	public void onDestroy() {
		// Don't forget to shutdown!
		if (textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub

		if (status == TextToSpeech.SUCCESS) {

			int result = textToSpeech.setLanguage(Locale.US);

			// tts.setPitch(5); // set pitch level
			// tts.setSpeechRate(2); // set speech speed rate

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Toasty.info(this, "Language is not supported",
						Toast.LENGTH_SHORT, true).show();
			} else {
//				btnSpeak.setEnabled(true);
				// speakOut(); // what do i need it for ?
			}

		} else {
			Toasty.info(this, "Text to speech Initialization Failed",
					Toast.LENGTH_SHORT, true).show();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("dest_selected", searchBarDestTextView.getText().toString().isEmpty());
		outState.putString("dest_name", searchBarDestTextView.getText().toString());

		outState.putBoolean("src_selected", searchBarSourceTextView.getText().toString().isEmpty());
		outState.putString("src_name", searchBarSourceTextView.getText().toString());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			bottomNav.setVisibility(View.GONE);
		}
		else {
			bottomNav.setVisibility(View.VISIBLE);
		}
		super.onConfigurationChanged(newConfig);
	}

	private void updateOnScreenRotation(Bundle savedInstanceState) {
		if (savedInstanceState.getBoolean("dest_selected")) {
			String dest = savedInstanceState.getString("dest_name");

			searchBarDestTextView.setText(dest);
			searchBarSourceTextView.setVisibility(View.VISIBLE);
			orScanLocationTextView.setVisibility(View.VISIBLE);
			qrButton.setVisibility(View.VISIBLE);
			goButton.setVisibility(View.VISIBLE);
			constraintLayoutSearch.setBackground(searchBackground);
			LatLng destCoordinate = navigator.getCoordinate(dest);
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destCoordinate, 17.5f));

			if (destMarker != null) {
				destMarker.remove();
			}
			destMarker = mMap.addMarker(new MarkerOptions().position(destCoordinate).title(dest));
		}

		if (savedInstanceState.getBoolean("src_selected")) {
			String src = savedInstanceState.getString("src_name");
			searchBarSourceTextView.setText(src);

			LatLng sourceCoordinate = navigator.getCoordinate(src);
			if (srcMarker != null) {
				srcMarker.remove();
			}
			srcMarker = mMap.addMarker(new MarkerOptions().position(sourceCoordinate)
					.title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(183)));
		}
		tryEnableButton();
	}

	// we have text to speech object inside the instructions adapter
//	private void speakOut(String textToReadOutLoud) {
//		textToSpeech.speak(textToReadOutLoud, TextToSpeech.QUEUE_FLUSH, null, null);
//	}

	// text to speech code end

	private void goButtonAction() {
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

				RecyclerView recyclerView = findViewById(R.id.rcv_data);

				LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
				InstructionsAdapter adapter = new InstructionsAdapter(this, directions, textToSpeech);
				RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

				searchBarDestTextView.setClickable(false);
				showSrcDestTextView.setText(src + " ??? " + dest);
				recyclerView.setLayoutManager(linearLayoutManager);
				recyclerView.setAdapter(adapter);
				recyclerView.addItemDecoration(itemDecoration);
				sheetBehavior.setPeekHeight(DEF_PEEK_HEIGHT);
				hideSearch();


				ArrayList<LatLng> path = navigator.getPathLatLng(src, dest);
				pathPolyline = drawPathOnMap(path);
			}
		}
	}

	private void deletePathFromMap() {
		pathPolyline.remove();
	}

	private Polyline drawPathOnMap(ArrayList<LatLng> path) {
		PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(10);
		return mMap.addPolyline(opts);
	}

	private void initVars() {
		textToSpeech = new TextToSpeech(this, this);
		searchBackground = ContextCompat.getDrawable(this,
				R.drawable.rounded_rectangle_view_search_background);
		app = NaviableApplication.getInstance();
		doneNavigationButton = findViewById(R.id.done_navigation_btn);
		doneNavigationButton.setOnClickListener(view -> showHomeUI());
		showSrcDestTextView = findViewById(R.id.show_src_dest_text_view);
		searchBarDestTextView = findViewById(R.id.search_bar_dest_text_view);
		searchBarSourceTextView = findViewById(R.id.search_bar_source_text_view);
		constraintLayoutSearch = findViewById(R.id.search_constraint_layout);
		goButton = findViewById(R.id.go_button);
		doneNavigationButton = findViewById(R.id.done_navigation_btn);
		orScanLocationTextView = findViewById(R.id.or_scan_text_view);

		// Bottom Sheet Definitions
		layoutBottomSheet = findViewById(R.id.bottom_sheet);
		sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

		sheetBehavior.setPeekHeight(0);
		sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
		orScanLocationTextView.setVisibility(View.VISIBLE);
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
		orScanLocationTextView.setVisibility(View.GONE);
		constraintLayoutSearch.setBackgroundColor(0x00ffffff);
	}

	private void showHomeUI() {
		srcMarker.remove();
		destMarker.remove();

		searchBarDestTextView.setClickable(true);
		searchBarDestTextView.setVisibility(View.VISIBLE);
		searchBarDestTextView.setText("");
		searchBarSourceTextView.setText("");
		sheetBehavior.setPeekHeight(0);
		sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
			} else {
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

				for (String locationName : locations) {
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
		for (int i = 0; i < menu.size(); i++) {
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
