package com.example.textme.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseMessages extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DB";
    public static final String TABLE_MESSAGES = "messages";

    public static final String KEY_ID = "_id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NAME = "name";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_PORTRAIT = "portrait";
    public static final String KEY_TIME = "time";

    public DataBaseMessages(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_MESSAGES + "(" + KEY_ID + " integer primary key," + KEY_NAME + " text," + KEY_EMAIL + " text,"
                + KEY_MESSAGE + " text," + KEY_PORTRAIT + " text," + KEY_TIME + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_MESSAGES);
        onCreate(db);
    }
}
