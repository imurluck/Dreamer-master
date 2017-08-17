package com.example.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * Created by yourgod on 2017/8/17.
 */

public class GreyPicTransform extends BitmapTransformation {

    public GreyPicTransform(Context context) {
        super(context);
    }
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return convertGreyImg(toTransform);
    }

    @Override
    public String getId() {
        return "grey";
    }

    public  Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey;
                if (pixels[width * i + j] == 0) {
                    continue;
                } else grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.44 + (float) green * 0.45 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        //创建空的bitmap时，格式一定要选择ARGB_4444,或ARGB_8888,代表有Alpha通道，RGB_565格式的不显示灰度
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}
