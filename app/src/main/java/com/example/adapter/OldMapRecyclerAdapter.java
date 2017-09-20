package com.example.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dreamera_master.R;
import com.example.view.OldMapChoosePop;

import java.util.ArrayList;

/**
 * Created by yourgod on 2017/9/20.
 */

public class OldMapRecyclerAdapter extends RecyclerView.Adapter<OldMapRecyclerAdapter.ViewHolder>
                implements View.OnClickListener {

    private Context mContext;

    private OldMapRecyclerAdapter.OnItemClickListener onItemClickListener;

    private ArrayList<OldMapChoosePop.Map> mapList = new ArrayList<OldMapChoosePop.Map>();

    public OldMapRecyclerAdapter(Context context, ArrayList<OldMapChoosePop.Map> mapList) {
        this.mContext = context;
        this.mapList = mapList;
    }
    class ViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private ImageView img;
        private ImageView imgTag;
        private TextView tv;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.img = (ImageView) itemView.findViewById(R.id.pop_window_recycler_item_image);
            this.imgTag = (ImageView) itemView.findViewById(R.id.pop_window_item_selected_tag);
            this.tv = (TextView) itemView.findViewById(R.id.pop_window_recycler_item_text);
        }
    }

    public void onClick(View v) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    public void setOnItemClickListener(OldMapRecyclerAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public OldMapRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext != null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.pop_window_recycler_item,
                    null);
            ViewHolder holder = new ViewHolder(view);
            holder.itemView.setOnClickListener(this);
            return holder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(OldMapRecyclerAdapter.ViewHolder holder, int position) {
        OldMapChoosePop.Map map = mapList.get(position);
        holder.img.setImageResource(map.mapId);
        holder.tv.setText(String.valueOf(map.year));
        holder.itemView.setTag(map.mapId);
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int mapId);
    }
}
