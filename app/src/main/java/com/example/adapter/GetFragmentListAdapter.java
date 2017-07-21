package com.example.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.dreamera_master.MyApplication;
import com.example.dreamera_master.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangZongxiang on 2017/5/25.
 */

public class GetFragmentListAdapter extends BaseAdapter {

    private List<String> placesList = new ArrayList<String>();

    private LayoutInflater inflater;

    public GetFragmentListAdapter(List<String> placesList) {
        this.placesList = placesList;
        inflater = LayoutInflater.from(MyApplication.getContext());
    }
    @Override
    public int getCount() {
        return placesList.size();
    }

    @Override
    public Object getItem(int position) {
        return placesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.layout_place_item, null);
        String placeName = (String)getItem(position);
        TextView placeNameText = (TextView) view.findViewById(R.id.place_name);
        placeNameText.setText(placeName);
        return view;
    }
}
