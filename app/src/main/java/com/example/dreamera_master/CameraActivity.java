package com.example.dreamera_master;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.example.utils.APPUtils;
import com.example.utils.AsyncGetDataUtil;
import com.example.utils.FileCacheUtil;
import com.example.utils.ImgToolKits;
import com.example.utils.ZoomListener;
import com.example.view.RevealBackgroundView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity implements
        RevealBackgroundView.OnStateChangeListener, CameraHostProvider {

    private static final String TAG = "CameraActivity";

    @BindView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @BindView(R.id.vPhotoRoot)
    View vTakePhotoRoot;
    @BindView(R.id.cameraView)
    CameraView cameraView;
    @BindView(R.id.btnTakePhoto)
    Button btnTakePhoto;
    @BindView(R.id.match_percent)
    TextView percentTextView;

    private String pictureID;//从上一个Activity传递过来的图片的ID
    private String pictureUrl;
    private SurfaceView surfaceView;     //surfaceView用于绘制边缘图
    private SurfaceHolder surfaceHolder;
    private int zoom = 0;                //相机焦距
    private Bitmap borderBitmap;         //边缘图
    private Matrix lastMatrix = new Matrix();  //初始化变换矩阵
    private Canvas canvas;

    /**
     * A static function，used to pass parameter from the last activity\.
     */
    public static void startCameraFromLocation(int[] startingLocation,
                                               Activity startingActivity, String id) {
        Intent intent = new Intent(startingActivity, CameraActivity.class);
        intent.putExtra("startingLocation", startingLocation);
        intent.putExtra("pictureId", id);
        startingActivity.startActivity(intent);
    }

    public static void startCameraFromLocation(int[] startingLocation, Activity startingActivity,
                                               String id, String pictureUrl) {
        Intent intent = new Intent(startingActivity, CameraActivity.class);
        intent.putExtra("startingLocation", startingLocation);
        intent.putExtra("pictureId", id);
        intent.putExtra("pictureUrl", pictureUrl);
        startingActivity.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        setupRevealBackground(savedInstanceState);

        APPUtils.getScreenWidth(this); //计算屏幕宽度
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setFillPaintColor(0xFF16181a);
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra("startingLocation");
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            vRevealBackground.setToFinishedFrame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
        // 为了避免onPause后转偏旋转90度
        this.finish(); //force quit
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当activity 被destory时需要立即清除之前加载的view，否则会出现窗体泄露异常
        APPUtils.wm.removeViewImmediate(surfaceView);
    }

    @OnClick(R.id.btnTakePhoto)
    void onTakePhotoClick() {
        btnTakePhoto.setEnabled(false);
        cameraView.takePicture(true, true);
    }

    @OnClick(R.id.btnCloseCamera)
    void onCloseCamera() {
        onBackPressed();
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            vTakePhotoRoot.setVisibility(View.VISIBLE);
            addBorderPicture(); //添加边缘图

            /**
             * 添加测试
             */
            //testMyCanny();

        } else {
            vTakePhotoRoot.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Zoom in the camera preview
     */
//    @OnClick(R.id.smaller)
//    void onZoomSmaller(){
//        try{
//            zoom -= 6;
//            zoom = zoom < 0 ? 0 : zoom;
//            ZoomTransaction z = cameraView.zoomTo(zoom);
//            z.go();
//        }catch (IllegalArgumentException e){
//            e.printStackTrace();
//        }
//    }


    /**
     * Zoom out the camera preview
     */
//    @OnClick(R.id.bigger)
//    void onZoomBigger(){
//        try{
//            zoom += 6;
//            ZoomTransaction z = cameraView.zoomTo(zoom);
//            z.go();
//        }catch (IllegalArgumentException e){
//            e.printStackTrace();
//        }
//    }


    /**
     * 添加轮廓图
     */
    private void addBorderPicture() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();//从上一个Activity获取参数图片
        pictureID = bundle.getString("pictureId");
        pictureUrl = bundle.getString("pictureUrl");
        Log.e(TAG, "pictureId = " + pictureID);
        Bitmap picFromFile = AsyncGetDataUtil.getPicFromFile(pictureID);

        borderBitmap = ImgToolKits.initBorderPic(picFromFile,
               APPUtils.screenWidth , APPUtils.screenWidth, true);
        addSurfaceView();  //添加surfaceView
        cameraViewOnTouch(); // 设置cameraView的监听事件
    }

    /**
     * 添加用于显示边缘图的SurfaceView
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addSurfaceView() {
        surfaceView = new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceView.setBackground(new BitmapDrawable(borderBitmap));
        surfaceView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                        onCloseCamera();
                }
                return false;
            }
        });

        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.width = borderBitmap.getWidth();
        wmParams.height = borderBitmap.getHeight();
        wmParams.flags = FLAG_NOT_TOUCHABLE;
        ViewGroup parent = (ViewGroup) surfaceView.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        APPUtils.wm.addView(surfaceView, wmParams);
    }

    /**
     * cameraViewOnTouch();
     */
    void cameraViewOnTouch() {
        cameraView.autoFocus();
        cameraView.setOnTouchListener(new ZoomListener(this, borderBitmap) { //触摸监听
            @Override
            public void zoom(Matrix matrix) {
                surfaceView.setBackgroundResource(0); //删除背景
                canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawBitmap(borderBitmap, matrix, null);

                surfaceHolder.unlockCanvasAndPost(canvas);
                surfaceHolder.setFormat(PixelFormat.TRANSPARENT); //设置背景透明
                surfaceHolder.lockCanvas(new Rect(0, 0, 0, 0));
                surfaceHolder.unlockCanvasAndPost(canvas);
                canvas.save();

                lastMatrix.set(matrix);
            }
        });
    }

    @Override
    public CameraHost getCameraHost() {
        return new MyCameraHost(this);
    }

    private class MyCameraHost extends SimpleCameraHost {

        private Camera.Size previewSize;

        MyCameraHost(Context ctxt) {
            super(ctxt);
        }

        @Override
        public boolean useFullBleedPreview() {
            return true;
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            return previewSize;
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
            previewSize = parameters1.getPreviewSize();
            return parameters1;
        }

        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {
            saveImageToFile(image);
        }
    }


    /**
     * Save photo to file by path
     */
    private void saveImageToFile ( byte[] image) {
        String path = FileCacheUtil.CAMERAPATH; //存储JSON的路径
        FileCacheUtil fileCacheUtil = new FileCacheUtil(path);
        // 目录下只存一个临时文件，所以在保存之前，删除其余文件
        FileCacheUtil.deleteFile( new File(path));
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        try {
            bitmap = Bitmap.createBitmap(bitmap, vTakePhotoRoot.getLeft(),
                    vTakePhotoRoot.getTop() + frame.top,
                    vTakePhotoRoot.getWidth(), vTakePhotoRoot.getHeight()
            );
            fileCacheUtil.savePicture(bitmap, "Photo", false);
            gotoFusionActivity();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Got fusion activity
     */
    private void gotoFusionActivity() {
        //跳转到下一个Activity
        Intent intent = new Intent();
        intent.setClass(CameraActivity.this, FusionActivity.class);
        intent.putExtra("id", pictureID);

        float[] matrixValues = new float[9];
        lastMatrix.getValues(matrixValues);
        intent.putExtra("matrix", matrixValues); //传递矩阵
        CameraActivity.this.startActivity(intent);
    }


    /**
     * 2017-04-11
     * 根据导师的需求修改
     * 每秒从摄像头中获取一张图片，返回该图片和轮廓图之间的匹配程度
     */

    private int[] getPix(String id) {
        Bitmap src = AsyncGetDataUtil.getPicFromFile(id);
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pix = new int [width * height];
        src.getPixels(pix, 0, width, 0, 0, width, height);
        return pix;
    }

    private void testMyCanny() {
        int[] pix1 = getPix("25");
        int[] pix2 = getPix("9");

        // OpenCVCanny.match(pix, width, height);
    }


}