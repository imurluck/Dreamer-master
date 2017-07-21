package com.example.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.dreamera_master.MyApplication;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Zhangzongxiang on 2017/5/17.
 */

public class ParseJSON {
    public static List<String> handleJSONForPlaces(String jsonData) {
        List<String> places = new ArrayList<String>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                places.add(jsonObject.getString("name"));
                Log.d("ParseJSON", jsonObject.getString("name"));
                Log.d("ParseJSON", jsonObject.getString("id"));
                SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("places",
                        Context.MODE_PRIVATE).edit();
                editor.putString(jsonObject.getString("name"), String.valueOf(jsonObject.getInt("id")));
                editor.apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return places;
    }

    public static ArrayList<HashMap<String, Object>> handleJSONForPlacesOnMap(String jsonData) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        if ("".equals(jsonData)) {
            return list;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i ++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                HashMap<String, Object> map = new HashMap();
                map.put("name", jsonObject.getString("name"));
                map.put("longitude", jsonObject.getString("longitude"));
                map.put("latitude", jsonObject.getString("latitude"));
                map.put("cross_pictures", jsonObject.getString("cross_pictures"));
                list.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
    public static Place handleJSONForConcretePlace(String jsonData) {
        return new Gson().fromJson(jsonData, Place.class);
    }
}
