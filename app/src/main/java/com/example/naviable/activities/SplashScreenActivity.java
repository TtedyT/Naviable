package com.example.naviable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.naviable.R;

public class SplashScreenActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		new Handler().postDelayed(() -> {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}, 100);
		// todo change the delay back to 3000
		//      for debug options its now 100
	}
}