package com.example.utils;

/**
 * Created by 10405 on 2016/7/2.
 */

public class OpenCVCanny {
    static {
        System.loadLibrary("OpenCV");
    }

    /**
     * 边缘检测
     *
     * @param buf
     * @param w
     * @param h
     * @return
     */
    public static native int[] canny(int[] buf, int w, int h);
}
