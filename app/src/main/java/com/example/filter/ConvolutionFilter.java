package com.example.filter;

/**
 * Created by lijialin on 2016/9/12.
 */

public class ConvolutionFilter implements IImageFilter{


    protected int GetPixelBrightness(Image input, int x, int y,  int w,  int h)
    {
        if (x < 0){
            x = 0;
        }
        else if (x >= w){
            x = w - 1;
        }
        if (y < 0){
            y = 0;
        }
        else if (y >= h){
            y = h - 1;
        }
        return ((((input.getRComponent(x, y) * 0x1b36) + (input.getGComponent(x, y) * 0x5b8c)) + (input.getBComponent(x, y) * 0x93e)) >> 15);
    }

    protected int GetPixelColor(Image input, int x, int y, int w, int h)
    {
        if (x < 0){
            x = 0;
        }
        else if (x >= w){
            x = w - 1;
        }
        if (y < 0){
            y = 0;
        }
        else if (y >= h){
            y = h - 1;
        }
        return (((255 << 24 | (input.getRComponent(x, y) << 16)) | (input.getGComponent(x, y) << 8)) | input.getBComponent(x, y));
    }


    // Fields
    private int factor;
    private float[][] kernel;
    private int offset;

    // Methods
    public ConvolutionFilter(){
        float[][] numArray = new float[3][3];
        numArray[0][0] = 0;
        numArray[0][1] = 0;
        numArray[0][2] = 0;
        numArray[1][0] = 0;
        numArray[1][1] = 1;
        numArray[1][2] = 0;
        numArray[2][0] = 0;
        numArray[2][1] = 0;
        numArray[2][2] = 0.4f;
        this.kernel = numArray;
        this.factor = 1;
        this.offset = 1;
    }



    //@Override
    public Image process(Image imageIn) {
        int color;
        int width = imageIn.getWidth();
        int height = imageIn.getHeight();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int r = 0, g = 0, b = 0;
                float value = kernel[0][0];
                if (value != 0)
                {
                    color = GetPixelColor(imageIn, x - 1, y - 1, width, height);
                    r += ((0x00FF0000 & color) >> 16) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                value =  kernel[0][1];
                if (value != 0)
                {
                    color = GetPixelColor(imageIn, x, y - 1, width, height);
                    r += ((0x00FF0000 & color) >> 16) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                value = kernel[0][2];
                if (value != 0)
                {
                    color = GetPixelColor(imageIn, x + 1, y - 1, width, height);
                    r += ((0x00FF0000 & color) >> 16) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                value = kernel[1][0];
                if (value != 0)
                {
                    color = GetPixelColor(imageIn, x - 1, y, width, height);
                    r += ((0x00FF0000 & color) >> 16) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                value = kernel[1][1];
                if (value != 0)
                {
                    color = GetPixelColor(imageIn, x, y, width, height);
                    r += ((0x00FF0000 & color) >> 0x10) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                value = kernel[1][2];
                if (value != 0)
                {
                    color = GetPixelColor(imageIn, x + 1, y, width, height);
                    r += ((0x00FF0000 & color) >> 16) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                value = kernel[2][0];
                if (value != 0)
                {
                    color = GetPixelColor(imageIn, x - 1, y + 1, width, height);
                    r += ((0x00FF0000 & color) >> 16) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                value = kernel[2][1];
                if (value != 0)
                {
                    color = GetPixelColor(imageIn, x, y + 1, width, height);
                    r += ((0x00FF0000 & color) >> 16) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                value = kernel[2][2];
                if (value != 0) {
                    color = GetPixelColor(imageIn, x + 1, y + 1, width, height);
                    r += ((0x00FF0000 & color) >> 16) * value;
                    g += ((0x0000FF00 & color) >> 8) * value;
                    b += (0x000000FF & color) * value;
                }
                r = (r / this.factor) + this.offset;
                g = (g / this.factor) + this.offset;
                b = (b / this.factor) + this.offset;
                if (r < 0){
                    r = 0;
                }
                if (r > 0xff){
                    r = 0xff;
                }
                if (b < 0) {
                    b = 0;
                }
                if (b > 0xff){
                    b = 0xff;
                }
                if (g < 0){
                    g = 0;
                }
                if (g > 0xff){
                    g = 0xff;
                }
                imageIn.setPixelColor(x,y,r,g,b);
            }
        }

        return imageIn;
    }
}