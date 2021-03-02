package com.example.seung.octotproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by USER on 2018-08-26.
 */
class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="octotapp.db";
    private static final int DATABASE_VERSION=3;

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS members(date TEXT,id TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS checkbox(date TEXT, checked INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS first_toggle(date TEXT, onoff INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS second_toggle(date TEXT, onoff INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS third_toggle(date TEXT, onoff INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS fourth_toggle(date TEXT, onoff INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //db.execSQL("DROP TABLE members");
        db.execSQL("CREATE TABLE IF NOT EXISTS members(date TEXT,id TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS checkbox(date TEXT, checked INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS first_toggle(date TEXT, onoff INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS second_toggle(date TEXT, onoff INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS third_toggle(date TEXT, onoff INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS fourth_toggle(date TEXT, onoff INTEGER);");

        System.out.println("테이블 생성완료");
    }
}
