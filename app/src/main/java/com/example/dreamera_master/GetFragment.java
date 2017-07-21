package com.example.dreamera_master;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.GetFragmentListAdapter;
import com.example.utils.HttpUtil;
import com.example.utils.ParseJSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class GetFragment extends Fragment {

    private ListView placesListView;

    private TextView nullText = null;

    private String placeName = null;

    private String placeId = null;

    private final int GET_PLACES = 1;

    private final int DELETE_PLACE = 2;

    private final int REFRESH_PLACE = 3;

    private GetFragmentListAdapter adapter;

    private  SwipeRefreshLayout swipeRefresh;

    private List<String> placesList = new ArrayList<String>();

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_PLACES:
                    adapter = new GetFragmentListAdapter(placesList);
                    placesListView.setAdapter(adapter);
                    break;
                case DELETE_PLACE:
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    break;
                case REFRESH_PLACE:
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    break;
                default:
            }
        }
    };

    public GetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get, container, false);
        placesListView = (ListView) view.findViewById(R.id.place_list);
        nullText = (TextView) view.findViewById(R.id.null_text);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.get_fragment_place_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPlace();
            }
        });
        placesListView.setEmptyView(nullText);
        getPlaces();
        handleListView();
        return view;
    }
    private void refreshPlace() {
                HttpUtil.getPlaces(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "refresh failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String jsonData = response.body().string();
                        placesList = ParseJSON.handleJSONForPlaces(jsonData);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "refresh success", Toast.LENGTH_SHORT).show();
                            }
                        });
                        placesList.clear();
                        placesList.addAll(ParseJSON.handleJSONForPlaces(jsonData));
                        Message message = new Message();
                        message.what = REFRESH_PLACE;
                        handler.sendMessage(message);
                    }
                });
    }
    private void getPlaces() {
        HttpUtil.getPlaces(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                placesList = ParseJSON.handleJSONForPlaces(jsonData);
                Message message = new Message();
                message.what = GET_PLACES;
                handler.sendMessage(message);
            }
        });
    }

    private void handleListView() {
        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentString = placesList.get(position);
                Intent intent = new Intent(getActivity(), MyPlaceActivity.class);
                intent.putExtra("placeName", currentString);
                startActivity(intent);
            }
        });
        placesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String currentString = placesList.get(position);
                final int currentPosition = position;
                SharedPreferences prefs = getActivity().getSharedPreferences("places", MODE_PRIVATE);
                placeId = prefs.getString(currentString, "");
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("删除此地点?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                swipeRefresh.setRefreshing(true);
                                HttpUtil.deletePlace(placeId, new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {

                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        placesList.remove(currentPosition);
                                        Message message = new Message();
                                        message.what = DELETE_PLACE;
                                        handler.sendMessage(message);
                                    }
                                });
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
                return true;
            }
        });
    }
}
