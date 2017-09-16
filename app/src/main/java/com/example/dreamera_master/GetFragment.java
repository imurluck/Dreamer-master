package com.example.dreamera_master;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.interfaces.CityInterface;
import com.example.utils.CityItem;
import com.example.utils.HttpUtil;
import com.example.utils.ParseJSON;
import com.example.utils.Place;
import com.example.view.AddressSelector;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class GetFragment extends Fragment {

    private String TAG = "GetFragment";

    //private ListView placesListView;

    private AddressSelector mAddressSelector;

    private TextView nullText = null;

    private String placeName = null;

    private String placeId = null;

    private final int GET_PLACES = 1;

    //private final int DELETE_PLACE = 2;

    private final int REFRESH_PLACE = 3;
    private final int PLACE_NOT_EXIST = 4;

    //private GetFragmentListAdapter adapter;

    private  SwipeRefreshLayout swipeRefresh;

    private ArrayList<Place> allPlaces = new ArrayList<>();
    private ArrayList<Place> cityPlaces = new ArrayList<>();
    private ArrayList<CityItem> provinces = new ArrayList<>();
    private ArrayList<CityItem> citys = new ArrayList<>();
    private ArrayList<CityItem> places = new ArrayList<>();

    private CityItem choosedCityItem;

    //private List<String> placesList = new ArrayList<String>();

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_PLACES:
                    getProvinceList();
                    mAddressSelector.setCities(provinces);
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                    break;
                case PLACE_NOT_EXIST:
                    Log.e(TAG, "handleMessage: ");
                    if (choosedCityItem != null && places != null && mAddressSelector != null) {
                        if (places.contains(choosedCityItem)) {
                            places.remove(choosedCityItem);
                            mAddressSelector.notifyAdapter();
                        }
                    }
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
        //placesListView = (ListView) view.findViewById(R.id.place_list);
        mAddressSelector = (AddressSelector) view.findViewById(R.id.address_selector);
        nullText = (TextView) view.findViewById(R.id.null_text);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.get_fragment_place_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        //placesListView.setEmptyView(nullText);
        //adapter = new GetFragmentListAdapter(placesList);
        //placesListView.setAdapter(adapter);
        initAddressSelector();
        getPlaces();
        //refreshPlace();
        //handleListView();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refreshPlace();
                mAddressSelector.resetAddressSelector();
                getPlaces();
            }
        });
        return view;
    }

    public void initAddressSelector() {
        mAddressSelector.init(getActivity());
        mAddressSelector.setTabAmount(3);
        mAddressSelector.setOnItemClickListener(new AddressSelector.OnItemClickListener() {
            @Override
            public void itemClick(AddressSelector addressSelector, CityInterface city, int tabPosition) {
                switch (tabPosition) {
                    case 0:
                        getCityList(city.getCityName());
                        mAddressSelector.setCities(citys);
                        break;
                    case 1:
                        getPlaceList(city.getCityName());
                        mAddressSelector.setCities(places);
                        break;
                    case 2:
                        if (city != null) {
                            Intent intent = new Intent(getActivity(), MyPlaceActivity.class);
                            intent.putExtra("placeName", city.getCityName());
                            intent.putExtra("placeId", ((CityItem) city).getPlaceId());
                            choosedCityItem = (CityItem) city;
                            startActivity(intent);
                        }
                        break;
                }
            }
        });
        mAddressSelector.setOnTabSelectedListener(new AddressSelector.OnTabSelectedListener() {
            @Override
            public void onTabSelected(AddressSelector addressSelector, AddressSelector.Tab tab) {
                switch (tab.getIndex()) {
                    case 0:
                        mAddressSelector.setCities(provinces);
                        break;
                    case 1:
                        mAddressSelector.setCities(citys);
                        break;
                    case 2:
                        mAddressSelector.setCities(places);
                        break;
                }
            }

            @Override
            public void onTabReselected(AddressSelector addressSelctor, AddressSelector.Tab tab) {

            }
        });
    }

    public void getProvinceList() {
        provinces.clear();
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < allPlaces.size(); i++) {
            Place place = allPlaces.get(i);
            if (map.get(place.getCity().getProvince().getProvinceName()) == null) {
                map.put(place.getCity().getProvince().getProvinceName(), 1);
            } else {
                Integer n = map.get(place.getCity().getProvince().getProvinceName());
                map.put(place.getCity().getProvince().getProvinceName(), ++n);
            }
        }
        Set<String> keyList = map.keySet();
        for (String key : keyList) {
            CityItem cityItem = new CityItem();
            cityItem.setCityName(key);
            provinces.add(cityItem);
        }
    }

    public void getCityList(String provinceName) {
        citys.clear();
        cityPlaces.clear();
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < allPlaces.size(); i++) {
            Place place = allPlaces.get(i);
            if (provinceName.equals(place.getCity().getProvince().getProvinceName())) {
                if (map.get(place.getCity().getCityName()) == null) {
                    map.put(place.getCity().getCityName(), 1);
                } else {
                    Integer n = map.get(place.getCity().getCityName());
                    map.put(place.getCity().getCityName(), ++n);
                }
                cityPlaces.add(place);
            }
        }

        Set<String> keyList = map.keySet();
        for (String key : keyList) {
            CityItem cityItem = new CityItem();
            cityItem.setCityName(key);
            citys.add(cityItem);
        }
    }

    public void getPlaceList(String cityName) {
        places.clear();
        for (int i = 0; i < cityPlaces.size(); i++) {
            Place place = cityPlaces.get(i);
            if (cityName.equals(place.getCity().getCityName())) {
                CityItem cityItem = new CityItem();
                cityItem.setCityName(place.getName());
                cityItem.setPlaceId(String.valueOf(place.getPlaceId()));
                places.add(cityItem);
            }
        }
    }

    private void getPlaces() {
        HttpUtil.getPlaces(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                        Toast.makeText(getContext(), "网络不好,获取地点失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                try {
                    JSONArray jsonAllPlace = new JSONArray(jsonData);
                    allPlaces.clear();
                    for (int i = 0; i < jsonAllPlace.length(); i++) {
                        allPlaces.add(new Gson().fromJson(jsonAllPlace.get(i).toString(), Place.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //placesList.clear();
                //placesList.addAll(ParseJSON.handleJSONForPlaces(jsonData));
                Message message = new Message();
                message.what = GET_PLACES;
                handler.sendMessage(message);
            }
        });
    }

    /**private void handleListView() {
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
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(), "删除失败",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
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
    }*/

    private void notifyPlaces() {
        if (choosedCityItem != null) {
            HttpUtil.getConCretePlace(choosedCityItem.getPlaceId(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonData = response.body().string();
                    Place place = ParseJSON.handleJSONForConcretePlace(jsonData);
                    if (place.getName() == null) {
                        Message msg = new Message();
                        msg.what = PLACE_NOT_EXIST;
                        handler.sendMessage(msg);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        notifyPlaces();
        super.onResume();
    }
}
