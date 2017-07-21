package com.example.dreamera_master;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.utils.HandleImagePath;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class PutFragment extends Fragment {

    private String imgPath;

    public PutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_put, container, false);
        Button postPicture = (Button) view.findViewById(R.id.post_picture);
        Button choosePhoto = (Button) view.findViewById(R.id.choose_photo);
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        /**final Map<String, String> paraMap = new HashMap<String, String>();
        postPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paraMap.put("title", "wushe");
                paraMap.put("datetime", "2017-05-18T01:01:00Z");
                paraMap.put("time_str", "nothing");
                paraMap.put("detail_url", "nothing");
                paraMap.put("detail_title", "nothing");
                paraMap.put("like_count", "0");
                paraMap.put("longitude", "124.1611");
                paraMap.put("latitude", "65.151");
                paraMap.put("altitude", "0.0");
                paraMap.put("place", "237");
                HttpUtil.postPicture(paraMap, imgPath,  new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("PutFragment", response.body().string());
                    }
                });
            }
        });*/
        postPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> paraMap = new HashMap<String, String>();
                paraMap.put("name", "nothing");
                paraMap.put("longitude", "116.1615151");
                paraMap.put("latitude", "39.115151");
                paraMap.put("altitude", "0.0");
                /**HttpUtil.putPlace("231/", paraMap, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("PutFragment", response.body().string());
                    }
                });*/
                /**HttpUtil.postPlace(paraMap, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("PutFragment", response.body().string());
                    }
                });
                /**HttpUtil.deletePlace("233/", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("PutFragment", response.body().string());
                    }
                });*/
                /**HttpUtil.getConCretePlace("120", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseContent = response.body().string();
                        Log.d("PutFragment", responseContent);
                        Place place = ParseJSON.handleJSONForConcretePlace(responseContent);
                        for (MyPicture picture : place.getPicturesList()) {
                            Log.d("PutFragment", picture.getPictureUrl());
                        }
                    }
                });*/
                Intent intent = new Intent(getActivity(), MyPlaceActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                imgPath = HandleImagePath.handleImagePath(data);
                break;
            default:
        }
    }
}
