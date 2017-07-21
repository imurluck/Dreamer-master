package com.example.filter;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by 10405 on 2016/7/26.
 * This class use static factory model to construct each
 * Filter class.
 */

public class FilterFactory {
    private Bitmap src;
    private Image image;
    private IImageFilter filter;

    private ArrayList<String> typeArray = new ArrayList<String>(){{
        add("高饱和");
        add("黑白");
        add("怀旧");
        add("复古");
        add("毛玻璃");
    }};


    public FilterFactory(Bitmap src) {
        this.src = src;
    }

    public String getFilterType(int i) {
        return typeArray.get(i);
    }

    public BaseFilter createFilter(int id) {
        BaseFilter bf;
        switch (id) {
            case 0:
                bf = new HSAT(src);
                break;
            case 1:
                bf = new Gray(src);
                break;
            case 2:
                bf = new Nostalgic(src);
                break;
            default:
                bf =  new BaseFilter(src);
                break;
        }
        return bf;
    }

}
