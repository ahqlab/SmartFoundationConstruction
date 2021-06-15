package com.wooti.tech.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wooti.tech.domain.Device;

import java.util.ArrayList;
import java.util.List;


public class DBHandlerOfDevice {

    public static final String TABLE_NAME = "TB_DEVICE";

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandlerOfDevice(Context context) {
        this.dbHelper = new DBHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public boolean insertDevice(Device device) {
        Log.e("device", "device : " + device.toString());
        ContentValues values = new ContentValues();
        values.put("id", device.getId());
        values.put("lavelNo", device.getLavelNo());
        values.put("bluetoothNo", device.getBluetoothNo());
        values.put("tabletNo", device.getTabletNo());
        values.put("password", device.getPassword());
        values.put("tabletManager", device.getTabletNo());
        values.put("startDate", device.getStartDate());
        values.put("endDate", device.getEndDate());
        values.put("constructionIdx", device.getConstructionIdx());
        return (int) db.insert(TABLE_NAME, null, values) > 0;

    }

    public Integer allDelete() {
        return db.delete(TABLE_NAME, null, null);
    }

    public List<Device> select(){
        List<Device> devices = new ArrayList<Device>() ;
        this.db = dbHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null,null,null,null,null,"id desc");
        while (c.moveToNext()){
            Device device = new Device();
            int id = c.getInt(c.getColumnIndex("id"));
            device.setId(id);
            String lavelNo = c.getString(c.getColumnIndex("lavelNo"));
            device.setLavelNo(lavelNo);
            String bluetoothNo = c.getString(c.getColumnIndex("bluetoothNo"));
            device.setBluetoothNo(bluetoothNo);
            String tabletNo = c.getString(c.getColumnIndex("tabletNo"));
            device.setTabletNo(tabletNo);
            String password = c.getString(c.getColumnIndex("password"));
            device.setPassword(password);
            String tabletManager = c.getString(c.getColumnIndex("tabletManager"));
            device.setTabletManager(tabletManager);
            String startDate = c.getString(c.getColumnIndex("startDate"));
            device.setStartDate(startDate);
            String endDate = c.getString(c.getColumnIndex("endDate"));
            device.setEndDate(endDate);
            int constructionIdx = c.getInt(c.getColumnIndex("constructionIdx"));
            device.setConstructionIdx(constructionIdx);
            devices.add(device);
        }
        return devices;
    }

    public int delete(Integer id) {
        return db.delete(TABLE_NAME, "id = ?", new String[]{Integer.toString(id)});
    }


    public int getCount(){
        this.db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select count(*) from " +  TABLE_NAME, null);
        return c.getCount();
    }

    public Device login(Device device) {
        Device rDevice = null;
        this.db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_NAME + " where tabletNo = ? and password = ? ", new String[]{ device.getTabletNo(),  device.getPassword() });
        while (c.moveToNext()){
            rDevice = new Device();
            int id = c.getInt(c.getColumnIndex("id"));
            rDevice.setId(id);
            String lavelNo = c.getString(c.getColumnIndex("lavelNo"));
            rDevice.setLavelNo(lavelNo);
            String bluetoothNo = c.getString(c.getColumnIndex("bluetoothNo"));
            rDevice.setBluetoothNo(bluetoothNo);
            String tabletNo = c.getString(c.getColumnIndex("tabletNo"));
            rDevice.setTabletNo(tabletNo);
            String password = c.getString(c.getColumnIndex("password"));
            rDevice.setPassword(password);
            String tabletManager = c.getString(c.getColumnIndex("tabletManager"));
            rDevice.setTabletManager(tabletManager);
            String startDate = c.getString(c.getColumnIndex("startDate"));
            rDevice.setStartDate(startDate);
            String endDate = c.getString(c.getColumnIndex("endDate"));
            rDevice.setEndDate(endDate);
            int constructionIdx = c.getInt(c.getColumnIndex("constructionIdx"));
            rDevice.setConstructionIdx(constructionIdx);
        }
        return rDevice;
    }


}
