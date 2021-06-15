package com.wooti.tech.sharedPref;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hjlee on 2018-04-23.
 */

public class SharedPrefManager {
    //프리퍼런스 KEY값
    public static final String PREFERENCE_NAME = "woori_tech_pref";
    private static SharedPrefManager sharedPrefManager = null;
    private static Context mContext;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;

    public static SharedPrefManager getInstance(Context context) {
        mContext = context;

        if (sharedPrefManager == null) {
            sharedPrefManager = new SharedPrefManager();
        }
        if (prefs == null) {
            prefs = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            editor = prefs.edit();
        }
        return sharedPrefManager;
    }

    public void putFloatExtra(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public void putIntExtra(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void putStringExtra(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void putLongExtra(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void putBooleanExtra(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public Float getFloatExtra(String key) {
        return prefs.getFloat(key, 0);
    }


    public int getIntExtra(String key) {
        return prefs.getInt(key, 0);
    }


    public String getStringExtra(String key) {
        return prefs.getString(key, "");
    }


    public long getLongExtra(String key) {
        return prefs.getLong(key, 0);
    }


    public boolean getBooleanExtra(String key) {
        return prefs.getBoolean(key, false);
    }


    public void removePreference(String key) {
        editor.remove(key).commit();
    }


    public boolean containCheck(String key) {
        return prefs.contains(key);
    }


    public void removeAllPreferences() {
        editor.clear();
        editor.commit();
    }
}
