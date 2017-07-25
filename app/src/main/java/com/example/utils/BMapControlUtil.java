package com.example.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.example.dreamera_master.R;
import com.example.utils.clusterutil.clustering.ClusterItem;
import com.example.utils.clusterutil.clustering.ClusterManager;
import com.example.utils.navigationutils.NavigationUtil;
import com.example.view.MarkerPopupWindowView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by ZhangZongxiang on 2017/5/31.
 */

public class BMapControlUtil {

    private BaiduMap mBaiduMap;

    private ClusterManager mClusterManager;

    private Activity activity;

    private MarkerPopupWindowView popupWindowView;

    private NavigationUtil mNavigationUtil;

    private  ArrayList<HashMap<String, Object>> pointList;

    public BMapControlUtil(final Activity activity, final BaiduMap mBaiduMap, NavigationUtil navigationUtil) {
        this.activity = activity;
        this.mBaiduMap = mBaiduMap;
        this.mNavigationUtil = navigationUtil;
        mClusterManager = new ClusterManager<MyItem>(activity, mBaiduMap);
        //addMarkerOnMap();
        mBaiduMap.setOnMarkerClickListener(mClusterManager);
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener() {
            @Override
            public boolean onClusterItemClick(ClusterItem item) {
                List<HashMap<String, String>> pictureList = new ArrayList<HashMap<String, String>>();
                String placeName = item.getName();
                String crossPictureContent = item.getCrossPictures();
                try {
                    JSONArray crossPictures = new JSONArray(crossPictureContent);
                    for (int i = 0; i < crossPictures.length(); i ++) {
                        JSONObject pictureItem = crossPictures.getJSONObject(i);
                        String pictureTitie = pictureItem.getString("title");
                        String pictureUrl = pictureItem.getString("picture");
                        String pictureId = pictureItem.getString("id");
                        String placeId = pictureItem.getString("place");
                        String datetime = pictureItem.getString("datetime");
                        String timeStr = pictureItem.getString("time_str");
                        String pictureLongitude = pictureItem.getString("longitude");
                        String pictureLatitude = pictureItem.getString("latitude");
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("title", pictureTitie);
                        map.put("pictureUrl", pictureUrl);
                        map.put("pictureId", pictureId);
                        map.put("placeId", placeId);
                        map.put("datetime", datetime);
                        map.put("timeStr", timeStr);
                        if (pictureLongitude == null || pictureLongitude.equals("0.0")) {
                            map.put("pictureLongitude", null);
                        } else {
                            map.put("pictureLongitude", pictureLongitude);
                        }
                        if (pictureLatitude == null || pictureLatitude.equals("0.0")) {
                            map.put("pictureLatitude", null);
                        } else {
                            map.put("pictureLatitude", pictureLatitude);
                        }
                        pictureList.add(map);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                popupWindowView = new MarkerPopupWindowView(activity, mBaiduMap, pictureList, placeName, mNavigationUtil);
                popupWindowView.showAtLocation(activity.findViewById(R.id.bd_map_view), Gravity.BOTTOM, -20, -20);
                return false;
            }
        });
    }

    public void addMarkerOnMap() {
        HttpUtil.getPlaces(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                Log.d("BMapControlUtil", jsonData);
                pointList = new ArrayList<HashMap<String, Object>>();
                pointList = ParseJSON.handleJSONForPlacesOnMap(jsonData);
                if (pointList.size() > 0) {
                    List<MyItem> items = new ArrayList<MyItem>();
                    for (int i = 0; i< pointList.size(); i++) {
                        HashMap<String, Object> item = pointList.get(i);
                        if (item.get("cross_pictures").equals("[]")) {
                            continue;
                        }
                        Log.d("BMapControlUtil", "laittude = " + item.get("latitude").toString());
                        Log.d("BMapControlUtil", "longitude = " + item.get("longitude").toString());
                        LatLng latLngMarker = new LatLng(
                                Double.parseDouble(item.get("latitude").toString()),
                                Double.parseDouble(item.get("longitude").toString())
                        );
                        items.add(new MyItem(latLngMarker, item.get("name").toString(),
                                item.get("cross_pictures").toString()));
                    }
                    mClusterManager.clearItems();
                    mClusterManager.addItems(items);
                }

            }
        });
    }

    public class MyItem implements ClusterItem {

        private final LatLng mPosition;

        private String name;

        private String cross_pictures;

        public MyItem(LatLng latLng, String name, String cross_pictures) {
            this.mPosition = latLng;
            this.name = name;
            this.cross_pictures = cross_pictures;
        }
        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            View view = activity.getLayoutInflater().inflate(R.layout.marker_layout, null);
            TextView textView = (TextView) view.findViewById(R.id.marker_name);
            textView.setText(name);
            //Bitmap bitmap = getViewBitmap(view);
            return BitmapDescriptorFactory.fromView(view);
        }

        private Bitmap getViewBitmap(View addViewContent) {
            addViewContent.setDrawingCacheEnabled(true);
            addViewContent.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            addViewContent.layout(0, 0,
                    addViewContent.getMeasuredWidth(),
                    addViewContent.getMeasuredHeight());
            addViewContent.buildDrawingCache();
            Bitmap cacheBitmap = addViewContent.getDrawingCache();
            return cacheBitmap;
        }

        public String getName() {
            return name;
        }

        public String getCrossPictures() {
            return cross_pictures;
        }
    }
}
