package com.example.naviable.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.naviable.MyAdapter;
import com.example.naviable.NaviableApplication;
import com.example.naviable.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private static final int SPEECH_INPUT = 1;

    RecyclerView recyclerViewSearchSuggestions;
    private MyAdapter.RecyclerViewClickListener clickListener;
    private EditText searchBarEditText;
    private ImageButton micVoiceBtn;
    private NaviableApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // todo: use this to get the search type (source) or destination
        Intent intent = getIntent();
        // default value never used
        boolean searchTypeIsDestinationSearch = intent.getBooleanExtra("searchTypeIsDestinationSearch", false);

        searchBarEditText = (EditText) findViewById(R.id.search_bar_edit_text);
        searchBarEditText.requestFocus(); // focuses on the search on when entering this screen
        ImageButton backButton = findViewById(R.id.back_button_search);
        micVoiceBtn = findViewById(R.id.voiceBtn);
        micVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech To Text");

                try {
                    startActivityForResult(intent, SPEECH_INPUT);
                }
                catch (Exception e){
                    Toast.makeText(SearchActivity.this," " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        backButton.setOnClickListener(view -> {
            finish();
        });
        recyclerViewSearchSuggestions = (RecyclerView) findViewById(R.id.search_suggestions_recycler_view);
        app = NaviableApplication.getInstance();
//        searchBarEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
////                recyclerViewSearchSuggestions.setVisibility(View.VISIBLE);
//                // instead ill tey to set the height to his original height:
//                recyclerViewSearchSuggestions.requestLayout();
//                recyclerViewSearchSuggestions.getLayoutParams().height = 400;
//            }
//        });

        List<String> locations = app.getDB().getLocations();
        this.clickListener = new MyAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                System.out.println("clicked position is: " + position);
                // todo: pass to main the chosen location and if its "source" or "destination"
                if(searchTypeIsDestinationSearch){
                    app.setSearchDestination(locations.get(position));
                }
                else{
                    app.setSearchSource(locations.get(position));
                }
                finish();
            }
        };

        MyAdapter myAdapter = new MyAdapter(this, (ArrayList<String>) locations, this.clickListener);
        recyclerViewSearchSuggestions.setAdapter(myAdapter);
        recyclerViewSearchSuggestions.setLayoutManager(new LinearLayoutManager(this));

        searchBarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                myAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("resultCode: " + resultCode);
        if(requestCode == SPEECH_INPUT)
        {
            if((resultCode == RESULT_OK) && (data != null))
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                searchBarEditText.setText(Objects.requireNonNull((result).get(0)));
            }
        }
    }
}