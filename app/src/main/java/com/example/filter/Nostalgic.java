package com.example.filter;

import android.graphics.Bitmap;

/**
 * Created by 10405 on 2016/7/26.
 * extends BaseFilter
 */

class Nostalgic extends BaseFilter {
    Nostalgic(Bitmap src) {
        super(src);
    }

    @Override
    public Bitmap filterBitmap() {
        setCm(new float[] { // 怀旧效果
                0.393f, 0.769f, 0.189f, 0, 0,
                0.349f, 0.686f, 0.168f, 0, 0,
                0.272f, 0.543f, 0.131f, 0, 0,
                0, 0, 0, 1, 0 });
        return super.filterBitmap();
    }

}
