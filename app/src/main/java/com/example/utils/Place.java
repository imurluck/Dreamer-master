package com.example.utils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Zhangzongxiang on 2017/5/20.
 */

public class Place {

    @SerializedName("id")
    private int placeId;

    @SerializedName("cross_pictures")
    private List<MyPicture> picturesList;

    @SerializedName("city")
    private City city;

    private String name;

    private double longitude;

    private double latitude;

    private double altitude;


    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public List<MyPicture> getPicturesList() {
        return picturesList;
    }

    public void setPicturesList(List<MyPicture> picturesList) {
        this.picturesList = picturesList;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

}
