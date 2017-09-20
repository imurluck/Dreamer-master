package com.example.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlay;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.example.adapter.OldMapRecyclerAdapter;
import com.example.dreamera_master.R;
import com.example.utils.WindowUtil;

import java.util.ArrayList;

/**
 * Created by yourgod on 2017/9/20.
 */

public class OldMapChoosePop extends PopupWindow {

    private BaiduMap mBaiduMap;

    private Context mContext;

    private View popView;

    private SeekBar seekBar;

    private ImageButton cancelButton;

    private RecyclerView recycler;

    private GroundOverlay groundOverlay;

    private OverlayOptions overlayOptions;

    private LatLngBounds bounds;

    private LatLng northeast;

    private LatLng southwest;

    private BitmapDescriptor bitmapDescriptor;

    private View lastItemView;

    private ArrayList<Map> mapList = new ArrayList<Map>();

    private OldMapRecyclerAdapter adapter;

    public OldMapChoosePop(Context context, BaiduMap baiduMap) {
        this.mContext = context;
        this.mBaiduMap = baiduMap;
        addMaps();
        popView = LayoutInflater.from(context).inflate(R.layout.old_map_pop, null);
        setContentView(popView);
        setAnimationStyle(R.style.popup_windows_anim);
        DisplayMetrics dm = WindowUtil.getScreenMetircs(context);
        setWidth(dm.widthPixels);
        setHeight(dm.heightPixels / 2 - 300);
        setOutsideTouchable(false);
        setFocusable(false);
        setBackgroundDrawable(null);
        initView();
        setAdapterItemClickListener();
        setCancelListener();
        setSeekBarListener();
     }

     public void dismissOldMap() {
         if (groundOverlay != null) {
             groundOverlay.remove();
         }
     }

     private void setCancelListener() {
         cancelButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 dismiss();
             }
         });
     }

     private void setSeekBarListener() {
         seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 if (groundOverlay != null) {
                     groundOverlay.setTransparency(((float) (100 - progress)) / 100);
                 }
             }

             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {

             }

             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {

             }
         });
     }

     private void setAdapterItemClickListener() {
         if (adapter != null) {
             adapter.setOnItemClickListener(new OldMapRecyclerAdapter.OnItemClickListener() {
                 @Override
                 public void onItemClick(View v, int mapId) {
                     if (lastItemView != null) {
                         lastItemView.findViewById(R.id.pop_window_item_selected_tag)
                                 .setVisibility(View.GONE);
                     }
                     v.findViewById(R.id.pop_window_item_selected_tag).setVisibility(View.VISIBLE);
                     addOldMap(mapId);
                     lastItemView = v;
                 }
             });
         }
     }

     private void addOldMap(int mapId) {
         if (groundOverlay != null) {
             groundOverlay.remove();
             groundOverlay = null;
             if (bitmapDescriptor != null) {
                 bitmapDescriptor.recycle();
                 bitmapDescriptor = null;
             }
             System.gc();
         }
         northeast = new LatLng(41.755163,123.489881);
         southwest = new LatLng(41.807449,123.400122);

         if (mapId == R.drawable.map1930) {
             bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map1930);
             northeast = new LatLng(41.83242,123.488156); //Override northeast above
             southwest = new LatLng(41.782737,123.431634);
         } else if (mapId == R.drawable.map1940) {
             bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map1940);
             northeast = new LatLng(41.838708,123.490671);
             southwest = new LatLng(41.782441,123.382227);
         } else if (mapId == R.drawable.map1950) {
             bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map1950);
         } else if (mapId == R.drawable.map1980) {
             bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map1980);
         } else {
             bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.map2000);
         }
         bounds = new LatLngBounds.Builder()
                 .include(northeast)
                 .include(southwest)
                 .build();
         overlayOptions = new GroundOverlayOptions()
                 .positionFromBounds(bounds)
                 .image(bitmapDescriptor);
         groundOverlay = (GroundOverlay) mBaiduMap.addOverlay(overlayOptions);
     }

    private void initView() {
        seekBar = (SeekBar) popView.findViewById(R.id.alpha_seek_bar);
        recycler = (RecyclerView) popView.findViewById(R.id.old_map_recycler);
        cancelButton = (ImageButton) popView.findViewById(R.id.old_map_cancel);
        adapter = new OldMapRecyclerAdapter(mContext, mapList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
    }

    private void addMaps() {
        Map map1 = new Map(R.drawable.map1930, 1930);
        Map map2 = new Map(R.drawable.map1940, 1940);
        Map map3 = new Map(R.drawable.map1950, 1950);
        Map map4 = new Map(R.drawable.map1980, 1980);
        Map map5 = new Map(R.drawable.map2000, 2000);
        mapList.add(map1);
        mapList.add(map2);
        mapList.add(map3);
        mapList.add(map4);
        mapList.add(map5);
    }

    public class Map {
        public int mapId = 0;
        public int year = 0;
        public Map(int mapId, int year) {
            this.mapId = mapId;
            this.year = year;
        }
    }
}
