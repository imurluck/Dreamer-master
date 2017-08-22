package com.example.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yourgod on 2017/8/21.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_SEARCH_HISTORY = "create table searchHistory (" +
            "id integer primary key autoincrement, " +
            "placeName text)";
    private Context mContext;

    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factoty, int version)  {
        super(context, name, factoty, version);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SEARCH_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
