package com.example.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.example.adapter.PopWindowRecyclerAdapter;
import com.example.dreamera_master.CameraActivity;
import com.example.dreamera_master.MyApplication;
import com.example.dreamera_master.PostFragment;
import com.example.dreamera_master.R;
import com.example.interfaces.OnItemClickListener;
import com.example.utils.AsyncGetDataUtil;
import com.example.utils.HttpUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

public class MarkerPopupWindowView extends PopupWindow {

    private static final String TAG = "MarkerPopupWindowView";

    private BaiduMap mBaiduMap;

    private Activity activity;

    public static View popupView;

    private ImageButton closeButton;

    private TextView nameText;

    private String placeName;

    private FloatingActionButton floatingActionButton;

    private PopWindowRecyclerAdapter adapter;

    private View lastView;

    private static String pictureId;

    private List<HashMap<String, String>> pictureList = new ArrayList<HashMap<String, String>>();

    private static HashMap<String, String> paraMap = new HashMap<String, String>();

    private RecyclerView galleryRecycler;
    public MarkerPopupWindowView(Context context, BaiduMap mBaiduMap, List<HashMap<String, String>> pictureList,
                                 String placeName) {
        super(((Activity)context));
        this.mBaiduMap = mBaiduMap;
        this.activity = (Activity) context;
        this.placeName = placeName;
        this.pictureList = pictureList;
        popupView = LayoutInflater.from(activity).inflate(R.layout.popup_window_practice, null);
        closeButton = (ImageButton) popupView.findViewById(R.id.id_close_popup);
        nameText = (TextView) popupView.findViewById(R.id.id_marker_name);
        galleryRecycler = (RecyclerView) popupView.findViewById(R.id.pop_window_recycler);
        floatingActionButton = (FloatingActionButton) popupView.findViewById(R.id.pop_window_floating);
        floatingActionButton.setVisibility(View.GONE);
        /**closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/
        initViews();
        this.setContentView(popupView);//
        this.setAnimationStyle(R.style.popup_windows_anim);//
        this.setWidth(((Activity) context).getWindowManager().getDefaultDisplay().getWidth());
        this.setHeight(((Activity)context).getWindowManager().getDefaultDisplay().getHeight()/2 - 300);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        //this.update();
        ColorDrawable dw = new ColorDrawable(0);
        this.setBackgroundDrawable(dw);

    }

    private void initViews() {
        adapter = new PopWindowRecyclerAdapter(activity, pictureList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        galleryRecycler.setLayoutManager(layoutManager);
        galleryRecycler.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                choosePhoto(view, position);
            }
        });
        nameText.setText(placeName);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void choosePhoto(View view, final int position) {
        if (lastView != null) {
            lastView.findViewById(R.id.pop_window_item_selected_tag).setVisibility(View.GONE);
        }
        view.findViewById(R.id.pop_window_item_selected_tag).setVisibility(View.VISIBLE);
        floatingActionButton.setVisibility(View.VISIBLE);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] startingLocation = new int[2];
                floatingActionButton.getLocationOnScreen(startingLocation);
                startingLocation[0] += floatingActionButton.getWidth() / 2;
                pictureId = pictureList.get(position).get("pictureId");
                String pictureUrl = pictureList.get(position).get("pictureUrl");
                String placeId = pictureList.get(position).get("placeId");
                String datetime = pictureList.get(position).get("datetime");
                String timeStr = pictureList.get(position).get("timeStr");
                String title = pictureList.get(position).get("title");
                if (!paraMap.isEmpty()) {
                    paraMap.clear();
                }
                paraMap.put("title", title);
                paraMap.put("time_str", timeStr);
                paraMap.put("datetime", datetime);
                paraMap.put("place", placeId);
                paraMap.put("latitude", String.valueOf(PostFragment
                        .getCurrentLocation().getLatitude()));
                paraMap.put("longitude", String.valueOf(PostFragment
                        .getCurrentLocation().getLongitude()));
                Log.e(TAG, "pictureId = " + pictureId);
                CameraActivity.startCameraFromLocation(startingLocation, activity, pictureId, pictureUrl);
                activity.overridePendingTransition(0, 0);
            }
        });
        lastView = view;
    }

    public static void putPictureLocation() {
        if (pictureId != null && !paraMap.isEmpty()) {
            HttpUtil.putPicture(pictureId, paraMap, null, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(MyApplication.getContext(), "上传失败",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseContent = response.body().string();
                    Log.e(TAG, "responseContent : " + responseContent);
                    Toast.makeText(MyApplication.getContext(), "上传成功",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MyApplication.getContext(), "paraMap为空",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static class AsyncGetPicTask extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "AsyncGetPicTask";
        private ImageView imgView;
        private Bitmap pictureBitmap;
        private String pictureUrl;
        private String pictureId;

        public AsyncGetPicTask(ImageView i, String pictureUrl, String picId){
            this.imgView = i;
            this.pictureUrl = pictureUrl;
            this.pictureId = picId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.e(TAG, "AsyncGetPicTask(doInBackground excute...)");
            AsyncGetDataUtil.getPictureData(pictureId, pictureUrl);
            pictureBitmap  = AsyncGetDataUtil.getPicFromFile(pictureId);
            int time = 0;
            while (time <= 3000 && pictureBitmap == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time += 1000;
                //AsyncGetDataUtil.getPictureData(pictureId, pictureUrl);//重新加载数据
                //JSON数据从文件缓存中读到内存中
                pictureBitmap  = AsyncGetDataUtil.getPicFromFile(pictureId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void integer) {
            //compress the resource pictures

            if(pictureBitmap != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                BitmapFactory.Options newOpts =  new  BitmapFactory.Options();
                int be = 2;
                newOpts.inSampleSize = be;
                ByteArrayInputStream isBm =  new  ByteArrayInputStream(out.toByteArray());
                pictureBitmap = BitmapFactory.decodeStream(isBm,  null ,  null );

                //this.imgView.setImageBitmap(pictureBitmap);
            }else {
                System.out.println("asdf pictureBitmap is null");
            }

        }
    }
}
