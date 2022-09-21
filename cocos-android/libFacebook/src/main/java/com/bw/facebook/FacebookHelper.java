package com.bw.facebook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.LoginStatusCallback;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.Iterator;

/**
 * @author GengTao
 * @date 2021/9/26
 * @description Facebook帮助类
 */
public class FacebookHelper {

    private static String TAG = FacebookHelper.class.getSimpleName();

    private volatile static FacebookHelper instance;
    private CallbackManager callbackManager;
    private ILoginListener loginlistener;
    private AppEventsLogger logger;

    private FacebookHelper() {
        callbackManager = CallbackManager.Factory.create();
    }

    public static FacebookHelper getInstance() {
        if (instance == null) {
            synchronized (FacebookHelper.class) {
                if (instance == null) {
                    instance = new FacebookHelper();
                }
            }
        }
        return instance;
    }

    public void init(Application application) {
        FacebookSdk.fullyInitialize();
//        FacebookSdk.setIsDebugEnabled(true);
//        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        AppEventsLogger.activateApp(application);
        logger = AppEventsLogger.newLogger(application.getApplicationContext());
    }

    /**
     * 注册登陆监听器
     * @param listener
     */
    public void registerListener(ILoginListener listener) {
        if(this.loginlistener == null) {
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    loginlistener.onLoginSuccess(accessTokenToJson(loginResult.getAccessToken()));
                }

                @Override
                public void onCancel() {
                    loginlistener.onLoginCancel();
                }

                @Override
                public void onError(FacebookException error) {
                    loginlistener.onLoginFail(errorToJson(error));
                }
            });
        }
        this.loginlistener = listener;
    }

    /**
     * 登出
     */
    public void logOut() {
        LoginManager.getInstance().logOut();
    }

    /**
     * accessToken 转 json
     * @param accessToken
     * @return
     */
    public JSONObject accessTokenToJson(AccessToken accessToken) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("token", accessToken.getToken());
            jsonData.put("userId", accessToken.getUserId());
            jsonData.put("applicationId", accessToken.getApplicationId());
            jsonData.put("expires", accessToken.getExpires().getTime());
            jsonData.put("dataAccessExpirationTime", accessToken.getDataAccessExpirationTime().getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    /**
     * error 转 json
     * @param exception
     * @return
     */
    public JSONObject errorToJson(Exception exception) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("error", exception.getMessage().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    /**
     * 获取缓存口令
     * @return
     */
    public JSONObject getAccessTokenJson() {
        return accessTokenToJson(AccessToken.getCurrentAccessToken());
    }

    /**
     * 是否已经登陆
     * @return
     */
    public boolean isLogin() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        return isLoggedIn;
    }

    /**
     * 已经登陆，读取权限登陆
     * @param activity
     */
    public void logInWithReadPermissions(Activity activity) {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile"));
    }

    /**
     * 快捷登录， 用于不同设备之间切换（暂不使用）
     * @param activity
     */
    public void retrieveLoginStatus(Activity activity) {
        LoginManager.getInstance().retrieveLoginStatus(activity, new LoginStatusCallback() {
            @Override
            public void onCompleted(AccessToken accessToken) {

            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onError(Exception exception) {

            }
        });
    }

    /**
     * 回调activityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void activityResultCB(int requestCode, int resultCode, Intent data) {
        if(callbackManager == null) return;
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 分享链接
     * @param activity
     * @param url
     * @param listener
     */
    public void shareLink(Activity activity, String title, String url, final IShareListener listener) {
        FacebookSdk.fullyInitialize();
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .setQuote(title)
                .build();
        ShareDialog shareDialog = new ShareDialog(activity);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.d(TAG, "facebook share success");
                if(listener != null) listener.onSuccess(result.getPostId());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook share cancel");
                if(listener != null) listener.onCancel();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "facebook share failed, error = " + error.toString());
                if(listener != null) listener.onSuccess(error.toString());
            }
        });
        shareDialog.show(content);
    }

    /**
     * 上报facebook事件
     * @param eventName
     * @param jsonObject
     */
    public void logEvent(String eventName, JSONObject jsonObject) {
        Bundle bundle = new Bundle();
        Iterator it = jsonObject.keys();
        String key;
        Object value;
        while (it.hasNext()) {
            key = it.next().toString();
            value = jsonObject.opt(key);
            bundle.putString(key, value.toString());
        }
        logger.logEvent(eventName, bundle);
    }

    /***
     * 上报facebook的支付事件
     * @param purchaseAmount
     * @param currency
     */
    public void logPurchase(BigDecimal purchaseAmount, Currency currency) {
        logger.logPurchase(purchaseAmount, currency);
    }

    /***
     * 上报facebook的支付事件
     * @param purchaseAmount
     * @param currency
     */
    public void logPurchase(BigDecimal purchaseAmount, Currency currency, JSONObject parameters) {
        Bundle bundle = new Bundle();
        Iterator it = parameters.keys();
        String key;
        Object value;
        while (it.hasNext()) {
            key = it.next().toString();
            value = parameters.opt(key);
            bundle.putString(key, value.toString());
        }
        logger.logPurchase(purchaseAmount, currency, bundle);
    }
}
