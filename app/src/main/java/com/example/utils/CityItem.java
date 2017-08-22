package com.example.utils;

import com.example.interfaces.CityInterface;

/**
 * Created by yourgod on 2017/8/20.
 */

public class CityItem implements CityInterface{

    private String placeId;

    private String cityName;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public String getCityName() {
        return cityName;
    }
}
