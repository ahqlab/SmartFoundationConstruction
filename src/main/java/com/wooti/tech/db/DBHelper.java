package com.wooti.tech.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "wooritech.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("HJLEE", "CREATE DATABASE");
        db.execSQL("CREATE TABLE TB_DEVICE (" +
                "id INTEGER NOT NULL, " +
                "lavelNo TEXT NOT NULL, " +
                "bluetoothNo TEXT NOT NULL, " +
                "tabletNo TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "tabletManager TEXT NOT NULL, " +
                "startDate TEXT NOT NULL, " +
                "endDate TEXT NOT NULL, " +
                "constructionIdx INTEGER NOT NULL);");

        db.execSQL("CREATE TABLE TB_PENETRATION (" +
                "reportIdx TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "value TEXT NOT NULL);");

        db.execSQL("CREATE TABLE TB_PIECE (" +
                "reportIdx TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "value TEXT NOT NULL);");


        db.execSQL("CREATE TABLE TB_REPORT (" +
                "reportIdx TEXT NOT NULL, " +
                "deviceIdx INTEGER NOT NULL, " +
                "currentDateTime TEXT NOT NULL, " +
                "location TEXT NOT NULL, " +
                "pileNo TEXT NOT NULL, " +
                "pileStandard TEXT NOT NULL, " +
                "drillingDepth TEXT NOT NULL, " +
                "intrusionDepth TEXT NOT NULL, " +
                "balance TEXT NOT NULL, " +
                "connectLength TEXT NOT NULL, " +
                "managedStandard TEXT NOT NULL, " +
                "avgPenetrationValue TEXT NOT NULL, " +
                "totalPenetrationValue TEXT NOT NULL, " +
                "hammaT TEXT NOT NULL, " +
                "fallMeter TEXT NOT NULL, " +
                "createDate TEXT NOT NULL, " +
                "pileType TEXT NOT NULL, " +
                "method TEXT NOT NULL, " +
                "totalConnectWidth TEXT NOT NULL, " +
                "ultimateBearingCapacity TEXT NOT NULL," +
                "hammaEfficiency TEXT NOT NULL," +
                "modulusElasticity TEXT NOT NULL," +
                "crossSection TEXT NOT NULL," +
                "isUpload INTEGER DEFAULT 0 );");


        db.execSQL("CREATE TABLE TB_SENDING_COUNT (" +
                "sndCount INTEGER NOT NULL, " +
                "currentDateTime TEXT NOT NULL);");
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
