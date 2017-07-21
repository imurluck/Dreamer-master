package com.example.utils;

import java.io.File;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Zhangzongxiang on 2017/5/13.
 */

public class HttpUtil {

    private static final String BASE_PLACE = "http://www.dreamera.net:8000/cross/place/";

    private static final String BASE_PICTURE = "http://www.dreamera.net:8000/cross/picture/";

    public static OkHttpClient okHttpClient = new OkHttpClient();
    public static void getPlaces(okhttp3.Callback callback) {
        Request request = new Request.Builder().url(BASE_PLACE).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static void postPlace(Map<String, String> paraMap, okhttp3.Callback callback) {
        if (callback == null) throw new NullPointerException("callback is null");
        if (paraMap == null) throw new NullPointerException("paraMap is null");
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Set<String> keySet = paraMap.keySet();
        for (String key : keySet) {
            String value = paraMap.get(key);
            formBodyBuilder.add(key, value);
        }
        FormBody formBody = formBodyBuilder.build();
        Request request = new Request
                .Builder()
                .post(formBody)
                .url(BASE_PLACE)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
    public static void getConCretePlace(String placeId, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_PLACE + placeId + "/")
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
    public static void putPlace(String placeId, Map<String, String> paraMap, okhttp3.Callback callback) {
        if (callback == null) throw new NullPointerException("callback is null");
        if (paraMap == null) throw new NullPointerException("paraMap is null");
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Set<String> keySet = paraMap.keySet();
        for (String key : keySet) {
            formBodyBuilder.add(key, paraMap.get(key));
        }
        FormBody formBody = formBodyBuilder.build();
        Request request = new Request
                .Builder()
                .url(BASE_PLACE + placeId + "/")
                .put(formBody)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
    public static void deletePlace(String placeId, okhttp3.Callback callback) {
        Request request = new Request
                .Builder()
                .url(BASE_PLACE + placeId + "/")
                .delete()
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static void getPicture(String pictureId, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_PICTURE + pictureId + "/")
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
    public static void postPicture(Map<String, String> paraMap, String imgPath, okhttp3.Callback callback) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        File img = new File(imgPath);
        Set<String> keySet = paraMap.keySet();
        for (String key : keySet) {
            builder.addFormDataPart(key, paraMap.get(key));
        }
        if (img != null) {
            builder.addFormDataPart("picture", img.getName(), RequestBody.create(MediaType.parse("image/jpg"), img));
        }
        MultipartBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(BASE_PICTURE)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
    public static void putPicture(String pictureId, Map<String, String> paraMap, String imgPath, okhttp3.Callback callback) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        File img = new File(imgPath);
        Set<String> keySet = paraMap.keySet();
        for (String key : keySet) {
            builder.addFormDataPart(key, paraMap.get(key));
        }
        if (img != null) {
            builder.addFormDataPart("picture", img.getName(), RequestBody.create(MediaType.parse("image/jpg"), img));
        }
        MultipartBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(BASE_PICTURE + pictureId + "/")
                .put(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
    public static void deletePicture(String pictureId, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_PICTURE + pictureId + "/")
                .delete()
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
