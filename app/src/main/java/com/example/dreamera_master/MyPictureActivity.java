package com.example.dreamera_master;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.utils.HttpUtil;
import com.example.utils.MyPicture;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyPictureActivity extends AppCompatActivity {

    private MyPicture myPicture;

    private final int GET_PICTURE_COMPLETED = 1;

    FloatingActionButton floatingActionButton;

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private Toolbar toolbar;

    private ImageView myPictureImage;

    private TextView pictureContentText;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_PICTURE_COMPLETED:
                    collapsingToolbarLayout.setTitle(myPicture.getTitle());
                    Log.d("MyPictureActivity2", myPicture.getPictureUrl() + "hass");
                    Glide.with(MyPictureActivity.this).load(myPicture.getPictureUrl()).into(myPictureImage);
                    String pictureContent = myPicture.getDetail_title();
                    pictureContentText.setText(pictureContent);
                    modifyPicture();
                    showMyPicture();
                    break;
                default:
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_picture);
        myPicture = (MyPicture) getIntent().getSerializableExtra("picture_data");
        floatingActionButton = (FloatingActionButton)
                findViewById(R.id.my_picture_floating_button);
        toolbar = (Toolbar) findViewById(R.id.my_picture_toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.my_picture_collapsing_toolbar);
        myPictureImage = (ImageView) findViewById(R.id.my_picture_image);
        pictureContentText = (TextView) findViewById(R.id.my_picture_content_text);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getPicture();
    }

    public void showMyPicture() {
        collapsingToolbarLayout.setTitle(myPicture.getTitle());
        Log.d("MyPictureActivity2", myPicture.getPictureUrl() + "hass");
        Glide.with(MyPictureActivity.this).load(myPicture.getPictureUrl())
                .placeholder(R.drawable.photo_insert)
                .error(R.drawable.photo_error)
                .into(myPictureImage);
        String pictureContent = myPicture.getDetail_title();
        pictureContentText.setText(pictureContent);
        modifyPicture();
    }
    private void getPicture() {
        HttpUtil.getPicture(String.valueOf(myPicture.getPictureId()), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(), "获取图片信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseContent = response.body().string();
                Log.d("MyPictureActivity1", responseContent);
                myPicture = new Gson().fromJson(responseContent, MyPicture.class);
                Message message = new Message();
                message.what = GET_PICTURE_COMPLETED;
                handler.sendMessage(message);

            }
        });
    }

    private void modifyPicture() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPictureActivity.this, PictureModifyActivity.class);
                //intent.putExtra("pictureId", myPicture.getPictureId());
                //intent.putExtra("placeId", myPicture.getPlaceId());
                //intent.putExtra("pictureUrl", myPicture.getPictureUrl());
                intent.putExtra("picture_data", myPicture);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        getPicture();
    }
}
