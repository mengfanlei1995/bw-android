package com.bw.game;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.bw.appsflyer.AppsflyerHelper;
import com.bw.appsflyer.IInitEvent;
import com.bw.facebook.FacebookHelper;
import com.bw.game.manager.ActivityLifeCycle;
import com.bw.game.util.PhoneUtil;
import com.meituan.android.walle.ChannelInfo;
import com.meituan.android.walle.WalleChannelReader;

import org.json.JSONObject;

import java.util.Map;

import static com.bw.game.bridge.CommonBridge.commonEvalString;


public class App extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        ActivityLifeCycle.init(this);
        initChannelInfo(this);
        FacebookHelper.getInstance().init(this);
        PhoneUtil.init(this);
        AppsflyerHelper.getInstance().init(this, new IInitEvent() {
            @Override
            public void onInitSuccess(String data) {
                commonEvalString("appsflyerLaunchSuccess",  data);
            }

            @Override
            public void onInitFail(String data) {
                /*Log.d("", "jswrapper: appsflyerLaunchFail");*/
                /*commonEvalString("appsflyerLaunchFail");*/
            }
        });
    }

    public static Context getContext() {
        return mContext;
    }

    //渠道信息
    public static JSONObject curChannelInfo;
    public static void initChannelInfo(Application application) {
        try {
            JSONObject jsonObject = new JSONObject();
            ChannelInfo info = WalleChannelReader.getChannelInfo(application);
            if (info != null) {
                String channel = info.getChannel();
                jsonObject.put("channel", channel);
                Map<String, String> extraInfo = info.getExtraInfo();
                for(String key : extraInfo.keySet()) {
                    jsonObject.put(key, extraInfo.get(key));
                }
                curChannelInfo = jsonObject;
                AppsflyerHelper.getInstance().setOutOfMedia(channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
