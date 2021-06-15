package com.wooti.tech.activity.controll;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wooti.tech.R;
import com.wooti.tech.activity.ble.service.BluetoothLeService;
import com.wooti.tech.activity.ble.service.SampleGattAttributes;
import com.wooti.tech.activity.common.BaseActivity;
import com.wooti.tech.adapter.AbsractCommonAdapter;
import com.wooti.tech.databinding.ActivityControllBinding;
import com.wooti.tech.databinding.PenetrationListviewItemBinding;
import com.wooti.tech.databinding.PieceListviewItemBinding;
import com.wooti.tech.databinding.TonListviewItemBinding;
import com.wooti.tech.db.DBHandlerOfPenetration;
import com.wooti.tech.db.DBHandlerOfPiece;
import com.wooti.tech.db.DBHandlerOfReport;
import com.wooti.tech.db.DBHandlerOfSndCount;
import com.wooti.tech.domain.Penetration;
import com.wooti.tech.domain.Report;
import com.wooti.tech.domain.SndCount;
import com.wooti.tech.domain.report.Piece;
import com.wooti.tech.domain.Ton;
import com.wooti.tech.domain.report.SqlPenetration;
import com.wooti.tech.domain.report.SqlPiece;
import com.wooti.tech.sharedPref.SharedPrefKeys;
import com.wooti.tech.sharedPref.SharedPrefManager;
import com.wooti.tech.util.NetworkStatus;
import com.wooti.tech.util.util.DateUtil;
import com.wooti.tech.util.util.MathUtill;
import com.wooti.tech.util.util.Utill;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ControllActivity extends BaseActivity<ControllActivity> implements ControllIn.View {

    private long mLastClickTime = 0;

    private long mLastClickSendTime = 0;

    //private static long measureStartTime = 0;

    private static boolean measureResult = true;

    private Handler errorMessagehandler;

    private final long FINISH_INTERVAL_TIME = 2000;

    private long backPressedTime = 0;

    private boolean isAllWrite = false;

    private final static String TAG = "HJLEE";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 1500;

    private ActivityControllBinding binding;

    private ControllIn.Presenter presenter;

    private AbsractCommonAdapter<Penetration> penetrationAbsractCommonAdapter;

    private AbsractCommonAdapter<Penetration> pieceAbsractCommonAdapter;


    private AbsractCommonAdapter<Ton> tonAbsractCommonAdapter;

    private float dDepth = 0;

    private float iDepth = 0;

    private static EditText keyboardTarget;

    //BLE SCAN
    private BluetoothManager bluetoothManager;
    //블루트스 기능을 총괄적으로 관리함.
    private BluetoothAdapter bluetoothAdapter;

    private TimerTask scanTimerTask;

    private Timer scanTimer;

    private boolean scanning = false;

    private Handler mHandler;

    Intent gattServiceIntent;

    //BLE CONNECTION
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

    private float initValue = 0;

    private int count = 0;

    ProgressDialog dialog;

    private boolean connected = false;

    private MenuItem progressItem;

    SharedPrefManager sharedPrefManager;


    AlertDialog.Builder publicBuilder;
    AlertDialog publicAlertDialog;

    TimerTask publicTask;

    Timer publicTimer;

    int internetStatus = 0;

    static AlertDialog.Builder errorBuilder;

    static AlertDialog errorDialog;


    private DBHandlerOfReport dbHandlerReport;


    private DBHandlerOfPenetration dbHandlerPenetration;


    private DBHandlerOfPiece dbHandlerOfPiece;

    private DBHandlerOfSndCount dbHandlerOfSndCount;

    private String sharedBlueNo;

    private ConnectivityManager.NetworkCallback networkCallback;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void registNerworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Toast.makeText(getApplicationContext(), "연결됨", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Toast.makeText(getApplicationContext(), "끊어짐", Toast.LENGTH_SHORT).show();
            }
        };
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void unRegisterNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    // onCreate 가 로드되면서 bind한다.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            //Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback bludtoothScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device.getName() != null) {
                        if (device.getName().contains("ED-BT")) {
                            Log.e("HJLEE", "device name : " + device.getName() + " device address : " + device.getAddress() + " sharedBlueNo : " + sharedBlueNo);
                            if (sharedBlueNo.equals("1")) {
                                if (device.getAddress().equals("DA:8F:72:48:F9:05")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("2")) {
                                if (device.getAddress().equals("D5:7A:35:0C:9F:A8")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("3")) {
                                if (device.getAddress().equals("D5:81:42:A6:97:A3")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("4")) {
                                if (device.getAddress().equals("E9:6D:41:30:49:AE")) {
                                    doCallbackWorking(device);
                                }
                                       /* if (device.getAddress().equals("D8:83:A6:94:1D:A3")) {
                                            doCallbackWorking(device);
                                        }*/
                            } else if (sharedBlueNo.equals("5")) {
                                if (device.getAddress().equals("E4:CA:9C:34:A2:F9")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("6")) {
                                if (device.getAddress().equals("E5:A8:D3:13:3B:7F")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("7")) {
                                if (device.getAddress().equals("C8:F3:58:29:81:4F")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("8")) {
                                if (device.getAddress().equals("D2:F5:DF:D3:FC:9B")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("9")) {
                                if (device.getAddress().equals("E9:9B:9A:74:73:C1")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("10")) {
                                if (device.getAddress().equals("EC:45:25:5E:EE:F1")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("11")) {
                                        /*if (device.getAddress().equals("C7:BC:58:9B:E5:70")) {
                                            doCallbackWorking(device);
                                        }*/

                                if (device.getAddress().equals("E4:40:5C:37:AA:22")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("12")) {
                                if (device.getAddress().equals("CB:B9:AD:7F:02:08")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("13")) {
                                if (device.getAddress().equals("FE:19:EE:A0:50:59")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("14")) {
                                if (device.getAddress().equals("E2:DF:94:8B:5B:16")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("15")) {
                                if (device.getAddress().equals("D9:83:36:F9:A0:7B")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("16")) {
                                        /*if (device.getAddress().equals("E7:ED:D7:37:D3:2F")) {
                                            doCallbackWorking(device);
                                        }*/
                                if (device.getAddress().equals("E0:05:37:F9:8A:87")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("17")) {
                                if (device.getAddress().equals("C2:BF:10:B5:FB:9E")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("18")) {
                                if (device.getAddress().equals("FA:C8:51:42:87:16")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("19")) {
                                if (device.getAddress().equals("ED:85:25:24:89:29")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("20")) {
                                if (device.getAddress().equals("F8:82:B5:37:6F:FE")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("21")) {
                                if (device.getAddress().equals("CE:4D:86:1E:DE:5D")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("22")) {
                                if (device.getAddress().equals("C3:C9:14:AD:0D:3D")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("23")) {
                                if (device.getAddress().equals("E4:0C:9D:B7:72:AC")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("24")) {
                                if (device.getAddress().equals("DC:89:21:13:C8:40")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("25")) {
                                if (device.getAddress().equals("D3:EB:81:BC:65:F3")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("26")) {
                                if (device.getAddress().equals("EF:F0:41:D6:A3:DD")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("27")) {
                                if (device.getAddress().equals("EE:A7:CB:58:06:04")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("28")) {
                                if (device.getAddress().equals("CD:E2:5F:EB:1B:5A")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("29")) {
                                if (device.getAddress().equals("EA:2A:0D:EC:26:28")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("30")) {
                                if (device.getAddress().equals("D7:8F:33:DB:AF:85")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("31")) {
                                if (device.getAddress().equals("E6:BA:44:A4:C0:99")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("32")) {
                                if (device.getAddress().equals("DB:CF:E5:DB:9E:2F")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("33")) {
                                if (device.getAddress().equals("C5:B8:C1:73:86:53")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("34")) {
                                if (device.getAddress().equals("C6:C5:C3:D9:5F:CE")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("35")) {
                                if (device.getAddress().equals("C2:1A:67:4F:39:35")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals("36")) {
                                if (device.getAddress().equals("D6:8D:91:AF:27:BC")) {
                                    doCallbackWorking(device);
                                }
                            } else if (sharedBlueNo.equals(device.getAddress())) {
                                // if (device.getAddress().equals(sharedBlueNo)) {
                                doCallbackWorking(device);
                                // }
                            }
                        }
                    }
                }
            });
        }
    };

    private void doCallbackWorking(BluetoothDevice device) {
        /*if (scanning) {
            Log.e("HJLEE", "device name : " + device.getName() + " device address : " + device.getAddress());
            Toast.makeText(getApplicationContext(), "device name : " + device.getName(), Toast.LENGTH_LONG).show();
            scanLeDevice(false);
            scanning = false;
        }
        mDeviceName = device.getName();
        mDeviceAddress = device.getAddress();
        if (mBluetoothLeService != null) {

        }else{
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
        doConnection();*/

        if (scanning) {
            //Log.e("HJLEE", "device name : " + device.getName() + " device address : " + device.getAddress());
            Toast.makeText(getApplicationContext(), "device name : " + device.getName(), Toast.LENGTH_LONG).show();
            mDeviceName = device.getName();
            mDeviceAddress = device.getAddress();
            if (mBluetoothLeService != null) {
            } else {
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }
            doConnection();
            scanLeDevice(false);
            scanning = false;
        }
    }

    private void doConnection() {
        //if(!isRegisted){

        //IntentFilter intentFilter = new IntentFilter();
        // intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        // intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        // intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        // intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        // registerReceiver(mGattUpdateReceiver, intentFilter);
        //}

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.e(TAG, "Connect request result=" + result);
            scanTimerTask.cancel();
        } else {
            Log.e(TAG, "????");
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(bludtoothScanCallback);
                }
            }, SCAN_PERIOD);
            scanning = true;
            bluetoothAdapter.startLeScan(bludtoothScanCallback);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(bludtoothScanCallback);
        }
        // invalidateOptionsMenu();
    }

    public void startScan() {
        //scanLeDevice(true);
        scanTimerTask = new TimerTask() {
            @Override
            public void run() {
                ControllActivity.this.runOnUiThread(new Runnable() { //이 부분 추가
                    public void run() {
                        if (!connected) {
                            scanLeDevice(true);
                        }
                    }
                }); //여기까지
            }
        };
        scanTimer = new Timer();
        scanTimer.schedule(scanTimerTask, 1000, 3000);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_refresh).setVisible(true);
*//*

        menu.findItem(R.id.menu_stop).setVisible(true);
        menu.findItem(R.id.menu_disconnect).setVisible(true);
        menu.findItem(R.id.menu_connect).setVisible(true);
        menu.findItem(R.id.menu_connecting).setVisible(true);
*//*

        //menu.findItem(R.id.menu_reset).setVisible(true);

        if (connected) {
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_connect).setVisible(false);
        } else {
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            if (scanning) {
                menu.findItem(R.id.menu_connecting).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                menu.findItem(R.id.menu_refresh).setVisible(false);
                menu.findItem(R.id.menu_connecting).setVisible(false);
            }
        }
        return true;
    }*/


   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            *//*case R.id.menu_reset:

                ControllActivity.super.showBasicOneBtnPopup(null, "데이터를 리셋하시겠습니까?")
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetController();
                        dialog.dismiss();
                    }
                }).show();


                break;*//*
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                invalidateOptionsMenu();
                break;
            case R.id.menu_connect:
                startScan();
                break;
        }
        return true;
    }*/

    private void resetController() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() { // 메시지 큐에 저장될 메시지의 내용
                        final int sdk = android.os.Build.VERSION.SDK_INT;
                        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            //전송버튼 원상태로
                            //측정버튼 원상채로
                            binding.btnSend.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_btn));
                            binding.btnMeasure.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_btn));
                        } else {
                            //전송버튼 원상태로
                            //측정버튼 원상채로
                            binding.btnSend.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_btn));
                            binding.btnMeasure.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_btn));
                        }
                        //관리기준 상태 숨김
                        binding.manageFlagLayout.setVisibility(View.GONE);
                    }
                });
            }
        }).start();


        if (binding.pileNo.getText().length() > 0) {
            binding.pileNo.setText("");
        }
        //2020.10.09 주석처리
      /*  if (binding.drillingDepth.getText().length() > 0) {
            binding.drillingDepth.setText("");
        }
        if (binding.intrusionDepth.getText().length() > 0) {
            binding.intrusionDepth.setText("");
        }*/
        //pieceAbsractCommonAdapter.data.clear();
        //pieceAbsractCommonAdapter.notifyDataSetChanged();
        //presenter.getPeiceItems();
        //합계 원상태
        //binding.totalConnectWidth.setText("0");
        //용접개소 원상태
        //binding.connectLength.setText("0");
        binding.ultimateBearingCapacity.setText("0");

        initValue = 0;
        count = 0;

        penetrationAbsractCommonAdapter.data.clear();
        penetrationAbsractCommonAdapter.notifyDataSetChanged();


        tonAbsractCommonAdapter.data.clear();
        tonAbsractCommonAdapter.notifyDataSetChanged();

        binding.totalPenetrationValue.setText("0");
        binding.avgPenetrationValue.setText("0");

        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_PENETRATION_VALUES);
        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_TON_VALUES);
        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_COUNT_VALUE);
        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_INITVALUE_VALUE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //처음시작
        binding.currentDatetime.setText(DateUtil.getCurrentDateTime());
        Log.e("LIFE", "onStart");
        startScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.currentDatetime.setText(DateUtil.getCurrentDateTime());
        Log.e("LIFE", "onResume");
    }

    //종료시 호출 하나로 합쳐야한다.
    @Override
    protected void onPause() {
        super.onPause();
        Log.e("LIFE", "onPause");
 /*       scanTimerTask.cancel();
        scanLeDevice(false);*/
    }

    //종료시 호출 하나로 합쳐야한다.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("LIFE", "onDestroy");
        //unRegisterNetworkCallback();
        unregisterReceiver(networkReceiver);
        unregisterReceiver(mGattUpdateReceiver);
        if (mBluetoothLeService != null) {
            // if (mBluetoothLeService.isConnect() == BluetoothLeService.STATE_CONNECTED) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
            //  }
        }

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getSupportActionBar().setTitle(R.string.app_name);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        // registNerworkCallback();
        registerReceiver(networkReceiver, intentFilter);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        binding = DataBindingUtil.setContentView(this, R.layout.activity_controll);
        binding.setActivity(this);
        presenter = new ControllPresenter(ControllActivity.this);
        presenter.getPenetrationItems();
        presenter.getPeiceItems();
        presenter.getTonItems();

        sharedPrefManager = SharedPrefManager.getInstance(ControllActivity.this);

        sharedBlueNo = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_BLUE_NO);
        binding.currentDatetime.setText(DateUtil.getCurrentDateTime());


        dbHandlerReport = new DBHandlerOfReport(getApplicationContext());
        dbHandlerOfPiece = new DBHandlerOfPiece(getApplicationContext());
        dbHandlerPenetration = new DBHandlerOfPenetration(getApplicationContext());
        dbHandlerOfSndCount = new DBHandlerOfSndCount(getApplicationContext());

        //이부분 브로트케스트로 바꿔야 한다.
        scanTimerTask = new TimerTask() {
            @Override
            public void run() {
                ControllActivity.this.runOnUiThread(new Runnable() { //이 부분 추가
                    public void run() {
                        internetStatus = NetworkStatus.getConnectivityStatus(getApplicationContext());
                        setInterNetStatus(internetStatus);

                    }
                }); //여기까지
            }
        };
        scanTimer = new Timer();
        scanTimer.schedule(scanTimerTask, 1000, 1000);


        errorBuilder = new AlertDialog.Builder(this);
        errorBuilder.setMessage(getBigText("측정기를 확인하세요."));
        errorBuilder.setPositiveButton(android.R.string.ok, null);
        errorBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });
        // errorDialog = errorBuilder.create();
        //errorBuilder.show();

        //BLE SCAN START
        //beacon 을 활용하려면 블루투스 권한획득(Andoird M버전 이상)
        mHandler = new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("사용자의 위지 정보가 필요합니다.");
                builder.setMessage("비콘을 감지 할 수 있도록 위치 액세스 권한을 부여하십시겠습니까?");
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


        String pileType = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_PILE_TYPE);
        if (pileType.length() > 0) {
            binding.fileType.setText(pileType);
        }
        String method = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_PILE_METHOD);
        if (method.length() > 0) {
            binding.method.setText(method);
        }
        String pileStandard = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_PILE_STANDARD);
        if (pileStandard.length() > 0) {
            binding.pileStandard.setText(pileStandard);
        }
        String pileLocation = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_PILE_LOCATION);
        if (pileLocation.length() > 0) {
            binding.location.setText(pileLocation);
        }
        String hammaT = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_PILE_HAMMAT);
        if (hammaT.length() > 0) {
            binding.hammaT.setText(hammaT);
        }
        String managedStandard = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_PILE_MANAGED_STANDARD);
        if (managedStandard.length() > 0) {
            binding.managedStandart.setText(managedStandard);
        }
        String fallMeter = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_PILE_FALL_METER);
        if (fallMeter.length() > 0) {
            binding.fallMeter.setText(fallMeter);
        }

        String hammaEfficiency = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_HAMMA_EFFICIENCY);
        if (hammaEfficiency.length() > 0) {
            binding.hammaEfficiency.setText(hammaEfficiency);
        }

        String modulusElasticity = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_MODULUS_ELASTICITY);
        if (modulusElasticity.length() > 0) {
            binding.modulusElasticity.setText(modulusElasticity);
        }

        String crossSection = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_CROSS_SECTION);
        if (crossSection.length() > 0) {
            binding.crossSection.setText(crossSection);
        }


        String drillingDepth = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_DRILLING_DEPTH);
        if (drillingDepth.length() > 0) {
            binding.drillingDepth.setText(drillingDepth);
        }

        String intructionDepth = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_INTRUSCION_DEPTH);
        if (intructionDepth.length() > 0) {
            binding.intrusionDepth.setText(intructionDepth);
        }


