package com.jantzapps.jantz.xmltranslatorfree.helpers

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by jantz on 7/10/2017.
 */

class DbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VER) {

    val charCount: Int?
        get() {
            val db = this.writableDatabase
            val c = db.rawQuery("SELECT $DB_COLUM FROM $DB_TABLE WHERE $DB_COLUM2 = 'Char'", null)
            c.moveToFirst()
            val index = c.getColumnIndex(DB_COLUM)
            val result = c.getInt(index)
            db.close()
            return result
        }

    val time: Long?
        get() {
            val db = this.writableDatabase
            val c = db.rawQuery("SELECT $DB_COLUM3 FROM $DB_TABLE WHERE $DB_COLUM2 = 'Char'", null)
            c.moveToFirst()
            val index = c.getColumnIndex(DB_COLUM3)
            val result = c.getLong(index)
            db.close()
            return result
        }

    override fun onCreate(db: SQLiteDatabase) {
        val query = "CREATE TABLE " + DB_TABLE + " (" +
                DB_COLUM + " NUMERIC, " +
                DB_COLUM2 + " TEXT, " +
                DB_COLUM3 + " NUMERIC " +
                ")"
        db.execSQL(query)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val query = "DROP TABLE IF EXISTS $DB_TABLE;"
        db.execSQL(query)
        onCreate(db)

    }

    fun initialize() {
        val db = this.writableDatabase
        val initialInt = 0
        val time = 0L
        val charName = "Char"
        val query2 = "INSERT INTO $DB_TABLE VALUES ($initialInt, '$charName', $time);"
        db.execSQL(query2)
        db.close()
    }

    fun addCharCount(addChar: Int?) {
        val db = this.writableDatabase
        val query = "UPDATE $DB_TABLE SET $DB_COLUM = DailyChar +$addChar WHERE Char = 'Char';"
        db.execSQL(query)
        db.close()
    }

    fun newCharCount() {
        val db = this.writableDatabase
        val query = "UPDATE $DB_TABLE SET $DB_COLUM = 0 WHERE Char = 'Char';"
        db.execSQL(query)
        db.close()
    }

    fun newTime() {
        val db = this.writableDatabase
        val query = "UPDATE " + DB_TABLE + " SET " + DB_COLUM3 + " = " + System.currentTimeMillis() + " WHERE Char = 'Char';"
        db.execSQL(query)
        db.close()
    }

    companion object {
        private val DB_NAME = "XMLTranslatorFree"
        private val DB_VER = 2
        val DB_TABLE = "TransTrack"
        val DB_COLUM = "DailyChar"
        val DB_COLUM2 = "Char"
        val DB_COLUM3 = "Time"
    }

}

