package com.bw.game.bridge;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.bw.facebook.FacebookHelper;
import com.bw.facebook.ILoginListener;
import com.bw.facebook.IShareListener;
import com.bw.game.manager.ActivityLifeCycle;

import com.bw.appsflyer.AppsflyerHelper;
import com.bw.game.App;
import com.activity.AppActivity;
import com.bw.game.cocos.SDKWrapper;
import com.bw.game.download.config.UpdateConfiguration;
import com.bw.game.download.dialog.UpdateDialog;
import com.bw.game.download.manager.DownloadManager;
import com.bw.game.manager.ActivityManager;
import com.bw.game.util.ApkUtil;
import com.bw.game.util.FileUtil;
import com.bw.game.util.GlobalConstant;
import com.bw.game.util.PhoneUtil;
import com.bw.game.util.SharePreUtil;
import com.bw.game.window.WebDialog;
//import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Iterator;

import static com.bw.game.bridge.CommonBridge.commonEvalString;

/**
 * @author hwz
 * @time 2021-04-15
 * @describe 游戏交互公共方法
 */
public class JsbBridge {
    private static final String TAG = "JsbBridge";

    public static void appTurnIntoBackGround() {
        commonEvalString("appTurnIntoBackGround");
    }

    public static void appTurnIntoForeground() {
        commonEvalString("appTurnIntoForeground");
    }

