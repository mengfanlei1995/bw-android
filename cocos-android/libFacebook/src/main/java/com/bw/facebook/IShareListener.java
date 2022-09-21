package com.bw.facebook;

/**
 * @author GengTao
 * @date 2022/3/24
 * @description
 */

public interface IShareListener {

    /**
     * 分享成功
     * @param postId
     */
    void onSuccess(String postId);

    /**
     * 分享取消
     */
    void onCancel();

    /**
     * 分享失败
     * @param reason
     */
    void onFail(String reason);
}
