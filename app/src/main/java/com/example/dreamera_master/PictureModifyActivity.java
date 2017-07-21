package com.example.dreamera_master;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.example.utils.MyPicture;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PictureModifyActivity extends AppCompatActivity {

    private ImageView imageView;

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

    private Button modify;

    private String pictureId;

    private String placeId;

    private String pictureUrl;

    private MyPicture myPicture;

    private Button cancel;

    private boolean pictureIsChoosed = false;

    private boolean timeIsChoosed = false;

    private final int MODIFY_COMPLETED = 1;

    private Map<String, String> paraMap = new HashMap<String, String>();

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MODIFY_COMPLETED:
                    Toast.makeText(PictureModifyActivity.this, "Modify completed!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PictureModifyActivity.this, MyPictureActivity.class);
                    intent.putExtra("picture_data", myPicture);
                    startActivity(intent);
                    /**Intent intent = new Intent(PictureModifyActivity.this, MyPictureActivity.class);
                    intent.putExtra("picture_data", myPicture);
                    startActivity(intent);*/
                    finish();
                    break;
                default:
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_modify);
        Intent intent = getIntent();
        myPicture = (MyPicture) intent.getSerializableExtra("picture_data");
        pictureId = String.valueOf(myPicture.getPictureId());
        placeId = String.valueOf(myPicture.getPlaceId());
        pictureUrl = myPicture.getPictureUrl();
        imageView = (ImageView) findViewById(R.id.picture_modify_image);
        titleEdit = (EditText) findViewById(R.id.picture_modify_title);
        timeStrEdit = (EditText) findViewById(R.id.picture_modify_time_str);
        detailUrlEdit = (EditText) findViewById(R.id.picture_modify_detail_url);
        detailTitleEdit = (EditText) findViewById(R.id.picture_modify_detail_title);
        likeCountEdit = (EditText) findViewById(R.id.picture_modify_like_count);
        longitudeEdit = (EditText) findViewById(R.id.picture_modify_longtitude);
        latitudeEdit = (EditText) findViewById(R.id.picture_modify_latitude);
        altitudeEdit = (EditText) findViewById(R.id.picture_modify_altitude);
        choosePhoto = (Button) findViewById(R.id.picture_modify_choose_photo);
        chooseTime = (Button) findViewById(R.id.picture_modify_choose_time);
        modify = (Button) findViewById(R.id.picture_modify_modify);
        cancel = (Button) findViewById(R.id.picture_modify_cancel);
        Glide.with(this).load(myPicture.getPictureUrl()).into(imageView);
        initEditText();
        choosePhoto();
        chooseTime();
        modifyPicture();
        cancel();
     }

     private void cancel() {
         cancel.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
                 Intent intent = new Intent(PictureModifyActivity.this, MyPictureActivity.class);
                 intent.putExtra("picture_data", myPicture);
                 startActivity(intent);
             }
         });
     }

     private void initEditText() {
         titleEdit.setText(myPicture.getTitle() + "");
         timeStrEdit.setText(myPicture.getTime_str() + "");
         detailTitleEdit.setText(myPicture.getDetail_title() + "");
         detailUrlEdit.setText(myPicture.getDetail_url() + "");
         likeCountEdit.setText(myPicture.getLike_count() + "");
         longitudeEdit.setText(String.valueOf(myPicture.getLongitude()) + "");
         latitudeEdit.setText(String.valueOf(myPicture.getLatitude()) + "");
         altitudeEdit.setText(String.valueOf(myPicture.getAltitude()) + "");

     }

     private void choosePhoto() {
         choosePhoto.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 pictureIsChoosed = true;
                 Intent intent = new Intent("android.intent.action.GET_CONTENT");
                 intent.setType("image/*");
                 startActivityForResult(intent, 1);
             }
         });
     }

     @RequiresApi(api = Build.VERSION_CODES.N)
     private void chooseTime() {
         chooseTime.setOnClickListener(new View.OnClickListener() {
             Calendar calendar = Calendar.getInstance();
             @Override
             public void onClick(View v) {
                 timeIsChoosed = true;
                 new DatePickerDialog(PictureModifyActivity.this,
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data != null) {
                    pictureUrl = HandleImagePath.handleImagePath(data);
                    showImage();
                }
                break;
        }
    }

    private void modifyPicture() {
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pictureIsChoosed && timeIsChoosed) {
                    paraMap.put("title", titleEdit.getText().toString());
                    paraMap.put("time_str", timeStrEdit.getText().toString());
                    paraMap.put("detail_url", detailUrlEdit.getText().toString());
                    paraMap.put("detail_title", detailTitleEdit.getText().toString());
                    paraMap.put("like_count", likeCountEdit.getText().toString());
                    paraMap.put("longitude", longitudeEdit.getText().toString());
                    paraMap.put("latitude", latitudeEdit.getText().toString());
                    paraMap.put("altitude", altitudeEdit.getText().toString());
                    paraMap.put("place", String.valueOf(myPicture.getPlaceId()));
                    HttpUtil.putPicture(pictureId, paraMap, pictureUrl, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseContent = response.body().string();
                            myPicture = new Gson().fromJson(responseContent, MyPicture.class);
                            Message message = new Message();
                            message.what = MODIFY_COMPLETED;
                            handler.sendMessage(message);
                        }
                    });
                } else if (!pictureIsChoosed){
                    Toast.makeText(PictureModifyActivity.this, "Please choose a photo first",
                            Toast.LENGTH_SHORT).show();
                } else if (!timeIsChoosed) {
                    Toast.makeText(PictureModifyActivity.this, "Please choose a time first",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showImage() {
         Glide.with(this).load(pictureUrl).into(imageView);
     }
}
