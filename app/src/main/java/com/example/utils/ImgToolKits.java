package com.example.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;


/**
 * Created by 10405 on 2016/07/03
 */
public class ImgToolKits {

    public static int addHeight = 0;

    /**
     * Change bitmap size
     * @param bitmap the picture needed to be changed
     * @param width picture width
     * @param height picture height
     */
    public static Bitmap changeBitmapSize(Bitmap bitmap, float width, float height) {
        float pw = width / (float) bitmap.getWidth();//pw 宽比
        float ph = height / (float)bitmap.getHeight();//ph 高比
        Matrix matrix = new Matrix();
        matrix.postScale(pw, ph); //长和宽放大缩小的比例
        try{
            Bitmap after = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);// after是缩放后的图片
            return after;
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Call canny function to create border picture
     */
    public static Bitmap getBorder(Bitmap srcb) {
        int width = srcb.getWidth();
        int height = srcb.getHeight();

        int[] pix = new int [width * height];
        srcb.getPixels(pix, 0, width, 0, 0, width, height);
        int[] resultPixes = OpenCVCanny.canny(pix, width, height);

        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        b.setPixels(resultPixes, 0, width, 0, 0, width, height);
        return b;
    }


    /**
     * Init border picture
     * @param bitmap the picture needed to be changed
     * @param width picture width
     * @param height picture height
     */
    public static Bitmap initBorderPic(Bitmap bitmap, float width, float height, boolean flag) {

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        Bitmap result;
        if(bitmapWidth >= bitmapHeight) {
            result = initBorderPic1(bitmap, width, height, flag);
        } else {
            result = initBorderPic2(bitmap, width, height, flag);
        }
        return result;
    }


    /**
     * Init border picture that width bigger than height
     * @param bitmap the picture needed to be changed
     * @param width picture width
     * @param height picture width
     */
    public static Bitmap initBorderPic1(Bitmap bitmap, float width, float height, boolean flag) {
        float newHeight =  (width / bitmap.getWidth()) * bitmap.getHeight();// 保持原有比例下的新高度
        Bitmap after = changeBitmapSize(bitmap, width, newHeight); // 调整比例
        if(flag) {
            after = getBorder(after);//边缘检测
        }

        //在after上下添加补充的透明空白
        addHeight = (int) (height - newHeight) / 2;
        Bitmap add = Bitmap.createBitmap((int)width, addHeight, Bitmap.Config.ARGB_8888);
        // 构造最后结果图片
        Bitmap result = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);

        Paint p = new Paint(); // 构造画笔
        p.setStyle( Paint.Style.STROKE );
        p.setAlpha(0);//设置透明度为0,即全透明
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(add,0, 0, p);// 开始在结果图上绘制
        canvas.drawBitmap(after, 0, addHeight, null);
        canvas.drawBitmap(add,0, addHeight+after.getHeight(), p);

        after.recycle(); //内存回收
        add.recycle();
        return result;
    }


    /**
     * Init border picture that width smaller than height
     * @param bitmap the picture needed to be changed
     * @param width picture width
     * @param height picture width
     */
    public static Bitmap initBorderPic2(Bitmap bitmap, float width, float height, boolean flag) {

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmapRotate = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // 旋转90度，是问题转换为 宽 > 高的情况
        Bitmap after = initBorderPic1(bitmapRotate, width, height, flag);
        matrix.postRotate(-180);
        Bitmap result = Bitmap.createBitmap(
                after, 0, 0, after.getWidth(), after.getHeight(), matrix, true);
        return result;
    }

}
