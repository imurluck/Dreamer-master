package com.example.dreamera_master;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.example.adapter.MyPlaceRecyclerAdapter;
import com.example.interfaces.OnItemLongClickListener;
import com.example.utils.HttpUtil;
import com.example.utils.ParseJSON;
import com.example.utils.Place;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyPlaceActivity extends AppCompatActivity {


    private RecyclerView recyclerView;

    private static String placeId = null;

    private String placeName = null;

    private String fromWhere = null;

    private Toolbar myPlaceToolbar = null;

    private FloatingActionButton addPictureButton;

    private MyPlaceRecyclerAdapter adapter;

    private static  SwipeRefreshLayout swipeRefreshLayout;

    private static Place concretePlace;

    /**private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case
            }
        }
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_place);
        myPlaceToolbar = (Toolbar) findViewById(R.id.my_place_toolbar);
        addPictureButton = (FloatingActionButton) findViewById(R.id.add_picture_floating_button);
        TextView titleText = (TextView) findViewById(R.id.my_place_title);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_place_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        Intent intent = getIntent();
        placeName = intent.getStringExtra("placeName");
        myPlaceToolbar.setTitle("");
        titleText.setText(placeName);
        setSupportActionBar(myPlaceToolbar);
        Log.d("MyPlaceActivity", placeName);
        placeId = getPlaceIdFromPlaceName(placeName);
        Log.d("MyPlaceActivity", placeId);
        recyclerView = (RecyclerView) findViewById(R.id.my_place_recycler);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVisibility(View.GONE);
        refreshPictures();
        addPicture();
    }

    @Nullable
    public static LatLng getPlaceLatLng() {
        if (concretePlace != null) {
            return new LatLng(concretePlace.getLatitude(), concretePlace.getLongitude());
        } else {
            Toast.makeText(MyApplication.getContext(), "无法获取到地点经纬度信息",
                    Toast.LENGTH_SHORT);
        }
        return null;
    }

    public void refreshPictures() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingPictures(placeId);
            }
        });
    }
    private  void loadingPictures(String placeId) {
        Log.e("MyPlaceActivity", "loadingPictures");
        HttpUtil.getConCretePlace(placeId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseContent = response.body().string();
                concretePlace = ParseJSON.handleJSONForConcretePlace(responseContent);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        TextView pictureNullText = (TextView) findViewById(R.id.picture_null_text);
                        TextView noPicturesText = (TextView) findViewById(R.id.no_pictures_text);
                        if (concretePlace.getPicturesList().size() > 0) {
                            pictureNullText.setVisibility(View.GONE);
                            noPicturesText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter = new MyPlaceRecyclerAdapter(concretePlace.getPicturesList(), MyPlaceActivity.this);
                            adapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                                @Override
                                public void onItemLongClick(View view, int position) {
                         Log.e("MyPlaceActivity", "position is " + position);
                                    Log.e("MyPlaceActivity", "OnLongClick");
                                    showDialogForDeletePicture(position);
                                }
                            });
                            recyclerView.setAdapter(adapter);
                        } else {
                            pictureNullText.setVisibility(View.GONE);
                            noPicturesText.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void showDialogForDeletePicture(final int position) {
        Log.e("MyPlaceActivtiy", "ShowDialogForDeletePicture");
        AlertDialog.Builder alert = new AlertDialog.Builder(MyPlaceActivity.this);
        alert.setTitle("删除此图片?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pictureId = String.valueOf(concretePlace.getPicturesList().get(position).getPictureId());
                        concretePlace.getPicturesList().remove(position);
                        adapter.notifyDataSetChanged();
                        HttpUtil.deletePicture(pictureId, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MyPlaceActivity.this,
                                                "删除成功!", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        });
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    private String getPlaceIdFromPlaceName(String placeName) {
        SharedPreferences prefs = getSharedPreferences("places", MODE_PRIVATE);
        return prefs.getString(placeName, "");
    }
    private void addPicture() {
        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (concretePlace != null) {
                    Intent intent = new Intent(MyPlaceActivity.this, AddPictureActivity.class);
                    intent.putExtra("placeName", placeName);
                    intent.putExtra("placeId", placeId);
                    startActivity(intent);
                } else {
                    Toast.makeText(MyPlaceActivity.this, "当前地点未获取到，" +
                            "无法添加照片", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
        loadingPictures(placeId);
    }
}
