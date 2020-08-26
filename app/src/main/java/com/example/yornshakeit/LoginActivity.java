package com.example.yornshakeit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        ProgressBar progressBar = findViewById(R.id.loggingin);
        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }
        TextView textView = findViewById(R.id.loginerror);
        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        EditText editText;

        editText = findViewById(R.id.username);
        String username = editText.getText().toString();

        editText = findViewById(R.id.password);
        String password = editText.getText().toString();

        authCredentials(username, password);
    }

    private void authCredentials(String username, String password) {
        Map<String, String> data = new HashMap<>();
        data.put("MyVodafoneUsername", username);
        data.put("MyVodafonePassword", password);

        Requests.post("https://mw.my.vodafone.pt/RESTv2/authcredentials.json", new HashMap<>(), data)
                .thenAccept(response -> {
                    try {
                        JSONObject responseText = (JSONObject) new JSONTokener(response.getText()).nextValue();

                        switch(responseText.getString("statusMessage")) {
                            case "generic.error": // email ou password vazios
                            case "credentials": // password errada
                            case "drt_gig_005": // email inexistente
                            case "b_001": // email/nº inválido
                                runOnUiThread(() -> {
                                    ProgressBar progressBar = findViewById(R.id.loggingin);
                                    progressBar.setVisibility(View.GONE);
                                    TextView textView = findViewById(R.id.loginerror);
                                    textView.setVisibility(View.VISIBLE);
                                });
                                break;
                            case "success":
                                String sessionId = (String) responseText.get("sessionId");

                                SharedPreferences.Editor editor = getSharedPreferences("com.example.yornshakeit.PREFERENCE_FILE_SESSIONID", Context.MODE_PRIVATE).edit();
                                editor.putString("sessionId", sessionId);
                                editor.apply();

                                Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
                                intent.putExtra("sessionId", sessionId);
                                startActivity(intent);

                                finish();
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
    }
}
