package com.example.utils;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by 10405 on 2016/6/6.
 * A class between activity and file cache.
 */

public class AsyncGetDataUtil {
    private static Bitmap bitmap;   //图片资源
    private static final String TAG = "AsyncGetDataUtil";

    /**
     * Get Json from file. If Json missed then download from server.
     */
    public static String getJsonData() {
        String json = "";
        try {
            FileCacheUtil fileCacheUtil = new FileCacheUtil();
            String filename = fileCacheUtil.getFilename(FileCacheUtil.JSONPATH);
            //如果不存在，或超时了
            if("".equals(filename) || overTime(filename)) {
                getDataFromServer(); //然后从服务器下载数据
            }
            json = fileCacheUtil.getJsonFromFile();
            int time = 0;
            while("".equals(json)) {
                Thread.sleep(1000); //等待1s，留作下载数据
                time += 1000;
                json = fileCacheUtil.getJsonFromFile();
                //TODO 如果始终下载不到数据，就一直下
                if("".equals(json) && time == 2000) {
                    getDataFromServer(); //再下载一次
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Get JSON data from server
     */
    private static void getDataFromServer() throws Exception {
        final FileCacheUtil fileCacheUtil = new FileCacheUtil(FileCacheUtil.JSONPATH);
        fileCacheUtil.jsonCacheClear(); //清除原有JSON缓存
        final HttpConnectionUtil http = HttpConnectionUtil.getHttpConnectionUtilInstance();
        new Thread(){//开启一个新线程，从服务器端下载JSON数据
            public void run(){
                try{
                    http.doGet();//下载JSON数据
                    if(http.getStatus()){//如果下载成功了存到文件里
                        System.out.println("asdf download success");
                        String jsonString = http.getJsonString();//下载的JSON数据资源
                        fileCacheUtil.saveJSON(jsonString);
                    }else {
                        System.out.println("asdf download failed");
//                        getDataFromServer();//重新下载
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * Decode JSON data，expect picture json data
     * @param jsonStr
     * @return  ArrayList<HashMap<String, Object>>
     */
     static ArrayList<HashMap<String, Object>> decodeJsonToPoint(String jsonStr)
            throws JSONException {
         ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
         if("".equals(jsonStr))  {
             return list;
         }

         JSONArray jsonArray = new JSONArray(jsonStr);
         for (int i = 0; i < jsonArray.length(); i++) {
             JSONObject jsonObject = jsonArray.getJSONObject(i);
             HashMap<String, Object> map = new HashMap();
             map.put("name", jsonObject.getString("name"));
             //TODO 替换为point的文字介绍
//             map.put("text", "");
             map.put("longitude", jsonObject.getString("longitude"));
             map.put("latitude", jsonObject.getString("latitude"));
             map.put("cross_pictures", jsonObject.getString("cross_pictures"));
             list.add(map);
         }
         return list;
    }


    /**
     * Decode picture json data
     * @param jsonStr the picture json string data
     * @return  ArrayList<HashMap<String, Object>>
     */
    public static ArrayList<HashMap<String, Object>> decodeCrossPicturesJsonToPoint(String jsonStr)
            throws JSONException {
        ArrayList<HashMap<String, Object>> list = new ArrayList();
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            final HashMap<String, Object> map = new HashMap();
            map.put("id", jsonObject.getString("pk"));
            map.put("url", jsonObject.getString("picture"));
            map.put("detail_title", jsonObject.getString("detail_title"));
            String year = jsonObject.getString("datetime").substring(0,4);//截取datetime中的year
            map.put("date", year);
            list.add(map);
        }
        return list;
    }



    /**
     * Get picture by picture ID and url
     * @param id  picture ID
     * @param url picture url
     */
    public static void getPictureData(String id, String url){
        try {
            Log.e(TAG, "AsyncGetDataUtil(getPictureData) excute...");
            FileCacheUtil fileCacheUtil = new FileCacheUtil(FileCacheUtil.PICTUREPATH);
            String filename;
            filename = fileCacheUtil.getPicFilename(id);
            Log.e(TAG, "filename is " + filename);
            if("".equals(filename)){//如果文件不存在，则从服务器端获取
                getPicFromServer(url, id);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * 按id从文件中读图片
     */
    public static Bitmap getPicFromFile(String picId){
        try {
            bitmap = FileCacheUtil.getPicFromFile(FileCacheUtil.PICTUREPATH, picId);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Get photo by camera path
     * @return bitmap the picture in CAMERAPATH folder
     */
    public static Bitmap getPhotoFromFile(){
        try {
            bitmap = FileCacheUtil.getPhotoFromFile(FileCacheUtil.CAMERAPATH);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Judge whether file is over time
     * @param string a date time string
     * @return true or false
     */
    private static boolean overTime(String string) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date createDate = format.parse(string);
        Date presentDate = new Date();
        long createDay = createDate.getTime();//getTime()毫秒级别
        long presentDay = presentDate.getTime();
        long oneDay = 86400000; //一天的毫秒数 24*3600*1000
//        long oneHour = 3600000; //一小时
        // 超过一小时就更新，用于测试阶段
        return presentDay - createDay >= oneDay;
    }

    /**
     * Get picture from server
     */
    private static void getPicFromServer(final String url, final String id){
        //开启一个新线程，从服务器端下载图片
        new Thread(){
            public void run(){
                try{
                    System.out.println("asdf image url " + url);

                    Log.e(TAG, "getPicFormServer: url is " + url);

                    Drawable drawable = Drawable.createFromStream(new URL(url).openStream(), null);
                    if(drawable != null){
                        //如果下载成功了存到文件里
                        Log.e(TAG, "load picture success");
                        FileCacheUtil fileCacheUtil = new FileCacheUtil(FileCacheUtil.PICTUREPATH);
                        BitmapDrawable bd = (BitmapDrawable) drawable;
                        fileCacheUtil.savePicture(bd.getBitmap(), id, true);
                    } else {
                        //下载失败了，就再次调用自己
                        System.out.println("asdf 下载图片失败");
                        //getPicFromServer(url, id);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
