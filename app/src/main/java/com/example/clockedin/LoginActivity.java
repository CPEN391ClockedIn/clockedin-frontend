package com.example.clockedin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.EditText;
import android.view.View;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private EditText text_email, text_password;
    private Button button;
    private String string_email, string_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        text_email = findViewById(R.id.text_email_login);
        text_password = findViewById(R.id.text_pwd_login);
        button = findViewById(R.id.button_login);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_login) { login(); }
    }

    private void login() {
        string_email = text_email.getText().toString().trim();
        string_pwd = text_password.getText().toString().trim();

        JSONObject data = new JSONObject();
        try {
            data.put("email", string_email);
            data.put("password", string_pwd);
            postToLogin(data);
        } catch (JSONException e ) {
            e.printStackTrace();
        }
    }

    private void postToLogin(final JSONObject userData) {
        ServerCommu server = new ServerCommu();
        final String data = userData.toString();

        server.post("http://clockedin-env.eba-dqrpikem.ca-central-1.elasticbeanstalk.com/api/employee/login", data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                JSONObject responseJson;
                String string_token = "";
                try {
                    responseJson = new JSONObject(responseStr);
                    if (responseJson.has("token")) {
                        string_token = responseJson.getString("token");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    Log.d(TAG, "login is successful");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("user", new User(string_token, null, string_email));
                    startActivity(intent);
                }
                else {
                    Log.d(TAG, "failed to login");
                }
            }
        });
    }
}
