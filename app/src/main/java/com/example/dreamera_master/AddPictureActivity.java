package com.example.dreamera_master;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
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
import com.example.utils.HandleImagePath;
import com.example.utils.HttpUtil;
import com.example.view.RecommendPhotoPopupWindow;

import java.io.File;
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

    private Button camera;

    private Button cancel;

    private Button addPicture;

    private String pictureUrl;

    private ImageView imageView;

    private String placeId;

    private Uri imageUri;

    private RecommendPhotoPopupWindow recommendPhotoPopupWindow;

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
        recommendPhotoPopupWindow = new RecommendPhotoPopupWindow(this);
        if (Build.VERSION.SDK_INT <= 23) {
            findViewById(R.id.add_picture_linear_layout).post(new Runnable() {
                @Override
                public void run() {
                    recommendPhotoPopupWindow.showAtLocation(AddPictureActivity.this
                                    .findViewById(R.id.add_picture_linear_layout),
                            Gravity.BOTTOM, 0, 0);
                }
            });
        }
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
        camera = (Button) findViewById(R.id.add_picture_camera);
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
                    imageUri = FileProvider.getUriForFile(AddPictureActivity.this,
                            "com.example.dreamera-master.fileprovider",
                            outPutImage);
                } else {
                    imageUri = Uri.fromFile(outPutImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 1);
            }
        });
    }

    public void showPicture(String pictureUrl) {
        Glide.with(this).load(pictureUrl)
                .bitmapTransform(new BlurTransformation(this)).into(imageView);
        this.photoIsChoosed = true;
    }

    public void setPictureLatLng(LatLng latLng) {
        longitudeEdit.setText(String.valueOf(latLng.longitude));
        latitudeEdit.setText(String.valueOf(latLng.latitude));
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (imageUri != null && resultCode == RESULT_OK) {
                    pictureUrl = imageUri.getPath();
                    Log.e("AddPictureActivity", imageUri.getPath());
                    photoIsChoosed = true;
                    Glide.with(this).load(pictureUrl)
                            .bitmapTransform(new BlurTransformation(this))
                            .into(imageView);
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
                            Toast.makeText(AddPictureActivity.this, "该图片无经纬度信息，将使用该地点经纬度, ",
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
                if (data != null) {
                    pictureUrl = HandleImagePath.handleImagePath(data);
                    Log.e("AddPictureActivity", "pictureUrl--" + pictureUrl);
                    photoIsChoosed = true;
                    Glide.with(this).load(pictureUrl)
                            .bitmapTransform(new BlurTransformation(this)).into(imageView);
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
                            Toast.makeText(AddPictureActivity.this, "该图片无经纬度信息，将使用该地点经纬度, ",
                                    Toast.LENGTH_SHORT).show();
                            longitudeEdit.setText(String.valueOf(MyPlaceActivity.getPlaceLatLng().longitude) + "");
                            latitudeEdit.setText(String.valueOf(MyPlaceActivity.getPlaceLatLng().latitude + ""));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
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
                DialogUtil.showProgressDialog(AddPictureActivity.this, "正在上传...");
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtil.closeProgressDialog();
                                    Toast.makeText(AddPictureActivity.this, "上传失败!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            finish();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddPictureActivity.this, "上传成功!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            DialogUtil.closeProgressDialog();
                            finish();
                        }
                    });
                } else if (!photoIsChoosed){
                    Toast.makeText(AddPictureActivity.this, "请先选择一张图片",
                            Toast.LENGTH_SHORT).show();
                } else if (!timeIsChoosed) {
                    Toast.makeText(AddPictureActivity.this, "请先选择日期",
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
