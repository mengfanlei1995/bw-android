package com.bw.game.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;

import com.bw.appsflyer.AppsflyerHelper;
import com.bw.game.App;
import com.bw.game.manager.ActivityManager;
import com.bw.game.xpermissionutils.LocationUtils;
import com.bw.game.xpermissionutils.PermissionHelper;
//import com.google.android.gms.ads.identifier.AdvertisingIdClient;
//import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
//import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.Context.SENSOR_SERVICE;
import static com.bw.game.bridge.CommonBridge.commonEvalStringParam;


public class PhoneUtil {

    private static Context context;

    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public static boolean androidPieScreenAdaptation() {
        boolean isPieScreen = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Activity activity = ActivityManager.getInstance().getCocosActivity();
            View decorView = activity.getWindow().getDecorView();
            WindowInsets windowInsets = decorView.getRootWindowInsets();
            if (windowInsets != null) {
                // 当全屏顶部显示黑边时，getDisplayCutout()返回为null
                DisplayCutout displayCutout = windowInsets.getDisplayCutout();

                if (displayCutout != null) {
                    //通过判断是否存在rects来确定是否刘海屏手机
                    List<Rect> rects = displayCutout.getBoundingRects();
                    if (rects != null && rects.size() > 0) {
                        //Log.d("PhoneUtil", "jswrapper: JS: 异形屏手机！");
                        isPieScreen = true;
                    }

                    //Log.d("PhoneUtil", "jswrapper: JS: 安全区域距离屏幕左边的距离 SafeInsetLeft:" + displayCutout.getSafeInsetLeft());
                    //Log.d("PhoneUtil", "jswrapper: JS: 安全区域距离屏幕右部的距离 SafeInsetRight:" + displayCutout.getSafeInsetRight());
                    //Log.d("PhoneUtil", "jswrapper: JS: 安全区域距离屏幕顶部的距离 SafeInsetTop:" + displayCutout.getSafeInsetTop());
                    //Log.d("PhoneUtil", "jswrapper: JS: 安全区域距离屏幕底部的距离 SafeInsetBottom:" + displayCutout.getSafeInsetBottom());
                }
            }
        }
        return isPieScreen;
    }

    /**
     * 检测GPS、位置权限是否开启
     */
    public static void checkGPSContacts() {
        Activity activity = ActivityManager.getInstance().getCocosActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionHelper.isLocServiceEnable(activity)) {
                commonEvalStringParam("checkGPSContacts", "1");
                return;
            }
            LocationUtils.requestLocation(activity);
        }
    }

    /**
     * 拉起设置 type 1 定位 2 权限
     */
    public static void pullUpSet(String type) {
        // Log.d("PhoneUtil", "jswrapper: JS: pullUpSet - " + type);
        if (type.equals("1")) {
            //Log.d("PhoneUtil", "jswrapper: JS: pullUpSet - " + 1);
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                ActivityManager.getInstance().getCocosActivity().startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                intent.setAction(Settings.ACTION_SETTINGS);
                try {
                    ActivityManager.getInstance().getCocosActivity().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (type.equals("2")) {
            //Log.d("PhoneUtil", "jswrapper: JS: pullUpSet - " + 2);
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + ActivityManager.getInstance().getCocosActivity().getPackageName()));
            ActivityManager.getInstance().getCocosActivity().startActivity(intent);
        }
    }

    /**
     * 基础信息
     */
    private static JSONObject reportJson;

    /**
     * 获取上报信息
     *
     * @return
     */
    public static JSONObject getPhoneInfo() {
        try {
            if (reportJson == null) {
                reportJson = new JSONObject();
                reportJson.put("screen_height", getHeight());
                reportJson.put("screen_width", getWith());
                reportJson.put("device_id", getUniqueId());
                reportJson.put("device_brand", getDeviceBrand());
                reportJson.put("device_model", getSystemModel());
                reportJson.put("os_version", getSystemVersion());
                reportJson.put("os_name", "Android");
                reportJson.put("app_version", getVersionName());
                reportJson.put("app_package_name", getPackageName());
                reportJson.put("android_id", getAndroidId());
                reportJson.put("mac", getMacAddress());
                reportJson.put("oaid", "");
                reportJson.put("apkVersion", getVersionName());
                reportJson.put("channelInfo", App.curChannelInfo);
                reportJson.put("android_id", getAndroidId());
                reportJson.put("bar_height", getStatusBarHeight());
                reportJson.put("isPieScreen", androidPieScreenAdaptation());
                reportJson.put("afUid", AppsflyerHelper.getInstance().getAFUid());
            }
            reportJson.put("location", getLocation());
            reportJson.put("network_type", getAPNType());
            reportJson.put("access", getAPNType());
//            reportJson.put("carrier", getProvider());
            reportJson.put("os_language", getLanguage());
            reportJson.put("timezone", getTimeZone());
            reportJson.put("sim_id", getSimId());
            reportJson.put("imei", getIMEI());
            reportJson.put("gaid", getGAID());
            reportJson.put("root", CheckRootPathSU());
            reportJson.put("simulator", isEmulator());
//            reportJson.put("ipAddress", getIpAddress());
            //Log.d("PhoneUtil", "jswrapper: JS :  imei   " + getIMEI());
        } catch (Exception e) {
            //Log.d("PhoneUtil", "jswrapper: JS :  Exception" + e.toString());
            e.printStackTrace();
        }
        //Log.d("PhoneUtil", "jswrapper: JS: getPhoneInfo : " + reportJson.toString());
        return reportJson;
    }

    public static boolean CheckRootPathSU() {
        File f = null;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    return true;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String getGAID() {
        return "";
//        AdvertisingIdClient.Info adInfo = null;
//        try {
//            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
//        } catch (IOException e) {
//            // Unrecoverable error connecting to Google Play services (e.g.,
//            // the old version of the service doesn't support getting AdvertisingId).
//        } catch (GooglePlayServicesRepairableException e) {
//            // Encountered a recoverable error connecting to Google Play services.
//        } catch (GooglePlayServicesNotAvailableException e) {
//            // Google Play services is not available entirely.
//        }
//
//        String advertisingId = adInfo.getId();
//        boolean isLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled();
//        return advertisingId;
    }

    public static String getIpAddress() {
        String ipAddress = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        ipAddress = inetAddress.getHostAddress();
                        Log.d("IP Address", ipAddress);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight == 0 ? DensityUtil.dip2px(context, 25) : statusBarHeight;
    }

    /**
     * 获取渠道名
     *
     * @return 如果没有获取成功，那么返回值为空
     */
    public static String getChannelName() {
        if (context == null) {
            return "";
        }
        String channelName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是某activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = String.valueOf(applicationInfo.metaData.get("CHANNEL"));
                    }
                }
            }
            //Log.d("CommonBridge", "jswrapper: JS:" + channelName.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channelName;
    }

    //获取版本名
    public static String getVersionName() throws Exception {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        return packInfo.versionName;
    }

    //获取版本号
    public static int getVersionCode() throws Exception {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        return packInfo.versionCode;
    }

    /**
     * 获取时区
     *
     * @return
     */
    public static String getTimeZone() {
        //String timeZone1 = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT);
        String timeZone2 = TimeZone.getDefault().getID();
        return timeZone2;
    }

    /**
     * 获取地理位置信息
     *
     * @return
     */
    public static String getLocation() {
        String retJson = "{}";

            /*if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                Location _location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (_location == null) {
                    _location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                location.put("latitude", String.valueOf(_location.getLatitude()));
                location.put("longitude", String.valueOf(_location.getLongitude()));
            }*/

        //获取地理位置管理器
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers == null) {
            return retJson;
        }

        //获取Location
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return retJson;
        }
        //对提供者进行排序，gps、net、passive
        List<String> providerSortList = new ArrayList<>();
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //Log.d(TAG, "jswrapper:GPS_PROVIDER");
            providerSortList.add(LocationManager.GPS_PROVIDER);
        }
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //Log.d(TAG, "jswrapper:NETWORK_PROVIDER");
            providerSortList.add(LocationManager.NETWORK_PROVIDER);
        }
        if (providers.contains(LocationManager.PASSIVE_PROVIDER)) {
            //Log.d(TAG, "jswrapper:PASSIVE_PROVIDER");
            providerSortList.add(LocationManager.PASSIVE_PROVIDER);
        }
        try {
            JSONObject localJson = new JSONObject();
            for (int i = 0; i < providerSortList.size(); i++) {
                String provider = providerSortList.get(i);
                //Log.d(TAG, "jswrapper:正在尝试：" + provider);
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    //Log.d(TAG, "jswrapper:定位成功：" + provider + "-" + location.getLatitude() + "-" + location.getLongitude() );
                    localJson.put("latitude", String.valueOf(location.getLatitude()));
                    localJson.put("longitude", String.valueOf(location.getLongitude()));
                    break;
                } else {
                    //Log.d(TAG, "jswrapper:定位失败：" + provider);
                }
            }
            //Log.d(TAG, "jswrapper:定位成功后的值：" + localJson.toString());
            retJson = localJson.toString();
        } catch (JSONException e) {

        }
        return retJson;
    }

    /**
     * sim卡信息（需要检查权限）
     *
     * @return
     */
    public static String getSimId() {
        SubscriptionManager mSubscriptionManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            mSubscriptionManager = SubscriptionManager.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return "";
            }
            List<SubscriptionInfo> mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
            if (mSubInfoList != null) {
                for (SubscriptionInfo info : mSubInfoList) {
                    if (null != info) {
                        return info.getIccId();
                    }
                }
            }
        }
        return "";
    }

    /**
     * 获取IMEI
     *
     * @return
     */
    public static String getIMEI() {
        String imei = "";
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return imei;
        }
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// android 8 即以后建议用getImei 方法获取
                Method method = null;
                method = tm.getClass().getMethod("getImei", int.class);
                imei = (String) method.invoke(tm, 0);
                if (TextUtils.isEmpty(imei)) {
                    imei = (String) method.invoke(tm, 1);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0的系统如果想获取MEID/IMEI1/IMEI2
                imei = getSystemPropertyByReflect("ril.gsm.imei");

            } else {//5.0以下获取imei/meid只能通过 getDeviceId  方法去取
                imei = tm.getDeviceId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei == null ? "" : imei;
    }

    private static String getSystemPropertyByReflect(String key) {
        try {
            @SuppressLint("PrivateApi")
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method getMethod = clz.getMethod("get", String.class, String.class);
            return (String) getMethod.invoke(clz, key, "");
        } catch (Exception e) {/**/}
        return "";
    }

    /**
     * 获取唯一Id
     *
     * @return
     */
    public static String getUniqueId() {
        StringBuilder sb = new StringBuilder();
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//        Log.d("PhoneUtil", "jswrapper：" + androidID);
        if (!TextUtils.isEmpty(androidID)) {
            sb.append(androidID);
        }
        String serial = "";
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//8.0+
                serial = Build.SERIAL;
            } else {//8.0-
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serial = (String) get.invoke(c, "ro.serialno");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(serial)) {
            sb.append(serial);
        }

        // 当 androidID 和 Serial 都为空时 检查 IMEI
        if (TextUtils.isEmpty(sb.toString())) {
            String deviceId = getIMEI();
            if (!TextUtils.isEmpty(serial)) {
                sb.append(deviceId);
            }
        }
        // 如果还是没有， 则UUID生成
        if (TextUtils.isEmpty(sb.toString())) {
            String uuid = UUID.randomUUID().toString();
            sb.append(uuid);
        }
        return MD5Util.encryptMD5ToString(sb.toString());
    }


    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取屏幕宽度（像素）
     *
     * @return px
     */
    public static int getWith() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度（像素）
     *
     * @return px
     */
    public static int getHeight() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取系统语言
     *
     * @return
     */
    public static String getLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        String lang = locale.getLanguage() + "_" + locale.getCountry();
        return lang;
    }

    /**
     * 获取AndroidId
     *
     * @return
     */
    public static String getAndroidId() {
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(androidID)) {
            return androidID;
        }
        return "";
    }

    /**
     * 获取Mac地址
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getMacAddress() {
        String macAddress = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());
        if (!wifiManager.isWifiEnabled()) {
            //必须先打开，才能获取到MAC地址
            wifiManager.setWifiEnabled(true);
            wifiManager.setWifiEnabled(false);
        }
        if (null != info) {
            macAddress = info.getMacAddress();
        }
        return macAddress;
    }

    /**
     * 获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2
     */
    public static String getAPNType() {
        //结果返回值
        String netType = "none";
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return "none";
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = "wifi";
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "4g";
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "3g";
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "2g";
            } else {
                netType = "other";
            }
        }
        return netType;
    }

    /**
     * 获取运营商
     *
     * @return 中国移动/中国联通/中国电信/未知
     */
    public static String getProvider() {
        String provider = "未知";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("MissingPermission") String IMSI = telephonyManager.getSubscriberId();
//            Log.d(TAG, "getProvider.IMSI:" + IMSI);
            if (IMSI == null) {
                if (TelephonyManager.SIM_STATE_READY == telephonyManager.getSimState()) {
                    String operator = telephonyManager.getSimOperator();
//                    Log.d(TAG, "getProvider.operator:" + operator);
                    if (operator != null) {
                        if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {
                            provider = "中国移动";
                        } else if (operator.equals("46001")) {
                            provider = "中国联通";
                        } else if (operator.equals("46003")) {
                            provider = "中国电信";
                        }
                    }
                }
            } else {
                if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {
                    provider = "中国移动";
                } else if (IMSI.startsWith("46001")) {
                    provider = "中国联通";
                } else if (IMSI.startsWith("46003")) {
                    provider = "中国电信";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return provider;
    }


    /**
     * 是否是模拟器
     *
     * @return
     */
    public static boolean isEmulator() {
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        // 是否可以处理跳转到拨号的 Intent
        boolean canResolveIntent = intent.resolveActivity(context.getPackageManager()) != null;
        return !canResolveIntent;
//        return Build.FINGERPRINT.startsWith("generic")
//                || Build.FINGERPRINT.toLowerCase().contains("vbox")
//                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
//                || Build.MODEL.contains("google_sdk")
//                || Build.MODEL.contains("Emulator")
//                || Build.SERIAL.equalsIgnoreCase("unknown")
//                || Build.SERIAL.equalsIgnoreCase("android")
//                || Build.MODEL.contains("Android SDK built for x86")
//                || Build.MANUFACTURER.contains("Genymotion")
//                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
//                || "google_sdk".equals(Build.PRODUCT)
//                || ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
//                .getNetworkOperatorName().toLowerCase().equals("android")
//                || !canResolveIntent;
    }

    /**
     * 判断是否存在光传感器来判断是否为模拟器
     * 部分真机也不存在温度和压力传感器。其余传感器模拟器也存在。
     *
     * @return true 为模拟器
     */
    public static Boolean notHasLightSensorManager() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensor8 = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //光
        if (null == sensor8) {
            return true;
        } else {
            return false;
        }
    }

    /*
     *判断蓝牙是否有效来判断是否为模拟器
     *返回:true 为模拟器
     */
    public static boolean notHasBlueTooth() {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null) {
            return true;
        } else {
            // 如果有蓝牙不一定是有效的。获取蓝牙名称，若为null 则默认为模拟器
            @SuppressLint("MissingPermission") String name = ba.getName();
            if (TextUtils.isEmpty(name)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /*
     *根据部分特征参数设备信息来判断是否为模拟器
     *返回:true 为模拟器
     */
    public static boolean isFeatures() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    /*
     *根据CPU是否为电脑来判断是否为模拟器
     *返回:true 为模拟器
     */
    public static boolean checkIsNotRealPhone() {
        String cpuInfo = readCpuInfo();
        if ((cpuInfo.contains("intel") || cpuInfo.contains("amd"))) {
            return true;
        }
        return false;
    }

    /*
     *根据CPU是否为电脑来判断是否为模拟器(子方法)
     *返回:String
     */
    public static String readCpuInfo() {
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            ProcessBuilder cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            StringBuffer sb = new StringBuffer();
            String readLine = "";
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine);
            }
            responseReader.close();
            result = sb.toString().toLowerCase();
        } catch (IOException ex) {
        }
        return result;
    }

    public static synchronized String getPackageName() {
        try {
            PackageManager pkgMgr = context.getPackageManager();
            PackageInfo pkgInfo = pkgMgr.getPackageInfo(context.getPackageName(), 0);
            return pkgInfo.packageName;
        } catch (Exception e) {

        }
        return "";
    }
}
