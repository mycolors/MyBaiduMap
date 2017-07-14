package com.fengniao.baidumap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.fengniao.baidumap.activity.BaseActivity;
import com.fengniao.baidumap.activity.SearchActivity;
import com.fengniao.baidumap.map.MapManager;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.text_address)
    TextView textAddress;
    BDLocation myLocation;
    GeoCoder search;
    String addressStr;

    private MapManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    public void setAddress(String address) {
        textAddress.setText(address);
    }

    public void initView() {
        mManager = new MapManager(this);
        getLocationPermission();
        mManager.enableFollow(true);
        mManager.enableShowMyLocation(true);
        mManager.setOnMapTouchListener(new MapManager.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    mManager.enableFollow(false);
                }
            }
        });

        mManager.setOnMapStatusChangeListener(new MapManager.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //获取屏幕中间中间的经纬度
                LatLng latLng = new LatLng(mapStatus.target.latitude, mapStatus.target.longitude);
                search.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
            }
        });
        search = GeoCoder.newInstance();
        search.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            //根据屏幕中间的经纬度获取地址
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null ||
                        reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "未能找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                mManager.clear();
                mManager.setMapStatus(MapStatusUpdateFactory.newLatLng(reverseGeoCodeResult.getLocation()));
                addressStr = reverseGeoCodeResult.getAddress();
                setAddress(addressStr);
            }
        });
    }


    public void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //用于用户已经拒绝一次申请权限，这里可写一些为何申请此权限，获取用户的理解
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "请同意定位权限", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        } else {
            mManager.enableLocation(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mManager.enableLocation(true);
        } else {
            Toast.makeText(this, "获取权限失败", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.edit_search)
    public void search(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("city", getCity());
        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.my_location)
    public void myLocation(View view) {
        mManager.enableFollow(true);
        myLocation = mManager.getMyLocation();
        if (myLocation == null) {
            Toast.makeText(this, "正在定位...", Toast.LENGTH_SHORT).show();
        } else {
            mManager.showLocationOnMapCenter(myLocation);
        }
    }


    //获取当前城市
    public String getCity() {
        String city = null;
        if (TextUtils.isEmpty(addressStr)) {
            return null;
        }
        int indexProvince = addressStr.indexOf("省");
        int indexCity = addressStr.indexOf("市");
        city = addressStr.substring(indexProvince + 1, indexCity);
        return city;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    double lat = data.getDoubleExtra("lat", 0);
                    double lng = data.getDoubleExtra("lng", 0);
                    mManager.showLocationOnMapCenter(new LatLng(lat, lng));
//                    NaviUtils.startNavi(this, new LatLng(AppContext.lat, AppContext.lng), new LatLng(lat, lng));
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //管理地图生命周期
        mManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.onDestroy();
        mManager = null;
    }
}
