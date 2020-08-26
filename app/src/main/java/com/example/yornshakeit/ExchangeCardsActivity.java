package com.example.yornshakeit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class ExchangeCardsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_cards);
    }

    public void exchangeWithFriends(View view) {
        cardsExchangeInitialize();
    }

    private void cardsExchangeInitialize() {
        ProgressBar progressBar = findViewById(R.id.initializing);
        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }
        TextView textView = findViewById(R.id.no3g);
        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        Map<String, String> cookies = new HashMap<>();
        cookies.put("x-sm-identity", Session.x_sm_identity);
        cookies.put("ObSSOCookie", Session.ObSSOCookie);

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("cookies", cookies);

        Requests.get("https://yornshakeit.vodafone.pt/rest/cards/exchange/initialize", args)
                .thenAccept(response -> {
                    try {
                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();
                        String serviceUrl = responseText.getString("serviceUrl");
                        serviceUrl(serviceUrl);
                    } catch (JSONException ignore) {}
                });
    }

    private void serviceUrl(String serviceUrl) {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("x-sm-identity", Session.x_sm_identity);
        cookies.put("ObSSOCookie", Session.ObSSOCookie);

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("cookies", cookies);

        Requests.get(serviceUrl, args)
                .thenAccept(response -> {
                    String location = response.getHeader("Location").get(0);

                    if (location.contains("USER_IS_IN_3G")) {
                        Intent intent = new Intent(getApplicationContext(), ChooseFriendActivity.class);
                        startActivity(intent);
                        runOnUiThread(() -> {
                            ProgressBar progressBar = findViewById(R.id.initializing);
                            progressBar.setVisibility(View.GONE);
                        });
                    }
                    else if (location.contains("ERROR_NOT_POSSIBLE_TO_AUTHENTICATE_USER")) {
                        runOnUiThread(() -> {
                            ProgressBar progressBar = findViewById(R.id.initializing);
                            progressBar.setVisibility(View.GONE);
                            TextView textView = findViewById(R.id.no3g);
                            textView.setVisibility(View.VISIBLE);
                        });
                    }
                });
    }
}
