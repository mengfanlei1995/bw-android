package com.bw.game.download.base;

import android.content.Context;

import com.bw.game.download.listener.OnDownloadListener;

/**
 * @author GengTao
 * @date 2020/9/18
 * @description
 */
public abstract class BaseHttpDownloadManager {

    /**
     * 下载apk
     *
     * @param apkUrl   apk下载地址
     * @param apkName  apk名字
     * @param listener 回调
     */
    public abstract void download(Context context, String apkUrl, String apkName, OnDownloadListener listener);

    /**
     * 取消下载apk
     */
    public abstract void cancel();
}
