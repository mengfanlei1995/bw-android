package com.bw.appsflyer;

public interface IInitEvent {

    void onInitSuccess(String data);

    void onInitFail(String data);
}
