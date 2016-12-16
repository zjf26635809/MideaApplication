package com.midea.tonometer.mideaapplication.model.index;

import java.io.Serializable;

/**
 * author：ex_zhongjf on 2016-12-12 13:42
 * email：<jianfeng.zhong@partner.midea.com>
 */
public class IndexInfo implements Serializable {

    private String macAddress;
    private int index;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "IndexInfo{" +
                "macAddress='" + macAddress + '\'' +
                ", index=" + index +
                '}';
    }
}
