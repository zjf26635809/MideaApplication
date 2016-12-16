package com.midea.tonometer.mideaapplication.model;


import java.io.Serializable;

/**
 * Created by ex_zhongjf on 2016-12-3.
 */

public class DevInfo implements Serializable {

    private String devName;
    private String devMac;
    private String devState;
    private int devRSSI;
    private int devIndex;
    private boolean isBundle;
    private String devSn;
    private String testLeak;
    private String testLife;
    private String testPressure;
    private String testStaticPressure;
    private String testSleep;
    private String testModelInfo;

    private boolean isOnLined = false;

    public boolean isOnLined() {
        return isOnLined;
    }

    public void setOnLined(boolean onLined) {
        isOnLined = onLined;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevMac() {
        return devMac;
    }

    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }

    public String getDevState() {
        return devState;
    }

    public void setDevState(String devState) {
        this.devState = devState;
    }

    public int getDevRSSI() {
        return devRSSI;
    }

    public void setDevRSSI(int devRSSI) {
        this.devRSSI = devRSSI;
    }

    public int getDevIndex() {
        return devIndex;
    }

    public void setDevIndex(int devIndex) {
        this.devIndex = devIndex;
    }

    public boolean isBundle() {
        return isBundle;
    }

    public void setBundle(boolean bundle) {
        isBundle = bundle;
    }

    public String getDevSn() {
        return devSn;
    }

    public void setDevSn(String devSn) {
        this.devSn = devSn;
    }

    public String getTestLeak() {
        return testLeak;
    }

    public void setTestLeak(String testLeak) {
        this.testLeak = testLeak;
    }

    public String getTestLife() {
        return testLife;
    }

    public void setTestLife(String testLife) {
        this.testLife = testLife;
    }

    public String getTestPressure() {
        return testPressure;
    }

    public void setTestPressure(String testPressure) {
        this.testPressure = testPressure;
    }

    public String getTestStaticPressure() {
        return testStaticPressure;
    }

    public void setTestStaticPressure(String testStaticPressure) {
        this.testStaticPressure = testStaticPressure;
    }

    public String getTestSleep() {
        return testSleep;
    }

    public void setTestSleep(String testSleep) {
        this.testSleep = testSleep;
    }

    public String getTestModelInfo() {
        return testModelInfo;
    }

    public void setTestModelInfo(String testModelInfo) {
        this.testModelInfo = testModelInfo;
    }

}
