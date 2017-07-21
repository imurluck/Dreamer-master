package com.example.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dreamera_master.MyPictureActivity;
import com.example.dreamera_master.R;
import com.example.interfaces.OnItemLongClickListener;
import com.example.utils.MyPicture;

import java.util.List;

/**
 * Created by Zhangzongxiang on 2017/5/21.
 */

public class MyPlaceRecyclerAdapter extends RecyclerView.Adapter<MyPlaceRecyclerAdapter.
        ViewHolder> implements View.OnLongClickListener{
    Context mContext;

    private int currentPosition;

    private List<MyPicture> picturesList;

    private OnItemLongClickListener mOnItemLongClickListener;

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null) {
            mOnItemLongClickListener.onItemLongClick(v, (int) v.getTag());
        }
        return true;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView placeImage;
        TextView detailTitle;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            placeImage = (ImageView) view.findViewById(R.id.place_picture);
            detailTitle = (TextView) view.findViewById(R.id.detail_title);
        }
    }

    public MyPlaceRecyclerAdapter(List<MyPicture> picturesList, Context context) {
        this.picturesList = picturesList;
        this.mContext = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.place_card_view, parent,
                false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                MyPicture myPicture = picturesList.get(position);
                Intent intent = new Intent(mContext, MyPictureActivity.class);
                intent.putExtra("picture_data", myPicture);
                mContext.startActivity(intent);
            }
        });
        holder.cardView.setOnLongClickListener(this);
        return holder;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (picturesList == null) {

        }
        currentPosition = position;
        MyPicture myPicture = picturesList.get(position);
        holder.detailTitle.setText(myPicture.getTitle());
        holder.cardView.setTag(position);
        Glide.with(mContext).load(myPicture.getPictureUrl())
                .placeholder(R.drawable.photo_insert)
                .error(R.drawable.photo_error)
                .into(holder.placeImage);
    }

    @Override
    public int getItemCount() {
        return picturesList.size();
    }

}
