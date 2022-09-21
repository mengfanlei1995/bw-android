package com.bw.game.util;

import com.bw.game.bridge.CommonBridge;

import org.json.JSONException;
import org.json.JSONObject;

public final class ReportUtil {
    public static void nativeClickReport(String elemId, String elemName) {
        try {
            JSONObject reportJson = new JSONObject();
            reportJson.put("element_id", elemId);
            reportJson.put("element_name", elemName);
            CommonBridge.commonEvalStringParam("nativeClickReport", reportJson.toString());
        } catch (JSONException e) {

        }
    }

    public static void nativeSceneReport(String pageName) {
        try {
            JSONObject reportJson = new JSONObject();
            reportJson.put("page_name", pageName);
            CommonBridge.commonEvalStringParam("nativeSceneReport", reportJson.toString());
        } catch (JSONException ee) {

        }
    }
}
