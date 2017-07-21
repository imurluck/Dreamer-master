package com.example.filter;

import android.graphics.Bitmap;

/**
 * Created by 10405 on 2016/7/26.
 * extends BaseFilter
 */

class Gray extends BaseFilter {

    Gray(Bitmap src) {
        super(src);
    }

    @Override
    public Bitmap filterBitmap() {
        setCm(new float[] { //灰度图
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0 });
        return super.filterBitmap();
    }
}
