package com.example.clockedin.ui.calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.clockedin.MainActivity;
import com.example.clockedin.R;
import com.example.clockedin.ServerCommu;
import com.example.clockedin.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CalendarFragment extends Fragment implements CalendarView.OnDateChangeListener {
    private static final String TAG = "CalendarFragment";
    private User user;
    private CalendarView calendar;
    private TextView text_in, text_out;
    private String string_in, string_out;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        user = MainActivity.getUser();
        calendar = root.findViewById(R.id.calendar);
        text_in = root.findViewById(R.id.text_in);
        text_out = root.findViewById(R.id.text_out);

        calendar.setOnDateChangeListener(this);
        return root;
    }

    @Override
    public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {
        String year = String.valueOf(i), month = String.valueOf(i1+1), day = String.valueOf(i2);
        if (i1 < 10) { month = "0" + month; }
        if (i2 < 10) { day = "0" + day; }
        getClockInOutTime(year + "-" + month + "-" + day);
    }

    private void getClockInOutTime(String dateTime) {
        ServerCommu server = new ServerCommu();

        server.getWithAuth("http://clockedin-env.eba-dqrpikem.ca-central-1.elasticbeanstalk.com/api/history/daily/" + dateTime, user.getToken(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                JSONObject responseJson;
                try {
                    responseJson = new JSONObject(responseStr).getJSONArray("record").getJSONObject(0);
                    if (responseJson.has("clockInTime")) {
                        string_in = responseJson.getString("clockInTime");
                    }
                    if (responseJson.has("clockOutTime")) {
                        string_out = responseJson.getString("clockOutTime");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    Log.d(TAG, "get time is successful");
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_in.setText(string_in.substring(0,5));
                            text_out.setText(string_out.substring(0,5));
                        }
                    });
                }
                else {
                    Log.d(TAG, "failed to get time");
                }
            }
        });
    }
}