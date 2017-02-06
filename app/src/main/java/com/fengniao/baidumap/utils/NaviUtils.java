package com.fengniao.baidumap.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.fengniao.baidumap.activity.BaiduNaviActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by a1 on 2016/12/29.
 */

public class NaviUtils {
    private static String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "baiduMap";


    private static boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private BNRoutePlanNode.CoordinateType mCoordinateType = null;

    public static void startNavi(final Activity context, final LatLng start, final LatLng end) {
        initDirs();
        BaiduNaviManager.getInstance().init(context, mSDCardPath, APP_FOLDER_NAME,
                new BaiduNaviManager.NaviInitListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        final String authinfo;
                        if (0 == status) {
                            authinfo = "key校验成功!";
                        } else {
                            authinfo = "key校验失败, " + msg;
                        }
                        context.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(context, authinfo, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    public void initSuccess() {
                        BNRoutePlanNode sNode = null;
                        BNRoutePlanNode eNode = null;
                        sNode = new BNRoutePlanNode(start.longitude, start.latitude, "", null,
                                BNRoutePlanNode.CoordinateType.BD09LL);
                        eNode = new BNRoutePlanNode(end.longitude, end.latitude, "", null,
                                BNRoutePlanNode.CoordinateType.BD09LL);
                        List<BNRoutePlanNode> list = new ArrayList<>();
                        list.add(sNode);
                        list.add(eNode);
                        BaiduNaviManager.getInstance().launchNavigator((Activity) context, list, 1, true, new
                                DemoRoutePlanListener(sNode, context));

                        Toast.makeText(context, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                    }

                    public void initStart() {
//                        Toast.makeText(context, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    public void initFailed() {
//                        Toast.makeText(context, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
                    }
                }, null /*mTTSCallback*/);
    }

    public static class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {
        private Context mContext;

        public DemoRoutePlanListener(BNRoutePlanNode BNRoutePlanNode, Context context) {
            mBNRoutePlanNode = BNRoutePlanNode;
            mContext = context;
        }

        private BNRoutePlanNode mBNRoutePlanNode = null;


        @Override
        public void onJumpToNavigator() {
            Intent intent = new Intent(mContext, BaiduNaviActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("data", (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            mContext.startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            Toast.makeText(mContext, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }


}
