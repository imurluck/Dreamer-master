package com.example.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

/**
 * Created by 10405 on 2016/7/26.
 * Base filter is the paretn class.
 */

public class BaseFilter {
    Bitmap src;
    private Bitmap dst;
    private Canvas canvas;
    private ColorMatrix cm;

    BaseFilter(Bitmap src) {
        this.src = src;
        dst = Bitmap.createBitmap(src.getWidth(),src.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(dst);
        cm = new ColorMatrix();
        initCm();
    }

    private void initCm() { //初始化为单位阵
        cm.set(new float[] { //默认矩阵
                1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0 });
    }

    public void setCm (float[] cmfloat) { //设置颜色矩阵的值
        this.cm.set(cmfloat);
    }

    public Bitmap filterBitmap() {
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG); // 保存图像
        canvas.restore(); // 存储
        return dst;
    }
}
