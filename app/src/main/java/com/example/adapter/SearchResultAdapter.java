package com.example.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dreamera_master.R;
import com.example.utils.Place;

import java.util.ArrayList;

/**
 * Created by yourgod on 2017/8/22.
 */

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder>
                                    implements View.OnClickListener {

    private final String TAG = "SearchResultAdapter";

    private Context mContext;

    private ArrayList<Place> resultList;

    private SearchResultAdapter.OnItemClickListener mOnItemClickListener;

    public SearchResultAdapter(Context context, ArrayList<Place> resultList) {
        this.mContext = context;
        this.resultList = resultList;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (Place) v.getTag());
        }
    }

    public void setOnItemClickListener(SearchResultAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTv;
        private View itemView;
        private ImageView gotoImg;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.search_result_item_tv);
            gotoImg = (ImageView) itemView.findViewById(R.id.search_result_item_img);
            this.itemView = itemView;
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_item, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.e(TAG, "name -- " + resultList.get(position).getName());
        holder.nameTv.setText(resultList.get(position).getName());
        holder.itemView.setTag(resultList.get(position));
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View v, Place place);
    }
}
