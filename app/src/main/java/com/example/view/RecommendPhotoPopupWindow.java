package com.example.view;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.example.adapter.RecommendPhotoAdapter;
import com.example.dreamera_master.AddPictureActivity;
import com.example.dreamera_master.MyPlaceActivity;
import com.example.dreamera_master.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yourgod on 2017/8/2.
 */

public class RecommendPhotoPopupWindow extends PopupWindow {

    static double DEF_PI = 3.14159265359; // PI
    static double DEF_2PI = 6.28318530712; // 2*PI
    static double DEF_PI180 = 0.01745329252; // PI/180.0
    static double DEF_R = 6378137.5; // radius of earth
    private final static double EARTH_RADIUS = 6378137.5;//地球半径

    private String TAG = "RecommendPhotoPopup";

    private double currentLongitude = MyPlaceActivity.getPlaceLatLng().longitude;
    private double currentLatitude = MyPlaceActivity.getPlaceLatLng().latitude;

    private List<PhotoPoint> photoList = new ArrayList<PhotoPoint>();

    private Context context;

    private Activity activity;

    private RecyclerView recommendPhotoRecycler;

    private ImageButton recommendPhotoImageButton;

    private RecommendPhotoAdapter adapter;

    private String photoUrl;

    private View pastView;

    private LatLng photoLatLng;

    private PhotoDistanceComparator photoDistanceComparator = new PhotoDistanceComparator();
    public RecommendPhotoPopupWindow(final Context context) {
        this.context = context;
        this.activity = (Activity) context;
        View view = LayoutInflater.from(context).inflate(R.layout.popup_recommend_photo, null);
        recommendPhotoRecycler = (RecyclerView) view.findViewById(R.id.recommend_photo_recycler);
        recommendPhotoImageButton = (ImageButton) view.findViewById(R.id.recommend_photo_image_button);
        recommendPhotoImageButton.setVisibility(View.GONE);
        this.setContentView(view);
        this.setAnimationStyle(R.style.popup_windows_anim);
        this.setWidth(((Activity) context).getWindowManager()
                .getDefaultDisplay().getWidth());
        this.setHeight(((Activity) context).getWindowManager()
                .getDefaultDisplay().getHeight() / 2 - 300);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        ColorDrawable dw = new ColorDrawable(0);
        this.setBackgroundDrawable(dw);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recommendPhotoRecycler.setLayoutManager(layoutManager);
        if (photoList != null) {
            getPhotoList();
            Collections.sort(photoList, photoDistanceComparator);
            adapter = new RecommendPhotoAdapter(context, photoList);
            recommendPhotoRecycler.setAdapter(adapter);
            adapter.setOnItemClickListener(new RecommendPhotoAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    choosePhoto(view, position);
                }
            });
        }
    }

    public void choosePhoto(View view, int position) {
        if (pastView != null) {
            pastView.findViewById(R.id.recommend_photo_item_selected_tag)
                    .setVisibility(View.GONE);
        }
        view.findViewById(R.id.recommend_photo_item_selected_tag)
                .setVisibility(View.VISIBLE);
        PhotoPoint point = photoList.get(position);
        photoUrl = point.photoUrl;
        photoLatLng = point.photoLatLng;
        recommendPhotoImageButton.setVisibility(View.VISIBLE);
        recommendPhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity instanceof AddPictureActivity) {
                    AddPictureActivity addPictureActivity = (AddPictureActivity) activity;
                    addPictureActivity.showPicture(photoUrl);
                    addPictureActivity.setPictureUrl(photoUrl);
                    addPictureActivity.setPictureLatLng(photoLatLng);
                }
                dismiss();
            }
        });
        pastView = view;
    }

    public void getPhotoList() {
        photoList.clear();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        if (cursor != null) {
            while(cursor.moveToNext()) {
                byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore
                        .Images.Media.DATA));
                String fileName = new String(data, 0, data.length - 1);
                PhotoPoint point = new PhotoPoint();
                point.photoUrl = fileName;
                double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore
                        .Images.Media.LONGITUDE));
                double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore
                        .Images.Media.LATITUDE));
            if ((longitude != 0.0 || latitude != 0.0)) {
                    CoordinateConverter converter  = new CoordinateConverter();//转化坐标
                    converter.from(CoordinateConverter.CoordType.GPS);
                    converter.coord(new LatLng(latitude, longitude));
                    LatLng desLatLng = converter.convert();
                    double distance = getDistance(currentLatitude, currentLongitude,
                            desLatLng.latitude, desLatLng.longitude);
                    Log.e(TAG, "longitude--" + desLatLng.longitude + "current--" + currentLongitude);
                    Log.e(TAG, "latitude--" + desLatLng.latitude + "current--" + currentLatitude);
                    Log.e(TAG, "distance= " + String.valueOf(distance));
                    point.photoDistance = Math.round(distance);
                    point.photoLatLng = desLatLng;
                    photoList.add(point);
                }
            }
        }
    }

    public class PhotoDistanceComparator implements Comparator<PhotoPoint> {

        @Override
        public int compare(PhotoPoint o1, PhotoPoint o2) {
            if (o1.photoDistance == o2.photoDistance) {
                return 0;
            } else if (o1.photoDistance < o2.photoDistance) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    public class PhotoPoint {
        public LatLng photoLatLng;
        public String photoUrl;
        public long photoDistance;
        public PhotoPoint() {};
    }
}
