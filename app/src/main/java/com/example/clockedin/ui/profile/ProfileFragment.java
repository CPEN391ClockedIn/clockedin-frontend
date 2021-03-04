package com.example.clockedin.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.clockedin.LoginActivity;
import com.example.clockedin.MainActivity;
import com.example.clockedin.ServerCommu;
import com.example.clockedin.User;
import com.example.clockedin.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProfileFragment";
    private User user;
    private TextView text_name;
    private TextView text_email;
    private Button button;
    private String string_name;
    private String string_email;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        user = MainActivity.getUser();
        text_name = root.findViewById(R.id.text_name_profile);
        text_email = root.findViewById(R.id.text_email_profile);
        button = root.findViewById(R.id.button_logout);

        getProfile();
        button.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_logout) { logout(); }
    }

    private void logout() {
        user.setToken("");
        Intent intent = new Intent(this.getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private void getProfile() {
        ServerCommu server = new ServerCommu();

        server.getWithAuth("http://clockedin-env.eba-dqrpikem.ca-central-1.elasticbeanstalk.com/api/employee/me", user.getToken(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                JSONObject responseJson;
                try {
                    responseJson = new JSONObject(responseStr);
                    if (responseJson.has("name")) {
                        string_name = responseJson.getString("name");
                    }
                    if (responseJson.has("email")) {
                        string_email = responseJson.getString("email");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    Log.d(TAG, "logout is successful");
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_name.setText(string_name);
                            text_email.setText(string_email);
                        }
                    });
                }
                else {
                    Log.d(TAG, "failed to logout");
                }
            }
        });
    }
}