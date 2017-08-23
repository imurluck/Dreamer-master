package com.example.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dreamera_master.MyApplication;
import com.example.dreamera_master.R;
import com.example.view.RecommendPhotoPopupWindow;

import java.util.List;

/**
 * Created by yourgod on 2017/8/2.
 */

public class RecommendPhotoAdapter extends RecyclerView
        .Adapter<RecommendPhotoAdapter.ViewHolder> implements
        View.OnClickListener {

    private String TAG = "RecommendPhotoAdapter";

    private List<RecommendPhotoPopupWindow.PhotoPoint> photoList;

    private Context context;

    private OnItemClickListener onItemClickListener;

    public RecommendPhotoAdapter(Context context, List<RecommendPhotoPopupWindow.PhotoPoint> photoList) {
        this.photoList = photoList;
        this.context = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView tagImageView;

        private ImageView imageView;

        private TextView distanceText;

        private View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
            tagImageView = (ImageView) itemView.findViewById(R.id.recommend_photo_item_selected_tag);
            imageView = (ImageView) itemView.findViewById(R.id.recommend_photo_item_image);
            distanceText = (TextView) itemView.findViewById(R.id.recommend_photo_item_text);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = MyApplication.getContext();
        }
        View view = LayoutInflater.from(context)
                .inflate(R.layout.popup_recommend_photo_item, null);
        view.setOnClickListener(this);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mView.setTag(position);
        RecommendPhotoPopupWindow.PhotoPoint point = photoList.get(position);
        Glide.with(context).load(String.valueOf(point.photoUrl))
                .into(holder.imageView);
        Log.e(TAG,  point.photoUrl);
        //holder.imageView.setImageBitmap(BitmapFactory.decodeFile((String) map.get("photoUrl")));
        holder.tagImageView.setVisibility(View.GONE);
        double finalDistance;
        long distance = Math.round(point.photoDistance);
        Log.e(TAG, "start distance --" + distance);
        if ((distance - 1000) > 0) {
            distance = (long) distance / 100;
            Log.e(TAG, "distance --" + distance);
            finalDistance = ((double)distance) / 10;
            Log.e(TAG, "finalDistance --" + finalDistance);
            holder.distanceText.setText(String.valueOf(finalDistance) + "km");
        } else {
            holder.distanceText.setText(String.valueOf(distance) + "m");
        }
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public void onClick(View view) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(view, (int) view.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

}
