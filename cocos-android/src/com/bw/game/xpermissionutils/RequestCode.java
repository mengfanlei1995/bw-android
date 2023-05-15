package com.bw.game.xpermissionutils;


public interface RequestCode {
    /**
     * 电话
     */
    int PHONE = 0x00;
    /**
     * 位置
     */
    int LOCATION = 0x01;
    /**
     * 相机
     */
    int CAMERA = 0x02;
    /**
     * 语音
     */
    int AUDIO = 0x04;
    /**
     * 存储
     */
    int EXTERNAL = 0x08;
    /**
     * 多个
     */
    int MORE = 0x10;
}