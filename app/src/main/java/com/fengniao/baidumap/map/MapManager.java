package com.fengniao.baidumap.map;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.fengniao.baidumap.R;

import java.util.List;

public class MapManager {
    //当前的位置
    private BDLocation mLocation;

    //是否开启跟随
    private boolean isFollow;

    private Context mContext;

    private BaiduMap mBaiduMap;

    private MapView mMapView;

    private LocationClient mLocationClient = null;

    private BDLocationListener myListener = new MyLocationListener();

    private OnMapTouchListener mOnMapTouchListener;

    private OnMapClickListener mOnMapClickListener;

    private OnMapStatusChangeListener mOnMapStatusChangeListener;

    public MapManager(Context context) {
        this.mContext = context.getApplicationContext();
        SDKInitializer.initialize(context.getApplicationContext());
        initLocation();
    }

    public MapManager(Activity activity) {
        this.mContext = activity.getApplicationContext();
        mMapView = (MapView) activity.findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        initLocation();
        initListener();
    }


    public void initListener() {
        if (mMapView == null) return;
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (mOnMapTouchListener != null) {
                    mOnMapTouchListener.onTouch(motionEvent);
                }
            }
        });
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mOnMapClickListener != null) {
                    mOnMapClickListener.onMapClick(latLng);
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return mOnMapClickListener != null && mOnMapClickListener.onMapPoiClick(mapPoi);
            }
        });
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                if (mOnMapStatusChangeListener != null) {
                    mOnMapStatusChangeListener.onMapStatusChangeStart(mapStatus);
                }
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                if (mOnMapStatusChangeListener != null) {
                    mOnMapStatusChangeListener.onMapStatusChange(mapStatus);
                }
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                if (mOnMapStatusChangeListener != null) {
                    mOnMapStatusChangeListener.onMapStatusChangeFinish(mapStatus);
                }
            }
        });
    }

    public void setOnMapTouchListener(OnMapTouchListener listener) {
        mOnMapTouchListener = listener;
    }

    public void setOnMapClickListener(OnMapClickListener listener) {
        mOnMapClickListener = listener;
    }

    public void setOnMapStatusChangeListener(OnMapStatusChangeListener listener) {
        mOnMapStatusChangeListener = listener;
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
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(mContext);
        }
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.setLocOption(option);
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

            if (mBaiduMap != null) {
                //显示自己位置
                if (mBaiduMap.isMyLocationEnabled()) {
                    updateMyLocationData(location);
                }
                if (isFollow) {
                    showLocationOnMapCenter(location);
                }
            }
            mLocation = location;
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }
    }


    //开启定位功能
    public void enableLocation(boolean status) {
        if (status) startLocation();
        else stopLocation();

    }

    //是否开启定位
    public boolean isLocation() {
        return mLocationClient.isStarted();
    }

    //获取当前位置
    public BDLocation getMyLocation() {
        return mLocation;
    }

    //开始定位
    public void startLocation() {
        mLocationClient.start();
        Log.i("test", "startLocation");
    }

    //停止定位
    public void stopLocation() {
        if (mLocationClient.isStarted())
            mLocationClient.stop();
    }

    //开启地图显示自身自身位置功能
    public void enableShowMyLocation(boolean status) {
        mBaiduMap.setMyLocationEnabled(status);
    }

    //是否开启显示自身位置
    public boolean isShowMyLocation() {
        return mBaiduMap.isMyLocationEnabled();
    }


    //更新自己位置信息
    public void updateMyLocationData(BDLocation location) {
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData data = builder.build();
        mBaiduMap.setMyLocationData(data);
    }

    //是否开启地图跟随移动
    public void enableFollow(boolean status) {
        isFollow = status;
    }


    //地图跳转到指定位置
    public void showLocationOnMapCenter(BDLocation location) {
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        showLocationOnMapCenter(center);
    }

    //地图跳转到指定位置
    public void showLocationOnMapCenter(LatLng latLng) {
        //定义地图状态
        MapStatus status = new MapStatus.Builder().target(latLng).zoom(18).build();
        //描述地图状态将要发生的变化
        MapStatusUpdate update = MapStatusUpdateFactory.newMapStatus(status);
        //动画跳转到自己位置
        mBaiduMap.animateMapStatus(update);
    }

    public void clear() {
        if (mBaiduMap != null)
            mBaiduMap.clear();
    }

    public void setMapStatus(MapStatusUpdate statusUpdate) {
        if (mBaiduMap != null)
            mBaiduMap.setMapStatus(statusUpdate);
    }


    //添加marker
    public void addOverLay(List<IMarker> list) {

    }


    public interface IMarker {
        double getLat();

        double getLon();

        String getMsg();
    }

    public void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        if (mMapView != null)
            mMapView.onResume();
    }

    public void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        if (mMapView != null)
            mMapView.onPause();
    }

    public void onDestroy() {
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        if (mMapView != null)
            mMapView.onDestroy();
        if (mLocationClient != null)
            stopLocation();
    }


    public interface OnMapTouchListener {
        void onTouch(MotionEvent event);
    }

    public interface OnMapClickListener {
        void onMapClick(LatLng var1);

        boolean onMapPoiClick(MapPoi var1);
    }


    public interface OnMapStatusChangeListener {
        void onMapStatusChangeStart(MapStatus var1);

        void onMapStatusChange(MapStatus var1);

        void onMapStatusChangeFinish(MapStatus var1);
    }

}
