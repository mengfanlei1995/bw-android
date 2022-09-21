package com.bw.game.download.manager;

import android.content.Context;
import android.util.Log;

import com.bw.game.util.FileUtil;
import com.bw.game.util.GlobalConstant;
import com.bw.game.util.SharePreUtil;
import com.bw.game.download.base.BaseHttpDownloadManager;
import com.bw.game.download.listener.OnDownloadListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

/**
 * @author GengTao
 * @date 2020/9/18
 * @description
 */
public class HttpDownloadManager extends BaseHttpDownloadManager {

    private static final String TAG = "HttpDownloadManager";
    private boolean shutdown = false;
    private Context context;
    private String apkUrl, apkName, downloadPath;
    private OnDownloadListener listener;

    public HttpDownloadManager(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    @Override
    public void download(Context context, String apkUrl, String apkName, OnDownloadListener listener) {
        this.context = context;
        this.apkUrl = apkUrl;
        this.apkName = apkName;
        this.listener = listener;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(GlobalConstant.THREAD_NAME);
                return thread;
            }
        });
        executor.execute(runnable);
    }

    @Override
    public void cancel() {
        shutdown = true;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            if (FileUtil.fileExists(downloadPath, apkName)) {
//                FileUtil.delete(downloadPath, apkName);
//            }
//            fullDownload();
            if (!FileUtil.fileExists(downloadPath, apkName)) {
                FileUtil.createFile(downloadPath, apkName);
            }
            startDownload();
        }
    };


    private void startDownload() {
        listener.start();
        BufferedInputStream bin = null;
        HttpURLConnection con = null;
        File file = new File(downloadPath, apkName);
        int startOffset = SharePreUtil.getInt(context,GlobalConstant.APKDOWNLOADPOSTION, 0);
        URL url;
        try {
            url = new URL(apkUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(GlobalConstant.HTTP_TIME_OUT);
            con.setConnectTimeout(GlobalConstant.HTTP_TIME_OUT);
            con.setRequestProperty("Accept-Encoding", "identity");
            con.setRequestProperty("Range" , "bytes=" + startOffset + "-");
            con.connect();

            Log.d(TAG, "js: con.getResponseCode() - " + con.getResponseCode() + "-" + startOffset);
            if (con.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                int length = con.getContentLength();
                int totalLength = length + startOffset;
                InputStream is = con.getInputStream();
                RandomAccessFile raf = new RandomAccessFile(file.getAbsolutePath(), "rwd");
                raf.seek(startOffset);
                byte[] buffer = new byte[1024 * 1024];
                int len;
                while ((len = is.read(buffer)) != -1 && !shutdown) {
                    raf.write(buffer, 0, len);
                    startOffset += len;
                    listener.downloading(totalLength, startOffset);
                }
                is.close();
                raf.close();
                if (shutdown) {
                    shutdown = false;
                    Log.d(TAG, "fullDownload: 取消了下载");
                    listener.cancel();
                } else {
                    listener.done(file);
                }
            } else if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM ||
                    con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                apkUrl = con.getHeaderField("Location");
                con.disconnect();
                Log.d(TAG, "fullDownload: 当前地址是重定向Url，定向后的地址：" + apkUrl);
                startDownload();
            } else if (con.getResponseCode() == 416) {
                listener.done(file);
            }else {
                listener.error(new SocketTimeoutException("下载失败：Http ResponseCode = " + con.getResponseCode()));
            }
        } catch (Exception e) {
            listener.error(e);
            e.printStackTrace();
        } finally {
            if (bin != null) {
                try {
                    bin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(con != null) {
                con.disconnect();
            }
        }
    }


    /**
     * 全部下载
     */
    private void fullDownload() {
        listener.start();
        try {
            URL url = new URL(apkUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(GlobalConstant.HTTP_TIME_OUT);
            con.setConnectTimeout(GlobalConstant.HTTP_TIME_OUT);
            con.setRequestProperty("Accept-Encoding", "identity");

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = con.getInputStream();
                int length = con.getContentLength();
                int len;
                //当前已下载完成的进度
                int progress = 0;
                byte[] buffer = new byte[1024 * 2];
                File file = FileUtil.createFile(downloadPath, apkName);
                FileOutputStream stream = new FileOutputStream(file);
                while ((len = is.read(buffer)) != -1 && !shutdown) {
                    //将获取到的流写入文件中
                    stream.write(buffer, 0, len);
                    progress += len;
                    listener.downloading(length, progress);
                }
                if (shutdown) {
                    //取消了下载 同时再恢复状态
                    shutdown = false;
                    Log.d(TAG, "fullDownload: 取消了下载");
                    listener.cancel();
                } else {
                    listener.done(file);
                }
                //完成io操作,释放资源
                stream.flush();
                stream.close();
                is.close();
                //重定向
            } else if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM ||
                    con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                apkUrl = con.getHeaderField("Location");
                con.disconnect();
                Log.d(TAG, "fullDownload: 当前地址是重定向Url，定向后的地址：" + apkUrl);
                fullDownload();
            } else {
                listener.error(new SocketTimeoutException("下载失败：Http ResponseCode = " + con.getResponseCode()));
            }
            con.disconnect();
        } catch (Exception e) {
            listener.error(e);
            e.printStackTrace();
        }
    }
}

