package com.example.dreamera_master;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.bumptech.glide.Glide;
import com.example.utils.BlurTransformation;
import com.example.utils.DialogUtil;
import com.example.utils.GreyPicTransform;
import com.example.utils.HandleImagePath;
import com.example.utils.HttpUtil;
import com.example.utils.MyPicture;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PictureModifyActivity extends AppCompatActivity {

    private String TAG = "PictureModifyActivity";

    private ImageView imageView;

    private EditText titleEdit;

    private EditText timeStrEdit;

    private EditText detailUrlEdit;

    private EditText detailTitleEdit;

    private EditText likeCountEdit;

    private EditText longitudeEdit;

    private EditText latitudeEdit;

    //private EditText altitudeEdit;

    private Button choosePhoto;

    private Button chooseTime;

    private Button modify;

    private Button camera;

    private String pictureId;

    private String placeId;

    private String pictureUrl;

    private MyPicture myPicture;

    private Button cancel;

    private Uri imageUri;

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
        //altitudeEdit = (EditText) findViewById(R.id.picture_modify_altitude);
        choosePhoto = (Button) findViewById(R.id.picture_modify_choose_photo);
        chooseTime = (Button) findViewById(R.id.picture_modify_choose_time);
        modify = (Button) findViewById(R.id.picture_modify_modify);
        cancel = (Button) findViewById(R.id.picture_modify_cancel);
        camera = (Button) findViewById(R.id.picture_modify_camera);
        Glide.with(this).load(myPicture.getPictureUrl())
                .bitmapTransform(new BlurTransformation(this))
                .into(imageView);
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
         //altitudeEdit.setText(String.valueOf(myPicture.getAltitude()) + "");
         chooseTime.setText(String.valueOf(myPicture.getDatetime().substring(0, 10)) + "");
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
         camera.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 File outPutImage = new File(getExternalCacheDir(), "out_image.jpg");
                 try {
                     if (outPutImage.exists()) {
                         outPutImage.delete();
                     }
                     outPutImage.createNewFile();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 if (Build.VERSION.SDK_INT >= 24) {
                     imageUri = FileProvider.getUriForFile(PictureModifyActivity.this,
                             "com.example.dreamera-master.fileprovider",
                             outPutImage);
                 } else {
                     imageUri = Uri.fromFile(outPutImage);
                 }
                 Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                 intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                 startActivityForResult(intent, 2);
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
                                 //paraMap.put("datetime", info + "T01:01:00Z");
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
                    try {
                        ExifInterface exifInterface = new ExifInterface(pictureUrl);
                        String pictureLongitude = exifInterface
                                .getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        String pictureLatitude = exifInterface
                                .getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String pictureLongitudeRef = exifInterface.getAttribute(
                                ExifInterface.TAG_GPS_LONGITUDE_REF);
                        String pictureLatitudeRef = exifInterface.getAttribute(
                                ExifInterface.TAG_GPS_LATITUDE_REF);
                        if (pictureLatitude != null && pictureLongitude != null) {
                            CoordinateConverter converter  = new CoordinateConverter();//转化坐标
                            converter.from(CoordinateConverter.CoordType.GPS);
                            converter.coord(new LatLng(
                                    convertRationalLatLonToDouble(pictureLatitude, pictureLatitudeRef),
                                    convertRationalLatLonToDouble(pictureLongitude, pictureLongitudeRef)));
                            LatLng desLatLng = converter.convert();
                            longitudeEdit.setText(String.valueOf(desLatLng.longitude));
                            latitudeEdit.setText(String.valueOf(desLatLng.latitude));
                        } else {
                            Toast.makeText(PictureModifyActivity.this, "该图片无经纬度信息，将使用该地点经纬度, ",
                                    Toast.LENGTH_SHORT).show();
                            longitudeEdit.setText(String.valueOf(MyPlaceActivity.getPlaceLatLng().longitude) + "");
                            latitudeEdit.setText(String.valueOf(MyPlaceActivity.getPlaceLatLng().latitude + ""));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                if (imageUri != null && resultCode == RESULT_OK) {
                    pictureUrl = imageUri.getPath();
                    Log.e("AddPictureActivity", imageUri.getPath());
                    pictureIsChoosed = true;
                    showImage();
                    try {
                        ExifInterface exifInterface = new ExifInterface(pictureUrl);
                        String pictureLongitude = exifInterface
                                .getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        String pictureLatitude = exifInterface
                                .getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String pictureLongitudeRef = exifInterface.getAttribute(
                                ExifInterface.TAG_GPS_LONGITUDE_REF);
                        String pictureLatitudeRef = exifInterface.getAttribute(
                                ExifInterface.TAG_GPS_LATITUDE_REF);
                        if (pictureLatitude != null && pictureLongitude != null) {
                            CoordinateConverter converter  = new CoordinateConverter();//转化坐标
                            converter.from(CoordinateConverter.CoordType.GPS);
                            converter.coord(new LatLng(
                                    convertRationalLatLonToDouble(pictureLatitude, pictureLatitudeRef),
                                    convertRationalLatLonToDouble(pictureLongitude, pictureLongitudeRef)));
                            LatLng desLatLng = converter.convert();
                            longitudeEdit.setText(String.valueOf(desLatLng.longitude));
                            latitudeEdit.setText(String.valueOf(desLatLng.latitude));
                        } else {
                            Toast.makeText(PictureModifyActivity.this, "该图片无经纬度信息，将使用该地点经纬度, ",
                                    Toast.LENGTH_SHORT).show();
                            longitudeEdit.setText(String.valueOf(MyPlaceActivity.getPlaceLatLng().longitude) + "");
                            latitudeEdit.setText(String.valueOf(MyPlaceActivity.getPlaceLatLng().latitude + ""));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private double convertRationalLatLonToDouble(
            String rationalString, String ref) {

        String[] parts = rationalString.split(",");

        String[] pair;
        pair = parts[0].split("/");
        double degrees = Double.parseDouble(pair[0].trim())
                / Double.parseDouble(pair[1].trim());

        pair = parts[1].split("/");
        double minutes = Double.parseDouble(pair[0].trim())
                / Double.parseDouble(pair[1].trim());

        pair = parts[2].split("/");
        double seconds = Double.parseDouble(pair[0].trim())
                / Double.parseDouble(pair[1].trim());

        double result = degrees + (minutes / 60.0) + (seconds / 3600.0);
        if ((ref.equals("S") || ref.equals("W"))) {
            return  -result;
        }
        return  result;
    }

    private void modifyPicture() {
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showProgressDialog(PictureModifyActivity.this, "正在上传...");
                paraMap.put("title", titleEdit.getText().toString());
                paraMap.put("time_str", timeStrEdit.getText().toString());
                paraMap.put("detail_url", detailUrlEdit.getText().toString());
                paraMap.put("detail_title", detailTitleEdit.getText().toString());
                paraMap.put("like_count", likeCountEdit.getText().toString());
                paraMap.put("longitude", longitudeEdit.getText().toString());
                paraMap.put("latitude", latitudeEdit.getText().toString());
                //paraMap.put("altitude", altitudeEdit.getText().toString());
                paraMap.put("place", String.valueOf(myPicture.getPlaceId()));
                paraMap.put("datetime", chooseTime.getText().toString() + "T01:01:00Z");
                if (pictureIsChoosed) {
                    HttpUtil.putPicture(pictureId, paraMap, pictureUrl, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, call.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtil.closeProgressDialog();
                                    Toast.makeText(PictureModifyActivity.this,
                                            "上传失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseContent = response.body().string();
                            Log.e(TAG, responseContent);
                            DialogUtil.closeProgressDialog();
                            myPicture = new Gson().fromJson(responseContent, MyPicture.class);
                            Message message = new Message();
                            message.what = MODIFY_COMPLETED;
                            handler.sendMessage(message);
                        }
                    });

                } else {
                    HttpUtil.putPicture(pictureId, paraMap, null, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, call.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtil.closeProgressDialog();
                                    Toast.makeText(PictureModifyActivity.this,
                                            "上传失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseContent = response.body().string();
                            Log.e(TAG, responseContent);
                            DialogUtil.closeProgressDialog();
                            myPicture = new Gson().fromJson(responseContent, MyPicture.class);
                            Message message = new Message();
                            message.what = MODIFY_COMPLETED;
                            handler.sendMessage(message);
                        }
                    });
                }

                /**else if (!pictureIsChoosed){
                    Toast.makeText(PictureModifyActivity.this, "请先选择一张图片",
                            Toast.LENGTH_SHORT).show();
                } else if (!timeIsChoosed) {
                    Toast.makeText(PictureModifyActivity.this, "请先选择日期",
                            Toast.LENGTH_SHORT).show();
                }*/
            }
        });
    }
    private void showImage() {
         Glide.with(this).load(pictureUrl)
                 .bitmapTransform(new GreyPicTransform(this))
                 .into(imageView);
     }
}
