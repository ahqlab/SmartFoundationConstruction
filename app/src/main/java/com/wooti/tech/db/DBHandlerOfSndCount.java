package com.wooti.tech.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wooti.tech.domain.SndCount;
import com.wooti.tech.util.util.DateUtil;


public class DBHandlerOfSndCount {

    public static final String TABLE_NAME = "TB_SENDING_COUNT";

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandlerOfSndCount(Context context) {
        this.dbHelper = new DBHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public boolean insert(SndCount sndCount) {
        ContentValues values = new ContentValues();
        values.put("sndCount", sndCount.getSndCount());
        values.put("currentDateTime", DateUtil.getCurrentDateTime());
        return (int) db.insert(TABLE_NAME, null, values) > 0;

    }

    public int getCount(){
        Cursor c = db.rawQuery("select * from " + TABLE_NAME + " where currentDateTime = ? ", new String[]{ DateUtil.getCurrentDateTime() });
        return c.getCount();
    }

    public int deleteNotToday() {
        return db.delete(TABLE_NAME, "currentDateTime <> ?", new String[]{DateUtil.getCurrentDateTime()});
    }
}
