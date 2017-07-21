package com.example.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by lijialin on 16-3-9.
 * This class use state pattern to response users' gesture
 */


public abstract class ZoomListener implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener {

    private int bitmapWidth;
    private int bitmapHeight;
    private final int INIT = 0;        //初始状态
    private final int DRAG = 1;        //拖拽状态
    private final int ZOOM = 2;        //放缩状态
    private final int ROTATE = 3;      //旋转
    private final int ZOOM_OR_ROTATE = 4; //缩放或旋转
    private int mode = INIT;
    private ScaleGestureDetector mScaleGestureDetector = null;
    private Matrix mScaleMatrix = new Matrix();
    private Matrix saveMatrix = new Matrix(); // 保存的matrix
    PointF pA = new PointF();
    PointF pB = new PointF();

    private double rotation;
    private static PointF mid = new PointF();
    private float dist = 1f;

    /**
     * Constructor
     * @author 10405
     */
    public ZoomListener(Context context, Bitmap bitmap) {
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        APPUtils.getScreenWidth(context);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: //手指按下
                saveMatrix.set(mScaleMatrix);
                pA.set(event.getX(), event.getY());
                pB.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getActionIndex() > 1) {
                    break;
                }
                dist = spacing(event.getX(0), event.getY(0), event.getX(1),
                        event.getY(1));
                if (dist > 10f) { // 如果连续两点距离大于10，则判定为多点模式
                    saveMatrix.set(mScaleMatrix);
                    pA.set(event.getX(0), event.getY(0));
                    pB.set(event.getX(1), event.getY(1));
                    mid.set((event.getX(0) + event.getX(1)) / 2,
                            (event.getY(0) + event.getY(1)) / 2);
                    //TODO 旋转不好使，所以设置mode为ZOOM
//                    mode = ZOOM_OR_ROTATE;
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(mode == ZOOM_OR_ROTATE) {
                    PointF pC = new PointF(event.getX(1) - event.getX(0) + pA.x,
                            event.getY(1) - event.getY(0) + pA.y);
                    double a = spacing(pB.x, pB.y, pC.x, pC.y);
                    double b = spacing(pA.x, pA.y, pC.x, pC.y);
                    double c = spacing(pA.x, pA.y, pB.x, pB.y);
                    if (a >= 10) {
                        double cosB = (a * a + c * c - b * b) / (2 * a * c);
                        double angleB = Math.acos(cosB);
                        double PID4 = Math.PI / 4;
                        if (angleB > PID4 && angleB < 3 * PID4) {
                            mode = ROTATE;
                            rotation = 0;
                        } else {
                            mode = ZOOM;
                        }
                    }
                }
                if(mode == DRAG){
                    mScaleMatrix.set(saveMatrix);
                    mScaleMatrix.postTranslate(event.getX()- pA.x, event.getY() - pA.y);// 平移
                    zoom(mScaleMatrix);
                } else if(mode == ZOOM) {
                    mScaleGestureDetector.onTouchEvent(event);
                } else if(mode == ROTATE) {
                    PointF pC = new PointF(event.getX(1) - event.getX(0) + pA.x,
                            event.getY(1) - event.getY(0) + pA.y);
                    double a = spacing(pB.x, pB.y, pC.x, pC.y);
                    double b = spacing(pA.x, pA.y, pC.x, pC.y);
                    double c = spacing(pA.x, pA.y, pB.x, pB.y);
                    if (b > 10) {
                        double cosA = (b * b + c * c - a * a) / (2 * b * c);
                        double angleA = Math.acos(cosA);
                        double ta = pB.y - pA.y;
                        double tb = pA.x - pB.x;
                        double tc = pB.x * pA.y - pA.x * pB.y;
                        double td = ta * pC.x + tb * pC.y + tc;
                        if (td > 0) {
                            angleA = 2 * Math.PI - angleA;
                        }
                        rotation = angleA;
                        mScaleMatrix.set(saveMatrix);
                        mScaleMatrix.postRotate((float) (rotation * 180 / Math.PI), mid.x, mid.y);
                        zoom(mScaleMatrix);
                    }
                }
                break;

            case MotionEvent.ACTION_UP: //手指抬起
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_OUTSIDE: //出界
                mode = INIT;
                break;

        }
        return true; //这个一定要写true，否则对移动等event无反应
    }

    /**
     * Calculate the space of point1 and point2
     * @param x1 point1.x
     * @param x2 point2.x
     * @param y1 point1.y
     * @param y2 point2.y
     */
    private float spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Zoom function,needed to be verride
     * @param matrix zoom matrix
     */
    public abstract void zoom (Matrix matrix);

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();

        //避免图片大于边界
        if(bitmapWidth * scaleFactor >= APPUtils.screenWidth
                || bitmapHeight * scaleFactor >= APPUtils.screenWidth) {
            return true;
        }
        bitmapWidth *= scaleFactor;
        bitmapHeight *= scaleFactor;

        mScaleMatrix.postScale(scaleFactor, scaleFactor,
                detector.getFocusX(), detector.getFocusY());
        zoom(mScaleMatrix);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

}