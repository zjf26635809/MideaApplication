package com.midea.tonometer.mideaapplication.model.http;

import java.util.List;

/**
 * author：EX_ZHONGJF on 2016-12-12 00:44
 * email：<jianfeng.zhong@partner.midea.com>
 */
public class DevListResult {

    private String ret;
    private String enMsg;
    private String zhMsg;
    private String detailMsg;
    private String start;
    private String rows;
    private String totalCount;
    private List<DevDetail> list;


    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
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

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public List<DevDetail> getDevDetails() {
        return list;
    }

    public void setDevDetails(List<DevDetail> devDetails) {
        this.list = devDetails;
    }


    @Override
    public String toString() {
        return "DevListResult{" +
                "ret='" + ret + '\'' +
                ", enMsg='" + enMsg + '\'' +
                ", zhMsg='" + zhMsg + '\'' +
                ", detailMsg='" + detailMsg + '\'' +
                ", start='" + start + '\'' +
                ", rows='" + rows + '\'' +
                ", totalCount='" + totalCount + '\'' +
                ", devDetails=" + list +
                '}';
    }
}
