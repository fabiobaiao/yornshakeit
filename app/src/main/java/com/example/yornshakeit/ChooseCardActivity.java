package com.example.yornshakeit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChooseCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_card);

        RecyclerView tradeableCards = findViewById(R.id.tradeableCards);
        tradeableCards.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        tradeableCards.setAdapter(new TradeableCardsAdapter(intent.getStringExtra("friend"), intent.getStringExtra("tradeableCards")));
    }

    private void selectCard(String friend, String cardId) {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("x-sm-identity", Session.x_sm_identity);
        cookies.put("ObSSOCookie", Session.ObSSOCookie);

        Map<String, Map<String, String>> args = new HashMap<>();
        args.put("cookies", cookies);

        Map<String, String> data = new HashMap<>();
        data.put("friend", friend);
        data.put("cardId", cardId);

        Requests.post("https://yornshakeit.vodafone.pt/rest/cards/exchange/friend/selectcard", args, data)
                .thenAccept(response -> {
                    try {
                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();

                        switch (responseText.getString("result")) {
                            case "CARD_SELECT_PENDING":
                                await(5000).thenRun(() -> friendStatus(friend));
                                break;
                            case "CARD_SELECT_OK":
                                confirmExchange(friend, responseText.getJSONObject("sentCard").toString(), responseText.getJSONObject("receivedCard").toString());
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
                            case "CARD_SELECT_PENDING":
                                await(5000).thenRun(() -> friendStatus(friend));
                                break;
                            case "CARD_SELECT_OK":
                                confirmExchange(friend, responseText.getJSONObject("sentCard").toString(), responseText.getJSONObject("receivedCard").toString());
                                break;
                        }
                    } catch (JSONException ignore) {}
                });
    }

    private void confirmExchange(String friend, String sentCard, String receivedCard) {
        Intent intent = new Intent(getApplicationContext(), ConfirmExchangeActivity.class);
        intent.putExtra("friend", friend);
        intent.putExtra("sentCard", sentCard);
        intent.putExtra("receivedCard", receivedCard);
        startActivity(intent);
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

    private class TradeableCardsAdapter extends RecyclerView.Adapter<TradeableCardsAdapter.ViewHolder> {

        private String friend;
        private JSONArray tradeableCards;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public TextView textView;

            public ViewHolder(View view) {
                super(view);
                this.textView = view.findViewById(R.id.tradeableCard);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                try {
                    JSONObject tradeableCard = tradeableCards.getJSONObject(position);
                    selectCard(friend, Integer.toString(tradeableCard.getInt("cardId")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public TradeableCardsAdapter(String friend, String tradeableCards) {
            this.friend = friend;
            try {
                this.tradeableCards = (JSONArray) new JSONTokener(tradeableCards).nextValue();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tradeablecard, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject tradeableCard = this.tradeableCards.getJSONObject(position);
                int numberOfRepeats = tradeableCard.getInt("numberOfRepeats");
                holder.textView.setText(tradeableCard.getInt("cardPosition") + (numberOfRepeats > 1 ? " (x" + numberOfRepeats + ")" : ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return tradeableCards.length();
        }
    }
}
