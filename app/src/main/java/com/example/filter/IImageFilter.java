package com.example.filter;

/**
 * Created by lijialin on 2016/9/12.
 */

public interface IImageFilter {

    Image process(Image imageIn);

    double  LIB_PI = 3.14159265358979323846;

    class Function{
        //-------------------------------------------------------------------------------------
        // basic function
        //-------------------------------------------------------------------------------------
        // bound in [tLow, tHigh]
        public static int FClamp(final int t, final int tLow, final int tHigh)
        {
            if (t < tHigh)
            {
                return ((t > tLow) ? t : tLow) ;
            }
            return tHigh ;
        }

        public static double FClampDouble(final double t, final double tLow, final double tHigh)
        {
            if (t < tHigh)
            {
                return ((t > tLow) ? t : tLow) ;
            }
            return tHigh ;
        }

        public static int FClamp0255(final double d)
        {
            return (int)(FClampDouble(d, 0.0, 255.0) + 0.5) ;
        }
    }
}
