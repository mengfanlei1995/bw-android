package com.bw.appsflyer;

import android.app.Application;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.attribution.AppsFlyerRequestListener;


import org.json.JSONObject;

/**
 * @author GengTao
 * @date 2021/0/14
 * @description
 */
public class AppsflyerHelper {
    /*包名：in.xilnnd.bw的时候用*/
    //private static final String AF_DEV_KEY = "f8GaCKeVRgWjgMubyqQjFX";
    /*包名：luckywinner.website的时候用*/
    private static final String AF_DEV_KEY = "to8XEHPaN3hiEa8nPA6fiZ";

    private volatile static AppsflyerHelper instance;

    private String afData;

    private boolean initFail = false;

    private static Context mContext;

    private AppsflyerHelper() {
    }

    public static AppsflyerHelper getInstance() {
        if (instance == null) {
            synchronized (AppsflyerHelper.class) {
                if (instance == null) {
                    instance = new AppsflyerHelper();
                }
            }
        }
        return instance;
    }


    /**
     * 初始化
     * @param application
     */
    public void init(Application application, final IInitEvent listener) {
        mContext = application;
        AppsFlyerLib instance = AppsFlyerLib.getInstance();
        instance.setDebugLog(false);
        //instance.setAppId("in.xilnnd.bw");
        //instance.setImeiData(imei);
        //instance.setAndroidIdData(androidId);
        instance.setCollectIMEI(true);
        instance.setCollectAndroidID(true);

        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                initFail = false;
//                for (String attrName : conversionData.keySet()) {
//                    Log.d("CommonBridge", "jswrapper: " + attrName + " = " + conversionData.get(attrName));
//                }
                conversionData.put("appsflyer_id", AppsFlyerLib.getInstance().getAppsFlyerUID(mContext));

                JSONObject jsonObject = new JSONObject(conversionData);
                afData = jsonObject.toString();
                listener.onInitSuccess(jsonObject.toString());
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                /*Log.d("CommonBridge", "jswrapper: error getting conversion data: " + errorMessage);*/
                initFail = true;
                listener.onInitFail(errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                /*for (String attrName : attributionData.keySet()) {
                    Log.d("CommonBridge", "jswrapper: attribute: " + attrName + " = " + attributionData.get(attrName));
                }*/
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                /*Log.d("CommonBridge", "jswrapper: error onAttributionFailure : " + errorMessage);*/
            }

        };

        instance.init(AF_DEV_KEY, conversionListener, application);
        instance.start(application);
    }

    public String getAFUid() {
        return AppsFlyerLib.getInstance().getAppsFlyerUID(mContext);
    }

    /**
     * 获取初始化成功参数
     * @return
     */
    public String getAFData() {
        return TextUtils.isEmpty(afData) ? "" : afData;
    }

    public boolean afInitFail() {
        return initFail;
    }

    //userId, sessionId, nickName, headPic, phone, accountType
    public void reportLogin(Context context, JSONObject info){
        Map<String, Object> eventValues = new HashMap<String, Object>();
        eventValues.put("userId", info.optString("userId"));
        eventValues.put("sessionId", info.optString("sessionId"));
        eventValues.put("nickName", info.optString("nickName"));
        eventValues.put("headPic", info.optString("headPic"));
        eventValues.put("phone", info.optString("phone"));
        eventValues.put("accountType", info.optString("accountType"));
//        Log.d("afLaunch=======reportL", String.valueOf(eventValues));
        AppsFlyerLib.getInstance().logEvent(context,AFInAppEventType.LOGIN,eventValues,
                new AppsFlyerRequestListener() {
                    @Override
                    public void onSuccess() {
//                        Log.d("afLaunch=======login", "Event sent successfully");
                    }
                    @Override
                    public void onError(int i, String s) {
                        /*Log.d("afLaunch=======login", "Event failed to be sent:\n" +
                                "Error code: " + i + "\n"
                                + "Error description: " + s);*/
                    }
                });
    }

    public void recordIncome(Context context, JSONObject info) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        eventValue.put(AFInAppEventParameterName.REVENUE,info.optString("income"));
        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE,info.optString("goodsname"));
        eventValue.put(AFInAppEventParameterName.CONTENT_ID,info.optString("goodsid"));
        eventValue.put(AFInAppEventParameterName.CURRENCY,"INR");
//        Log.d("afLaunch=======addcash", String.valueOf(eventValue));
        AppsFlyerLib.getInstance().logEvent(context , AFInAppEventType.PURCHASE , eventValue,
                new AppsFlyerRequestListener() {
                    @Override
                    public void onSuccess() {
//                        Log.d("afLaunch=======addcash", "Event sent successfully");
                    }
                    @Override
                    public void onError(int i, String s) {
                        /*Log.d("afLaunch=======addcash", "Event failed to be sent:\n" +
                                "Error code: " + i + "\n"
                                + "Error description: " + s);*/
                    }
                });
    }

    public void recordWithdraw(Context context, JSONObject info) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        eventValue.put(AFInAppEventParameterName.REVENUE,info.optString("withdraw"));
        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE,info.optString("goodsname"));
        eventValue.put(AFInAppEventParameterName.CONTENT_ID,info.optString("goodsid"));
        eventValue.put(AFInAppEventParameterName.CURRENCY,"INR");
        AppsFlyerLib.getInstance().logEvent(context , "withdraw_cash" , eventValue,
                new AppsFlyerRequestListener() {
                    @Override
                    public void onSuccess() {
//                        Log.d("afLaunch=======withdraw", "Event sent successfully");
                    }
                    @Override
                    public void onError(int i, String s) {
                        /*Log.d("afLaunch=======withdraw", "Event failed to be sent:\n" +
                                "Error code: " + i + "\n"
                                + "Error description: " + s);*/
                    }
                });
    }

    public void setOutOfMedia(String channel) {
        AppsFlyerLib.getInstance().setOutOfStore(channel);
    }
}