//        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_PENETRATION_VALUES);
//        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_TON_VALUES);
//        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_COUNT_VALUE);
//        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_INITVALUE_VALUE);


        //int scount = sharedPrefManager.getIntExtra(SharedPrefKeys.SHARED_COUNT_VALUE);
        //float sinitValue = sharedPrefManager.getFloatExtra(SharedPrefKeys.SHARED_INITVALUE_VALUE);
        //initValue = sinitValue;
        //count = scount;
//        Log.e("SAVE_META", "initValue : " + initValue);
//        Log.e("SAVE_META", "count : " + count);


        /*Gson gson = new Gson();
        //여기서 저장된 측정데이터를 셋팅한다.
        String penetrationValueJson = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_PENETRATION_VALUES);
        //sharedPrefManager.removePreference(SharedPrefKeys.SHARED_PENETRATION_VALUES);
        Log.e("SAVEJSON", "1 : " + penetrationValueJson);
        try{
            if(!penetrationValueJson.equals("")){
                Type penetrationType = new TypeToken<ArrayList<Penetration>>(){}.getType();
                List<Penetration> penetrationList = gson.fromJson(penetrationValueJson, penetrationType);
                if(!penetrationList.isEmpty()){
                    Log.e("SAVESAVE", "penetrationList.size : " + penetrationList.size());
                    for (Penetration penetration : penetrationList){
                        Log.e("SAVESAVE", ">>" + penetration);
                        penetrationAbsractCommonAdapter.data.add(penetration);
                    }
                    penetrationAbsractCommonAdapter.notifyDataSetChanged();
                }else{
                    Log.e("SAVESAVE", "penetration list가 비어있음");
                }
            }
        }catch (NullPointerException e){
            Log.e("SAVESAVE", "pNullPointerException");
        }



        String tonValueJson = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_TON_VALUES);
        sharedPrefManager.removePreference(SharedPrefKeys.SHARED_TON_VALUES);
        Log.e("SAVEJSON", "2 : " + tonValueJson);
        try{
            if(!tonValueJson.equals("")){
                Type tonType = new TypeToken<ArrayList<Ton>>(){}.getType();
                List<Ton> tonList = gson.fromJson(tonValueJson, tonType);
                if(!tonList.isEmpty()){
                    Log.e("SAVESAVE", "tonList.size : " + tonList.size());
                    for (Ton tons : tonList){
                        tonAbsractCommonAdapter.data.add(tons);
                    }
                    tonAbsractCommonAdapter.notifyDataSetChanged();

                }else{
                    Log.e("SAVESAVE", "ton list가 비어있음");
                }
            }
        }catch (NullPointerException e){
            Log.e("SAVESAVE", "tNullPointerException");
        }
*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            //블루투스를 지원하지 않거나 켜져있지 않으면 장치를끈다.
            Toast.makeText(this, "블루투스를 켜주세요", Toast.LENGTH_SHORT).show();
            finish();
        }

        //BLE CONNECTION
        //final Intent intent = getIntent();
        //mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        //mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //BLE CONNECTION END
        //dbHandlerReport.allDelete();
        changeEditTextMode(binding.managedStandart);
        changeEditTextMode(binding.avgPenetrationValue);
        changeEditTextMode(binding.totalPenetrationValue);

        //changeEditTextMode(binding.totalConnectWidth);
        changeEditTextModeFileNo(binding.pileNo);
        changeEditTextMode(binding.pileStandard);
        changeEditTextMode(binding.crossSection);
        changeEditTextMode(binding.drillingDepth);
        changeEditTextMode(binding.intrusionDepth);

        changeEditTextMode(binding.hammaT);
        changeEditTextMode(binding.fallMeter);

        changeEditTextMode(binding.hammaEfficiency);
        changeEditTextMode(binding.modulusElasticity);


        setSpinner();

        //changeEditTextMode(binding.balance);
        //changeEditTextMode(binding.connectLength);
        binding.hammaEfficiency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Log.e("HJLEE", "beforeTextChanged : " + s.toString());
                binding.hammaEfficiency.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.e("HJLEE", "onTextChanged : " + s.toString());
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_HAMMA_EFFICIENCY, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    Float.parseFloat(s.toString());
                    sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_HAMMA_EFFICIENCY, s.toString());
                    Log.e("HJLEE", s.toString());
                } catch (NumberFormatException e) {
                    try {
                        binding.hammaEfficiency.setText(binding.hammaEfficiency.getText().delete(binding.hammaEfficiency.getText().length() - 1, binding.hammaEfficiency.getText().length()));
                    } catch (IndexOutOfBoundsException ex) {

                    }
                }
            }
        });

        binding.modulusElasticity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.modulusElasticity.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_MODULUS_ELASTICITY, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    Float.parseFloat(s.toString());
                    sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_MODULUS_ELASTICITY, s.toString());
                } catch (NumberFormatException e) {
                    try {
                        binding.modulusElasticity.setText(binding.modulusElasticity.getText().delete(binding.modulusElasticity.getText().length() - 1, binding.modulusElasticity.getText().length()));
                    } catch (IndexOutOfBoundsException ex) {

                    }
                }
            }
        });


        binding.crossSection.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.crossSection.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_CROSS_SECTION, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    Float.parseFloat(s.toString());
                    sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_CROSS_SECTION, s.toString());
                } catch (NumberFormatException e) {
                    try {
                        binding.crossSection.setText(binding.crossSection.getText().delete(binding.crossSection.getText().length() - 1, binding.crossSection.getText().length()));
                    } catch (IndexOutOfBoundsException ex) {

                    }
                }
            }
        });

        binding.hammaT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.hammaT.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_HAMMAT, s.toString());
                //binding.pileNo.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // binding.pileNo.setError(null);
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_HAMMAT, s.toString());
            }
        });
        binding.fallMeter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.fallMeter.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_FALL_METER, s.toString());
                //binding.pileNo.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // binding.pileNo.setError(null);
                try {
                    Float.parseFloat(s.toString());
                    sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_FALL_METER, s.toString());
                } catch (NumberFormatException e) {
                    try {
                        binding.fallMeter.setText(binding.fallMeter.getText().delete(binding.fallMeter.getText().length() - 1, binding.fallMeter.getText().length()));
                    } catch (IndexOutOfBoundsException ex) {

                    }
                }
            }
        });
        //binding.pileNo.

        binding.pileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.pileNo.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.pileStandard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.pileStandard.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //binding.pileStandard.addFoc
        //천공 깊이
        binding.drillingDepth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.drillingDepth.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    dDepth = Float.parseFloat(s.toString());
                } catch (NumberFormatException e) {
                    try {
                        binding.drillingDepth.setText(binding.drillingDepth.getText().delete(binding.drillingDepth.getText().length() - 1, binding.drillingDepth.getText().length()));

                    } catch (IndexOutOfBoundsException ex) {

                    }
                }

            }
        });
        //관입 깊이
        binding.intrusionDepth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.intrusionDepth.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    iDepth = Float.parseFloat(s.toString());
                } catch (NumberFormatException e) {
                    if (s.toString().length() == 0) {
                        iDepth = Float.parseFloat("0");
                    }
                    try {
                        binding.intrusionDepth.setText(binding.intrusionDepth.getText().delete(binding.intrusionDepth.getText().length() - 1, binding.intrusionDepth.getText().length()));
                    } catch (IndexOutOfBoundsException ex) {

                    }
                }

                //  Log.e("HJLEE1", "iDepth : " + iDepth);
                DecimalFormat form = new DecimalFormat("#.#");
                binding.balance.setText(form.format(Float.parseFloat(binding.totalConnectWidth.getText().toString()) - iDepth));
                getTotalTonValue();

                for (int i = 0; i < penetrationAbsractCommonAdapter.data.size(); i++) {
                    if (Float.parseFloat(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue()) == 0) {
                        tonAbsractCommonAdapter.data.get(i).setValue(String.valueOf(calDanish(Float.parseFloat("0"))));
                    } else {
                        tonAbsractCommonAdapter.data.get(i).setValue(String.valueOf(calDanish(Float.parseFloat(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue()))));
                    }
                }
                tonAbsractCommonAdapter.notifyDataSetChanged();

                //if (dDepth < iDepth) {
                   /* ControllActivity.super.showBasicOneBtnPopup(null, "관입 깊이가 천공깊이보다 많은 수가 입력되었습니다. 이대로 진행하시겠습니까?")
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    binding.intrusionDepth.setText("");
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();*/
                // }
                binding.drillingDepth.setText(binding.intrusionDepth.getText().toString());
            }
        });

        binding.totalConnectWidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.e("HJLEE", "asdlkjasdlkasd");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("HJLEE", "asdlkjasdlkasd");
            }

            @Override
            public void afterTextChanged(Editable s) {
                String totalConnectWidth;
                String intrusionDepth;
                if (binding.totalConnectWidth.getText().toString().length() == 0) {
                    totalConnectWidth = "0";
                } else {
                    totalConnectWidth = binding.totalConnectWidth.getText().toString();
                }
                if (binding.intrusionDepth.getText().toString().length() == 0) {
                    intrusionDepth = "0";
                } else {
                    intrusionDepth = binding.intrusionDepth.getText().toString();
                }

                float balance = Float.parseFloat(totalConnectWidth) - Float.parseFloat(intrusionDepth);
               /* if(balance < 0){
                    ControllActivity.super.showBasicOneBtnPopup(null, "관입 깊이가 파일길이보다 많은 수가 입력되었습니다. 이대로 진행하시겠습니까?")
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    binding.intrusionDepth.setText("");
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }*/
                DecimalFormat form = new DecimalFormat("#.#");
                binding.balance.setText(form.format(balance));
            }
        });

        binding.managedStandart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.managedStandart.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_MANAGED_STANDARD, s.toString());
                if (!s.toString().matches("")) {
                    try {
                        float avg = Float.valueOf(s.toString());
                    } catch (NumberFormatException e) {
                        binding.managedStandart.setText(binding.managedStandart.getText().delete(binding.managedStandart.getText().length() - 1, binding.managedStandart.getText().length()));
                    }
                }

                /*if(!s.toString().matches("")){
                    float avg = Float.valueOf(binding.avgPenetrationValue.getText().toString());
                    if(!s.toString().matches("")){
                        float standard = Float.valueOf(s.toString());
                        if(!s.toString().matches("")){
                            if(avg < standard){
                                binding.managedStandart.setError("관리기준 이하입니다.");
                                //binding.managedStandart.requestFocus();
                            }else{
                                binding.managedStandart.setError("관리기준 이상입니다.");
                                //binding.managedStandart.requestFocus();
                            }
                        }
                    }
                }*/
            }
        });


        binding.balance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                /*if (!s.toString().matches("")) {
                    float balance = Float.parseFloat(s.toString());
                    if(balance < 0){
                        ControllActivity.super.showBasicOneBtnPopup(null, "관입 깊이가 파일길이보다 많은 수가 입력되었습니다. 이대로 진행하시겠습니까?")
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        binding.intrusionDepth.setText("");
                                        dialog.dismiss();
                                    }
                                }).setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                    }
                }*/
            }
        });


        //평균관입량
        binding.avgPenetrationValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().matches("")) {
                    float avg = Float.valueOf(s.toString());
                    float standard = Float.valueOf(binding.managedStandart.getText().toString());
                    float totPenValue = Float.valueOf(binding.totalPenetrationValue.getText().toString());
                    if (!binding.managedStandart.getText().toString().matches("")) {
                        try {
                            String lastPenetrationValue = penetrationAbsractCommonAdapter.data.get(penetrationAbsractCommonAdapter.data.size() - 1).getPenetrationValue();
                            float flastPenetrationValue = Float.parseFloat(lastPenetrationValue);
                            if (!binding.managedStandart.getText().toString().matches("")) {
                                if (avg <= standard) {
                                    if (flastPenetrationValue <= standard) {

                                        if (getFivePenatrationValue() != null) {
                                            if (totPenValue <= getFivePenatrationValue()) {
                                                setManagedFlagStatus(1);
                                            } else {
                                                setManagedFlagStatus(2);
                                            }
                                        } else {
                                            setManagedFlagStatus(1);
                                        }

                                    } else {
                                        setManagedFlagStatus(2);
                                    }
                                } else {
                                    setManagedFlagStatus(2);
                                }
                            }
                        } catch (Exception e) {

                            if (!binding.managedStandart.getText().toString().matches("")) {
                                if (avg <= standard) {
                                    setManagedFlagStatus(1);
                                } else {
                                    setManagedFlagStatus(2);
                                }
                            }
                        }

                    }
                }
            }
        });

        binding.drillingDepth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_DRILLING_DEPTH, s.toString());
            }
        });


        binding.intrusionDepth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_INTRUSCION_DEPTH, s.toString());
            }
        });

        if (keyboardTarget != null) {
            keyboardTarget.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.e("HJLEE", "onTextChanged : " + s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.e("HJLEE", "afterTextChanged");
                    keyboardTarget.setError(null);
                }
            });
        }

        binding.fileType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.fileType.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_TYPE, s.toString());
            }
        });
        //파일종류
        binding.fileType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showOpenDialog(ControllActivity.this, binding.fileType, "파일종류를 입력하세요.");
                }
                binding.fileType.clearFocus();
            }
        });
        //공법
        binding.method.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showOpenDialog(ControllActivity.this, binding.method, "공법을 입력하세요.");
                }
                binding.method.clearFocus();
            }
        });
        //파일규격
        binding.pileStandard.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /*if (hasFocus) {
                    final String[] pileStadardArray = getResources().getStringArray(R.array.pileStadards);
                    final String[] crossSections = getResources().getStringArray(R.array.crossSections);
                    AlertDialog.Builder ab = new AlertDialog.Builder(ControllActivity.this);
                    ab.setTitle("파일규격");
                    ab.setItems(pileStadardArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            binding.pileStandard.setText(pileStadardArray[which]);
                            sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_CROSS_SECTION, crossSections[which]);
                            dialog.dismiss();
                        }
                    }).show();
                }
                binding.pileStandard.clearFocus();*/

                if (hasFocus) {
                    showOpenDialog(ControllActivity.this, binding.pileStandard, "파일규격을 입력하세요.");
                }
                binding.pileStandard.clearFocus();

            }
        });
        /*binding.crossSection.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                *//*if (hasFocus) {
                    final String[] pileStadardArray = getResources().getStringArray(R.array.pileStadards);
                    final String[] crossSections = getResources().getStringArray(R.array.crossSections);
                    AlertDialog.Builder ab = new AlertDialog.Builder(ControllActivity.this);
                    ab.setTitle("파일규격");
                    ab.setItems(pileStadardArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            binding.pileStandard.setText(pileStadardArray[which]);
                            sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_CROSS_SECTION, crossSections[which]);
                            dialog.dismiss();
                        }
                    }).show();
                }
                binding.pileStandard.clearFocus();*//*

                if (hasFocus) {
                    showOpenDialog(ControllActivity.this, binding.crossSection, "단면적을 입력하세요.");
                }
                binding.crossSection.clearFocus();

            }
        });*/

        /*binding.pileNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showOpenDialog(ControllActivity.this, binding.pileNo, "파일번호를 입력하세요.");
                }
                binding.pileNo.clearFocus();
            }
        });*/

        //위치
        binding.location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showOpenDialog(ControllActivity.this, binding.location, "위치를 입력하세요.");
                }
                binding.location.clearFocus();
            }
        });


        //위치
        binding.bigo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showOpenDialog(ControllActivity.this, binding.bigo, "비고 내용을 입력하세요.");
                }
                binding.bigo.clearFocus();
            }
        });

        binding.method.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.method.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_METHOD, s.toString());
            }
        });

        binding.pileStandard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.pileStandard.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_STANDARD, s.toString());

            }
        });

        binding.location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                binding.location.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PILE_LOCATION, s.toString());
            }
        });
        //setPieceSharedValue();

        int sndCount = dbHandlerOfSndCount.getCount();
        binding.sndCountTv.setText(sndCount + "회 전송");
        dbHandlerOfSndCount.deleteNotToday();
    }

    private void changeTonValus(String penetrationValue) {

    }

    private void checkSendServer() {
        int saveReportCount = dbHandlerReport.getCount();
        if (saveReportCount > 0) {
            Toast.makeText(getApplicationContext(), "미 업로드 건수 " + dbHandlerReport.getCount() + " 건이 존재합니다.", Toast.LENGTH_LONG).show();
            if (isOnline()) {
                sendSqlightToServer();
            }
        }
    }

    private void sendSqlightToServer() {
        String reportIdx;
        List<Report> list = dbHandlerReport.select();
        for (Report report : list) {
            reportIdx = report.getReportIdx();
            report.setPiece(dbHandlerOfPiece.selectOne(report.getReportIdx()));
            report.setPenetrations(dbHandlerPenetration.selectOne(report.getReportIdx()));
            report.setReportIdx(null);
            presenter.doSendReportOfConnectNetwork(report);
            dbHandlerReport.updateUploadState(reportIdx);
        }
    }

    private void setSpinner() {
        ArrayAdapter<String> mSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.autoCheckCount));
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.autoSpinner.setAdapter(mSpinnerAdapter);


        ArrayAdapter<String> autoCheckSecAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.autoCheckSec));
        autoCheckSecAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.autoSpinnerSec.setAdapter(autoCheckSecAdapter);
    }

    public static void setErrorMsg(String msg, EditText viewId) {
        //Osama ibrahim 10/5/2013
        int ecolor = Color.WHITE; // whatever color you want
        String estring = msg;
        ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
        ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);
        viewId.setError(ssbuilder);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void updateConnectionState(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBluetoothStatus(enable);
            }
        });
    }

    private void displayAlert() {

        Toast.makeText(getApplicationContext(), "블루투스 연결이 끊어졌습니다.", Toast.LENGTH_LONG).show();
        connected = false;
        invalidateOptionsMenu();
        //unregisterReceiver(mGattUpdateReceiver);

        /*final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setMessage("블");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
        builder.show();*/
    }

    //측정버튼
    public void onClickBtnMeasure(View view) {

        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            Toast.makeText(getApplicationContext(), "2번클릭 방지", Toast.LENGTH_SHORT).show();
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        if (!binding.currentDatetime.equals(DateUtil.getCurrentDateTime())) {
            binding.currentDatetime.setText(DateUtil.getCurrentDateTime());
        }

        try {
            errorMessagehandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {

        }
        boolean result = doMeasureValidation();
        if (result) {
            if (!connected) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                builder.setTitle("블루투스에 연결되지 않았습니다.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dg, int which) {
                        dg.dismiss();
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                //measureStartTime = System.currentTimeMillis();
                measureResult = true;
                if (count == 0) {
                    showDialog("영점 측정중");
                } else {
                    showDialog((penetrationAbsractCommonAdapter.data.size() + 1) + " 회 측정중");
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        writeCharacteristic();

                        errorMessagehandler = new Handler();
                        errorMessagehandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                measureResult = false;
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // if(errorDialog.isShowing()){
                                            errorBuilder.show();
                                            //  }
                                        }
                                    });
                                }
                                errorMessagehandler.removeCallbacksAndMessages(null);
                            }
                        }, 6000);
                        //Timer mTimer = new Timer();
                        //mTimer.schedule(mTask, 6000);
                    }
                }, 0);

            }
        }
    }

    public final BroadcastReceiver networkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (isOnline()) {
                checkSendServer();
            }
        }
    };

    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(true);
                Toast.makeText(getApplicationContext(), " 연결되었습니다.", Toast.LENGTH_SHORT).show();
                connected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                updateConnectionState(false);
                //블루투스 연결 해제(채널 OFF).
                if (mBluetoothLeService != null) {
                    unbindService(mServiceConnection);
                    mBluetoothLeService = null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayAlert();
                    }
                });
                Toast.makeText(getApplicationContext(), "블루투스 연결이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //그리기 시작
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                try {

                    if (measureResult) {
                        String readByte = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                        Log.e("HJLEE", "장비응답  : " + readByte);
                        if (!readByte.contains("#READ_START")) {
                            String str[] = readByte.split(",");
                            Log.e("VALUE", "str[1] : " + str[1]);
                            float realValue = Float.parseFloat(str[1]) * 1000;
                            Log.e("VALUE", "realValue : " + realValue);


                            if (count == 0) {
                                initValue = realValue;
//save penetration value                                sharedPrefManager.putFloatExtra(SharedPrefKeys.SHARED_INITVALUE_VALUE, initValue);
                                dialog.dismiss();
                                setMeasureBtnBackgroudchange("green");
                            } else {
                                float cValue = initValue - realValue;
                                //float cValue = Float.parseFloat("3.3");
                                DecimalFormat form = new DecimalFormat("#.###");
                                if (String.valueOf(form.format(cValue)).contains("-")) {
                                    if (penetrationAbsractCommonAdapter.data.size() > 9) {
                                        penetrationAbsractCommonAdapter.data.remove(0);
                                        tonAbsractCommonAdapter.data.remove(0);
                                        presenter.getPenetrationItems(penetrationAbsractCommonAdapter.data);
                                        presenter.getTonItems(tonAbsractCommonAdapter.data);
                                    }
                                    penetrationAbsractCommonAdapter.data.add(new Penetration(generateNewItem(penetrationAbsractCommonAdapter.data.size()), String.valueOf(form.format(cValue)).replaceAll("-", "")));
                                    tonAbsractCommonAdapter.data.add(new Ton(String.valueOf(calDanish(cValue))));
                                } else {
                                    if (penetrationAbsractCommonAdapter.data.size() > 9) {
                                        penetrationAbsractCommonAdapter.data.remove(0);
                                        tonAbsractCommonAdapter.data.remove(0);
                                        presenter.getPenetrationItems(penetrationAbsractCommonAdapter.data);
                                        presenter.getTonItems(tonAbsractCommonAdapter.data);
                                    }
                                    penetrationAbsractCommonAdapter.data.add(new Penetration(generateNewItem(penetrationAbsractCommonAdapter.data.size()), String.valueOf(form.format(cValue))));
                                    tonAbsractCommonAdapter.data.add(new Ton(String.valueOf(calDanish(cValue))));
                                }


//save penetration value                                Gson gson = new Gson();
//save penetration value                                String penetrationsJson = gson.toJson(penetrationAbsractCommonAdapter.data);
//save penetration value                                Log.e("GETJSON", penetrationsJson);
//save penetration value                                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_PENETRATION_VALUES , penetrationsJson);
//save penetration value
//save penetration value
//save penetration value                                String tonsJson = gson.toJson(tonAbsractCommonAdapter.data);
//save penetration value                                Log.e("GETJSON", tonsJson);
//save penetration value                                sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_TON_VALUES , tonsJson);


                                penetrationAbsractCommonAdapter.notifyDataSetChanged();
                                tonAbsractCommonAdapter.notifyDataSetChanged();

                                initValue = realValue;
//                                sharedPrefManager.putFloatExtra(SharedPrefKeys.SHARED_INITVALUE_VALUE, initValue);


                                Log.e("SAVE_META", "2 initValue : " + initValue);
                                Log.e("SAVE_META", "2 count : " + count);


                                dialog.dismiss();
                            }
                            count++;
//save penetration value                            sharedPrefManager.putIntExtra(SharedPrefKeys.SHARED_COUNT_VALUE, count);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
                //
            }
        }
    };

    private double calDanish(float S) {

        if (S < 0) {
            S = Math.abs(S);
        }

        float EH = Float.parseFloat(Utill.stringNullCheck(binding.hammaEfficiency.getText().toString()) ? binding.hammaEfficiency.getText().toString() : "0");
        float WR = Float.parseFloat(Utill.stringNullCheck(binding.hammaT.getText().toString()) ? binding.hammaT.getText().toString() : "0");
        float H = Float.parseFloat(Utill.stringNullCheck(binding.fallMeter.getText().toString()) ? binding.fallMeter.getText().toString() : "0");
        float L = Float.parseFloat(Utill.stringNullCheck(binding.intrusionDepth.getText().toString()) ? binding.intrusionDepth.getText().toString() : "0");
        float A = Float.parseFloat(Utill.stringNullCheck(binding.crossSection.getText().toString()) ? binding.crossSection.getText().toString() : "0");
        float E = Float.parseFloat(Utill.stringNullCheck(binding.modulusElasticity.getText().toString()) ? binding.modulusElasticity.getText().toString() : "0");

        double RU = MathUtill.calDanish(EH, WR, (H * 100), (L * 100), A, E, (S / 10));
        if (Double.isInfinite(RU)) {
            return 0;
        } else if (Double.isNaN(RU)) {
            return 0;
        } else {
            return RU;
        }
    }

    private double calDanish() {

        float S = Float.parseFloat(Utill.stringNullCheck(binding.avgPenetrationValue.getText().toString()) ? binding.avgPenetrationValue.getText().toString() : "0");
        float EH = Float.parseFloat(Utill.stringNullCheck(binding.hammaEfficiency.getText().toString()) ? binding.hammaEfficiency.getText().toString() : "0");
        float WR = Float.parseFloat(Utill.stringNullCheck(binding.hammaT.getText().toString()) ? binding.hammaT.getText().toString() : "0");
        float H = Float.parseFloat(Utill.stringNullCheck(binding.fallMeter.getText().toString()) ? binding.fallMeter.getText().toString() : "0");
        float L = Float.parseFloat(Utill.stringNullCheck(binding.intrusionDepth.getText().toString()) ? binding.intrusionDepth.getText().toString() : "0");
        float A = Float.parseFloat(Utill.stringNullCheck(binding.crossSection.getText().toString()) ? binding.crossSection.getText().toString() : "0");
        float E = Float.parseFloat(Utill.stringNullCheck(binding.modulusElasticity.getText().toString()) ? binding.modulusElasticity.getText().toString() : "0");

        double RU = MathUtill.calDanish(EH, WR, (H * 100), (L * 100), A, E, (S / 10));
        if (Double.isInfinite(RU)) {
            return 0;
        } else if (Double.isNaN(RU)) {
            return 0;
        } else {
            return RU;
        }
    }

    private void showDialog(String meggage) {
        dialog = new ProgressDialog(ControllActivity.this);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getBigText(meggage));
        dialog.show();
    }

    private void sendServer() {

        Report report = new Report();
        report.setDeviceIdx(sharedPrefManager.getIntExtra(SharedPrefKeys.SHARED_DEVICE_IDX));
        report.setCurrentDateTime(DateUtil.getCurrentDateTime());

        report.setPileType(binding.fileType.getText().toString());
        report.setMethod(binding.method.getText().toString());
        report.setLocation(binding.location.getText().toString());
        report.setPileNo(binding.pileNo.getText().toString());
        report.setPileStandard(binding.pileStandard.getText().toString());
        report.setDrillingDepth(binding.drillingDepth.getText().toString());
        report.setIntrusionDepth(binding.intrusionDepth.getText().toString());
        report.setBalance(Float.parseFloat(binding.balance.getText().toString()));
        report.setConnectLength(binding.connectLength.getText().toString());
        report.setManagedStandard(binding.managedStandart.getText().toString());
        report.setAvgPenetrationValue(binding.avgPenetrationValue.getText().toString());
        report.setTotalPenetrationValue(binding.totalPenetrationValue.getText().toString());
        report.setHammaT(binding.hammaT.getText().toString());
        report.setFallMeter(binding.fallMeter.getText().toString());
        report.setTotalConnectWidth(binding.totalConnectWidth.getText().toString());
        List<Piece> pieceList = new ArrayList<Piece>();
        for (Penetration list : pieceAbsractCommonAdapter.data) {
            pieceList.add(new Piece(list.getPenetrationNumber(), list.getPenetrationValue()));
        }
        report.setPiece(pieceList);
        report.setPenetrations(getTotalPenetrationValueList());
        report.setUltimateBearingCapacity(binding.ultimateBearingCapacity.getText().toString());
        report.setCrossSection(binding.crossSection.getText().toString());
        report.setHammaEfficiency(binding.hammaEfficiency.getText().toString());
        report.setModulusElasticity(binding.modulusElasticity.getText().toString());
        report.setBigo(binding.bigo.getText().toString());

        Log.e("HJLEE", "report : " + report);

        if (isOnline()) {
            presenter.doSendReport(report);
        } else {
            UUID one = UUID.randomUUID();
            boolean result = dbHandlerReport.insertReport(report, one.toString());
            for (com.wooti.tech.domain.report.Penetration list : getTotalPenetrationValueList()) {
                dbHandlerPenetration.insertPenetration(new SqlPenetration(list, one.toString()));
            }
            for (Piece piece : pieceList) {
                dbHandlerOfPiece.insertPiece(new SqlPiece(piece, one.toString()));
            }
            sendResult(true);
        }
    }

    private void displayGattServices(List<BluetoothGattService> supportedGattServices) {
        List<BluetoothGattService> gattCharacteristics = mBluetoothLeService.getSupportedGattServices();
        for (BluetoothGattService gattCharacteristic : gattCharacteristics) {
            //if (gattCharacteristic.getUuid().toString().matches("6e400001-b5a3-f393-e0a9-e50e24dcca9e")) {
            if (gattCharacteristic.getUuid().toString().matches(SampleGattAttributes.WOORI_PARENT_UUID)) {
                BluetoothGattCharacteristic mNotifyCharacteristic = gattCharacteristic.getCharacteristics().get(1);
                Log.e("HJLEE", "<><><> " + mNotifyCharacteristic.toString());
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
            }
        }
    }
