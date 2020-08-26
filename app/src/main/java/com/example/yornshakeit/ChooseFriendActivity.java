package com.example.yornshakeit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChooseFriendActivity extends AppCompatActivity {

    private boolean cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_friend);
    }

    public void friend(View view) {
        EditText editText = findViewById(R.id.friend);
        String friend = editText.getText().toString();

        if (friend.equals("")) {
            editText.setError("Número não pode ser vazio");
            return;
        }
        editText.setError(null);

        friendStart(friend);
    }

    private void friendStart(final String friend) {
        ProgressBar progressBar = findViewById(R.id.starting);
        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }
        TextView textView = findViewById(R.id.resultStart);
        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        Map<String, String> cookies = new HashMap<>();
        cookies.put("x-sm-identity", Session.x_sm_identity);
        cookies.put("ObSSOCookie", Session.ObSSOCookie);

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("cookies", cookies);

        Map<String, String> data = new HashMap<>();
        data.put("friend", friend);

        Requests.post("https://yornshakeit.vodafone.pt/rest/cards/exchange/friend/start", args, data)
                .thenAccept(response -> {
                    try {
                        if (cancel) {
                            cancel = false;
                            return;
                        }

                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();

                        switch (responseText.getString("result")) {
                            case "ERROR_GENERIC":
                                // await(5000).thenRun(() -> friendStart(friend));
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    textView.setText("Não foi possível encontrar o teu amigo");
                                    textView.setVisibility(View.VISIBLE);
                                });
                                break;
                            case "CONNECT_PENDING":
                                pending(friend);
                                break;
                            case "ERROR_NO_CARDS":
                                noCards();
                            case "CONNECT_OK":
                                chooseCard(friend, responseText.getJSONArray("tradeableCards"));
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
                        if (cancel) {
                            cancel = false;
                            return;
                        }

                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();

                        switch (responseText.getString("result")) {
                            // case "ERROR_GENERIC":
                            case "CONNECT_PENDING":
                                pending(friend);
                                break;
                            case "ERROR_NO_CARDS":
                                noCards();
                                break;
                            case "CONNECT_OK":
                                chooseCard(friend, responseText.getJSONArray("tradeableCards"));
                                break;
                        }
                    } catch (JSONException ignore) {}
                });
    }

    private void pending(String friend) {
        await(5000).thenRun(() -> friendStatus(friend));
        runOnUiThread(() -> {
            Button button;
            button = findViewById(R.id.start);
            button.setVisibility(View.GONE);
            button = findViewById(R.id.cancelStart);
            button.setVisibility(View.VISIBLE);
        });
    }

    private void noCards() {
        runOnUiThread(() -> {
            ProgressBar progressBar = findViewById(R.id.starting);
            progressBar.setVisibility(View.GONE);
            TextView textView = findViewById(R.id.resultStart);
            textView.setText("Não tens nenhuma carta que possas trocar");
            textView.setVisibility(View.VISIBLE);
        });
    }

    public void cancel(View view) {
        cancel = true;
        ProgressBar progressBar = findViewById(R.id.starting);
        progressBar.setVisibility(View.GONE);
        TextView textView = findViewById(R.id.resultStart);
        textView.setVisibility(View.GONE);
        Button button;
        button = findViewById(R.id.cancelStart);
        button.setVisibility(View.GONE);
        button = findViewById(R.id.start);
        button.setVisibility(View.VISIBLE);
    }

    private void chooseCard(String friend, JSONArray tradeableCards) {
        Intent intent = new Intent(getApplicationContext(), ChooseCardActivity.class);
        intent.putExtra("friend", friend);
        intent.putExtra("tradeableCards", tradeableCards.toString());
        startActivity(intent);
        runOnUiThread(() -> {
            ProgressBar progressBar = findViewById(R.id.starting);
            progressBar.setVisibility(View.GONE);
        });
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
