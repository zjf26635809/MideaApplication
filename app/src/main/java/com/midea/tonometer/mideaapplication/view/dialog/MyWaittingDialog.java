package com.midea.tonometer.mideaapplication.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.midea.tonometer.mideaapplication.R;


public class MyWaittingDialog extends Dialog {

    private String tip;

    public MyWaittingDialog(Context context) {
        super(context, R.style.MyDialogStyle);
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.CENTER);
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.PopWindowAnimStyle);

        // TODO Auto-generated constructor stub
    }

    public void setTip(String tip) {
        this.tip = tip;
    }


    public void setOnTouchOutsideCanceled(boolean canceledOnTouchOutside) {
        this.setCancelable(canceledOnTouchOutside); // 是否可以按“返回键”消失
        this.setCanceledOnTouchOutside(canceledOnTouchOutside); // 点击加载框以外的区域
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);

        this.setCancelable(true); // 是否可以按“返回键”消失
        this.setCanceledOnTouchOutside(false); // 点击加载框以外的区域

        ((TextView) findViewById(R.id.dialog_loading_tip)).setText(tip);// 设置加载信息
    }


    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ((TextView) findViewById(R.id.dialog_loading_tip)).setText(tip);// 设置加载信息
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

}