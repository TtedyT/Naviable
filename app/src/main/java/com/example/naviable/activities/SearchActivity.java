package com.example.naviable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.naviable.SearchResultsAdapter;
import com.example.naviable.NaviableApplication;
import com.example.naviable.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

	private static final int SPEECH_INPUT = 1;

	RecyclerView recyclerViewSearchSuggestions;
	private SearchResultsAdapter.RecyclerViewClickListener clickListener;
	private EditText searchBarEditText;
	private ImageButton micVoiceBtn;
	private NaviableApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		Intent intent = getIntent();
		// default value never used
		boolean searchTypeIsDestinationSearch = intent.getBooleanExtra("searchTypeIsDestinationSearch", false);

		searchBarEditText = findViewById(R.id.search_bar_edit_text);
		String hint = searchTypeIsDestinationSearch ? getResources().getString(R.string.dest_edit_text_hint) :
				getResources().getString(R.string.src_edit_text_hint);
		searchBarEditText.setHint(hint);
		searchBarEditText.requestFocus(); // focuses on the search on when entering this screen
		ImageButton backButton = findViewById(R.id.back_button_search);
		micVoiceBtn = findViewById(R.id.voiceBtn);

		micVoiceBtn.setOnClickListener(view -> {
			Intent intent1 = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent1.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent1.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
			intent1.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech To Text");

			try {
				startActivityForResult(intent1, SPEECH_INPUT);
			} catch (Exception e) {
				Toast.makeText(SearchActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
		backButton.setOnClickListener(view -> {
			finish();
		});
		recyclerViewSearchSuggestions = findViewById(R.id.search_suggestions_recycler_view);
		app = NaviableApplication.getInstance();

		ArrayList<String> locations = new ArrayList<String>(app.getDB().getLocations());

		ArrayList<String> recentSearchedLocations = new ArrayList<>();
		Object[] recentSearchedLocationsObjectArr = app.getDB().getRecentLocationsStaticArray();
		for (Object o : recentSearchedLocationsObjectArr) {
			recentSearchedLocations.add(o.toString());
		}

		ArrayList<String> searchSuggestionsNotRecents = new ArrayList<>(locations);
		searchSuggestionsNotRecents.removeAll(recentSearchedLocations);

		ArrayList<String> updatedLocationRecentThenNotRecent = new ArrayList<>(recentSearchedLocations);
		updatedLocationRecentThenNotRecent.addAll(searchSuggestionsNotRecents);

		this.clickListener = (v, position) -> {
			TextView clickedSuggestionText = v.findViewById(R.id.mySearchSuggestionTextView);
			String location = clickedSuggestionText.getText().toString();
			if (searchTypeIsDestinationSearch) {
				app.setSearchDestination(location);
			} else {
				app.setSearchSource(location);
			}
			app.getDB().addRecentLocation(location);
			finish();
		};

		SearchResultsAdapter searchResultsAdapter = new SearchResultsAdapter(this, searchSuggestionsNotRecents, recentSearchedLocations, this.clickListener);
		recyclerViewSearchSuggestions.setAdapter(searchResultsAdapter);
		recyclerViewSearchSuggestions.setLayoutManager(new LinearLayoutManager(this));

		searchBarEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				searchResultsAdapter.getFilter().filter(charSequence);
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("resultCode: " + resultCode);
		if (requestCode == SPEECH_INPUT) {
			if ((resultCode == RESULT_OK) && (data != null)) {
				ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				searchBarEditText.setText(Objects.requireNonNull((result).get(0)));
			}
		}
	}
}