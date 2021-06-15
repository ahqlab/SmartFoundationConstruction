package com.wooti.tech.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wooti.tech.domain.Device;
import com.wooti.tech.domain.Report;
import com.wooti.tech.domain.report.Piece;
import com.wooti.tech.domain.report.SqlPenetration;
import com.wooti.tech.domain.report.SqlPiece;

import java.util.ArrayList;
import java.util.List;


public class DBHandlerOfPenetration {

    public static final String TABLE_NAME = "TB_PENETRATION";

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandlerOfPenetration(Context context) {
        this.dbHelper = new DBHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public boolean insertPenetration(SqlPenetration penetration) {
        Log.e("HJLEE", "penetration : " + penetration.toString());
        ContentValues values = new ContentValues();
        values.put("reportIdx", penetration.getReportIdx());
        values.put("name", penetration.getName());
        values.put("value", penetration.getValue());
        return (int) db.insert(TABLE_NAME, null, values) > 0;
    }

    public Integer allDelete() {
        return db.delete(TABLE_NAME, null, null);
    }

    public List<com.wooti.tech.domain.report.Penetration> selectOne(String reportIdx){

        List<com.wooti.tech.domain.report.Penetration> penetrations = new ArrayList<com.wooti.tech.domain.report.Penetration>();
        this.db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_NAME + " where reportIdx = ? ", new String[]{ reportIdx });
        while (c.moveToNext()){
            com.wooti.tech.domain.report.Penetration penetration = new com.wooti.tech.domain.report.Penetration();
            String name = c.getString(c.getColumnIndex("name"));
            penetration.setName(name);
            String value = c.getString(c.getColumnIndex("value"));
            penetration.setValue(value);
            penetrations.add(penetration);
        }
        return penetrations;
    }

    public int delete(String reportIdx) {
        return db.delete(TABLE_NAME, "reportIdx = ?", new String[]{reportIdx});
    }
}
