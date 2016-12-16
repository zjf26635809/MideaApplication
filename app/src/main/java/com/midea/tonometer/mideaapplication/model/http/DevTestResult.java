package com.midea.tonometer.mideaapplication.model.http;

import java.util.List;

/**
 * author：EX_ZHONGJF on 2016-12-12 00:44
 * email：<jianfeng.zhong@partner.midea.com>
 */
public class DevTestResult {

    private int ret;
    private String enMsg;
    private String zhMsg;
    private String detailMsg;
    private String result;


    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getEnMsg() {
        return enMsg;
    }

    public void setEnMsg(String enMsg) {
        this.enMsg = enMsg;
    }

    public String getZhMsg() {
        return zhMsg;
    }

    public void setZhMsg(String zhMsg) {
        this.zhMsg = zhMsg;
    }

    public String getDetailMsg() {
        return detailMsg;
    }

    public void setDetailMsg(String detailMsg) {
        this.detailMsg = detailMsg;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "DevTestResult{" +
                "ret=" + ret +
                ", enMsg='" + enMsg + '\'' +
                ", zhMsg='" + zhMsg + '\'' +
                ", detailMsg='" + detailMsg + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
