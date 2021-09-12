package com.example.naviable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    RecyclerView recyclerViewSearchSuggestions;
    private MyAdapter.RecyclerViewClickListener clickListener;
    private EditText searchBarEditText;
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

        ArrayList<String> temporaryNamesForDebug = new ArrayList<>();
        temporaryNamesForDebug.add("Canada");
        temporaryNamesForDebug.add("Auditorium");
        temporaryNamesForDebug.add("Silberman");
        temporaryNamesForDebug.add("Feldman");
        temporaryNamesForDebug.add("Levi");
        temporaryNamesForDebug.add("Shprintzak");

        this.clickListener = new MyAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                System.out.println("clicked position is: " + position);
                // todo: pass to main the chosen location and if its "source" or "destination"
                if(searchTypeIsDestinationSearch){
                    app.setSearchDestination(temporaryNamesForDebug.get(position));
                }
                else{
                    app.setSearchSource(temporaryNamesForDebug.get(position));
                }
                finish();
            }
        };

        MyAdapter myAdapter = new MyAdapter(this, temporaryNamesForDebug, this.clickListener);
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
}