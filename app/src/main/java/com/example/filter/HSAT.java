package com.example.filter;

import android.graphics.Bitmap;

/**
 * Created by 10405 on 2016/7/26.
 * extends BaseFilter
 */

class HSAT extends BaseFilter {
    HSAT(Bitmap src) {
        super(src);
    }

    @Override
    public Bitmap filterBitmap() {
        setCm(new float[] { //高饱和度
                1.438f,  -0.122f, -0.016f, 0, -0.03f,
                -0.062f, 1.378f,  -0.016f, 0, 0.05f,
                -0.062f, -0.122f, 1.483f,  0, -0.02f,
                0,       0,       0,       1, 0});
        return super.filterBitmap();
    }
}
