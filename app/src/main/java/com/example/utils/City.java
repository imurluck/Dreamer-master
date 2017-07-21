package com.example.utils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Zhangzongxiang on 2017/5/20.
 */

public class City {

    @SerializedName("id")
    private int cityId;

    @SerializedName("name")
    private String cityName;

    private String code;

    private int province;

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getProvince() {
        return province;
    }

    public void setProvince(int province) {
        this.province = province;
    }
}
