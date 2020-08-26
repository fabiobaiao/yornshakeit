package com.example.yornshakeit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Intent intent = getIntent();
        String sessionId = intent.getStringExtra("sessionId");

        retrieveMainUserdata(sessionId);
    }

    private void retrieveMainUserdata(final String sessionId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-mCare-Session", sessionId);
        headers.put("x-mcare-device", "android");

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("headers", headers);

        Requests.get("https://mw.my.vodafone.pt/RESTv2/retrievemainuserdata.json", args)
                .thenAccept((response -> {
                    try {
                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();

                        String msisdn = responseText.getString("msisdn");

                        if (msisdn.equals("918587695")) {
                            MSSOLogin(sessionId);
                        }
                        else {
                            SharedPreferences.Editor editor = getSharedPreferences("com.example.yornshakeit.PREFERENCE_FILE_SESSIONID", Context.MODE_PRIVATE).edit();
                            editor.remove("sessionId");
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                    } catch (JSONException ignored) {}
                }));
    }


    private void MSSOLogin(String sessionId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-mCare-Session", sessionId);

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("headers", headers);

        Requests.get("https://mw.my.vodafone.pt/MSSOLogin.get", args)
                .thenAccept(response -> {
                    try {
                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();

                        JSONArray headersToSend = responseText.getJSONArray("headersToSend");
                        for (int i = 0; i < headersToSend.length(); i++) {
                            JSONObject headerToSend = headersToSend.getJSONObject(i);
                            if (headerToSend.getString("value").contains("x-sm-identity")) {
                                Session.x_sm_identity = headerToSend.getString("value").split("; ")[0].split("=")[1];
                            } else if (headerToSend.getString("value").contains("ObSSOCookie")) {
                                Session.ObSSOCookie = headerToSend.getString("value").split("; ")[0].split("=")[1] + "=";
                            }
                        }

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);

                    } catch (JSONException ignore) {}
                });
    }
}
