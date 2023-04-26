package com.bw.game.download.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bw.game.download.config.UpdateConfiguration;
import com.bw.game.download.listener.OnButtonClickListener;
import com.bw.game.download.listener.OnDownloadListener;
import com.bw.game.download.manager.DownloadManager;
import com.bw.game.download.service.DownloadService;
import com.bw.game.download.view.StrokeTextView;
import com.bw.game.util.ApkUtil;
import com.bw.game.util.DensityUtil;
import com.bw.game.util.FileUtil;
import com.winner.casino.wheel.R;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * @author GengTao
 * @date 2020/9/18
 * @description
 */
public class UpdateDialog extends Dialog implements View.OnClickListener, OnDownloadListener {
    private Context context;
    private DownloadManager manager;
    private boolean forcedUpgrade;
    private Button update;
    private ProgressBar progressBar;
    private TextView downloadingTip;
    private TextView errorTip;
    private FrameLayout progressLayout;
    private OnButtonClickListener buttonClickListener;
    private StrokeTextView progressText;
    private int dialogImage, dialogButtonTextColor, dialogButtonColor, dialogProgressBarColor;
    private File apk;
    private final int install = 0x45F;

    public UpdateDialog(@NonNull Context context) {
        super(context, R.style.UpdateDialog);
        init(context);
    }

    /**
     * 初始化布局
     */
    private void init(Context context) {
        this.context = context;
        manager = DownloadManager.getInstance();
        UpdateConfiguration configuration = manager.getConfiguration();
        configuration.setOnDownloadListener(this);
        forcedUpgrade = configuration.isForcedUpgrade();
        buttonClickListener = configuration.getOnButtonClickListener();
        dialogImage = configuration.getDialogImage();
        dialogButtonTextColor = configuration.getDialogButtonTextColor();
        dialogButtonColor = configuration.getDialogButtonColor();
        dialogProgressBarColor = configuration.getDialogProgressBarColor();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
        setContentView(view);
        setWindowSize(context);
        initView(view);
    }

    private void initView(View view) {
        View ibClose = view.findViewById(R.id.ib_close);
        //TextView description = view.findViewById(R.id.tv_description);
        progressBar = view.findViewById(R.id.np_bar);
        errorTip = view.findViewById(R.id.tv_error_tip);
        progressLayout = view.findViewById(R.id.fl_progress);
        downloadingTip = view.findViewById(R.id.tv_downloading_tip);
        progressText = view.findViewById(R.id.tv_progress_num);
//        progressBar.setVisibility(forcedUpgrade ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        update = view.findViewById(R.id.btn_update);
        update.setTag(0);
        update.setOnClickListener(this);
        ibClose.setOnClickListener(this);
        if (dialogButtonTextColor != -1) {
            update.setTextColor(dialogButtonTextColor);
        }
        if (dialogButtonColor != -1) {
            StateListDrawable drawable = new StateListDrawable();
            GradientDrawable colorDrawable = new GradientDrawable();
            colorDrawable.setColor(dialogButtonColor);
            colorDrawable.setCornerRadius(DensityUtil.dip2px(context, 3));
            drawable.addState(new int[]{android.R.attr.state_pressed}, colorDrawable);
            drawable.addState(new int[]{}, colorDrawable);
            update.setBackgroundDrawable(drawable);
        }
        //强制升级
        if (forcedUpgrade) {
            ibClose.setVisibility(View.GONE);
        }
        setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                //屏蔽返回键
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        //description.setText(manager.getApkDescription());

        context.startService(new Intent(context, DownloadService.class));
    }

    private void setWindowSize(Context context) {
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        //以下满足刘海屏等特殊屏幕
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            final View decorView = dialogWindow.getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        /*if (id == R.id.ib_close) {
            if (!forcedUpgrade) {
                dismiss();
            }
            //回调点击事件
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClick(OnButtonClickListener.CANCEL);
            }
            //SendMsgToUnity.cancelUpdateCB();
        } else */
        if (id == R.id.btn_update) {
            //ReportUtil.nativeClickReport(update.getText().toString(), "整包更新界面的按钮");
            if ((int) update.getTag() == install) {
                Log.d("UpdateDialog", "js: start install");
                installApk();
                return;
            }

            update.setVisibility(View.GONE);
            errorTip.setVisibility(View.INVISIBLE);
            downloadingTip.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.VISIBLE);
            if (forcedUpgrade) {
                update.setEnabled(false);
                update.setText(R.string.btn_txt_downloading);
            } else {
                dismiss();
            }
            //回调点击事件
            if (buttonClickListener != null) {
                buttonClickListener.onButtonClick(OnButtonClickListener.UPDATE);
            }
            context.startService(new Intent(context, DownloadService.class));
        }
    }

    /**
     * 强制更新，点击进行安装
     */
    private void installApk() {
        ApkUtil.installApk(context, apk);
    }

    @Override
    public void start() {

    }

    @Override
    public void downloading(int max, int progress) {
        if (max != -1 && progressBar.getVisibility() == View.VISIBLE) {
            int curr = (int) (progress / (double) max * 100.0);
            progressBar.setProgress(curr);
            progressText.setText(curr + "%");
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void done(File apk) {
        //ReportUtil.nativeSceneReport("apk_download_over");
        this.apk = apk;
        update.setTag(install);
        update.setEnabled(true);
        update.setText(R.string.btn_txt_install);
        update.setVisibility(View.VISIBLE);
        errorTip.setVisibility(View.INVISIBLE);
        downloadingTip.setVisibility(View.GONE);
        ApkUtil.installApk(getContext(), apk);
//        if (forcedUpgrade) {
//            ApkUtil.installApk(getContext(), apk);
//        }
    }

    public void setInstallTag() {
        apk = FileUtil.createFile(manager.getDownloadPath(), manager.getApkName());
        update.setTag(install);
        update.setEnabled(true);
        update.setText(R.string.btn_txt_install);
    }

    public void setClickText(int curPro) {
        update.setText(curPro <= 0 ? R.string.btn_txt_update : R.string.btn_txt_continue);
        progressLayout.setVisibility(curPro <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void error(Exception e) {
        update.setEnabled(true);
        update.setText(R.string.btn_txt_continue);
        update.setVisibility(View.VISIBLE);
        errorTip.setVisibility(View.VISIBLE);
        downloadingTip.setVisibility(View.GONE);
        //ReportUtil.nativeSceneReport("apk_download_fail");
    }
}