    public static void setOrientation(String dir) {
        String mode;
        try {
            JSONObject json = new JSONObject(dir);
            mode = json.optString("mode");
            if (mode.equals("P")) {
                //Log.d("CommonBridge", "jswrapper: JS: " +dir);
                ((AppActivity) (SDKWrapper.getInstance().getContext())).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                //Log.d("CommonBridge", "jswrapper: JS: " +dir);
                ((AppActivity) (SDKWrapper.getInstance().getContext())).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void vibrate(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            final int time = jsonObject.optInt("time");
            ActivityManager.getInstance().runAndroidThread(new Runnable() {
                @Override
                public void run() {
                    Vibrator vibrator = (Vibrator) ActivityManager.getInstance().getCurActivity().getSystemService(ActivityManager.getInstance().getCurActivity().VIBRATOR_SERVICE);
                    vibrator.vibrate(time);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开WebView
     *
     * @param json
     */
    public static void openWebView(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            final String url = jsonObject.optString("url");
            ActivityManager.getInstance().runAndroidThread(new Runnable() {
                @Override
                public void run() {
                    new WebDialog(ActivityManager.getInstance().getCurActivity(), url).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static String getSystemInfoSync() {
        JSONObject ret = new JSONObject();
        try {
            ret.put("method", "getSystemInfoSync");
            ret.put("data", PhoneUtil.getPhoneInfo());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("CommonBridge", "jswrapper: JS:" + ret.toString());
        return ret.toString();
    }

    public static void ClipBoard(final String text) {
        //Log.d("CommonBridge", "jswrapper: JS:" + text);
        ActivityManager.getInstance().runAndroidThread(new Runnable() {
            @Override
            public void run() {
                ClipboardManager clipboard = (ClipboardManager) ActivityManager.getInstance().getCurActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copy record", text.replace("#$", "\n"));
                clipboard.setPrimaryClip(clip);
            }
        });
    }

    /**
     * 获取AF数据
     *
     * @return {install_time=2021-10-28 08:33:07.791, af_status=Organic, af_message=organic install, is_first_launch=true}
     */
    public static String getAFData() {
        String data = AppsflyerHelper.getInstance().getAFData();
        return data;
    }

    public static String afInitFail() {
        boolean data = AppsflyerHelper.getInstance().afInitFail();
        return data ? "1" : "0";
    }

    /**
     * 拉起设置
     */
    public static void pullUpSet(String type) {
        PhoneUtil.pullUpSet(type);
    }

    public static void recordIncome(String json) {
        try {
            Context context = App.getContext();
            JSONObject jsonObject = new JSONObject(json);
            final Integer income = jsonObject.optInt("income");
            if (income > 0)
                AppsflyerHelper.getInstance().recordIncome(context, jsonObject);
            else
                AppsflyerHelper.getInstance().recordWithdraw(context, jsonObject);
        } catch (JSONException e) {

        }
    }

    public static void reportLogin(String json) {
        try {
            Context context = App.getContext();
            JSONObject jsonObject = new JSONObject(json);
            AppsflyerHelper.getInstance().reportLogin(context, jsonObject);
        } catch (JSONException e) {

        }
    }

    public static void checkGPSContacts() {
        ActivityManager.getInstance().runAndroidThread(new Runnable() {
            @Override
            public void run() {
                PhoneUtil.checkGPSContacts();
            }
        });
    }

    public static void installUpdate(final String json) {
        //Log.d(TAG, "js: json:" + json);
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        final Context context = App.getContext();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String apkUrl = "";
                String apkVersion = "";
                String updateTip = "";
                boolean isForceUpdate = false;

                try {
                    JSONObject jsonObject = new JSONObject(json);
                    apkVersion = jsonObject.optString("apkVersion");
                    apkUrl = jsonObject.optString("apkUrl");
                    updateTip = jsonObject.optString("updateTip");
                    isForceUpdate = jsonObject.optBoolean("isForceUpdate");
                } catch (JSONException e) {

                }

                if (apkUrl.isEmpty()) return;
                String apkName = GlobalConstant.APKDOWNLOADNAME + "_" + apkVersion + ".apk";
                String localApkUrl = SharePreUtil.getString(mActivity, GlobalConstant.APKDOWNLOADURL, "");
                UpdateConfiguration configuration = new UpdateConfiguration()
                        .setContext(mActivity.getApplicationContext())
                        .setJumpInstallPage(true)
                        .setDialogButtonTextColor(Color.WHITE)
                        .setShowBgdToast(true)
                        .setForcedUpgrade(isForceUpdate);
                DownloadManager manager = DownloadManager.getInstance(mActivity);
                manager.setApkName(apkName)
                        .setApkUrl(apkUrl)
                        .setShowNewerToast(true)
                        .setConfiguration(configuration)
                        .setApkDescription(Html.fromHtml(updateTip))
                        .download();

                String pageName = "";
                String content = "";

                if (FileUtil.fileExists(context.getExternalCacheDir().getPath(), apkName) && localApkUrl.equals(apkUrl)) {
                    UpdateDialog updateDialog = manager.getDefaultDialog();
                    int curProcess = SharePreUtil.getInt(mActivity, GlobalConstant.APKDOWNLOADPOSTION, 0);
                    int max = SharePreUtil.getInt(mActivity, GlobalConstant.APKDOWNLOADSIZE, 0);

                    //Log.d(TAG, "jswrapper: " + curProcess + " - " + max);

                    updateDialog.downloading(max, curProcess);
                    if (curProcess >= max) {
                        updateDialog.setInstallTag();
                        pageName = "apk_download";
                        content = "exist";
                    } else {
                        updateDialog.setClickText(curProcess);
                        pageName = "apk_download";
                        content = "process";
                    }
                } else {
                    SharePreUtil.putInt(mActivity, GlobalConstant.APKDOWNLOADPOSTION, 0);
                    SharePreUtil.putInt(mActivity, GlobalConstant.APKDOWNLOADSIZE, 0);
                    pageName = "apk_download";
                    content = "start";
                }
                //ReportUtil.nativeSceneReport(pageName + "_" + content);
            }
        });

    }

    /**
     * 拉起whatsApp，聊天
     *
     * @param mobileNum
     */
    private static String chatInWhatsApp(String mobileNum) {
        String result = "success";
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + mobileNum));
            intent.setPackage("com.whatsapp");
            mActivity.startActivity(intent);
        } catch (Exception e) {
            //  没有安装WhatsApp
            result = null;
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 拉起telegram，聊天
     *
     * @param name
     */
    private static String chatInTelegram(String name) {
        String result = "success";
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        String appName = "org.telegram.messenger.web";
        boolean isAppInstalled = ApkUtil.isAppInstall(mActivity, appName);
        if (!isAppInstalled) {
            appName = "org.telegram.messenger";
        }
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://telegram.me/" + name));
            intent.setPackage(appName);
            mActivity.startActivity(intent);
        } catch (Exception e) {
            //  没有安装telegram
            result = null;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 分享内容到whatsApp
     *
     * @param url
     */
    private static void shareLinkWhatsApp(String title, String url) {
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        String whatsAppPackageName = "com.whatsapp";
        if (!ApkUtil.isAppInstall(mActivity, whatsAppPackageName)) {
            Toast.makeText(mActivity, "whatsapp not Installed", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setPackage(whatsAppPackageName);
        mActivity.startActivity(Intent.createChooser(intent, title));
        //调用cocos的成功回调
    }

    /**
     * 分享内容到telegram
     *
     * @param url
     */
    private static void shareMessageTelegram(String title, String url) {
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        final String appName = "org.telegram.messenger.web";
        final boolean isAppInstalled = !ApkUtil.isAppInstall(mActivity, appName);
        if (isAppInstalled) {
            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            myIntent.setPackage(appName);
            myIntent.putExtra(Intent.EXTRA_TEXT, url);//
            mActivity.startActivity(Intent.createChooser(myIntent, title));
        } else {
            Toast.makeText(mActivity, "Telegram not Installed", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 分享链接
     *
     * @param jsonData
     */
    public static void shareLink(final String jsonData) {
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject json = new JSONObject(jsonData);
                    final String platform = json.optString("platform");
                    final String url = json.optString("url");
                    final String title = json.optString("title");
                    if ("facebook".equals(platform)) {
                        shareLinkFB(mActivity, title, url);
                    } else if ("whatsapp".equals(platform)) {
                        shareLinkWhatsApp(title, url);
                    } else if ("telegram".equals(platform)) {
                        shareMessageTelegram(title, url);
                    } else {
                        shareLinkSystem(title, url);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void shareLinkFB(Activity mActivity, String title, String url) {
        FacebookHelper.getInstance().shareLink(mActivity, title, url, new IShareListener() {
            @Override
            public void onSuccess(String postId) {
                //调用cocos的成功回调  result.getPostId()
                Toast.makeText(mActivity, "share success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                //调用cocos的取消回调
                Toast.makeText(mActivity, "share cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(String reason) {
                //调用cocos的失败回调
                Toast.makeText(mActivity, "share fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 拉起系统分享
     *
     * @param title
     * @param url
     */
    private static void shareLinkSystem(String title, String url) {
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        mActivity.startActivity(Intent.createChooser(intent, title));
        //调用cocos的成功回调
    }

    /**
     * @param jsonData
     */
    public static void loginFB(String jsonData) {
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleLoginFB();
            }
        });
    }

    /**
     * facebook 登出
     */
    public static void logoutFB() {
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FacebookHelper.getInstance().logOut();
            }
        });
    }

    private static void handleLoginFB() {
        final Activity mActivity = ActivityManager.getInstance().getCurActivity();
        if (FacebookHelper.getInstance().isLogin()) {
            String jsonStr = FacebookHelper.getInstance().getAccessTokenJson().toString();
            CommonBridge.commonEvalStringParam("faceBookLogined", jsonStr);
//            Log.d("CommonBridge", "jswrapper: JS: faceBookLogined");
        } else {
            FacebookHelper.getInstance().registerListener(new ILoginListener() {
                @Override
                public void onLoginSuccess(JSONObject result) {
                    closeFBActivity();
//                    Log.d("CommonBridge", "jswrapper: JS: faceBookLoginSuccess");
                    CommonBridge.commonEvalStringParam("faceBookLoginSuccess", result.toString());
                }

                @Override
                public void onLoginCancel() {
                    closeFBActivity();
//                    Log.d("CommonBridge", "jswrapper: JS: faceBookLoginCancel");
                    CommonBridge.commonEvalString("faceBookLoginCancel");
                }

                @Override
                public void onLoginFail(JSONObject reason) {
                    closeFBActivity();
//                    Log.d("CommonBridge", "jswrapper: JS: faceBookLoginFail");
                    CommonBridge.commonEvalStringParam("faceBookLoginFail", reason.toString());
                }
            });
            FacebookHelper.getInstance().logInWithReadPermissions(mActivity);
        }
    }

    /**
     * fb登录取消后会残留一个透明activity
     */
    private static void closeFBActivity() {
        Activity activity = ActivityManager.getInstance().getCurActivity();
        if (activity != null && !ActivityLifeCycle.getActivityName(activity).equals(ActivityLifeCycle.COCOS2DX_ACTIVITY_NAME)) {
            activity.finish();
        }
    }

//    /**
//     * 上报Firebase事件
//     * @jsonData jsonData
//     */
//    public static void postFirebaseEvent(final String jsonData) {
//        final Activity activity = ActivityManager.getInstance().getCurActivity();
//        try {
//            JSONObject json = new JSONObject(jsonData);
//            final String eventName = json.optString("eventName");
//            JSONObject jsonObject = (JSONObject) json.opt("params");
//            activity.runOnUiThread (new Runnable() {
//                @Override
//                public void run() {
//                    FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
//                    Bundle bundle = new Bundle();
//                    Iterator it = jsonObject.keys();
//                    String key;
//                    Object value;
//                    while (it.hasNext()) {
//                        key = it.next().toString();
//                        value = jsonObject.opt(key);
//                        bundle.putString(key, value.toString());
//                    }
//                    firebaseAnalytics.logEvent(eventName, bundle);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Firebase 设置UserId
//     * @userId userId
//     */
//    public static void setFirebaseUserId(final String userId) {
//        final Activity activity = ActivityManager.getInstance().getCurActivity();
//        activity.runOnUiThread (new Runnable() {
//            @Override
//            public void run() {
//                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
//                firebaseAnalytics.setUserId(userId);
//            }
//        });
//    }
//
//    /**
//     * Firebase 设置UserProperty
//     * @propJson propJson
//     */
//    public static void setFirebaseUserProperty(final String propJson) {
//        final Activity activity = ActivityManager.getInstance().getCurActivity();
//        try {
//            JSONObject json = new JSONObject(propJson);
//            JSONObject jsonObject = (JSONObject) json.opt("params");
//            activity.runOnUiThread (new Runnable() {
//                @Override
//                public void run() {
//                    FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
//                    Iterator it = jsonObject.keys();
//                    String key;
//                    Object value;
//                    while (it.hasNext()) {
//                        key = it.next().toString();
//                        value = jsonObject.opt(key);
//                        firebaseAnalytics.setUserProperty(key, value.toString());
//                    }
//                }
//
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 上报Facebook事件
     *
     * @jsonData jsonData
     */
    public static void postFacebookEvent(final String jsonData) {
        try {
            JSONObject json = new JSONObject(jsonData);
            final String eventName = json.optString("eventName");
            JSONObject jsonObject = (JSONObject) json.opt("params");
            FacebookHelper.getInstance().logEvent(eventName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void postFacebookPurchase(final String jsonData) {
        try {
            JSONObject json = new JSONObject(jsonData);
            final BigDecimal purchaseAmount = (BigDecimal) json.opt("purchaseAmount");
            final String currencyCode = json.optString("currency");
            JSONObject jsonObject = (JSONObject) json.opt("params");
            FacebookHelper.getInstance().logPurchase(purchaseAmount, Currency.getInstance(currencyCode), jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //跳转google市场
    public static void goStore() {
        final Activity app = ActivityManager.getInstance().getCurActivity();
        String packageName = app.getPackageName();
        //这里开始执行一个应用市场跳转逻辑，默认this为Context上下文对象
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName)); //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
        //存在手机里没安装应用市场的情况，跳转会包异常，做一个接收判断
        if (intent.resolveActivity(app.getPackageManager()) != null) { //可以接收
            app.startActivity(intent);
        } else { //没有应用市场，我们通过浏览器跳转到Google Play
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));

            //这里存在一个极端情况就是有些用户浏览器也没有，再判断一次
            if (intent.resolveActivity(app.getPackageManager()) != null) { //有浏览器
                app.startActivity(intent);
            } else { //天哪，这还是智能手机吗？
                Toast.makeText(app, "You don't have an app market installed, not even a browser!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 上报af事件
     *
     * @jsonData jsonDatap
     */
    public static void postAfEvent(final String jsonData) {
        try {
            JSONObject json = new JSONObject(jsonData);
            final String eventName = json.optString("eventName");
            JSONObject jsonObject = (JSONObject) json.opt("params");
            AppsflyerHelper.getInstance().afReport(eventName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
