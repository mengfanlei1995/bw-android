package com.bw.game.bridge;

import com.bw.game.manager.ActivityManager;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class CommonBridge {
    public static void commonSuccEvalString(String methon,Object callBackName, Object jsonStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("method", methon);
        map.put("data", jsonStr);
        commonEvalString(callBackName, new JSONObject(map).toString());
    }

    public static void commonSuccEvalString(Object callbakKey, Object jsonStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("method", "succ");
        map.put("data", jsonStr);
        commonEvalString(callbakKey,new JSONObject(map).toString());
    }

    public static void commonFailEvalString(Object callbakKey, Object jsonStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("method", "fail");
        map.put("data", jsonStr);
        commonEvalString(callbakKey, new JSONObject(map).toString());
    }

    public static void commonEvalString(final Object callBackName, final String jsonStr) {
        ActivityManager.getInstance().runCocosThread(new Runnable() {
            @Override
            public void run() {
                Cocos2dxJavascriptJavaBridge.evalString("ListenerMgr.callListener('" + callBackName + "', '" + jsonStr + "')");
            }
        });
    }

    public static void commonEvalString(final String callBackName) {
        ActivityManager.getInstance().runCocosThread(new Runnable() {
            @Override
            public void run() {
                Cocos2dxJavascriptJavaBridge.evalString("ListenerMgr.callListenerByKey('" + callBackName + "')");
            }
        });
    }

    public static void commonEvalStringParam(final String callBackName, final String jsonStr) {
        ActivityManager.getInstance().runCocosThread(new Runnable() {
            @Override
            public void run() {
                Cocos2dxJavascriptJavaBridge.evalString("ListenerMgr.callListenerByKeyParams('" + callBackName + "', '" + jsonStr + "')");
            }
        });
    }


}
