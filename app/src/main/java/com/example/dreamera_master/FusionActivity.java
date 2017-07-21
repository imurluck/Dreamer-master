package com.example.dreamera_master;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.utils.APPUtils;
import com.example.utils.AsyncGetDataUtil;
import com.example.utils.FileCacheUtil;
import com.example.utils.ImgToolKits;
import com.example.view.SquaredFrameLayout;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;


@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class FusionActivity extends AppCompatActivity {

    @BindView(R.id.photoView)
    ImageView photoView; //拍摄的照片
    @BindView(R.id.squareFrameLayout)
    SquaredFrameLayout squaredFrameLayout;
    @BindView(R.id.id_alpha)
    SeekBar alphaSeekBar;
    @BindView(R.id.id_blur)
    SeekBar blurSeekBar;
    @BindView(R.id.btnNextActivity)
    ImageButton nextButton;

    private String id;                           //图片的id
    private Matrix matrix = new Matrix();        //前一个Activity传回的矩阵参数
    private float[] matrixValues = new float[9]; //用于获取矩阵的参数
    private Bitmap photoBitmap;         //拍摄的照片
    private Bitmap copyPicFromFile;     //老照片的副本
    private SurfaceView oldPictureView; //显示老照片的SurfaceView
    private int left = 0;               //老照片的left
    private int top = 0;                //老照片的top
    private Bitmap maskBitmap;          //遮罩mask处理
    private Bitmap resultBitmap;        //最终结果图片
    private int xOffset = 0;            //悬浮窗中图片的x偏移量
    private int yOffset = 0;            //悬浮窗中图片的y偏移量
    private int alpha = 255;            //老照片的透明度
    private float addX = 0;             //x方向的补充值
    private float addY = 0;             //y方向的补充值
    private int type = 0;               //0 表示横向图片，1表示竖向图片
    private int w;                      //copyPicFromFile的width
    private int h;                      //copyPicFromFile的height
    private int[] picPixels;            //用于存储copyPicFromFile像素值得矩阵
    private int[] maskPixels;           //用于存储maskBitmap像素值得矩阵


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fusion);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        id = bundle.getString("id");           //获取新拍摄的照片的id
        matrixValues = bundle.getFloatArray("matrix"); //获取变换矩阵
        matrix.setValues(matrixValues);

        APPUtils.getScreenWidth(this);

        //获取X方向和Y方向补充图片的高度
        addX = ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_X];
        addY = ImgToolKits.addHeight * matrixValues[Matrix.MSCALE_Y];

        photoBitmap = AsyncGetDataUtil.getPhotoFromFile(); //取出拍摄的图片;
        photoView.setBackground(new BitmapDrawable(photoBitmap));
        initOldPicture();      //初始化老照片

    }

    /**
     * 初始化边缘图
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initOldPicture() {
        Bitmap picFromFile = AsyncGetDataUtil.getPicFromFile(id);
        type = judgePicStyle(picFromFile) ? 0:1;

        //先把图片变成初始大小，然后再通过matrix变换
        Bitmap borderBitmap = ImgToolKits.initBorderPic(picFromFile,
                APPUtils.screenWidth, APPUtils.screenWidth, false);
        borderBitmap = Bitmap.createBitmap(borderBitmap, 0, 0,
                borderBitmap.getWidth(), borderBitmap.getHeight(), matrix, true);
        int borderWidth = borderBitmap.getWidth();
        int borderHeight = borderBitmap.getHeight();

        copyPicFromFile = ImgToolKits.changeBitmapSize(picFromFile,
                borderWidth - 2 * addX * type, borderHeight - 2 * addY * (1 - type));

        w = copyPicFromFile.getWidth();
        h = copyPicFromFile.getHeight();
        float xTrans = matrixValues[Matrix.MTRANS_X];
        float yTrans = matrixValues[Matrix.MTRANS_Y];
        left = (int) xTrans;
        top = (int)(yTrans + photoView.getTop());

        addOldPictureView(); //添加老照片层
        addMask(); //添加mask

        //recycle
        borderBitmap.recycle();
    }

    /**
     * 添加老照片层
     */
    private void addOldPictureView() {
        oldPictureView = new SurfaceView(this);
        oldPictureView.setBackground(new BitmapDrawable(copyPicFromFile));
        oldPictureView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                        clickBack();
                }
                return false;
            }
        });

        int leftw = (APPUtils.screenWidth - copyPicFromFile.getWidth()) / 2;
        int topw = (APPUtils.screenWidth - copyPicFromFile.getHeight()) / 2;

        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.x = (int)(left - leftw + type * addX);
        wmParams.y = (int)(top - topw + (1 - type) * addY);
        xOffset = wmParams.x + leftw;
        yOffset = wmParams.y + topw;
        wmParams.width = copyPicFromFile.getWidth();
        wmParams.height = copyPicFromFile.getHeight();
        wmParams.flags = FLAG_NOT_TOUCHABLE;

        ViewGroup parent = (ViewGroup) oldPictureView.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        APPUtils.wm.addView(oldPictureView, wmParams);
    }

    /**
     * 添加mask
     */
    private void addMask() {
        maskBitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_mask);
        maskBitmap = Bitmap.createScaledBitmap(maskBitmap,
                copyPicFromFile.getWidth(), copyPicFromFile.getHeight(), false);
        resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //前置相片添加蒙板效果
        picPixels = new int[w * h];
        maskPixels = new int[w * h];
        copyPicFromFile.getPixels(picPixels, 0, w, 0, 0, w, h);
        maskBitmap.getPixels(maskPixels, 0, w, 0, 0, w, h);
        composite(); //老照片与mask的融合
    }

    /**
     * 老照片与mask的融合
     */
    private void composite() {
        int y, py;
        for(int i = 0; i < maskPixels.length; i++) {
            y = i / w;
            py = yOffset + y + photoView.getTop();
            if(py <= photoView.getTop() || py >= photoView.getTop() + APPUtils.screenWidth) {
                picPixels[i] = 0;
            }else {
                if(maskPixels[i] == 0xff000000){ //黑色
                    picPixels[i] = 0;
                }else if(maskPixels[i] == 0){ //透明色
                    //pass
                }else{
                    //把mask的a通道与picBitmap与
                    maskPixels[i] &= 0xff000000; //高两位位表示透明度，ff表示完全不透明
                    //mask是中间透明，四周不透明；copyPicFromFile是中间不透明，四周透明
                    //所以需要做一个减法
                    maskPixels[i] = 0xff000000 - maskPixels[i];
                    picPixels[i] &= 0x00ffffff; //提取出copyPicFromFile某点的alpha值
                    picPixels[i] |= maskPixels[i]; //做 “或”运算，合成alpha
                }
            }
        }
        //生成前置图片添加蒙板后的bitmap:resultBitmap
        resultBitmap.setPixels(picPixels, 0, w, 0, 0, w, h);
        oldPictureView.setBackground(new BitmapDrawable(resultBitmap));
    }

    @Override
    protected void onResume() {
        super.onResume();
        adjustAlpha(); //初始化滑块,调整老照片的alpha值
        adjustBlur();  //调整模糊范围
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当activity 被destory时需要立即清除之前加载的view，否则会出现窗体泄露异常
        APPUtils.wm.removeViewImmediate(oldPictureView);
    }

    @OnClick(R.id.btnBack)
    void clickBack() {
        onBackPressed();
    }


    /**
     * 判断是宽 > 高的图片，还是高 > 宽的图片
     */
    private boolean judgePicStyle(Bitmap bitmap) {
        return bitmap.getWidth() >= bitmap.getHeight();
    }

    /**
     * 转到下一个Activity
     */
    @OnClick(R.id.btnNextActivity)
    void gotoNextActivity() {

        Bitmap picture = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(picture);
        Paint paint = new Paint(); // 建立画笔
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(photoBitmap, 0, 0, paint);
        paint.setAlpha(alpha);
        canvas.drawBitmap(resultBitmap, xOffset, yOffset, paint);

        //先把图片存到SD
        String path = FileCacheUtil.TEMPPATH; //存储JSON的路径
        // 目录下只存一个临时文件，所以在保存之前，删除其余文件
        FileCacheUtil.deleteFile( new File(path));
        FileCacheUtil fileCacheUtil = new FileCacheUtil(path);
        try {
            fileCacheUtil.savePicture(picture, "Temp", false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //由文件得到uri
        Uri imageUri = Uri.fromFile(new File(path + "/" + fileCacheUtil.filename));
        int[] startingLocation = new int[2];
        nextButton.getLocationOnScreen(startingLocation);
        startingLocation[0] += nextButton.getWidth() / 2;
        FilterActivity.startFilterFromLocation(startingLocation, FusionActivity.this, imageUri.toString());
        this.overridePendingTransition(0, 0);
    }


    /**
     * 第一个SeekBar调整图片的alpha值
     */
    private void adjustAlpha() {
        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                alpha = 255 - progress;
                oldPictureView.getBackground().setAlpha(alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * 第二个SeekBar调整图片的alpha值
     */
    private void adjustBlur() {
        blurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float radius = (progress + 1) / 4.f;
                float scale = 0.1f;  //设置图片缩小的比例
                 /* 产生reSize后的Bitmap对象 */
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                Bitmap smallMask = Bitmap.createBitmap(
                        maskBitmap,0,0,maskBitmap.getWidth(),
                        maskBitmap.getHeight(), matrix, true);
                Bitmap blurMask = blurBitmap(smallMask, radius);

                 // 产生reSize后的Bitmap对象
                matrix = new Matrix();
                matrix.postScale(1/scale, 1/scale);
                Bitmap bigMask = Bitmap.createScaledBitmap(blurMask, w, h, false);

                //前置相片添加蒙板效果
                picPixels = new int[w * h];
                maskPixels = new int[w * h];
                copyPicFromFile.getPixels(picPixels, 0, w, 0, 0, w, h);
                bigMask.getPixels(maskPixels, 0, w, 0, 0, w, h);
                composite();
                oldPictureView.getBackground().setAlpha(alpha); //别忘了设置透明度
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Bitmap blurBitmap(Bitmap bitmap, float radius){

        Bitmap outBitmap = Bitmap.createBitmap(
                bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(getApplicationContext());
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        blurScript.setRadius(radius);
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        allOut.copyTo(outBitmap);
//        bitmap.recycle();
        rs.destroy();

        return outBitmap;
    }

}