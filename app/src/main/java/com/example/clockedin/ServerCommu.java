package com.example.clockedin;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ServerCommu {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public Call post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public Call postWithAuth(String url, String json, String token, Callback callback) {
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder().url(url).addHeader("Authorization","Bearer "+ token).post(body).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public Call getWithAuth(String url, String token, Callback callback) {
        Request request = new Request.Builder().url(url).addHeader("Authorization","Bearer "+ token).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }
}