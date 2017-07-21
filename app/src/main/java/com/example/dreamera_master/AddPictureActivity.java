package com.example.dreamera_master;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.utils.HandleImagePath;
import com.example.utils.HttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddPictureActivity extends AppCompatActivity {

    private EditText titleEdit;

    private EditText timeStrEdit;

    private EditText detailUrlEdit;

    private EditText detailTitleEdit;

    private EditText likeCountEdit;

    private EditText longitudeEdit;

    private EditText latitudeEdit;

    private EditText altitudeEdit;

    private Button choosePhoto;

    private Button chooseTime;

    private Button cancel;

    private Button addPicture;

    private String pictureUrl;

    private ImageView imageView;

    private String placeId;

    private Map<String, String> paraMap = new HashMap<String, String>();

    private boolean photoIsChoosed = false;

    private boolean timeIsChoosed = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_picture);
        Intent intent = getIntent();
        placeId = intent.getStringExtra("placeId");
        initViews();
        getPicture();
        chooseTime();
        addPicture();
        cancel();
    }

    private void initViews() {
        titleEdit = (EditText) findViewById(R.id.add_picture_title);
        timeStrEdit = (EditText) findViewById(R.id.add_picture_time_str);
        detailUrlEdit = (EditText) findViewById(R.id.add_picture_detail_url);
        detailTitleEdit = (EditText) findViewById(R.id.add_picture_detail_title);
        likeCountEdit = (EditText) findViewById(R.id.add_picture_like_count);
        longitudeEdit = (EditText) findViewById(R.id.add_picture_longtitude);
        latitudeEdit = (EditText) findViewById(R.id.add_picture_latitude);
        altitudeEdit = (EditText) findViewById(R.id.add_picture_altitude);
        choosePhoto = (Button) findViewById(R.id.add_picture_choose_photo);
        chooseTime = (Button) findViewById(R.id.add_picture_choose_time);
        cancel = (Button) findViewById(R.id.add_picture_cancel);
        addPicture = (Button) findViewById(R.id.add_picture_add);
        imageView = (ImageView) findViewById(R.id.add_picture_image);
    }

    private void getPicture() {
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2:
                if (data != null) {
                    pictureUrl = HandleImagePath.handleImagePath(data);
                    photoIsChoosed = true;
                    Glide.with(this).load(pictureUrl).into(imageView);
                }
                break;
            default:
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void chooseTime() {
        final Calendar calendar = Calendar.getInstance();
        chooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIsChoosed = true;
                new DatePickerDialog(AddPictureActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                String info = year + "-" + ++month + "-" + day;
                                paraMap.put("datetime", info + "T01:01:00Z");
                                chooseTime.setText(info);
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void addPicture() {
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoIsChoosed && timeIsChoosed) {
                    paraMap.put("title", titleEdit.getText().toString());
                    paraMap.put("time_str", timeStrEdit.getText().toString());
                    paraMap.put("detail_url", detailUrlEdit.getText().toString());
                    paraMap.put("detail_title", detailTitleEdit.getText().toString());
                    paraMap.put("like_count", likeCountEdit.getText().toString());
                    paraMap.put("longitude", longitudeEdit.getText().toString());
                    paraMap.put("latitude", latitudeEdit.getText().toString());
                    paraMap.put("altitude", altitudeEdit.getText().toString());
                    paraMap.put("place", placeId);
                    HttpUtil.postPicture(paraMap, pictureUrl, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddPictureActivity.this, "Post completed!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else if (!photoIsChoosed){
                    Toast.makeText(AddPictureActivity.this, "Please choose a photo first",
                            Toast.LENGTH_SHORT).show();
                } else if (!timeIsChoosed) {
                    Toast.makeText(AddPictureActivity.this, "Please choose a time first",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancel() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
