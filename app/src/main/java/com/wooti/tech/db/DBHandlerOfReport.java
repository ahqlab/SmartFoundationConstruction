package com.wooti.tech.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wooti.tech.domain.Device;
import com.wooti.tech.domain.Report;
import com.wooti.tech.util.util.DateUtil;

import java.util.ArrayList;
import java.util.List;


public class DBHandlerOfReport {

    public static final String TABLE_NAME = "TB_REPORT";

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandlerOfReport(Context context) {
        this.dbHelper = new DBHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public boolean insertReport(Report report, String reportIdx) {
        Log.e("HJLEE", "insertDevice : " + report.toString());
        ContentValues values = new ContentValues();
        values.put("reportIdx", reportIdx);
        values.put("deviceIdx", report.getDeviceIdx());
        values.put("currentDateTime", report.getCurrentDateTime());
        values.put("location", report.getLocation());
        values.put("pileNo", report.getPileNo());
        values.put("pileStandard", report.getPileStandard());
        values.put("drillingDepth", report.getDrillingDepth());
        values.put("intrusionDepth", report.getIntrusionDepth());
        values.put("balance", report.getBalance());
        values.put("connectLength", report.getConnectLength());
        values.put("managedStandard", report.getManagedStandard());
        values.put("avgPenetrationValue", report.getAvgPenetrationValue());
        values.put("totalPenetrationValue", report.getTotalPenetrationValue());
        values.put("hammaT", report.getHammaT());
        values.put("fallMeter", report.getFallMeter());
        values.put("createDate", DateUtil.getCurrentDateTimeSec());
        values.put("pileType", report.getPileType());
        values.put("method", report.getMethod());
        values.put("totalConnectWidth", report.getTotalConnectWidth());
        values.put("ultimateBearingCapacity", report.getUltimateBearingCapacity());
        //함마효율
        values.put("hammaEfficiency", report.getHammaEfficiency());
        //탄성계수
        values.put("modulusElasticity", report.getModulusElasticity());
        //지지력
        values.put("crossSection", report.getCrossSection());


        return (int) db.insert(TABLE_NAME, null, values) > 0;

    }

    public int getCount(){
       /* this.db = dbHelper.getReadableDatabase();


        long taskCount = DatabaseUtils.longForQuery(db, "SELECT COUNT (*) FROM " + TABLE_TODOTASK + " WHERE " + KEY_TASK_TASKLISTID + "=?",
                new String[] { String.valueOf(tasklist_Id) });

        Cursor cursor = db.rawQuery("select count(*) from " +  TABLE_NAME + " where isUpload = ? ", new String[]{Integer.toString(1)});*/
        return select().size();
    }

    public Integer allDelete() {
        return db.delete(TABLE_NAME, null, null);
    }

    public List<Report> select(){
        List<Report> reports = new ArrayList<Report>() ;
        this.db = dbHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null,null,null,null,null,"createDate asc");
        while (c.moveToNext()){
            Report report = new Report();

            String reportIdx = c.getString(c.getColumnIndex("reportIdx"));
            report.setReportIdx(reportIdx);

            int deviceIdx = c.getInt(c.getColumnIndex("deviceIdx"));
            report.setDeviceIdx(deviceIdx);

            String currentDateTime = c.getString(c.getColumnIndex("currentDateTime"));
            report.setCurrentDateTime(currentDateTime);

            String location = c.getString(c.getColumnIndex("location"));
            report.setLocation(location);

            String pileNo = c.getString(c.getColumnIndex("pileNo"));
            report.setPileNo(pileNo);

            String pileStandard = c.getString(c.getColumnIndex("pileStandard"));
            report.setPileStandard(pileStandard);

            String drillingDepth = c.getString(c.getColumnIndex("drillingDepth"));
            report.setDrillingDepth(drillingDepth);

            String intrusionDepth = c.getString(c.getColumnIndex("intrusionDepth"));
            report.setIntrusionDepth(intrusionDepth);

            float balance = c.getFloat(c.getColumnIndex("balance"));
            report.setBalance(balance);

            String connectLength = c.getString(c.getColumnIndex("connectLength"));
            report.setConnectLength(connectLength);

            String managedStandard = c.getString(c.getColumnIndex("managedStandard"));
            report.setManagedStandard(managedStandard);

            String avgPenetrationValue = c.getString(c.getColumnIndex("avgPenetrationValue"));
            report.setAvgPenetrationValue(avgPenetrationValue);

            String totalPenetrationValue = c.getString(c.getColumnIndex("totalPenetrationValue"));
            report.setTotalPenetrationValue(totalPenetrationValue);

            String hammaT = c.getString(c.getColumnIndex("hammaT"));
            report.setHammaT(hammaT);

            String fallMeter = c.getString(c.getColumnIndex("fallMeter"));
            report.setFallMeter(fallMeter);

            String createDate = c.getString(c.getColumnIndex("createDate"));
            report.setCreateDate(createDate);

            String pileType = c.getString(c.getColumnIndex("pileType"));
            report.setPileType(pileType);

            String method = c.getString(c.getColumnIndex("method"));
            report.setMethod(method);

            String totalConnectWidth = c.getString(c.getColumnIndex("totalConnectWidth"));
            report.setTotalConnectWidth(totalConnectWidth);

            String ultimateBearingCapacity = c.getString(c.getColumnIndex("ultimateBearingCapacity"));
            report.setUltimateBearingCapacity(ultimateBearingCapacity);

            String hammaEfficiency = c.getString(c.getColumnIndex("hammaEfficiency"));
            report.setHammaEfficiency(hammaEfficiency);

            String modulusElasticity = c.getString(c.getColumnIndex("modulusElasticity"));
            report.setModulusElasticity(modulusElasticity);

            String crossSection = c.getString(c.getColumnIndex("crossSection"));
            report.setCrossSection(crossSection);


            int isUpload = c.getInt(c.getColumnIndex("isUpload"));
            if(isUpload == 0){
                reports.add(report);
            }
        }
        return reports;
    }

    public int delete(Integer id) {
        return db.delete(TABLE_NAME, "reportIdx = ?", new String[]{Integer.toString(id)});
    }


    public boolean updateUploadState(String reportIdx){
        ContentValues values = new ContentValues();
        values.put("isUpload", 1);
        return (int) db.update(TABLE_NAME, values,"reportIdx= ? " , new String[]{reportIdx}) > 0;
    }
}
