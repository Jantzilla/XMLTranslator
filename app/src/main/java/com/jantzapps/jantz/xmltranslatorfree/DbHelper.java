package com.jantzapps.jantz.xmltranslatorfree;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by jantz on 7/10/2017.
 */

public class DbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME ="XMLTranslatorFree";
    private static final int DB_VER = 2;
    public static final String DB_TABLE ="TransTrack";
    public static final String DB_COLUM ="DailyChar";
    public static final String DB_COLUM2 ="Char";
    public static final String DB_COLUM3 ="Time";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + DB_TABLE + " (" +
                DB_COLUM + " NUMERIC, " +
                DB_COLUM2 + " TEXT, " +
                DB_COLUM3 + " NUMERIC " +
                ")";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS "+DB_TABLE+";";
        db.execSQL(query);
        onCreate(db);

    }

    public void initialize () {
        SQLiteDatabase db = this.getWritableDatabase();
        Integer initialInt = 0;
        Long time = 0L;
        String charName = "Char";
        String query2 = "INSERT INTO "+DB_TABLE+" VALUES ("+initialInt+", '"+charName+"', "+time+");";
        db.execSQL(query2);
        db.close();
    }

    public void addColumn () {
        SQLiteDatabase  db = this.getWritableDatabase();
        String query = "ALTER TABLE "+DB_TABLE+" ADD COLUMN Char TEXT;";                                                      //COLUMN ADD METHOD
        db.execSQL(query);
        db.close();
    }

    public void addCharCount (Integer addChar) {
        SQLiteDatabase  db = this.getWritableDatabase();
        String query = "UPDATE "+DB_TABLE+" SET "+DB_COLUM+" = DailyChar +"+addChar+" WHERE Char = 'Char';";
        db.execSQL(query);
        db.close();
    }

    public Integer getCharCount () {
        SQLiteDatabase  db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT "+DB_COLUM+" FROM "+DB_TABLE+" WHERE "+DB_COLUM2+" = 'Char'", null);
        c.moveToFirst();
        int index = c.getColumnIndex(DB_COLUM);
        Integer result = c.getInt(index);
        db.close();
        return result;
    }

    public Long getTime () {
        SQLiteDatabase  db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT "+DB_COLUM3+" FROM "+DB_TABLE+" WHERE "+DB_COLUM2+" = 'Char'", null);
        c.moveToFirst();
        int index = c.getColumnIndex(DB_COLUM3);
        Long result = c.getLong(index);
        db.close();
        return result;
    }

    public void newCharCount () {
        SQLiteDatabase  db = this.getWritableDatabase();
        String query = "UPDATE "+DB_TABLE+" SET "+DB_COLUM+" = "+0+" WHERE Char = 'Char';";
        db.execSQL(query);
        db.close();
    }

    public void newTime () {
        SQLiteDatabase  db = this.getWritableDatabase();
        String query = "UPDATE "+DB_TABLE+" SET "+DB_COLUM3+" = "+System.currentTimeMillis()+" WHERE Char = 'Char';";
        db.execSQL(query);
        db.close();
    }

}

