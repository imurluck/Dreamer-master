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

    @SerializedName("province")
    private Province province;

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

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public class Province {

        @SerializedName("pk")
        private int provinceId;

        @SerializedName("name")
        private String provinceName;

        public int getProvinceId() {
            return provinceId;
        }

        public void setProvinceId(int provinceId) {
            this.provinceId = provinceId;
        }

        public String getProvinceName() {
            return provinceName;
        }

        public void setProvinceName(String provinceName) {
            this.provinceName = provinceName;
        }
    }
}