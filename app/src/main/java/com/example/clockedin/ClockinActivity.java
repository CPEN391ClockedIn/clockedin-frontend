package com.example.clockedin;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClockinActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "ClockinActivity";
    private IntentFilter filter;
    private Dialog dialog;
    private ListView view_paired;
    private ListView view_discvred;
    private BluetoothAdapter adapter_bt;
    private BroadcastReceiver receiver;
    private BluetoothSocket socket = null;
    private Intent intent = null;
    private boolean connected = false;
    private ArrayAdapter<String> adapter_paired;
    private ArrayAdapter<String> adapter_discvred;
    private List<String> list_paired_name;
    private List<String> list_discvred_name;
    private List<BluetoothDevice> list_paired;
    private List<BluetoothDevice> list_discvred;
    public static InputStream stream_in = null;
    public static OutputStream stream_out = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_clockin);

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        view_paired = dialog.findViewById(R.id.list_paired);
        view_discvred = dialog.findViewById(R.id.list_discvred);
        list_paired_name = new ArrayList<>();
        list_discvred_name = new ArrayList<>();
        list_paired = new ArrayList<>();
        list_discvred = new ArrayList<>();
        adapter_paired = new ArrayAdapter<>(this, R.layout.activity_device, R.id.device, list_paired_name);
        adapter_discvred = new ArrayAdapter<>(this, R.layout.activity_device, R.id.device, list_discvred_name);
        view_paired.setAdapter(adapter_paired);
        view_discvred.setAdapter(adapter_discvred);
        view_paired.setOnItemClickListener(this);
        view_discvred.setOnItemClickListener(this);

        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        adapter_bt = BluetoothAdapter.getDefaultAdapter();
        if (adapter_bt != null) {
            listPaired();
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent mIntent) {
                    intent = mIntent;
                    listDiscovered();
                }
            };
            registerReceiver(receiver, filter);
            if (adapter_bt.isDiscovering()) { adapter_bt.cancelDiscovery(); }
            adapter_bt.startDiscovery();
        }
        dialog.show();
    }

    public void listPaired() {
        Set<BluetoothDevice> set = adapter_bt.getBondedDevices();
        List<BluetoothDevice> list = new ArrayList<>(set);
        for (BluetoothDevice device: list) {
            list_paired.add(device);
            list_paired_name.add(device.getName());
            adapter_paired.notifyDataSetChanged();
        }
    }

    public void listDiscovered() {
        if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (!list_discvred.contains(device)) {
                list_discvred.add(device);
                list_discvred_name.add(device.getName());
                adapter_discvred.notifyDataSetChanged();
            } }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        synchronized (this) {
            if (connected) {
                closeConnection();
            } }
        if (parent == view_paired) {
            createSocket(list_paired.get(position));
        }
        if (parent == view_discvred) {
            createSocket(list_discvred.get(position));
        }
    }

    public void createSocket(BluetoothDevice device) {
        socket = null;
        try {
            ParcelUuid[] uuids = device.getUuids();
            socket = device.createInsecureRfcommSocketToServiceRecord(uuids[0].getUuid());
        } catch (IOException ignored) {}
        connectToDevice(device);
    }

    public void connectToDevice(BluetoothDevice device) {
        adapter_bt.cancelDiscovery();
        try {
            synchronized (this) {
                if (connected) {
                    closeConnection();
                } }
            socket.connect();
        } catch (IOException ignored) {}
        synchronized (this) {
            connected = true;
        }
        getStreams();
    }

    public void getStreams() {
        try {
            stream_in = socket.getInputStream();
            stream_out = socket.getOutputStream();
            writeToDevice("test");
        } catch (IOException ignored) {}
    }

    public void writeToDevice(String message) {
        byte[] buffer = message.getBytes();
        synchronized (this) {
            if (!connected) {
                return;
            } }
        try {
            stream_out.write(buffer);
        } catch (IOException ignored) {}
    }

    public synchronized void closeConnection() {
        try {
            stream_in.close();
            stream_out.close();
            socket.close();
        } catch (IOException ignored) {}
        connected = false;
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}