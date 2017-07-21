package com.example.filter;

/**
 * Created by lijialin on 2016/9/12.
 */

public class CleanGlassFilter implements IImageFilter{

    /// <summary>
    /// Should be in the range [0, 1].
    /// </summary>
    public float Size = 0.5f;

    public CleanGlassFilter()
    {
        Size = 0.5f;
    }

    /**
     * a,b 之间产生随机数
     */
    public static int getRandomInt(int a, int b) {
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        return min + (int)(Math.random() * (max - min + 1));
    }



    //@Override
    public Image process(Image imageIn)
    {
        int width = imageIn.getWidth();
        int height = imageIn.getHeight();
        Image clone = imageIn.clone();
        int r = 0, g = 0, b = 0;

        int ratio = imageIn.getWidth() > imageIn.getHeight() ? imageIn.getHeight() * 32768 / imageIn.getWidth() : imageIn.getWidth() * 32768 / imageIn.getHeight();

        // Calculate center, min and max
        int cx = imageIn.getWidth() >> 1;
        int cy = imageIn.getHeight() >> 1;
        int max = cx * cx + cy * cy;
        int min = (int)(max * (1 - Size));
        int diff = max - min;

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                // Calculate distance to center and adapt aspect ratio
                int dx = cx - x;
                int dy = cy - y;
                if (imageIn.getWidth() > imageIn.getHeight())
                {
                    dy = (dy * ratio) >> 14;
                }
                else
                {
                    dx = (dx * ratio) >> 14;
                }
                int distSq = dx * dx + dy * dy;

                if (distSq > min)
                {
                    int k = getRandomInt(1, 123456);
                    //像素块大小
                    int pixeldx = x + k % 19;
                    int pixeldy = y + k % 19;
                    if (pixeldx >= width)
                    {
                        pixeldx = width - 1;
                    }
                    if (pixeldy >= height)
                    {
                        pixeldy = height - 1;
                    }
                    r = clone.getRComponent(pixeldx, pixeldy);
                    g = clone.getGComponent(pixeldx, pixeldy);
                    b = clone.getBComponent(pixeldx, pixeldy);
                    imageIn.setPixelColor(x, y, r, g, b);
                }
            }
        }
        return imageIn;
    }
}
