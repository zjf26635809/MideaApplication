package com.midea.tonometer.mideaapplication.interfaces;

/**
 * author：ex_zhongjf on 2016-12-7 14:56
 * email：<jianfeng.zhong@partner.midea.com>
 */
public interface BlueToothCommCallBack {
    void onConnectStart();

    void onConnectFail(String error);

    void onConnectSuccess();

    void onMsgReturn(String info);

}
