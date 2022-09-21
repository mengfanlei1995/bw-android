package com.bw.game.util;

/**
 * @author GengTao
 * @date 2019/12/20
 * @description
 */
public class GlobalConstant {
    /**
     * 网络连接超时时间
     */
    public static final int HTTP_TIME_OUT = 30_000;

    /**
     * 新版本下载线程名称
     */
    public static final String THREAD_NAME = "app_update_thread";

    /**
     * apk文件后缀
     */
    public static final String APK_SUFFIX = ".apk";
    /**
     * 兼容Android N Uri 授权
     */
    public static String AUTHORITIES;

    /**
     * apk下载文件进度
     */
    public static String APKDOWNLOADPOSTION = "apk_download_position";

    /**
     * apk总大小
     */
    public static String APKDOWNLOADSIZE = "apk_download_size";

    /**
     * apk版本号
     */
    public static String APKDOWNLOADURL = "apk_download_url";

    /**
     * apk名称
     */
    public static String APKDOWNLOADNAME = "luckyWinner";

}
