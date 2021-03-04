package com.example.clockedin.ui.chart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.clockedin.MainActivity;
import com.example.clockedin.R;
import com.example.clockedin.ServerCommu;
import com.example.clockedin.User;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChartFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "ChartFragment";
    private User user;
    private LineChart chart;
    private Spinner spinner_year, spinner_month;
    private String string_year = "2021", string_month = "02";
    LineDataSet set;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chart, container, false);
        user = MainActivity.getUser();
        chart = root.findViewById(R.id.chart);
        spinner_year = root.findViewById(R.id.spinner_year);
        spinner_month = root.findViewById(R.id.spinner_month);

        chart.getXAxis().setTextSize(15);
        chart.getAxisLeft().setTextSize(15);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        ArrayAdapter<CharSequence> adapter_year = ArrayAdapter.createFromResource(getContext(), R.array.array_year, android.R.layout.simple_spinner_item);
        adapter_year.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_year.setAdapter(adapter_year);
        spinner_year.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapter_month = ArrayAdapter.createFromResource(getContext(), R.array.array_month, android.R.layout.simple_spinner_item);
        adapter_month.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_month.setAdapter(adapter_month);
        spinner_month.setOnItemSelectedListener(this);
        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.spinner_year) {
            string_year = adapterView.getItemAtPosition(i).toString();
        }
        if (adapterView.getId() == R.id.spinner_month) {
            string_month = adapterView.getItemAtPosition(i).toString();
        }
        getMonthlyTemperature(string_year + "-" + string_month);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    private void getMonthlyTemperature(String monthTime) {
        ServerCommu server = new ServerCommu();

        server.getWithAuth("http://clockedin-env.eba-dqrpikem.ca-central-1.elasticbeanstalk.com/api/temperature/monthly/" + monthTime, user.getToken(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                JSONArray responseJson;
                ArrayList<Entry> entries = new ArrayList<>();
                try {
                    responseJson = new JSONObject(responseStr).getJSONArray("records");
                    for (int i = 0; i < responseJson.length(); i++) {
                        JSONObject recordJson = responseJson.getJSONObject(i);
                        String day = "";
                        int temperature = 0;
                        if (recordJson.has("date")) {
                            day = recordJson.getString("date");
                        }
                        if (recordJson.has("temperature")) {
                            temperature = recordJson.getInt("temperature");
                        }
                        entries.add(new Entry(Integer.parseInt(day.substring(8,10)), temperature));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    Log.d(TAG, "get temperature is successful");
                    set = new LineDataSet(entries, "");
                    set.setLineWidth(5);
                    set.setDrawCircles(false);
                    set.setDrawValues(false);
                    set.setDrawHighlightIndicators(false);
                    chart.setData(new LineData(set));
                }
                else {
                    Log.d(TAG, "failed to get temperature");
                }
            }
        });
    }
}