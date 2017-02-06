package com.fengniao.baidumap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.fengniao.baidumap.activity.BaseActivity;
import com.fengniao.baidumap.activity.SearchActivity;
import com.fengniao.baidumap.app.AppContext;
import com.fengniao.baidumap.utils.NaviUtils;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.text_address)
    TextView address;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    public BaiduMap mBaiduMap;
    boolean isShowOnCenter = true;
    BDLocation myLocation;
    GeoCoder search;
    String addressStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK前初始context信息
        //该方法要在setContentView前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {
        address.setText(AppContext.address);
        search = GeoCoder.newInstance();
        mBaiduMap = mapView.getMap();
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    isShowOnCenter = false;
                    Log.i("mycolors", "滑动地图时关闭跟随");
                }
            }
        });

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
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
        search.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            //根据屏幕中间爱你的经纬度获取地址
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null ||
                        reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "未能找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                mBaiduMap.clear();
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(reverseGeoCodeResult.getLocation()));
                addressStr = reverseGeoCodeResult.getAddress();
                address.setText(addressStr);
            }
        });
    }


    @OnClick(R.id.edit_search)
    public void search(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("city", getCity());
        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.my_location)
    public void myLocation(View view) {
        if (myLocation == null) {
            Toast.makeText(this, "正在定位...", Toast.LENGTH_SHORT).show();
            isShowOnCenter = true;
        } else {
            showOnCenter(myLocation);
            isShowOnCenter = true;
        }
    }


    //获取当前城市
    public String getCity() {
        String city = null;
        if (TextUtils.isEmpty(addressStr)) {
            return city;
        }
        int indexProvince = addressStr.indexOf("省");
        int indexCity = addressStr.indexOf("市");
        city = addressStr.substring(indexProvince + 1, indexCity);
        return city;
    }


    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();

    }


    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
            //显示自己位置
            showMyLocation(location);
            if (isShowOnCenter) {
                showOnCenter(new LatLng(location.getLatitude(), location.getLongitude()));
            }
            myLocation = location;
        }
    }


    //显示自己位置
    public void showMyLocation(BDLocation location) {
        //开启显示位置功能
        mBaiduMap.setMyLocationEnabled(true);
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData data = builder.build();
        mBaiduMap.setMyLocationData(data);
    }

    //在地图中心显示指定位置
    public void showOnCenter(BDLocation location) {
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        showOnCenter(center);
    }

    //在地图中心显示指定位置
    public void showOnCenter(LatLng latLng) {
        //定义地图状态
        MapStatus status = new MapStatus.Builder().target(latLng).zoom(18).build();
        //描述地图状态将要发生的变化
        MapStatusUpdate update = MapStatusUpdateFactory.newMapStatus(status);
        //动画跳转到自己位置
        mBaiduMap.animateMapStatus(update);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    double lat = data.getDoubleExtra("lat", 0);
                    double lng = data.getDoubleExtra("lng", 0);
                    showOnCenter(new LatLng(lat, lng));
                    NaviUtils.startNavi(this, new LatLng(AppContext.lat, AppContext.lng), new LatLng(lat, lng));
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //管理地图生命周期
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.stop();
    }
}
