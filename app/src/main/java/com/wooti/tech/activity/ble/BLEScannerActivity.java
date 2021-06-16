package com.wooti.tech.activity.ble;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wooti.tech.R;
import com.wooti.tech.activity.controll.ControllActivity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BLEScannerActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1500;

    private BluetoothManager bluetoothManager;
    //블루투스 매니저
    //블루트스 기능을 총괄적으로 관리함.
    private BluetoothAdapter bluetoothAdapter;
    //블루투스 연결자
    //블루투스를 스켄하거나, 페어링된장치목록을 읽어들일 수 있습니다.
    //이를 바탕으로 블루투스와의 연결을 시도할 수 있습니다.
    private BluetoothListviewAdapter bluetoothListviewAdapter = null;//리스트 어댑터

    private boolean scanning = false;

    private Handler mHandler;

    private Button button;//버튼

    private ListView listView;//리스트뷰 객체

    private TimerTask scanTimerTask;

    private Timer scanTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blescanner);
        mHandler = new Handler();
        listView = (ListView) findViewById(R.id.listView);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //beacon 을 활용하려면 블루투스 권한획득(Andoird M버전 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();

        }


        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            //블루투스를 지원하지 않거나 켜져있지 않으면 장치를끈다.
            Toast.makeText(this, "블루투스를 켜주세요", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
       /* if (!scanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()) {
            case R.id.menu_scan:
              *//*  bluetoothListviewAdapter.clear();
                scanLeDevice(true);*//*
              //scanTimerTask.run();\
                scanTimerTask.cancel();
                startScan();
                break;
            case R.id.menu_stop:
                //scanLeDevice(false);
                scanTimerTask.cancel();
                break;
        }*/
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        bluetoothListviewAdapter = new BluetoothListviewAdapter();
        listView.setAdapter(bluetoothListviewAdapter);
        //처음시작
        startScan();
    }

    public void startScan(){
        scanLeDevice(true);

        scanTimerTask = new TimerTask() {
            @Override
            public void run() {
                BLEScannerActivity.this.runOnUiThread(new Runnable(){ //이 부분 추가
                    public void run(){
                        Log.e("HJLEE", "난 돌고있어!!");
                        scanLeDevice(true);
                    }
                }); //여기까지
            }
        };
        scanTimer = new Timer();
        scanTimer.schedule(scanTimerTask, 1000, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanTimerTask.cancel();
        scanLeDevice(false);
        bluetoothListviewAdapter.clear();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(bludtoothScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            scanning = true;
            bluetoothAdapter.startLeScan(bludtoothScanCallback);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(bludtoothScanCallback);
        }
        invalidateOptionsMenu();
    }


    private class BluetoothListviewAdapter extends BaseAdapter {//리스트뷰 어뎁터 선언
        private ArrayList<BluetoothDevice> devices;
        private ArrayList<Integer> RSSIs;
        private LayoutInflater inflater;


        public BluetoothListviewAdapter() {
            super();
            devices = new ArrayList<BluetoothDevice>();
            RSSIs = new ArrayList<Integer>();
            inflater = ((Activity) BLEScannerActivity.this).getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device, int rssi) {
            if (!devices.contains(device)) {
                devices.add(device);
                RSSIs.add(rssi);
            } else {
                RSSIs.set(devices.indexOf(device), rssi);
            }
        }

        public void clear() {
            devices.clear();
            RSSIs.clear();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.custom_two_line_item, null);
                viewHolder.deviceName = (TextView) convertView.findViewById(R.id.text1);
                viewHolder.deviceRssi = (TextView) convertView.findViewById(R.id.text2);
                viewHolder.allItem = (LinearLayout) convertView.findViewById(R.id.all_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String deviceName = devices.get(position).getName();
            int rssi = RSSIs.get(position);

            viewHolder.deviceName.setText(deviceName != null && deviceName.length() > 0 ? deviceName : "알 수 없는 장치");
            viewHolder.deviceRssi.setText(String.valueOf(rssi));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //스톱시킨다.
                    scanTimer.cancel();
                    //다시클릭하면 이동한다. ㅜㅜ
                    final BluetoothDevice device = devices.get(position);
                    if (device == null) return;
                    Intent intent = new Intent(BLEScannerActivity.this, ControllActivity.class);
                    //Intent intent = new Intent(BLEScannerActivity.this, DeviceControlActivity.class);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());

                    if (scanning) {
                        Toast.makeText(getApplicationContext(), "device name : " + device.getName(), Toast.LENGTH_LONG).show();
                        bluetoothAdapter.stopLeScan(bludtoothScanCallback);
                        scanning = false;
                    }
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceRssi;
        LinearLayout allItem;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback bludtoothScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothListviewAdapter.addDevice(device, rssi);
                            bluetoothListviewAdapter.notifyDataSetChanged();
                            if(device.getName().contains("ED-BT")){
                                scanTimer.cancel();
                                Intent intent = new Intent(BLEScannerActivity.this, ControllActivity.class);
                                //Intent intent = new Intent(BLEScannerActivity.this, DeviceControlActivity.class);
                                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                                startActivity(intent);

                                //
                            }
                            //

                        }
                    });
                }
            };
}


