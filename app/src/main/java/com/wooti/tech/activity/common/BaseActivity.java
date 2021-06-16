package com.wooti.tech.activity.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.wooti.tech.sharedPref.SharedPrefManager;

public abstract class BaseActivity <D extends Activity> extends AppCompatActivity {

    protected final String TAG = "WOORI_TECH";

    public SharedPrefManager mSharedPrefManager;

    private boolean badgeState = false;

    public interface OnSubBtnClickListener {
        void onClick(View v);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefManager = SharedPrefManager.getInstance(getActivityClass());
    }

    protected abstract BaseActivity<D> getActivityClass();

    public void setToolbarColor() {
      /*  Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.hodoo_pink), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.end_enter, R.anim.end_exit);
            }
        });*/
    }

    public AlertDialog.Builder showBasicOneBtnPopup(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityClass());
        if(title != null){
            builder.setTitle(title);
        }
        if(message != null){
            builder.setMessage(message);
        }
        return builder;
    }

    public AlertDialog.Builder showBasicOneBtnPopup(int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityClass());
        if(title != 0){
            builder.setTitle(title);
        }
        if(message != 0){
            builder.setMessage(message);
        }
        return builder;
    }

    public void showToast(String message) {
        Toast.makeText(getActivityClass(), message, Toast.LENGTH_SHORT).show();
    }

    public void moveIntent(Context packageContext, Class<?> cls, int enterAnim, int exitAnim, boolean kill){
        Intent intent = new Intent(packageContext, cls);
        startActivity(intent);
        overridePendingTransition(enterAnim , exitAnim);
        if(kill){
            finish();
        }
    }

    public void moveIntent(Context packageContext, Class<?> cls, boolean kill){
        Intent intent = new Intent(packageContext, cls);
        startActivity(intent);
        if(kill){
            finish();
        }
    }

    @Override
    protected void onStart() {
        //Log.v(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        //Log.v(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        //Log.v(TAG, "onResume");
        super.onResume();
    }


    @Override
    protected void onPause() {
        //Log.v(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        //Log.v(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Log.v(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return super.onKeyDown(keyCode, event);
    }
}
