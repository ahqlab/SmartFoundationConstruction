package com.wooti.tech.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wooti.tech.domain.Device;
import com.wooti.tech.domain.report.Piece;
import com.wooti.tech.domain.report.SqlPiece;

import java.util.ArrayList;
import java.util.List;


public class DBHandlerOfPiece {

    public static final String TABLE_NAME = "TB_PIECE";

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandlerOfPiece(Context context) {
        this.dbHelper = new DBHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public boolean insertPiece(SqlPiece piece) {
        Log.e("HJLEE", "piece : " + piece.toString());
        ContentValues values = new ContentValues();
        values.put("reportIdx", piece.getReportIdx());
        values.put("name", piece.getName());
        values.put("value", piece.getValue());
        return (int) db.insert(TABLE_NAME, null, values) > 0;

    }

    public Integer allDelete() {
        return db.delete(TABLE_NAME, null, null);
    }

    public List<Piece> selectOne(String reportIdx){

        List<Piece> pieces = new ArrayList<>();
        this.db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_NAME + " where reportIdx = ? ", new String[]{ reportIdx });
        while (c.moveToNext()){
            Piece piece = new Piece();
            String name = c.getString(c.getColumnIndex("name"));
            piece.setName(name);
            String value = c.getString(c.getColumnIndex("value"));
            piece.setValue(value);
            pieces.add(piece);
        }
        return pieces;
    }
    public int delete(String reportIdx) {
        return db.delete(TABLE_NAME, "reportIdx = ?", new String[]{reportIdx});
    }
}
