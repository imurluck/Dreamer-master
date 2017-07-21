package com.example.filter;

/**
 * Created by lijialin on 2016/9/12.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.nio.IntBuffer;


public class Image {

    //original bitmap image
    public Bitmap image;
    public Bitmap destImage;

    //format of image (jpg/png)
    private String formatName;
    //dimensions of image
    private int width, height;
    // RGB Array Color
    protected int[] colorArray;

    public Image(Bitmap img){
        this.image =  img;
        formatName = "jpg";
        width = img.getWidth();
        height = img.getHeight();
        destImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        updateColorArray();
    }

    public Image clone(){
        return new Image(this.image);
    }

    /**
     * Method to reset the image to a solid color
     * @param color - color to rest the entire image to
     */
    public void clearImage(int color) {
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                setPixelColor(x, y, color);
            }
        }
    }


    /**
     * Set color array for image - called on initialisation
     * by constructor
     *
     */
    private void updateColorArray(){
        colorArray = new int[width * height];
        image.getPixels(colorArray, 0, width, 0, 0, width, height);
        int r, g, b;

//        System.out.println("asdf "+ colorArray[0]);
//        System.out.println("asdf "+ (colorArray[0] >> 16));
//        System.out.println("asdf " + ((colorArray[0]>>16) & 0xff));
//        System.out.println("asdf "+ (colorArray[0] >> 8));

        /**
         * 16进制的负数是怎么来的？
         * 答：对16进制正数，先取反，再加1
         * 如：16进制数值为-206，用16进制表示为 0xce
         * 即   1100 1110
         * 减1  1100 1101
         * 取反 0011 0010 即0x32 = 50 (10进制)
         */

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int index = y * width + x;
                r = (colorArray[index] >> 16) & 0xff;
                g = (colorArray[index] >> 8) & 0xff;
                b = colorArray[index] & 0xff;
                colorArray[index] = 0xff000000 | (b << 16) | (g << 8) | r;//android系统与window系统的rgb存储方式相反
            }
        }
    }


    /**
     * Method to set the color of a specific pixel
     *
     * @param x
     * @param y
     * @param color
     */
    public void setPixelColor(int x, int y, int color){
        colorArray[((y*image.getWidth()+x))] = color;
        //image.setPixel(x, y, color);
        //destImage.setPixel(x, y, colorArray[((y*image.getWidth()+x))]);
    }

    /**
     * Get the color for a specified pixel
     *
     * @param x
     * @param y
     * @return color
     */
    public int getPixelColor(int x, int y){
        return colorArray[y*width+x];
    }

    /**
     * Set the color of a specified pixel from an RGB combo
     *
     * @param x
     * @param y
     * @param c0
     * @param c1
     * @param c2
     */
    public void setPixelColor(int x, int y, int c0, int c1, int c2){
        int rgbcolor = (255 << 24) + (c0 << 16) + (c1 << 8) + c2;
        colorArray[((y*image.getWidth()+x))] = rgbcolor;
    }

    public void copyPixelsFromBuffer() { //从缓冲区中copy数据以加快像素处理速度
        IntBuffer vbb = IntBuffer.wrap(colorArray);
        //vbb.put(colorArray);
        destImage.copyPixelsFromBuffer(vbb);
        vbb.clear();
        //vbb = null;
    }

    /**
     * Method to get the RED color for the specified
     * pixel
     * @param x
     * @param y
     * @return color of R
     */
    public int getRComponent(int x, int y){
        return (getColorArray()[((y*width+x))]& 0x00FF0000) >>> 16;
    }


    /**
     * Method to get the GREEN color for the specified
     * pixel
     * @param x
     * @param y
     * @return color of G
     */
    public int getGComponent(int x, int y){
        return (getColorArray()[((y*width+x))]& 0x0000FF00) >>> 8;
    }


    /**
     * Method to get the BLUE color for the specified
     * pixel
     * @param x
     * @param y
     * @return color of B
     */
    public int getBComponent(int x, int y){
        return (getColorArray()[((y*width+x))] & 0x000000FF);
    }



    /**
     * Method to rotate an image by the specified number of degrees
     *
     * @param rotateDegrees
     */
    public void rotate (int rotateDegrees){
        Matrix mtx = new Matrix();
        mtx.postRotate(rotateDegrees);
        image = Bitmap.createBitmap(image, 0, 0, width, height, mtx, true);
        width = image.getWidth();
        height = image.getHeight();
        updateColorArray();
    }


    /**
     * @return the image
     */
    public Bitmap getImage() {
//        return image;
        return destImage;
    }


    /**
     * @param image the image to set
     */
    public void setImage(Bitmap image) {
        this.image = image;
    }


    /**
     * @return the formatName
     */
    public String getFormatName() {
        return formatName;
    }


    /**
     * @param formatName the formatName to set
     */
    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }


    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }


    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }


    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }


    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }


    /**
     * @return the colorArray
     */
    public int[] getColorArray() {
        return colorArray;
    }


    /**
     * @param colorArray the colorArray to set
     */
    public void setColorArray(int[] colorArray) {
        this.colorArray = colorArray;
    }


    public static int SAFECOLOR(int a) {
        if (a < 0)
            return 0;
        else if (a > 255)
            return 255;
        else
            return a;
    }

    //;R.drawable.image
    public static Image LoadImage(Activity activity, int resourceId)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), resourceId);
        return new Image(bitmap);
    }
}