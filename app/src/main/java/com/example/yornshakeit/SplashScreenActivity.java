package com.example.yornshakeit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences sharedPref = getSharedPreferences("com.example.yornshakeit.PREFERENCE_FILE_SESSIONID", Context.MODE_PRIVATE);
        String sessionId = sharedPref.getString("sessionId", null);
        if (sessionId == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
            intent.putExtra("sessionId", sessionId);
            startActivity(intent);
        }
    }
}
