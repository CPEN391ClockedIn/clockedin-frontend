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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RegisterActivity";
    private EditText text_name, text_email, text_pwd;
    private Button button;
    private String string_name, string_email, string_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        text_name = findViewById(R.id.text_name_register);
        text_email = findViewById(R.id.text_email_register);
        text_pwd = findViewById(R.id.text_pwd_register);
        button = findViewById(R.id.button_register);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_register) { register(); }
    }

    private void register() {
        string_name = text_name.getText().toString().trim();
        string_email = text_email.getText().toString().trim();
        string_pwd = text_pwd.getText().toString().trim();

        JSONObject data = new JSONObject();
        try {
            data.put("email", string_email);
            data.put("password", string_pwd);
            data.put("name", string_name);
            Log.d(TAG, "register data is " + data);
            postToRegister(data);
        } catch (JSONException e ) {
            e.printStackTrace();
        }
    }

    private void postToRegister(final JSONObject userData) {
        ServerCommu server = new ServerCommu();
        final String data = userData.toString();

        server.post("http://clockedin-env.eba-dqrpikem.ca-central-1.elasticbeanstalk.com/api/employee/signup", data, new Callback() {
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
                    Log.d(TAG, "register is successful");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("user", new User(string_token, string_name, string_email));
                    startActivity(intent);
                }
                else {
                    Log.d(TAG, "register not successful");
                }
            }
        });
    }
}
