package com.example.adapter;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dreamera_master.R;
import com.example.filter.BaseFilter;
import com.example.filter.CleanGlassFilter;
import com.example.filter.ComicFilter;
import com.example.filter.FilterFactory;
import com.example.filter.IImageFilter;
import com.example.filter.Image;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by 10405 on 2016/6/6.
 * photo filter adapter used in FilterActivity
 */
public class PhotoFiltersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private ImageView photoView;
    private Bitmap photoBitmap;
    public static Bitmap dstBitmap = null;
    public IImageFilter filter;
    private ProgressDialog proDia;

    public PhotoFiltersAdapter(Context context, ImageView photoView, Bitmap photoBitmap) {
        this.context = context;
        this.photoView = photoView;
        this.photoBitmap = photoBitmap;
        dstBitmap = photoBitmap;


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_photo_filter, parent, false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        WindowManager wm =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        lp.width = wm.getDefaultDisplay().getWidth()/4;
        view.setLayoutParams(lp);
        return new PhotoFilterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        PhotoFilterViewHolder holder = (PhotoFilterViewHolder) viewHolder;
        final FilterFactory ff = new FilterFactory(photoBitmap);
        holder.filterTextView.setText(ff.getFilterType(position));
        holder.filterImageView.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {

                if(position == 3) {
                    filter = new ComicFilter();
                    new processImageTask(filter).execute();
                } else if(position == 4) {
                    filter = new CleanGlassFilter();
                    new processImageTask(filter).execute();
                }else {
                    BaseFilter bf = ff.createFilter(position);
                    dstBitmap = bf.filterBitmap();
                    photoView.setImageBitmap(dstBitmap);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    static class PhotoFilterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.id_filterImage)
        ImageView filterImageView;
        @BindView(R.id.id_filterText)
        TextView filterTextView;

        PhotoFilterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class processImageTask extends AsyncTask<Void, Void, Bitmap> {

        private IImageFilter filter;
        public processImageTask(IImageFilter imageFilter) {
            this.filter = imageFilter;
        }

        @Override
        protected void onPreExecute() {

            proDia = new ProgressDialog(context);
            proDia.setMessage("加载中，请稍候");
//            proDia.onStart();
            proDia.show();

            super.onPreExecute();
        }

        public Bitmap doInBackground(Void... params) {

            Image img = null;
            try {
                img = new Image(photoBitmap);
                if (filter != null) {
                    img = filter.process(img);
                    img.copyPixelsFromBuffer();
                }
                return img.getImage();
            } catch(Exception e){
                if (img != null && img.destImage.isRecycled()) {
                    img.destImage.recycle();
                    img.destImage = null;
                    System.gc(); // 提醒系统及时回收
                }
            } finally{
                if (img != null && img.image.isRecycled()) {
                    img.image.recycle();
                    img.image = null;
                    System.gc(); // 提醒系统及时回收
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(result != null){
                super.onPostExecute(result);
                dstBitmap = result;
                photoView.setImageBitmap(dstBitmap);
            }
            proDia.dismiss();
        }
    }

}