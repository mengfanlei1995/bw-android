package com.bw.facebook;

import org.json.JSONObject;

/**
 * @author GengTao
 * @date 2021/9/26
 * @description
 */
public interface ILoginListener {

    /**
     * 登陆成功
     * @param result
     */
    void onLoginSuccess(JSONObject result);

    /**
     * 登陆取消
     */
    void onLoginCancel();

    /**
     * 登陆失败
     * @param reason
     */
    void onLoginFail(JSONObject reason);
}
