package com.bw.game.download.listener;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * @author GengTao
 * @date 2020/9/18
 * @description
 */
public interface OnButtonClickListener {

    /**
     * 升级按钮点击事件
     */
    int UPDATE = 0;
    /**
     * 取消按钮点击事件
     */
    int CANCEL = 1;

    @IntDef({UPDATE, CANCEL})
    @Retention(RetentionPolicy.SOURCE)
    @interface ID {

    }

    /**
     * 按钮点击回调
     *
     * @param id {@link OnButtonClickListener#UPDATE}
     *           {@link OnButtonClickListener#CANCEL}
     */
    void onButtonClick(@ID int id);
}

