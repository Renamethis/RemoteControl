package com.example.ivan.dogremotecontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    ArrayList<String> pairedD;
    ListView pairedDeviceslist;
    RelativeLayout ButPanel;
    ArrayAdapter<String> pairedDevicesadapter;
    private UUID bUUID;
    thread_connect threadBT;
    private StringBuilder sb = new StringBuilder();
    Button Left, Right, Forward, Back;
    BluetoothAdapter bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
        startActivityForResult(new Intent(enableBT), 0);
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        Forward = (Button)findViewById(R.id.button1);
        Left = (Button)findViewById(R.id.button2);
        Right = (Button)findViewById(R.id.button3);
        Back = (Button)findViewById(R.id.button4);
        ButPanel = (RelativeLayout)findViewById(R.id.butlayout);
        ButPanel.setVisibility(View.INVISIBLE);
        pairedDeviceslist = (ListView)findViewById(R.id.list);
        Forward.setOnClickListener(this);
        Left.setOnClickListener(this);
        Right.setOnClickListener(this);
        Back.setOnClickListener(this);
        if (bluetooth == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String stInfo = bluetooth.getAddress();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stInfo);
        builder.setTitle("Адрес этого устройства");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bluetooth.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
        setup();
    }

    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
        if (pairedDevices.size() > 0) {
            this.pairedD = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices) {
                this.pairedD.add(device.getName() + "\n" + device.getAddress());
            }
            pairedDevicesadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, this.pairedD);
            pairedDeviceslist.setAdapter(pairedDevicesadapter);

            pairedDeviceslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    pairedDeviceslist.setVisibility(View.GONE);
                    String itemValue = (String) pairedDeviceslist.getItemAtPosition(position);
                    String MAC = itemValue.substring(itemValue.length() - 17);
                    BluetoothDevice device2 = bluetooth.getRemoteDevice(MAC);
                    threadBT = new thread_connect(device2);
                    threadBT.start();
                }
            });
        }
    }
    @Override
    public void onClick(View view) {
        try {
            byte sendByte = (byte)((Button)findViewById(view.getId())).getText().charAt(0);
            threadBT.bluetoothSocket.getOutputStream().write(sendByte);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class thread_connect extends Thread {
        private BluetoothSocket bluetoothSocket = null;

        private thread_connect(BluetoothDevice device) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "The device cannot be connected", Toast.LENGTH_LONG).show();
                        pairedDeviceslist.setVisibility(View.VISIBLE);
                    }
                });
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ButPanel.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}
