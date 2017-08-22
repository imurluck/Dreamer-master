package com.example.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dreamera_master.R;

import java.util.ArrayList;

/**
 * Created by yourgod on 2017/8/22.
 */

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>
                                    implements View.OnClickListener {

    private ArrayList<String> historyList = new ArrayList<>();

    private Context mContext;

    private SearchHistoryAdapter.OnItemClickListener onItemClickListener;

    public SearchHistoryAdapter(Context context, ArrayList<String> historyList) {
        this.mContext = context;
        this.historyList = historyList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv;
        ImageView deleteImg;
        View itemView;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTv = (TextView) itemView.findViewById(R.id.search_history_item_tv);
            deleteImg = (ImageView) itemView.findViewById(R.id.search_history_item_img);
            this.itemView = itemView;
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_history_item, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (historyList.size() > 0) {
            holder.nameTv.setText(historyList.get(position));
        }
        holder.nameTv.setOnClickListener(this);
        holder.nameTv.setTag(position);
        holder.deleteImg.setTag(position);
        holder.deleteImg.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_history_item_tv:
                if (onItemClickListener != null) {
                    onItemClickListener.onNameTvClick(v, (int) v.getTag());
                }
                break;
            case R.id.search_history_item_img:
                if (onItemClickListener != null) {
                    onItemClickListener.onDeleteImgClick(v, (int) v.getTag());
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public interface OnItemClickListener {
        void onDeleteImgClick(View v, int position);
        void onNameTvClick(View v, int position);
    }

    public void setOnItemClickListener(SearchHistoryAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
