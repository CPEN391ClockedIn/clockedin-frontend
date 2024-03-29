package com.example.clockedin.ui.home;

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

import com.example.clockedin.ClockinActivity;
import com.example.clockedin.MainActivity;
import com.example.clockedin.User;
import com.example.clockedin.R;
import com.example.clockedin.ServerCommu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "HomeFragment";
    private User user;
    private TextView text_date;
    private TextView text_tmp;
    private Button button_in;
    private Button button_out;
    private String string_date;
    private String string_tmp;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        user = MainActivity.getUser();
        text_date = root.findViewById(R.id.text_date);
        text_tmp = root.findViewById(R.id.text_tmp);
        button_in = root.findViewById(R.id.button_clockin);
        button_out = root.findViewById(R.id.button_clockout);

        button_in.setOnClickListener(this);
        button_out.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        string_date = dateFormat.format(calendar.getTime());
        text_date.setText(string_date);
        getTemperature();

        return root;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_clockin) {
            clockIn();
        }
        else if (view.getId() == R.id.button_clockout) {
            clockOut();
        }
    }

    private void clockIn() {
        Intent intent = new Intent(HomeFragment.this.getActivity(), ClockinActivity.class);
        startActivityForResult(intent, 1);
    }

    private void clockOut() {
        ServerCommu server = new ServerCommu();

        server.postWithAuth("http://clockedin-env.eba-dqrpikem.ca-central-1.elasticbeanstalk.com/api/history/clockout", "", user.getToken(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "clockout is successful: " + response.body().toString());
                }
                else {
                    Log.d(TAG, "failed to clockout");
                }
            }
        });
    }

    private void getTemperature() {
        ServerCommu server = new ServerCommu();

        server.getWithAuth("http://clockedin-env.eba-dqrpikem.ca-central-1.elasticbeanstalk.com/api/temperature/today", user.getToken(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                JSONArray responseJson;
                try {
                    responseJson = new JSONObject(responseStr).getJSONArray("record");
                    if (responseJson.getJSONObject(0).has("temperature")) {
                        string_tmp = Double.toString(responseJson.getJSONObject(0).getDouble("temperature"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response.isSuccessful()) {
                    Log.d(TAG, "get temperature is successful");
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_tmp.setText(string_tmp + " °C");
                        }
                    });
                }
                else {
                    Log.d(TAG, "failed to get temperature");
                }
            }
        });
    }
}