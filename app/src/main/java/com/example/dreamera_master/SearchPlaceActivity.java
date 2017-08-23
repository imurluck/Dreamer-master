package com.example.dreamera_master;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.SearchHistoryAdapter;
import com.example.adapter.SearchResultAdapter;
import com.example.utils.HttpUtil;
import com.example.utils.Place;
import com.example.utils.SearchHistoryUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Response;

public class SearchPlaceActivity extends AppCompatActivity {

    private final String TAG = "SearchActivity";

    private EditText searchEdit;
    private TextView cancelTv;
    private LinearLayout historyLayout;
    private LinearLayout searchLayout;
    private TextView loadingTv;
    private TextView noResultTv;
    private TextView histtoryEmpty;
    private RecyclerView historyRecycler;
    private RecyclerView resultRecycler;

    private SearchHistoryAdapter historyAdapter;
    private SearchResultAdapter resultAdapter;

    private ScrollLinearLayoutManager mHistoryLayoutManager;
    private ScrollLinearLayoutManager mResultLayoutManager;

    private final int NO_VIEWS = 0;
    private final int HISTORY_LAYOUT = 1;
    private final int LOADING = 2;
    private final int NO_RESULT = 3;
    private final int RESULT_RECYCLER = 4;

    private final int GET_PLACES_FINISH = 5;


    private static ArrayList<String> historyList = new ArrayList<>();
    private ArrayList<Place> resultList = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_PLACES_FINISH:
                    Log.e(TAG, "handleMessage: " + "GET_PLACES_FINISH");
                    if (resultList.size() <= 0 || resultList == null) {
                        showViews(NO_RESULT);
                    } else {
                        showViews(RESULT_RECYCLER);
                        resultAdapter.notifyDataSetChanged();
                        Log.e(TAG, "initResultRecycler");
                    }
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        initViews();
        initHistoryRecycler();
        initResultRecycler();
        getHistoryList();
        if (historyList.size() <= 0) {
            showViews(NO_VIEWS);
        } else {
            showViews(HISTORY_LAYOUT);
        }
        setSearchEditListener();
        setCancelTvListener();
        setHistoryEmptyTvListener();
    }

    public void setHistoryEmptyTvListener() {
        histtoryEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (historyList.size() > 0) {
                    SearchHistoryUtils.getInstance().deleteAllSearchHistory();
                    getHistoryList();
                    historyAdapter.notifyDataSetChanged();
                    showViews(NO_VIEWS);
                }
            }
        });
    }

    public void setCancelTvListener() {
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setSearchEditListener() {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = searchEdit.getText().toString();
                if (!name.equals("")) {
                    getResultList();
                } else {
                    getHistoryList();
                    historyAdapter.notifyDataSetChanged();
                    if (historyList.size() <= 0) {
                        showViews(NO_VIEWS);
                    } else {
                        showViews(HISTORY_LAYOUT);
                    }
                }
            }
        });
    }

    public void getResultList() {
        showViews(LOADING);
        Log.e(TAG, "searchEditText -- " + searchEdit.getText().toString());
        HttpUtil.searchPlaces(searchEdit.getText().toString(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SearchPlaceActivity.this, "网络不好,查询失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseContent = response.body().string();
                Log.e(TAG, "response -- " + responseContent);
                resultList.clear();
                try {
                    JSONArray jsonArray = new JSONArray(responseContent);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        resultList.add(new Gson().fromJson(jsonArray.get(i).toString(), Place.class));
                    }
                    Log.e(TAG, "resultList add finished");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Message message = new Message();
                message.what = GET_PLACES_FINISH;
                handler.sendMessage(message);
            }
        });
    }

    public void initViews() {
        searchEdit = (EditText) findViewById(R.id.search_search_edit);
        cancelTv = (TextView) findViewById(R.id.search_cancel_tv);
        historyLayout = (LinearLayout) findViewById(R.id.search_history_layout);
        searchLayout = (LinearLayout) findViewById(R.id.search_search_layout);
        loadingTv = (TextView) findViewById(R.id.search_loading_tv);
        noResultTv = (TextView) findViewById(R.id.search_no_results_tv);
        histtoryEmpty = (TextView) findViewById(R.id.search_history_empty);
        historyRecycler = (RecyclerView) findViewById(R.id.search_history_recycler);
        resultRecycler = (RecyclerView) findViewById(R.id.search_result_recycler);
    }

    public void initHistoryRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        historyRecycler.setLayoutManager(layoutManager);
        historyRecycler.setNestedScrollingEnabled(false);
        historyAdapter = new SearchHistoryAdapter(this, historyList);
        historyRecycler.setAdapter(historyAdapter);
        historyAdapter.setOnItemClickListener(new SearchHistoryAdapter.OnItemClickListener() {
            @Override
            public void onDeleteImgClick(View v, int position) {
                String name = historyList.get(position);
                SearchHistoryUtils.getInstance().deleteSearchHistory(name);
                historyList.remove(position);
                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNameTvClick(View v, int position) {
                String name = historyList.get(position);
                searchEdit.setText(name);
            }
        });
    }

    public void initResultRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        resultRecycler.setLayoutManager(layoutManager);
        resultRecycler.setNestedScrollingEnabled(false);
        resultAdapter = new SearchResultAdapter(this, resultList);
        resultRecycler.setAdapter(resultAdapter);
        resultAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Place place) {
                SearchHistoryUtils.getInstance().putNewSearch(searchEdit.getText().toString());
                getHistoryList();
                historyAdapter.notifyDataSetChanged();
                Intent intent = new Intent(SearchPlaceActivity.this, MyPlaceActivity.class);
                intent.putExtra("placeName", place.getName());
                intent.putExtra("placeId", String.valueOf(place.getPlaceId()));
                Log.e(TAG, "onItemClick: placeId--" + place.getPlaceId());
                startActivity(intent);
            }
        });
    }

    public void getHistoryList() {
        historyList.clear();
        historyList.addAll(SearchHistoryUtils.getInstance().querySearchHistoryList());
    }

    public void showViews(int name) {
        if (name == NO_VIEWS) {
            historyLayout.setVisibility(View.GONE);
            searchLayout.setVisibility(View.GONE);
            noResultTv.setVisibility(View.GONE);
            loadingTv.setVisibility(View.GONE);
        } else if (name == HISTORY_LAYOUT) {
            historyLayout.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.GONE);
            noResultTv.setVisibility(View.GONE);
            loadingTv.setVisibility(View.GONE);
        } else if (name == LOADING) {
            loadingTv.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.GONE);
            historyLayout.setVisibility(View.GONE);
            noResultTv.setVisibility(View.GONE);
        } else if (name == NO_RESULT) {
            noResultTv.setVisibility(View.VISIBLE);
            historyLayout.setVisibility(View.GONE);
            loadingTv.setVisibility(View.GONE);
            searchLayout.setVisibility(View.GONE);
        } else if (name == RESULT_RECYCLER) {
            searchLayout.setVisibility(View.VISIBLE);
            historyLayout.setVisibility(View.GONE);
            noResultTv.setVisibility(View.GONE);
            loadingTv.setVisibility(View.GONE);
        }
    }

    public class ScrollLinearLayoutManager extends LinearLayoutManager {

        private boolean isScrollEnabled = true;

        public ScrollLinearLayoutManager(Context context) {
            super(context);
        }

        public ScrollLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public ScrollLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public void setScrollEnabled(boolean flag) {
            this.isScrollEnabled = false;
        }

        @Override
        public boolean canScrollVertically() {
            return super.canScrollVertically() && isScrollEnabled;
        }
    }
}
