/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.a3dmotechdesign.a3dmo;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = "a3dmo";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private int[] RGBFrame = {0, 0, 0};
    private TextView isSerial;
    private TextView mConnectionState;
    private TextView mDataField;
    private SeekBar mAuxSlide;
    private String mDeviceName;
    private String mDeviceAddress;
    //  private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;

    //private BluetoothGattCharacteristic targetGattCharacteristic = null;

    public final static UUID HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    GridLayout background;
    Button upButton;
    Button downButton;
    Button testButton;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        // is serial present?
        isSerial = (TextView) findViewById(R.id.isSerial);

        mDataField = (TextView) findViewById(R.id.data_value);

        mAuxSlide = (SeekBar) findViewById(R.id.seekAuxSlide);

        background = (GridLayout) findViewById(R.id.backgroud);
        upButton = (Button) findViewById(R.id.up_Button);
        downButton = (Button) findViewById(R.id.down_Button);
        testButton = (Button) findViewById(R.id.test_Button);

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeChangeUpPressed();
            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeChangeDownPressed();
            }
        });
//        upButton.setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View v, MotionEvent e) {
//
//                switch (e.getAction()){
//                    case MotionEvent.ACTION_DOWN:
//                        makeChangeUpPressed();
//                        //Log.i(TAG, "Up");
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        makeChangeUpPressed();
//                        break;
//                }
//                return true;
//            }
//        });
//
//        downButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent e) {
//
//                switch (e.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        makeChangeDownPressed();
//                        //Log.i(TAG, "Down");
//
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        break;
//                }
//                return true;
//            }
//        });

        testButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.a3dmotechdesign.a3dmo.CustomSurfaceViewNews");
                startActivity(intent);
            }
        });

        readSeek(mAuxSlide, 0);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(!mConnected || mBluetoothLeService == null)
//            return ;
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d("registerReceiver", "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mConnected) {
        unregisterReceiver(mGattUpdateReceiver);
//        }
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mConnected) {
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
//        }
    }

    public void reconnect_to_ble() {
//        if(!mConnected) { //|| mBluetoothLeService == null){
//            Log.d(TAG, "mConnected mBluetoothLeService = " + mConnected + " : " + mBluetoothLeService);
//            return;
//        }

        try {
            Log.d(TAG, "Try register");
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mBluetoothLeService != null) {
            Log.d(TAG, "mBluetoothLeService =" + mBluetoothLeService);
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "registerReceiver Connect request result=" + result);
        }
    }


    public synchronized void sendData(byte[] value) {
        if (mBluetoothLeService != null && mConnected == true) {
            int targetLen = 0;
            int offset = 0;
            for (int len = (int) value.length; len > 0; len -= 20) {
                if (len < 20)
                    targetLen = len;
                else
                    targetLen = 20;
                byte[] targetByte = new byte[targetLen];
                System.arraycopy(value, offset, targetByte, 0, targetLen);
                offset += 20;
                Log.i(TAG, "Length =" + len);
                //targetGattCharacteristic.setValue(targetByte);
                //mBluetoothLeService.writeCharacteristic(targetGattCharacteristic);

                characteristicTX.setValue(targetByte);
                mBluetoothLeService.writeCharacteristic(characteristicTX);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }

    public synchronized void sendData(String value) {
        if (mBluetoothLeService != null && mConnected == true) {
            characteristicTX.setValue(value);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {

        if (data != null) {
            mDataField.setText(data);
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

            // If the service exists for HM 10 Serial, say so.
            if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {
                isSerial.setText("Yes, serial :-)");
            } else {
                isSerial.setText("No, serial :-(");
            }
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void readSeek(SeekBar seekBar, final int pos) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                RGBFrame[pos] = progress;
                if (progress < 108)
                    makeChangeAuxLeftPressed();
                if (progress > 148)
                    makeChangeAuxRightPressed();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //makeChange();
                RGBFrame[pos] = 128;
                mAuxSlide.setProgress(128);

            }
        });
    }

    public void onClickC(View view) {
        makeChangeCPressed();
    }

    public void onClickZ(View view) {
        makeChangeZPressed();
    }

    public void onClickLeft(View view) {
        makeChangeLeftPressed();
    }

    public void onClickRight(View view) {
        makeChangeRightPressed();
    }

//    public void onClickUp(View view) {
//        makeChangeUpPressed();
//    }
//
//    public void onClickDown(View view) {
//        makeChangeDownPressed();
//    }

    private void makeChangeCPressed() {
        String str = "c"; // + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        Log.i(TAG, "Tx = " + tx);
        Log.i(TAG, "mConnected = " + mConnected);
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    private void makeChangeZPressed() {
        String str = "z"; // + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        Log.i(TAG, "Tx = " + tx);
        Log.i(TAG, "mConnected = " + mConnected);
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    public void makeChangeLeftPressed() {
        String str = "l" + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        Log.i(TAG, "Tx = " + tx);
        Log.i(TAG, "mConnected = " + mConnected);
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    public void makeChangeRightPressed() {
        String str = "r" + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        Log.i(TAG, "Tx = " + tx);
        Log.i(TAG, "mConnected = " + mConnected);
        if (mConnected) {
            Log.i(TAG, "ble tx before");
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    public void makeChangeAuxLeftPressed() {
        String str = "al" + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        Log.i(TAG, "Tx = " + tx);
        Log.i(TAG, "mConnected = " + mConnected);
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    public void makeChangeAuxRightPressed() {
        String str = "ar" + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        Log.i(TAG, "Tx = " + tx);
        Log.i(TAG, "mConnected = " + mConnected);
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    public void makeChangeUpPressed() {
        String str = "u" + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        Log.i(TAG, "Tx = " + tx);
        Log.i(TAG, "mConnected = " + mConnected);
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    public void makeChangeDownPressed() {
        String str = "d" + "\n";
        Log.d(TAG, "Sending result=" + str);
        byte[] tx = str.getBytes();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Tx = " + tx);
        Log.i(TAG, "mConnected = " + mConnected);
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }

    }

    // on change of bars write char 
    private void makeChange() {
        String str = RGBFrame[0] + "," + RGBFrame[1] + "," + RGBFrame[2] + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }
}