package com.example.utils;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.example.dreamera_master.MyApplication;

/**
 * Created by Zhangzongxiang on 2017/5/10.
 */

public class HandleImagePath {

    private static String imagePath;

    public static String handleImagePath(Intent data) {
        if (Build.VERSION.SDK_INT >= 19) {
            handleImageOnKitKat(data);
        } else {
            handleImageBeforeKitKat(data);
        }
        return imagePath;
    }
    private static void handleImageOnKitKat(Intent data) {
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(MyApplication.getContext(), uri)) {
            //如果是document类型的uri，就通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri,就用普通的方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
    }

    private static void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        imagePath = getImagePath(uri, null);
    }

    private static String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过uri和selection来获取图片的真实路径
        Cursor cursor = MyApplication.getContext().getContentResolver()
                .query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore
                        .Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
