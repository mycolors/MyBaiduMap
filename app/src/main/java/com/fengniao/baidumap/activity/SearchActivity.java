package com.fengniao.baidumap.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.fengniao.baidumap.MainActivity;
import com.fengniao.baidumap.R;
import com.fengniao.baidumap.adapter.SearchListAdapter;
import com.fengniao.baidumap.app.AppContext;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class SearchActivity extends BaseActivity {
    //    @BindView(R.id.toolbar)
//    Toolbar toolbar;
    @BindView(R.id.search_list)
    RecyclerView searchList;
    @BindView(R.id.edit_search)
    EditText editSearch;
    private List<PoiInfo> mList;
    private GeoCoder search = null;
    private String city;
    private SearchListAdapter adapter;
    private PoiSearch poiSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
//        setSupportActionBar(toolbar);
        initView();
    }
    public void initView() {
        city = getIntent().getStringExtra("city");
        if (TextUtils.isEmpty(city)) {
            if (!TextUtils.isEmpty(AppContext.city)) {
                city = AppContext.city;
            } else {
                return;
            }
        }
        poiSearch = PoiSearch.newInstance();
        search = GeoCoder.newInstance();
        mList = new ArrayList<>();
        adapter = new SearchListAdapter(this, mList);
        searchList.setLayoutManager(new LinearLayoutManager(this));
        searchList.setAdapter(adapter);
        editSearch.setText("");
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editSearch.getText().toString())) {
                    mList.clear();
                    adapter.notifyDataSetChanged();
                } else {
                    initData(editSearch.getText().toString());
                }
            }
        });

        search.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                if (geoCodeResult.getLocation() != null) {
                    Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                    intent.putExtra("lat", geoCodeResult.getLocation().latitude);
                    intent.putExtra("lng", geoCodeResult.getLocation().longitude);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

            }
        });

        adapter.setmOnItemClickListener(new SearchListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int positon) {

                    //根据所在城市转换为经纬度
                    search.geocode(new GeoCodeOption().city(city).address(mList.get(positon).address));
                }
        });
    }


    public void initData(String key) {

        OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                mList.clear();
                if (poiResult.getAllPoi() != null) {
                    mList.addAll(poiResult.getAllPoi());
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        };
        poiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
        poiSearch.searchInCity(new PoiCitySearchOption().city(city).keyword(key));

    }

}
