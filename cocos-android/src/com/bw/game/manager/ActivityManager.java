package com.bw.game.manager;

import android.app.Activity;

import org.cocos2dx.lib.Cocos2dxActivity;


public class ActivityManager {

    private static volatile ActivityManager instance;

    private Cocos2dxActivity mActivity;


    private Activity curActivity;

    public static ActivityManager getInstance() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    public void registerActivity(Cocos2dxActivity mActivity) {
        this.mActivity = mActivity;
    }

    public Cocos2dxActivity getCocosActivity() {
        return mActivity;
    }

    public void setCurActivity(Activity activity) {
        this.curActivity = activity;
    }

    public Activity getCurActivity() {
        return curActivity;
    }

    /**
     * 运行 Android线程
     */
    public void runAndroidThread(Runnable runnable) {
        if (mActivity != null && !mActivity.isFinishing()) {
            mActivity.runOnUiThread(runnable);
        }
    }

    /**
     * 运行 Cocos线程
     */
    public void runCocosThread(Runnable runnable) {
        if (mActivity != null && !mActivity.isFinishing()) {
            mActivity.runOnGLThread(runnable);
        }
    }
}
