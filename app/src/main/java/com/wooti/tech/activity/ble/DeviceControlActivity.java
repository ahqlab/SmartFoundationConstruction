package com.wooti.tech.activity.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wooti.tech.R;
import com.wooti.tech.activity.ble.service.BluetoothLeService;
import com.wooti.tech.activity.ble.service.SampleGattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceControlActivity extends AppCompatActivity {

    private final static String TAG = "HJLEE";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // onCreate 가 로드되면서 bind한다.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(TAG, "여기에 진입합니까?");
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.e(TAG, "comnnect!!!");
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        //getActionBar().setTitle(mDeviceName);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.e(TAG, "Connect request result=" + result);
        } else {
            Log.e(TAG, "????");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
     /*   getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }*/
        return true;
    }




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
           // byte[] data = intent.getByteArrayExtra("data");
           // Log.e("HJLEE",  "responce : " + data.length);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                Log.e("HJLEE", ">> 1");
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e("HJLEE", ">> 2");
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e("HJLEE", ">> 3");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                Log.e("HJLEE", "123324234 : ");
                /*try
                {
                    byte[] readByte = characteristic.getValue();
                    Log.e("HJLEE", "123324234 : " + readByte.length);
                }
                catch(Exception e)
                {
                    Log.d(TAG, e.toString());
                }*/
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,  int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();


                        Log.e("HJLEE", "charaProp : "  + charaProp);
                        Log.e("HJLEE", "PROPERTY_NOTIFY : "  + (charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY));
                        Log.e("HJLEE", "PROPERTY_READ : " + (charaProp | BluetoothGattCharacteristic.PROPERTY_READ));
                        Log.e("HJLEE", "PROPERTY_NOTIFY : " + (charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY));
                        Log.e("HJLEE", "WRITE_TYPE_NO_RESPONSE : " + (charaProp | BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE));


                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) == charaProp) {
                            Log.e("HJLEE","NOTI");
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(characteristic , true);

                        }else{
                            Log.e("HJLEE","WRITE");
                            mBluetoothLeService.setCharacteristicNotification(characteristic , true);
                            characteristic.setValue(new byte[] {0x24, 0x52, 0x45, 0x41, 0x44, 0x2C, 0x30, 0x0D, 0x0A});
                            mBluetoothLeService.writeCharacteristic(characteristic);
                        }


                       /* if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }

                       /* if((charaProp | BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) > 0){
       //Log.e("HJLEE","" + gattCharacteristic.getUuid());
                            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                                descriptor.setValue(data);
                                mBluetoothLeService.writeDescriptor(descriptor);
                            }
                        }*/


                        byte[] data = {0x24,0x52, 0x45, 0x41, 0x44, 0x2C, 0x30, 0x0D, 0x0A};

                        byte[] req_frame = new byte[9];
                        req_frame[0] = (byte) 0x24;
                        req_frame[1] = (byte) 0x52;
                        req_frame[2] = (byte) 0x45;
                        req_frame[3] = (byte) 0x41;
                        req_frame[4] = (byte) 0x44;
                        req_frame[5] = (byte) 0x2C;
                        req_frame[6] = (byte) 0x30;
                        req_frame[7] = (byte) 0x0D;
                        req_frame[8] = (byte) 0x0A;

                        /*if(characteristic.getUuid().toString().matches("6e400003-b5a3-f393-e0a9-e50e24dcca9e")){
                            //Log.e("HJLEE", "charaProp :" + characteristic.getUuid());
                            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                        }else if(characteristic.getUuid().toString().matches("6e400002-b5a3-f393-e0a9-e50e24dcca9e")){
                            //Log.e("HJLEE", "charaProp :" + characteristic.getUuid());

                            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                            //mBluetoothLeService.writeCharacteristic(characteristic);
                        }*/


                        //
                        //mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                        //mBluetoothLeService.readCharacteristic(characteristic);
                       // mBluetoothLeService.notify();
                   /*
                        characteristic.setValue(req_frame);
                        characteristic.setValue("24524541442C300D0A".getBytes());
                        characteristic.setValue(data);*/
                        //characteristic.setValue("$READ,0\\r\\n".getBytes());
                        //characteristic.setValue("$READ,0\\r\\n".getBytes());
                       // characteristic.setValue("24524541442C300D0A");
                        //DevicePro
                        Toast.makeText(getApplicationContext(), "Hi", Toast.LENGTH_LONG).show();
                        //

                        return true;
                    }
                    return false;
                }
            };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      /*  switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }*/
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
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =   gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
               // Log.e("HJLEE","" + gattCharacteristic.getUuid());

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this, gattServiceData, android.R.layout.simple_expandable_list_item_2, new String[]{LIST_NAME, LIST_UUID}, new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    public void serialSend(String theString) {
     /*   if (mConnectionState == connectionStateEnum.isConnected) {
            mNotifyCharacteristic.setValue(theString);
            //mBluetoothLeService.writeCharacteristic(mSCharacteristic);

        }*/
    }
}
