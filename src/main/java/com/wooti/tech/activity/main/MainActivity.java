package com.wooti.tech.activity.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.wooti.tech.R;
import com.wooti.tech.activity.common.BaseActivity;
import com.wooti.tech.activity.controll.ControllActivity;
import com.wooti.tech.databinding.ActivityMainBinding;
import com.wooti.tech.db.DBHandlerOfDevice;
import com.wooti.tech.domain.Device;
import com.wooti.tech.sharedPref.SharedPrefKeys;
import com.wooti.tech.sharedPref.SharedPrefManager;
import com.wooti.tech.util.NetworkStatus;

import java.util.List;

public class MainActivity extends BaseActivity<MainActivity> implements MainIn.View {

    private ActivityMainBinding binding;

    private MainIn.Presenter presenter;

    SharedPrefManager sharedPrefManager;

    private ProgressDialog dialog;

    //중복 클릭 방지 시간 설정 ( 해당 시간 이후에 다시 클릭 가능 )
    private static final long MIN_CLICK_INTERVAL = 2000;

    private long mLastClickTime = 0;

    private DBHandlerOfDevice dbHandler;

    int internetStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ////////
        dbHandler = new DBHandlerOfDevice(getApplicationContext());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getSupportActionBar().setTitle(R.string.app_name);
        sharedPrefManager = SharedPrefManager.getInstance(MainActivity.this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);
       // binding.setDomain(new Device("we8104"));
        binding.setDomain(new Device());

        presenter = new MainPresenter(this);
        internetStatus = NetworkStatus.getConnectivityStatus(getApplicationContext());

        setEditTextSharedInfo();
        binding.saveId.isChecked();
        binding.saveId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPrefManager.putBooleanExtra(SharedPrefKeys.SAVE_ID, isChecked);
            }
        });
        if(isOnline()){
            showProgress("서버 동기화중입니다.....");
            presenter.getAllDeviceList();
        }
    }

    private void setEditTextSharedInfo() {
        boolean saveUserId = sharedPrefManager.getBooleanExtra(SharedPrefKeys.SAVE_ID);
        if(saveUserId){
            binding.saveId.setChecked(true);
        }
        String tableNo = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_DEVICE_NO);
        if(tableNo != null || tableNo != ""){
            if(saveUserId){
                binding.getDomain().setTabletNo(tableNo);
            }
        }
        String tablePassword = sharedPrefManager.getStringExtra(SharedPrefKeys.SHARED_DEVICE_PASSWORD);
       /* if(tablePassword != null || tablePassword != ""){
            Log.e("HJLEE", "2 tablePassword : " + tablePassword);
            binding.getDomain().setPassword(tablePassword);
        }*/

    }

    @Override
    protected BaseActivity<MainActivity> getActivityClass() {
        return MainActivity.this;
    }

    public void onClickSubmit(View view) {

        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        mLastClickTime = currentClickTime;

        // 중복클릭 아닌 경우
        if (elapsedTime > MIN_CLICK_INTERVAL) {
            doLogin();
        }
    }

    private void doLogin(){

        if (!isOnline()) {
            if(binding.userId.getText().toString().length() == 0){
                binding.userId.setError("아이디를 입력하세요.");
                binding.userId.setFocusable(true);
                binding.userId.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.userId.setFocusableInTouchMode(true);
                        binding.userId.requestFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        imm.showSoftInput( binding.userId,0);
                    }
                });
            }else if(binding.password.getText().toString().length() == 0){
                binding.password.setError("비밀번호를 입력하세요.");
                binding.password.setFocusable(true);
                binding.password.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.password.setFocusableInTouchMode(true);
                        binding.password.requestFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        imm.showSoftInput( binding.password,0);
                    }
                });
            }else {

                int count = dbHandler.getCount();
                if(count  > 0){
                    showProgress();
                    Device parma = new Device();
                    parma.setTabletNo(binding.userId.getText().toString());
                    parma.setPassword(binding.password.getText().toString());
                    Device result = dbHandler.login(parma);
                    if(result != null){
                        Toast.makeText(getApplicationContext(), " 로그인 성공 ", Toast.LENGTH_SHORT).show();
                        setLoginResult(result);
                    }else{
                        dialog.dismiss();
                        idAndPasswordDoNotMatch("로그인정보가 맞지 않습니다. 아이디와 비밀번호를 확인하세요.");
                    }
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("서버동기화 실패로 로그인할 수 없습니다.");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dg, int which) {
                            dg.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }else{

            if(binding.userId.getText().toString().length() == 0){
                binding.userId.setError("아이디를 입력하세요.");
                binding.userId.setFocusable(true);
                binding.userId.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.userId.setFocusableInTouchMode(true);
                        binding.userId.requestFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        imm.showSoftInput( binding.userId,0);
                    }
                });
            }else if(binding.password.getText().toString().length() == 0){
                binding.password.setError("비밀번호를 입력하세요.");
                binding.password.setFocusable(true);
                binding.password.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.password.setFocusableInTouchMode(true);
                        binding.password.requestFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        imm.showSoftInput( binding.password,0);
                    }
                });
            }else{
                if (NetworkStatus.getConnectivityStatus(getApplicationContext()) != 1 && NetworkStatus.getConnectivityStatus(getApplicationContext()) != 2) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("인터넷 연결이 활성화 되지 않았습니다. 인터넷 상태를 확인하세요.");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dg, int which) {
                            dg.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else{
                    showProgress();
                    presenter.doLogin( binding.getDomain());
                }
            }
        }
    }


    @Override
    public void setServerError(String message) {
        showBasicOneBtnPopup(0, R.string.network_error)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                ).show();
    }

    @Override
    public void idAndPasswordDoNotMatch(String message) {
        showBasicOneBtnPopup("서버알림", message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                ).show();
    }

    @Override
    public void setLoginResult(Device device) {
        setSharedPref(device);
        MainActivity.super.moveIntent(MainActivity.this, ControllActivity.class, true);
    }


    public void showProgress(){
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("로딩중입니다..");
        dialog.setCancelable(false);
        dialog.show();
    }


    public void showProgress(String message){
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void setProgressGone() {
        dialog.dismiss();
    }

    @Override
    public void setDeviceList(List<Device> domain) {
        List<Device> list = dbHandler.select();
        if(list.size() > 0){
            //Toast.makeText(getApplicationContext(), "저장된 값 있음." , Toast.LENGTH_SHORT).show();
            dbHandler.allDelete();
            for (Device d : domain){
                dbHandler.insertDevice(d);
            }
        }else{
            //Toast.makeText(getApplicationContext(), "저장된 값 없음." , Toast.LENGTH_SHORT).show();
            for (Device d : domain){
                dbHandler.insertDevice(d);
            }
        }
        dialog.dismiss();
    }

    public void setSharedPref(Device device){
        sharedPrefManager.putIntExtra(SharedPrefKeys.SHARED_DEVICE_IDX, device.getId());
        sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_DEVICE_NO, device.getTabletNo());
        sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_DEVICE_PASSWORD, device.getPassword());
        sharedPrefManager.putStringExtra(SharedPrefKeys.SHARED_BLUE_NO, device.getBluetoothNo());
    }


    public boolean isOnline(){
        if (NetworkStatus.getConnectivityStatus(getApplicationContext()) != 1 && NetworkStatus.getConnectivityStatus(getApplicationContext()) != 2) {
            return false;
        }else{
            return true;
        }
    }
}
