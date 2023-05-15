package com.bw.game.xpermissionutils;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.bw.game.util.PhoneUtil;
import static com.bw.game.bridge.CommonBridge.commonEvalStringParam;


public class LocationUtils {
    private static final String TAG = "LocationUtil";

    public static void requestLocation(final Context context) {
        XPermissionUtils.requestPermissions(context, RequestCode.LOCATION, new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
        }, new XPermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                //6.0以下这个无法明确判断是否获取位置权限
                commonEvalStringParam("locationPermissionSuccess",  PhoneUtil.getLocation());
            }

            @Override
            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                if (alwaysDenied) {
                    commonEvalStringParam("checkGPSContacts","2");
                }
            }
        });
    }

    /**
     * 保存地理位置经度和纬度信息
     */
    private static void saveLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d(TAG, "jswrapper: latitude:" + latitude);
            Log.d(TAG, "jswrapper: longitude:" + longitude);
        }
    }
}
