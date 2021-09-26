package com.example.naviable.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.naviable.DB;
import com.example.naviable.NaviableApplication;
import com.example.naviable.R;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB db = NaviableApplication.getInstance().getDB();
        setContentView(R.layout.activity_settings);
        Spinner spinner = findViewById(R.id.campus_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.campuses_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(db.getSpinnerChosenOption());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                db.setCampus(spinner.getSelectedItem().toString());
                db.saveSpinnerChosenOption(spinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        ImageButton backButton = findViewById(R.id.back_button_settings);
        backButton.setOnClickListener(view -> {
            finish();
        });
    }

}
