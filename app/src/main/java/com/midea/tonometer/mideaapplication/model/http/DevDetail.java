package com.midea.tonometer.mideaapplication.model.http;

/**
 * author：EX_ZHONGJF on 2016-12-12 00:44
 * email：<jianfeng.zhong@partner.midea.com>
 */
public class DevDetail {
    private int id;
    private String createDate;
    private String updateDate;
    private String macAddress;
    private String deviceCode;
    private String testItem;
    private String testTimes;
    private String testResult;
    private String sn;
    private String testDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getTestItem() {
        return testItem;
    }

    public void setTestItem(String testItem) {
        this.testItem = testItem;
    }

    public String getTestTimes() {
        return testTimes;
    }

    public void setTestTimes(String testTimes) {
        this.testTimes = testTimes;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    @Override
    public String toString() {
        return "DevDetail{" +
                "id=" + id +
                ", createDate='" + createDate + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", deviceCode='" + deviceCode + '\'' +
                ", testItem='" + testItem + '\'' +
                ", testTimes='" + testTimes + '\'' +
                ", testResult='" + testResult + '\'' +
                ", sn='" + sn + '\'' +
                ", testDate='" + testDate + '\'' +
                '}';
    }
}