/*
    public void onClickAddPenetration(View view){
        if(penetrationAbsractCommonAdapter.data.size() <= 9){
            double dValue = Math.random() + 1;
            double mValue =  -dValue + -dValue;
            //Log.e("HJLEE", "dValue : " + dValue);
            //Log.e("HJLEE", "mValue : " + mValue);
            // double dValue = Math.random();
            penetrationAbsractCommonAdapter.data.add(new Penetration(generateNewItem(penetrationAbsractCommonAdapter.data.size()), String.valueOf(mValue).substring(0,4)));
            penetrationAbsractCommonAdapter.notifyDataSetChanged();
        }
    }*/


    public String generateNewItem(int length) {
        return (length + 1) + "회";
    }

    @Override
    protected BaseActivity<ControllActivity> getActivityClass() {
        return ControllActivity.this;
    }

    @Override
    public void setPenetrationItems(List<Penetration> penetrationItems) {
        penetrationAbsractCommonAdapter = new AbsractCommonAdapter<Penetration>(ControllActivity.this, penetrationItems) {

            PenetrationListviewItemBinding adapterBinding;

            @Override
            protected View getUserEditView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = penetrationAbsractCommonAdapter.inflater.inflate(R.layout.penetration_listview_item, null);
                    adapterBinding = DataBindingUtil.bind(convertView);
                    adapterBinding.setDomain(penetrationAbsractCommonAdapter.data.get(position));
                    convertView.setTag(adapterBinding);
                } else {
                    adapterBinding = (PenetrationListviewItemBinding) convertView.getTag();
                    adapterBinding.setDomain(penetrationAbsractCommonAdapter.data.get(position));
                }
               /* convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ControllActivity.super.showBasicOneBtnPopup(null, "삭제하시겠습니까?")
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                penetrationAbsractCommonAdapter.data.remove(position);
                                presenter.getPenetrationItems(penetrationAbsractCommonAdapter.data);
                            }
                        }).show();
                        return false;
                    }
                });*/

                adapterBinding.value.setTag(adapterBinding.value.getKeyListener());
                adapterBinding.value.setKeyListener(null);
                adapterBinding.value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        final EditText editText = (EditText) v;
                        final int sdk = android.os.Build.VERSION.SDK_INT;
                        keyboardTarget = editText;
                        if (hasFocus) {
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                editText.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_focus_edit_text));
                            } else {
                                editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_focus_edit_text));
                            }

                        } else {
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                editText.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_edit_text));
                            } else {
                                editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_edit_text));
                            }
                        }
                    }
                });


                adapterBinding.value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable edit) {
                        float totalValue = 0;
                        int realSize = 1;
                        for (int i = 0; i < penetrationAbsractCommonAdapter.data.size(); i++) {

                           /* if(!penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue().isEmpty() && Float.valueOf(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue()) > 0){
                                realSize = i + 1;
                                totalValue += Float.valueOf(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
                            }else{
                                penetrationAbsractCommonAdapter.data.get(i).setPenetrationValue("0");
                                totalValue += Float.valueOf(0);
                            }*/
                            realSize = i + 1;
                            totalValue += Float.valueOf(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
                        }


                        //DecimalFormat form = new DecimalFormat("#.#");
                        //binding.totalPenetrationValue.setText(String.valueOf(form.format(totalValue)));
                        //binding.avgPenetrationValue.setText(String.valueOf(form.format(totalValue / realSize)));
                        getTotalPenetrationValue();
                    }
                });

                return adapterBinding.getRoot();
            }

        };
        binding.penetrationListview.setAdapter(penetrationAbsractCommonAdapter);
    }

    @Override
    public void setPieceListview(List<Penetration> penetrationItems) {
        pieceAbsractCommonAdapter = new AbsractCommonAdapter<Penetration>(ControllActivity.this, penetrationItems) {

            PieceListviewItemBinding adapterBinding;

            @Override
            protected View getUserEditView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = pieceAbsractCommonAdapter.inflater.inflate(R.layout.piece_listview_item, null);
                    adapterBinding = DataBindingUtil.bind(convertView);
                    adapterBinding.setDomain(pieceAbsractCommonAdapter.data.get(position));
                    convertView.setTag(adapterBinding);
                } else {
                    adapterBinding = (PieceListviewItemBinding) convertView.getTag();
                    adapterBinding.setDomain(pieceAbsractCommonAdapter.data.get(position));
                }


                adapterBinding.value.setTag(adapterBinding.value.getKeyListener());
                adapterBinding.value.setKeyListener(null);
                adapterBinding.value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        final EditText editText = (EditText) v;
                        final int sdk = android.os.Build.VERSION.SDK_INT;
                        keyboardTarget = editText;
                        if (hasFocus) {
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                editText.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_focus_edit_text));
                            } else {
                                editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_focus_edit_text));
                            }

                        } else {
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                editText.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_edit_text));
                            } else {
                                editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_edit_text));
                            }
                        }

                    }
                });

                adapterBinding.value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        if (position == 0) {
                            // Log.e("HJLEE", "size : " + pieceAbsractCommonAdapter.data.size());

                            for (int i = 0; i < pieceAbsractCommonAdapter.data.size(); i++) {
                                getPieseEditText(i).setError(null);
                            }
                        } else {
                            getPieseEditText(0).setError(null);
                            getPieseEditText(position).setError(null);
                        }

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        float totalValue = 0;
                        float penertationValue = 0;
                        int connectionLength = 0;


                        try {
                            Float.parseFloat(s.toString());
                        } catch (NumberFormatException e) {
                            try {
                                keyboardTarget.setText(keyboardTarget.getText().delete(keyboardTarget.getText().length() - 1, keyboardTarget.getText().length()));
                            } catch (IndexOutOfBoundsException ex) {

                            }
                        }


                        try {

                            for (int i = 0; i < pieceAbsractCommonAdapter.data.size(); i++) {

                                try {
                                    penertationValue = Float.parseFloat(pieceAbsractCommonAdapter.data.get(i).getPenetrationValue().trim() == "" ? "0" : pieceAbsractCommonAdapter.data.get(i).getPenetrationValue().trim());
                                } catch (Exception e) {
                                    penertationValue = Float.parseFloat("0");
                                }

                                if (!pieceAbsractCommonAdapter.data.get(i).getPenetrationValue().isEmpty() && penertationValue > 0) {
                                    totalValue += penertationValue;
                                } else {
                                    pieceAbsractCommonAdapter.data.get(i).setPenetrationValue("");
                                }

                                for (int j = 0; j < pieceAbsractCommonAdapter.data.size(); j++) {
                                    if (pieceAbsractCommonAdapter.data.get(j).getPenetrationValue().length() > 0) {
                                        connectionLength++;
                                    }
                                }
                                int length = connectionLength / pieceAbsractCommonAdapter.data.size();
                                if (length > 0) {
                                    binding.connectLength.setText(String.valueOf(length - 1));
                                } else {
                                    binding.connectLength.setText(String.valueOf(length));
                                }
                            }


                            if (position == 0) {

                                //Log.e("HJLEE", "21");
                                if (s.toString().length() > 0) {
                                    //Log.e("HJLEE", "22");
                                    //if (totalValue != 0) {
                                    float value = 0;
                                    //Log.e("HJLEE", "23");
                                    try {
                                        //  Log.e("HJLEE", "24");
                                        value = Float.parseFloat(s.toString() == "" ? "0" : s.toString());
                                        //  Log.e("HJLEE", "25" + value);
                                        if (value < totalValue) {
                                            //   Log.e("HJLEE", "27");
                                            if (keyboardTarget.getText().length() > 0) {
                                                //Log.e("HJLEE", "28");
                                                totalValue -= value;
                                                // Log.e("HJLEE", " 1 ");
                                                keyboardTarget.setText(keyboardTarget.getText().delete(keyboardTarget.getText().length() - 1, keyboardTarget.getText().length()));
                                                // Log.e("HJLEE", " 2 ");
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        if (!s.toString().contains(".")) {
                                            //Log.e("HJLEE", " 3 ");
                                            try {
                                                keyboardTarget.setText(keyboardTarget.getText().delete(keyboardTarget.getText().length() - 1, keyboardTarget.getText().length()));
                                            } catch (IndexOutOfBoundsException ex) {

                                            }
                                        }
                                    }
                                }
                            } else {
                                //첫번째 글자가 입력되었다면
                                // Log.e("HJLEE", " 첫번쨰  : " + pieceAbsractCommonAdapter.data.get(0).getPenetrationValue());
                                //Log.e("HJLEE", " 5 ");
                                if (!pieceAbsractCommonAdapter.data.get(0).getPenetrationValue().matches("")) {
                                    //두번째 이상인놈들은 지운다.
                                    //Log.e("HJLEE", " 6 ");
                                    if (keyboardTarget.getText().length() > 0) {
                                        //  Log.e("HJLEE", ">> 7");
                                        try {
                                            totalValue -= Float.parseFloat(s.toString() == "" ? "0" : s.toString());
                                        } catch (NumberFormatException e) {
                                            //    Log.e("HJLEE", " 7 ");
                                        }
                                        // Log.e("HJLEE", " 9 ");
                                        keyboardTarget.setText(keyboardTarget.getText().delete(keyboardTarget.getText().length() - 1, keyboardTarget.getText().length()));
                                    }
                                }
                            }
                            //Log.e("HJLEE", " 10 ");
                            binding.totalConnectWidth.setText(String.valueOf(Math.round(totalValue * 10) / 10.0));


                        } catch (NumberFormatException e) {
                            try {
                                //Log.e("HJLEE", " 11 ");
                                keyboardTarget.setText(keyboardTarget.getText().delete(keyboardTarget.getText().length() - 1, keyboardTarget.getText().length()));
                            } catch (IndexOutOfBoundsException ex) {

                            }
                        }


                        //  binding.totalConnectWidth.setText(String.valueOf(totalValue));



/*
                                               if(pieceAbsractCommonAdapter.data.get(0).getPenetrationValue() != "0"){
                            Log.e("HJLEE", "첫번째 값이 있음");
                            if(Float.parseFloat(binding.totalConnectWidth.getText().toString()) > 0){
                                Log.e("HJLEE", "토탈이 0보다 크다");
                                if(position > 0){
                                    if(keyboardTarget.getText().length() > 0){
                                        keyboardTarget.setText(keyboardTarget.getText().delete(keyboardTarget.getText().length() -1 , keyboardTarget.getText().length()));
                                    }
                                }
                            }else{
                                Log.e("HJLEE", "토탈이 0보다 작다");
                                if(position == 0){
                                    if(keyboardTarget.getText().length() > 0){
                                        keyboardTarget.setText(keyboardTarget.getText().delete(keyboardTarget.getText().length() -1 , keyboardTarget.getText().length()));
                                    }
                                }
                            }
                        }*/

                    }
                });

                /*convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (position > 1 && position < pieceAbsractCommonAdapter.data.size() - 1) {
                            ControllActivity.super.showBasicOneBtnPopup(null, "삭제하시겠습니까?")
                                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            pieceAbsractCommonAdapter.data.remove(position);
                                            pieceAbsractCommonAdapter.notifyDataSetChanged();


                                            for (int i = 0; i < pieceAbsractCommonAdapter.data.size(); i++) {
                                                if (i > 0) {
                                                    if (pieceAbsractCommonAdapter.data.get(i).getPenetrationValue().matches("") || Integer.parseInt(pieceAbsractCommonAdapter.data.get(i).getPenetrationValue()) > 0) {
                                                        binding.connectLength.setText(String.valueOf(i - 1));
                                                    }
                                                }
                                            }
                                        }
                                    }).show();
                        }
                        return false;
                    }
                });*/
                return adapterBinding.getRoot();
            }
        };
        binding.pieceListview.setAdapter(pieceAbsractCommonAdapter);


    }


    public void onClickAddCenperPiece(View view) {


        if (pieceAbsractCommonAdapter.data.size() >= 5) {
            Toast.makeText(getApplicationContext(), "더이상 추가 할 수 없습니다.", Toast.LENGTH_LONG).show();
        } else {
            pieceAbsractCommonAdapter.data.add(pieceAbsractCommonAdapter.data.size() - 1, new Penetration("중단", ""));
            pieceAbsractCommonAdapter.notifyDataSetChanged();
        }

    }

    public void onClickDeleteCenperPiece(View view) {

        for (int i = 1; i < pieceAbsractCommonAdapter.data.size(); i++) {

            View child = binding.pieceListview.getChildAt(i);
            @SuppressLint("WrongViewCast")
            EditText et = (EditText) child.findViewById(R.id.value);
            TextView textView = (TextView) child.findViewById(R.id.name);

            if (textView.getText().toString().equals("중단")) {
                pieceAbsractCommonAdapter.data.remove(i);
                pieceAbsractCommonAdapter.notifyDataSetChanged();
                // binding.connectLength.setText(String.valueOf(i - 1));
                break;
            }
        }


        int totalConnectionLength = 0;
        float totalConnectionWidth = 0;
        for (int i = 1; i < pieceAbsractCommonAdapter.data.size(); i++) {
            if (!pieceAbsractCommonAdapter.data.get(i).getPenetrationValue().isEmpty()) {
                totalConnectionLength++;
                totalConnectionWidth += Float.parseFloat(pieceAbsractCommonAdapter.data.get(i).getPenetrationValue());
            }
        }
        if (totalConnectionLength > 0) {
            binding.connectLength.setText(String.valueOf(totalConnectionLength - 1));
        } else {
            binding.connectLength.setText(String.valueOf(0));
        }

        binding.totalConnectWidth.setText(String.valueOf(totalConnectionWidth));
    }


    @Override
    public void sendResult(Boolean domain) {
        Toast.makeText(getApplicationContext(), "전송완료!" + domain, Toast.LENGTH_LONG).show();
        dbHandlerOfSndCount.insert(new SndCount(1));
        resetController();
        int sndCount = dbHandlerOfSndCount.getCount();
        binding.sndCountTv.setText(sndCount + "회 전송");
    }

    @Override
    public void setTonItems(List<Ton> list) {

        tonAbsractCommonAdapter = new AbsractCommonAdapter<Ton>(ControllActivity.this, list) {

            TonListviewItemBinding adapterBinding;

            @Override
            protected View getUserEditView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = tonAbsractCommonAdapter.inflater.inflate(R.layout.ton_listview_item, null);
                    adapterBinding = DataBindingUtil.bind(convertView);
                    adapterBinding.setDomain(tonAbsractCommonAdapter.data.get(position));
                    convertView.setTag(adapterBinding);
                } else {
                    adapterBinding = (TonListviewItemBinding) convertView.getTag();
                    adapterBinding.setDomain(tonAbsractCommonAdapter.data.get(position));
                }

                adapterBinding.value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable edit) {
                        float totalValue = 0;
                        int realSize = 1;
                        for (int i = 0; i < tonAbsractCommonAdapter.data.size(); i++) {

                           /* if(!penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue().isEmpty() && Float.valueOf(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue()) > 0){
                                realSize = i + 1;
                                totalValue += Float.valueOf(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
                            }else{
                                penetrationAbsractCommonAdapter.data.get(i).setPenetrationValue("0");
                                totalValue += Float.valueOf(0);
                            }*/
                            realSize = i + 1;
                            totalValue += Float.valueOf(tonAbsractCommonAdapter.data.get(i).getValue());
                        }


                        //DecimalFormat form = new DecimalFormat("#.#");
                        //binding.totalPenetrationValue.setText(String.valueOf(form.format(totalValue)));
                        //binding.avgPenetrationValue.setText(String.valueOf(form.format(totalValue / realSize)));
                        getTotalTonValue();
                    }
                });
                return adapterBinding.getRoot();
            }

        };
        binding.tonListview.setAdapter(tonAbsractCommonAdapter);
    }

    @Override
    public void sendResultAtConnectNetwork(Boolean domain) {
        Toast.makeText(getApplicationContext(), "전송완료!" + domain, Toast.LENGTH_LONG).show();
    }

    private void shakeItBaby() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 5));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
        }
    }


    public void onClickPadOneClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "1");
            }
        } catch (Exception e) {

        }


    }

    public void onClickPadTwoClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "2");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadThreeClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "3");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadFourClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "4");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadFiveClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "5");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadSixClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "6");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadSevenClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "7");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadEightClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "8");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadNineClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "9");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadCommaClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + ".");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadZeroClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                keyboardTarget.setText(keyboardTarget.getText().toString() + "0");
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadDelClick(View view) {
        try {
            if (keyboardTarget != null) {
                if (keyboardTarget.getText().length() > 0) {
                    shakeItBaby();
                    keyboardTarget.setText(keyboardTarget.getText().delete(keyboardTarget.getText().length() - 1, keyboardTarget.getText().length()));
                }
            }
        } catch (Exception e) {
        }

    }

    public void onClickPadClrClick(View view) {
        try {
            if (keyboardTarget != null) {
                shakeItBaby();
                if (binding.clearBtn.getText().equals("C")) {
                    keyboardTarget.setText("");
                } else if (binding.clearBtn.getText().equals("-")) {
                    keyboardTarget.setText(keyboardTarget.getText().toString() + "-");
                }
                //keyboardTarget.setText(keyboardTarget.getText().toString() + keyboardTarget.getText().toString());
            }
        } catch (Exception e) {

        }
    }

    public void resetEditText() {
        //binding.location.setError(null);
        // binding.pileNo.setError(null);
        // binding.pileStandard.setError(null);
        // binding.drillingDepth.setError(null);
        // binding.intrusionDepth.setError(null);
        //    binding.balance.setError(null);
        //    binding.connectLength.setError(null);
        //binding.managedStandart.setError(null);
    }

  /*  public void onClickBtnComplete(View view){
        Log.e("HJLEE", "isAllWrite? : " + isAllWrite);
    }*/

    private void setMeasureBtnBackgroudchange(String color) {
        if (color == "red") {
            // binding.btnMeasure.setBackgroundColor(getResources().getColor(R.color.colorRed));
        } else if (color == "green") {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.btnMeasure.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_bl_btn));
            } else {
                binding.btnMeasure.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_bl_btn));
            }
        }
    }


    public void getTotalPenetrationValue() {
        int size = penetrationAbsractCommonAdapter.data.size();
        //Log.e("TOTALPE", "size : " + size);
        float totalValue = 0;
        if (size > 5) {
            int start = size - 5;
            //Log.e("TOTALPE", "start : " + size);
            for (int i = start; i < penetrationAbsractCommonAdapter.data.size(); i++) {
                //Log.e("TOTALPE", "i : " + i);
                //Log.e("TOTALPE", "last 5 : " + penetrationAbsractCommonAdapter.data.get(i).getPenetrationNumber() + " : " + penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
                totalValue += Float.parseFloat(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
            }
        } else {
            for (int i = 0; i < penetrationAbsractCommonAdapter.data.size(); i++) {
                totalValue += Float.parseFloat(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
            }
        }
        DecimalFormat form = new DecimalFormat("#.#");
        binding.totalPenetrationValue.setText(String.valueOf(form.format(totalValue)));
        if (size > 5) {
            binding.avgPenetrationValue.setText(String.valueOf(form.format(totalValue / 5)));
        } else {
            binding.avgPenetrationValue.setText(String.valueOf(form.format(totalValue / size)));
        }

    }

    public void getTotalTonValue() {
        binding.ultimateBearingCapacity.setText(String.valueOf(calDanish()));



       /* int size = tonAbsractCommonAdapter.data.size();
        Log.e("HJLEE", "size : " + size);
        float totalValue = 0;
        if (size > 5) {
            int start = size - 5;
            Log.e("HJLEE", "start : " + size);
            for (int i = start; i < tonAbsractCommonAdapter.data.size(); i++) {
                totalValue += Float.parseFloat(tonAbsractCommonAdapter.data.get(i).getValue());
            }
        } else {
            for (int i = 0; i < tonAbsractCommonAdapter.data.size(); i++) {
                totalValue += Float.parseFloat(tonAbsractCommonAdapter.data.get(i).getValue());
            }
        }
        DecimalFormat form = new DecimalFormat("#.#");
        binding.ultimateBearingCapacity.setText(String.valueOf(form.format(totalValue)));
        if (size > 5) {
            binding.ultimateBearingCapacity.setText(String.valueOf(form.format(totalValue / 5)));
        } else {
            binding.ultimateBearingCapacity.setText(String.valueOf(form.format(totalValue / size)));
        }*/
    }


    public List<com.wooti.tech.domain.report.Penetration> getTotalPenetrationValueList() {

        List<com.wooti.tech.domain.report.Penetration> list = new ArrayList<com.wooti.tech.domain.report.Penetration>();
        int size = penetrationAbsractCommonAdapter.data.size();
        //Log.e("HJLEE", "size : " + size);
        float totalValue = 0;
        if (size > 5) {
            int start = size - 5;
            //Log.e("HJLEE", "start : " + size);
            for (int i = start; i < penetrationAbsractCommonAdapter.data.size(); i++) {
                //Log.e("HJLEE", "i : " + i);
                //Log.e("HJLEE", "last 5 : " + penetrationAbsractCommonAdapter.data.get(i).getPenetrationNumber() + " : " + penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
                totalValue += Float.parseFloat(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
                list.add(new com.wooti.tech.domain.report.Penetration(penetrationAbsractCommonAdapter.data.get(i).getPenetrationNumber(), penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue()));
            }
        } else {
            for (int i = 0; i < penetrationAbsractCommonAdapter.data.size(); i++) {
                totalValue += Float.parseFloat(penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue());
                list.add(new com.wooti.tech.domain.report.Penetration(penetrationAbsractCommonAdapter.data.get(i).getPenetrationNumber(), penetrationAbsractCommonAdapter.data.get(i).getPenetrationValue()));
            }
        }
       /* DecimalFormat form = new DecimalFormat("#.#");
        binding.totalPenetrationValue.setText(String.valueOf(form.format(totalValue)));
        if (size > 5) {
            binding.avgPenetrationValue.setText(String.valueOf(form.format(totalValue / 5)));
        }else{
            binding.avgPenetrationValue.setText(String.valueOf(form.format(totalValue / size)));
        }*/
        return list;
    }


    public void onClickPadSendClick(View view) {

        if (SystemClock.elapsedRealtime() - mLastClickSendTime < 1000) {
            Toast.makeText(getApplicationContext(), "2번클릭 방지", Toast.LENGTH_SHORT).show();
            return;
        }
        mLastClickSendTime = SystemClock.elapsedRealtime();

        if (!binding.currentDatetime.getText().equals(DateUtil.getCurrentDateTime())) {
            binding.currentDatetime.setText(DateUtil.getCurrentDateTime());
        }

        float avg = 0;
        float standard = 0;
        try {
            avg = Float.valueOf(binding.avgPenetrationValue.getText().toString());
            standard = Float.valueOf(binding.managedStandart.getText().toString());
            errorMessagehandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {

        }
        boolean result = doValidation();
        if (result) {




/*
            if (internetStatus != 1 && internetStatus != 2) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                builder.setTitle("인터넷 연결이 활성화 되지 않았습니다. 인터넷 상태를 확인하세요.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dg, int which) {
                        dg.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else */


            if (binding.balance.getText().toString().length() > 0) {
                float balance = Float.parseFloat(binding.balance.getText().toString());
                if (balance < 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                    builder.setTitle("공삭공입니다. 이대로 진행하시겠습니끼?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dg, int which) {

                            if (penetrationAbsractCommonAdapter.data.size() < 5) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                                builder.setTitle("측정횟수가 5회 미만입니다. 그래도 전송하시겠습니까?");
                                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dg, int which) {

                                        if (dialog == null) {
                                            dialog = new ProgressDialog(ControllActivity.this);
                                        }
                                        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        dialog.setMessage(getBigText("서버에 전송중입니다."));
                                        dialog.show();
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                dialog.dismiss();
                                            }
                                        }, 3000);
                                        sendServer();
                                        dg.dismiss();
                                    }
                                });
                                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            } else if (penetrationAbsractCommonAdapter.data.size() == 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                                builder.setTitle("측정된 데이터가 없습니다. 관입량을 측정해 주세요.");
                                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dg, int which) {
                                        dg.dismiss();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            } else if (binding.managedFlagText.getText().toString().equals("관리기준 이상입니다.")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                                builder.setTitle("관입량이 관리기준 이상입니다. 그래도 전송하시겠습니까?");
                                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dg, int which) {
                                        if (dialog == null) {
                                            dialog = new ProgressDialog(ControllActivity.this);
                                        }
                                        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        dialog.setMessage(getBigText("서버에 전송중입니다."));
                                        dialog.show();
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                dialog.dismiss();
                                            }
                                        }, 3000);
                                        sendServer();
                                        dg.dismiss();
                                    }
                                });
                                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            } else {

                                if (dialog == null) {
                                    dialog = new ProgressDialog(ControllActivity.this);
                                }
                                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                dialog.setMessage(getBigText("서버에 전송중입니다."));
                                dialog.show();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        dialog.dismiss();
                                    }
                                }, 3000);
                                sendServer();
                            }


                            dg.dismiss();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else if (penetrationAbsractCommonAdapter.data.size() < 5) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                    builder.setTitle("측정횟수가 5회 미만입니다. 그래도 전송하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dg, int which) {
                            if (dialog == null) {
                                dialog = new ProgressDialog(ControllActivity.this);
                            }
                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            dialog.setMessage(getBigText("서버에 전송중입니다."));
                            dialog.show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    dialog.dismiss();
                                }
                            }, 3000);
                            sendServer();
                            dg.dismiss();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else if (penetrationAbsractCommonAdapter.data.size() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                    builder.setTitle("측정된 데이터가 없습니다. 관입량을 측정해 주세요.");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dg, int which) {
                            dg.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    //} else if (binding.managedFlagText.getText().toString().equals("관리기준 이상입니다.")) {
                } else if (avg > standard) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ControllActivity.this);
                    builder.setTitle("관입량이 관리기준 이상입니다. 그래도 전송하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dg, int which) {
                            if (dialog == null) {
                                dialog = new ProgressDialog(ControllActivity.this);
                            }
                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            dialog.setMessage(getBigText("서버에 전송중입니다."));
                            dialog.show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    dialog.dismiss();
                                }
                            }, 3000);
                            sendServer();
                            dg.dismiss();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    if (dialog == null) {
                        dialog = new ProgressDialog(ControllActivity.this);
                    }
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setMessage(getBigText("서버에 전송중입니다."));
                    dialog.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 3000);
                    sendServer();
                }
            }
        }
    }


    /*public void onClickPadEnterClick(View view) {
        doMeasureValidation();
    }*/


    private boolean doMeasureValidation() {

        if (binding.fileType.getText().length() == 0) {
            showEditTextError(binding.fileType, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;
            //공법
        } else if (binding.method.getText().length() == 0) {
            showEditTextError(binding.method, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;

            //파일규격
        } else if (binding.pileStandard.getText().length() == 0) {
            showEditTextError(binding.pileStandard, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;
            //위치
        } else if (binding.location.getText().length() == 0) {
            showEditTextError(binding.location, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;
            //헤머무게
        } else if (binding.hammaT.getText().length() == 0) {
            showEditTextError(binding.hammaT, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;
            //낙하높이
        } else if (binding.fallMeter.getText().length() == 0) {
            showEditTextError(binding.fallMeter, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;
            //관리기준
        } else if (binding.managedStandart.getText().length() == 0) {
            showEditTextError(binding.managedStandart, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;
        }

        //파일길이 벨리데이션 체크
         /*else if (!isEmptyAllPieseEditText()) {
        //모두 입력하지 않았다면?
            for (int i = 0; i < pieceAbsractCommonAdapter.data.size(); i++) {
                if (getPieseEditText(i).getText().length() == 0) {
                    showEditTextError(getPieseEditText(i), "입력", true);
                    isAllWrite = false;
                    return false;
                }
            }
        } else if (getPieseEditText(0).getText().length() == 0) {

        //단본이 입력되아있지 않음.
            getPieseEditText(0).setError(null);
            for (int i = 1; i < pieceAbsractCommonAdapter.data.size(); i++) {
                if (getPieseEditText(i).getText().length() == 0) {
                    showEditTextError(getPieseEditText(i), "필수 입력요소입니다.", true);
                    isAllWrite = false;
                    return false;
                }

            }
            isAllWrite = true;
            return true;

        } else if (getPieseEditText(0).getText().length() > 0) {
            //단본이 입력되있음..
            for (int i = 0; i < pieceAbsractCommonAdapter.data.size(); i++) {
                if(getPieseEditText(i) != null){
                    getPieseEditText(i).setError(null);
                }

            }
            isAllWrite = true;
            return true;

         }*/
        isAllWrite = true;
        return true;
    }

    private boolean doValidation() {
        //resetEditText();
        //파일종류
        if (binding.fileType.getText().length() == 0) {
            showEditTextError(binding.fileType, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;
            //공법
        } else if (binding.method.getText().length() == 0) {
            showEditTextError(binding.method, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;

            //파일규격
        } else if (binding.pileStandard.getText().length() == 0) {
            showEditTextError(binding.pileStandard, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;

            //위치
        } else if (binding.location.getText().length() == 0) {
            showEditTextError(binding.location, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;

            //헤머무게
        } else if (binding.hammaT.getText().length() == 0) {
            showEditTextError(binding.hammaT, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;

            //낙하높이
        } else if (binding.fallMeter.getText().length() == 0) {
            showEditTextError(binding.fallMeter, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;
            //관리기준
        } else if (binding.managedStandart.getText().length() == 0) {
            showEditTextError(binding.managedStandart, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;

            //파일 넘버
        } else if (binding.pileNo.getText().length() == 0) {
            showEditTextError(binding.pileNo, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;

            //관잎깊이
        } else if (binding.intrusionDepth.getText().length() == 0) {

            showEditTextError(binding.intrusionDepth, "필수 입력요소입니다.", true);
            isAllWrite = false;
            return false;


        } else if (!isEmptyAllPieseEditText()) {
            //모두 입력하지 않았다면?
            for (int i = 0; i < pieceAbsractCommonAdapter.data.size(); i++) {
                if (getPieseEditText(i).getText().length() == 0) {
                    showEditTextError(getPieseEditText(i), "입력요소", true);
                    isAllWrite = false;
                    return false;
                }
            }
        } else if (getPieseEditText(0).getText().length() == 0) {
            //단본이 입력되아있지 않음.
            getPieseEditText(0).setError(null);
            for (int i = 1; i < pieceAbsractCommonAdapter.data.size(); i++) {
                if (getPieseEditText(i).getText().length() == 0) {
                    showEditTextError(getPieseEditText(i), "입력요소", true);
                    isAllWrite = false;
                    return false;
                }

            }
            isAllWrite = true;
            return true;

        } else if (getPieseEditText(0).getText().length() > 0) {
            //단본이 입력되있음..
            for (int i = 0; i < pieceAbsractCommonAdapter.data.size(); i++) {
                if (getPieseEditText(i) != null) {
                    getPieseEditText(i).setError(null);
                }

            }
            isAllWrite = true;
            return true;

            //천공깊이
        } else if (binding.drillingDepth.getText().length() == 0) {

            showEditTextError(binding.drillingDepth, "필수 입수력요소입니다.", true);
            isAllWrite = false;
            return false;
        }

        isAllWrite = true;
        return true;
    }


    public void writeCharacteristic() {
        if (!connected) {
            Toast.makeText(getApplicationContext(), "블루투스에 연결되지 않았습니다.", Toast.LENGTH_LONG).show();
        } else {

            binding.managedStandart.setError(null);
            //  binding.managedStandart.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            shakeItBaby();
           /* if (count == 0) {
                binding.btnEnter.setText("READY");
            } else {
                binding.btnEnter.setText("READ");
            }*/
            List<BluetoothGattService> gattCharacteristics = mBluetoothLeService.getSupportedGattServices();
            int i = 0;
            for (BluetoothGattService gattCharacteristic : gattCharacteristics) {

                Log.e("HJEE", "UUID : " + gattCharacteristic.getUuid().toString());

                //if (gattCharacteristic.getUuid().toString().matches("6e400001-b5a3-f393-e0a9-e50e24dcca9e")) {


                if (gattCharacteristic.getUuid().toString().matches(SampleGattAttributes.WOORI_PARENT_UUID)) {
                    BluetoothGattCharacteristic writeCharacteristic = gattCharacteristic.getCharacteristics().get(0);
                    writeCharacteristic.setValue(new byte[]{0x24, 0x52, 0x45, 0x41, 0x44, 0x2C, 0x30, 0x0D, 0x0A});
                    Log.e("HJLEE", "전송합니다.");
                    mBluetoothLeService.writeCharacteristic(writeCharacteristic);
                    break;
                }
                i++;
            }

        }
    }

    public boolean isStatusIsPieceListview() {
        boolean isSucsess = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < pieceAbsractCommonAdapter.data.size(); i++) {
            View view = binding.pieceListview.getChildAt(i);
            @SuppressLint("WrongViewCast")
            EditText et = (EditText) view.findViewById(R.id.value);
            if (et.getText().length() == 0) {
                isSucsess = false;
                break;
            }
        }
        return isSucsess;
    }

    public boolean isEmptyAllPieseEditText() {
        StringBuilder sb = new StringBuilder();
        if (pieceAbsractCommonAdapter.data.size() <= 0) {
            return false;
        } else {
            for (int i = 0; i < pieceAbsractCommonAdapter.data.size(); i++) {
                View view = binding.pieceListview.getChildAt(i);
                @SuppressLint("WrongViewCast")
                EditText et = (EditText) view.findViewById(R.id.value);
                sb.append(et.getText().toString());
            }
            return sb.toString().length() > 0 ? true : false;
        }
    }


    public EditText getPieseEditText(int i) {
        try {
            View view = binding.pieceListview.getChildAt(i);
            @SuppressLint("WrongViewCast")
            EditText edittext = (EditText) view.findViewById(R.id.value);
            return edittext;
        } catch (Exception e) {
            return null;
        }
    }

    public void showEditTextError(EditText editText, String error, boolean enable) {
        editText.setError(error);
        editText.setFocusableInTouchMode(enable);
        editText.requestFocus();
    }

    public void setInterNetStatus(int status) {
        if (status == 1 || status == 2) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.connectInternet.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_green_network));
            } else {
                binding.connectInternet.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_green_network));
            }
        } else if (status == 3) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.connectInternet.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_network));
            } else {
                binding.connectInternet.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_network));
            }
        } else {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.connectInternet.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_network));
            } else {
                binding.connectInternet.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_network));
            }
        }
    }

    public void setBluetoothStatus(boolean status) {
        if (status) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.connectBluetooth.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_green_bluetooth));
            } else {
                binding.connectBluetooth.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_green_bluetooth));
            }
        } else {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.connectBluetooth.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_bluetooth));
            } else {
                binding.connectBluetooth.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_bluetooth));
            }
        }
    }


    public void setManagedFlagStatus(int status) {
        binding.manageFlagLayout.setVisibility(View.VISIBLE);
        if (status == 0) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.managedFlag.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_up_arrow));
            } else {
                binding.managedFlag.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_up_arrow));
            }
        } else if (status == 1) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.managedFlag.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_downward_black_24dp));
                binding.managedFlagText.setText("관리기준");


                binding.btnSend.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_gr_btn));
                binding.btnMeasure.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_gr_btn));
            } else {
                binding.managedFlag.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_downward_black_24dp));
                binding.managedFlagText.setText("관리기준");


                binding.btnSend.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_gr_btn));
                binding.btnMeasure.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_gr_btn));
            }
        } else if (status == 2) {
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                binding.managedFlag.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_up_arrow));
                binding.managedFlagText.setText("관리기준");


                binding.btnSend.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_gr_btn));
                binding.btnMeasure.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_gr_btn));
            } else {
                binding.managedFlag.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_red_up_arrow));
                binding.managedFlagText.setText("관리기준");


                binding.btnSend.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_rd_btn));
                binding.btnMeasure.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.custom_rd_btn));
            }
        }

    }

    public void changeEditTextMode(final EditText editText) {
        editText.setTag(editText.getKeyListener());
        editText.setKeyListener(null);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final int sdk = android.os.Build.VERSION.SDK_INT;
                keyboardTarget = editText;
                if (hasFocus) {
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        editText.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_focus_edit_text));
                    } else {
                        editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_focus_edit_text));
                    }

                } else {
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        editText.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_edit_text));
                    } else {
                        editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_edit_text));
                    }
                }
            }
        });
    }


    public void changeEditTextModeFileNo(final EditText editText) {
        editText.setTag(editText.getKeyListener());
        editText.setKeyListener(null);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final int sdk = android.os.Build.VERSION.SDK_INT;
                keyboardTarget = editText;
                if (hasFocus) {
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        editText.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_focus_edit_text));
                    } else {
                        editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_focus_edit_text));
                    }
                    binding.clearBtn.setText("-");
                } else {
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        editText.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_edit_text));
                    } else {
                        editText.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.common_edit_text));
                    }
                    binding.clearBtn.setText("C");
                }
            }
        });
    }

    public void showDialogEditText() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ControllActivity.this);
        EditText text = new EditText(ControllActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(24, 24, 24, 24);
        text.setLayoutParams(params);
        builder.setView(text);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public void showOpenDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.alert_layout, null);
        final EditText edit = (EditText) view.findViewById(R.id.edit_text);
        builder.setView(view);
        edit.setText(binding.location.getText().toString());
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                binding.location.setText(edit.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        edit.post(new Runnable() {
            @Override
            public void run() {
                edit.setFocusableInTouchMode(true);
                edit.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.showSoftInput(edit, 0);
            }
        });
    }

    public void showOpenDialog(Context context, final EditText bEdit, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.alert_layout, null);
        final EditText edit = (EditText) view.findViewById(R.id.edit_text);
        builder.setTitle(message);
        builder.setView(view);
        edit.setText(bEdit.getText().toString());
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bEdit.setText(edit.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        edit.post(new Runnable() {
            @Override
            public void run() {
                edit.setFocusableInTouchMode(true);
                edit.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.showSoftInput(edit, 0);
            }
        });
    }

    public void showSimpleDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(message);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public SpannableString getBigText(String message) {
        SpannableString ss1 = new SpannableString(message);
        ss1.setSpan(new RelativeSizeSpan(5f), 0, ss1.length(), 0);
        ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), 0, ss1.length(), 0);
        return ss1;
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;
        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), R.string.press_back_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickConnectBluetooth(View view) {
        Toast.makeText(getApplicationContext(), "블루투스에 연결합니다.", Toast.LENGTH_SHORT).show();
        if (mGattUpdateReceiver == null) {
            Toast.makeText(getApplicationContext(), "mGattUpdateReceiver 가 활성화되지 않았습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "mGattUpdateReceiver 가 활성화중.", Toast.LENGTH_SHORT).show();
        }
        if (mBluetoothLeService == null) {
            Toast.makeText(getApplicationContext(), "mBluetoothLeService 가 활성화되지 않았습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "mBluetoothLeService 가 활성화중.", Toast.LENGTH_SHORT).show();
        }
        startScan();
    }

    public boolean isOnline() {
        if (NetworkStatus.getConnectivityStatus(getApplicationContext()) != 1 && NetworkStatus.getConnectivityStatus(getApplicationContext()) != 2) {
            return false;
        } else {
            return true;
        }
    }

    public void resetPiece(View view) {

        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            //Toast.makeText(getApplicationContext(), "2번클릭 방지",Toast.LENGTH_SHORT).show();
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        presenter.getPeiceItems();
        //합계 원상태
        binding.totalConnectWidth.setText("0");
        //용접개소 원상태
        binding.connectLength.setText("0");
        //천공깊이 원상태
        binding.drillingDepth.setText("");
        //관입깊이 원상태
        binding.intrusionDepth.setText("");
    }

    public Float getFivePenatrationValue() {
        try {
            float standard = Float.valueOf(binding.managedStandart.getText().toString());
            float defaultValue;
            if (penetrationAbsractCommonAdapter.data.size() > 5) {
                defaultValue = 5 * standard;
            } else {
                defaultValue = penetrationAbsractCommonAdapter.data.size() * standard;
            }
            Log.e("getFivePenatrationValue", defaultValue + "");
            return defaultValue;
        } catch (Exception e) {
            return null;
        }

    }
}

