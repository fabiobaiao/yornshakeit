package com.example.yornshakeit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ConfirmExchangeActivity extends AppCompatActivity {

    private String friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_exchange);

        Intent intent = getIntent();
        this.friend = intent.getStringExtra("friend");
        try {
            JSONObject sentCard = (JSONObject) new JSONTokener(intent.getStringExtra("sentCard")).nextValue();
            JSONObject receivedCard = (JSONObject) new JSONTokener(intent.getStringExtra("receivedCard")).nextValue();

            TextView textView;
            textView = findViewById(R.id.sending);
            textView.setText("" + sentCard.getInt("position"));

            textView = findViewById(R.id.receiving);
            textView.setText("" + receivedCard.getInt("position"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void confirmExchange(View view) {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("x-sm-identity", Session.x_sm_identity);
        cookies.put("ObSSOCookie", Session.ObSSOCookie);

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("cookies", cookies);

        Map<String, String> data = new HashMap<>();
        data.put("friend", this.friend);

        Requests.post("https://yornshakeit.vodafone.pt/rest/cards/exchange/friend/confirm", args, data)
                .thenAccept(response -> {
                    try {
                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();

                        switch (responseText.getString("result")) {
                            case "CARD_TRADE_PENDING":
                                await(5000).thenRun(() -> friendStatus(this.friend));
                                break;
                            case "CARD_TRADE_OK":
                                // guessName();
                                break;
                        }
                    } catch (JSONException ignore) {}
                });
    }

    private void friendStatus(final String friend) {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("x-sm-identity", Session.x_sm_identity);
        cookies.put("ObSSOCookie", Session.ObSSOCookie);

        Map<String, String> params = new HashMap<>();
        params.put("friend", friend);

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("cookies", cookies);
        args.put("params", params);

        Requests.get("https://yornshakeit.vodafone.pt/rest/cards/exchange/friend/status", args)
                .thenAccept(response -> {
                    try {
                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();

                        switch (responseText.getString("result")) {
                            case "CARD_TRADE_PENDING":
                                await(5000).thenRun(() -> friendStatus(friend));
                                break;
                            case "CARD_TRADE_OK":
                                //guessName();
                                break;
                        }
                    } catch (JSONException ignore) {}
                });
    }

    private static void guessName(int cardId, String cardName) {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("x-sm-identity", Session.x_sm_identity);
        cookies.put("ObSSOCookie", Session.ObSSOCookie);

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("cookies", cookies);

        JSONObject json = new JSONObject();
        try {
            json.put("cardId", cardId);
            json.put("cardName", cardName);
            json.put("time", 10);

            Requests.post("https://yornshakeit.vodafone.pt/rest/cards/guessname", args, json)
                    .thenAccept(response -> {
                        // ...
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<Void> await(long millis) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Thread.sleep(millis);
                cf.complete(null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        return cf;
    }
}
