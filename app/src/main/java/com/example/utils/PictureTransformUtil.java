package com.example.utils;

/**
 * Created by yourgod on 2017/8/26.
 */

public class PictureTransformUtil {

    static {
        System.loadLibrary("OpenCV");
    }

    public static native int[] ImgToGrey(int[] buf, int w, int h);
}
