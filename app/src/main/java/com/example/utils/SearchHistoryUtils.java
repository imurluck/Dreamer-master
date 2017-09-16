package com.example.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dreamera_master.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yourgod on 2017/8/21.
 */

public class SearchHistoryUtils {

    private String TAG = "SearchHistoryUtils";

    private static SearchHistoryUtils mSearchHistory;

    private final String TABLE_NAME = "searchHistory";

    private MyDatabaseHelper mHelper;

    private SearchHistoryUtils () {
        mHelper = new MyDatabaseHelper(MyApplication.getContext(), "SearchRecord.db", null, 1);
    }

    public static SearchHistoryUtils getInstance() {
        if (mSearchHistory == null) {
            mSearchHistory = new SearchHistoryUtils();
        }
        return mSearchHistory;
    }

    public void putNewSearch(String name) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean flag = isExist(name);
        Log.e(TAG, "flag -- " + flag);
        if (!flag) {
            ContentValues values = new ContentValues();
            values.put("placeName", name);
            db.insert(TABLE_NAME, null, values);
        }
    }

    public boolean isExist(String name) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where placeName = ?", new String[]{name});
        if (cursor.moveToFirst() ) {
            return true;
        } else {
            return false;
        }
    }

    public List<String> querySearchHistoryList() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        List<String> list = new ArrayList<String>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("placeName"));
                list.add(name);
            } while(cursor.moveToNext());
        }
        return list;
    }

    public void deleteAllSearchHistory() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public void deleteSearchHistory(String name) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if (isExist(name)) {
            db.delete(TABLE_NAME, "placeName = " +"'" + name + "'", null);
        }
    }
}
