package com.example.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by 10405 on 2016/08/24.
 * This class contains some function used frequently
 */
public class APPUtils {

    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public static WindowManager wm;

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * This function is used to get the height of device's screen
     * @param c the context use this function
     * @return
     */
    public static int getScreenHeight(Context c) {
       wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
         if(screenHeight == 0) {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }
        return screenHeight;
    }

    /**
     * his function is used to get the width of device's screen
     * @param c the context use this function
     */
    public static int getScreenWidth(Context c) {
        wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        if(screenWidth == 0) {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }
        return screenWidth;
    }


}
