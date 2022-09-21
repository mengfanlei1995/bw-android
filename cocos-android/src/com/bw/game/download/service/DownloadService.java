package com.bw.game.download.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bw.game.util.FileUtil;
import com.bw.game.util.GlobalConstant;
import com.bw.game.util.SharePreUtil;
import com.bw.game.download.base.BaseHttpDownloadManager;
import com.bw.game.download.config.UpdateConfiguration;
import com.bw.game.download.listener.OnDownloadListener;
import com.bw.game.download.manager.DownloadManager;
import com.bw.game.download.manager.HttpDownloadManager;
import luckywinner.website.R;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author GengTao
 * @date 2020/9/18
 * @description
 */
public final class DownloadService extends Service implements OnDownloadListener {

    private static final String TAG = "DownloadService";
    private int smallIcon;
    private String apkUrl;
    private String apkName;
    private String downloadPath;
    private List<OnDownloadListener> listeners;
    private boolean showBgdToast;
    private boolean jumpInstallPage;
    private int lastProgress;
    private DownloadManager downloadManager;
    private int maxSize = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            return START_STICKY;
        }
        init();
        return super.onStartCommand(intent, flags, startId);
    }


    private void init() {
        downloadManager = DownloadManager.getInstance();
        if (downloadManager == null) {
            Log.d(TAG, "init DownloadManager.getInstance() = null ,请先调用 getInstance(Context context) !");
            return;
        }
        apkUrl = downloadManager.getApkUrl();
        apkName = downloadManager.getApkName();
        downloadPath = downloadManager.getDownloadPath();
        smallIcon = downloadManager.getSmallIcon();
        //创建apk文件存储文件夹
        FileUtil.createDirDirectory(downloadPath);
        UpdateConfiguration configuration = downloadManager.getConfiguration();
        listeners = configuration.getOnDownloadListener();
        showBgdToast = configuration.isShowBgdToast();
        jumpInstallPage = configuration.isJumpInstallPage();
        download(configuration);
//        if (checkApkMD5()) {
//            Log.d(TAG, "文件已经存在直接进行安装");
//            //直接调用完成监听即可
//            done(FileUtil.createFile(downloadPath, apkName));
//        } else {
//            Log.d(TAG, "文件不存在开始下载");
//            download(configuration);
//        }
    }

    /**
     * 校验Apk是否已经下载好了，不重复下载
     *
     * @return 是否下载完成
     */
    private boolean checkApkMD5() {
        if (FileUtil.fileExists(downloadPath, apkName)) {
            String fileMD5 = FileUtil.getFileMD5(FileUtil.createFile(downloadPath, apkName));
            return fileMD5.equalsIgnoreCase(downloadManager.getApkMD5());
        }
        return false;
    }

    /**
     * 获取下载管理者
     */
    private synchronized void download(UpdateConfiguration configuration) {
        if (downloadManager.isDownloading()) {
            Log.e(TAG, "download: 当前正在下载，请务重复下载！");
            return;
        }
        BaseHttpDownloadManager manager = configuration.getHttpManager();
        //使用自己的下载
        if (manager == null) {
            manager = new HttpDownloadManager(downloadPath);
            configuration.setHttpManager(manager);
        }
        //如果用户自己定义了下载过程
        manager.download(configuration.getContext(), apkUrl, apkName, this);
        downloadManager.setState(true);
    }


    @Override
    public void start() {
        if (showBgdToast) {
            handler.sendEmptyMessage(0);
        }
        handler.sendEmptyMessage(1);
    }

    @Override
    public void downloading(int max, int progress) {
        //Log.i(TAG, "js: max: " + max + " --- progress: " + progress);
        int curProgress = (int) (progress / (double) max * 100.0);
        if(curProgress != lastProgress) {
            lastProgress = curProgress;
            handler.obtainMessage(2, max, progress).sendToTarget();
        }
    }

    @Override
    public void done(File apk) {
        //Log.i(TAG, "js: done: 文件已下载至" + apk.toString());
        downloadManager.setState(false);
//        if (jumpInstallPage) {
//            ApkUtil.installApk(this, apk);
//        }
        //如果用户设置了回调 则先处理用户的事件 在执行自己的
        handler.obtainMessage(3, apk).sendToTarget();
    }

    @Override
    public void cancel() {
        downloadManager.setState(false);
        handler.sendEmptyMessage(4);
    }

    @Override
    public void error(Exception e) {
        Log.e(TAG, "error: " + e);
        downloadManager.setState(false);
        handler.obtainMessage(5, e).sendToTarget();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Log.d(TAG, "js:handler: " + msg.what + " | " + msg.arg1 + " | " + msg.arg2);
            switch (msg.what) {
                case 0:
                    SharePreUtil.putString(getApplicationContext(), GlobalConstant.APKDOWNLOADURL, downloadManager.getApkUrl());
                    Toast.makeText(DownloadService.this, R.string.background_downloading, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    for (OnDownloadListener listener : listeners) {
                        listener.start();
                    }
                    break;
                case 2:
                    SharePreUtil.putInt(getApplicationContext(),GlobalConstant.APKDOWNLOADPOSTION, msg.arg2);
                    if (msg.arg1 != maxSize) {
                        SharePreUtil.putInt(getApplicationContext(),GlobalConstant.APKDOWNLOADSIZE, msg.arg1);
                        maxSize = msg.arg1;
                    }
                    for (OnDownloadListener listener : listeners) {
                        listener.downloading(msg.arg1, msg.arg2);
                    }
                    break;
                case 3:
                    for (OnDownloadListener listener : listeners) {
                        listener.done((File) msg.obj);
                    }
                    //执行了完成开始释放资源
                    releaseResources();
                    break;
                case 4:
                    for (OnDownloadListener listener : listeners) {
                        listener.cancel();
                    }
                    break;
                case 5:
                    for (OnDownloadListener listener : listeners) {
                        listener.error((Exception) msg.obj);
                    }
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 下载完成释放资源
     */

    private void releaseResources() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        stopSelf();
        downloadManager.release();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
