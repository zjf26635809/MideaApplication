package com.midea.tonometer.mideaapplication.tools;

/**
 * author：EX_ZHONGJF on 2016-12-10 18:49
 * email：<jianfeng.zhong@partner.midea.com>
 */
public  class CMDConstant {

    public static  String LEAK_CALLBACK_RESULE_LEAK ="AA550800C801764B"; //反馈漏气
    public static  String LEAK_CALLBACK_RESULE_NO_LEAK ="AA550800C800764A";//没漏气

    public static  String LEAK_WRITE_START="AA550800C4007A4A";//漏气开始测试
    public static  String LEAK_WRITE_STOP ="AA550800C4107A5A";//漏气停止测试

    public static  String LEAK_NOTIFY_STOP ="AA550700C8FB38";//停止漏气反馈

    public static  String LIFE_WRITE_START="AA550800C5007B4A";//漏气开始测试
    public static  String LIFE_WRITE_STOP ="AA550800C5107B5A";//漏气停止测试


    public static  String PRESSURE_WRITE_START="AA550800C1007F4A";//压力开始测试
    public static  String PRESSURE_WRITE_STOP ="AA550800C1107F5A";//压力停止测试

    public static  String PRESSURE_NOTIFY_SET_SUCCESS ="AA550800C2007C4A";//压力校准值设置成功反馈
    public static  String PRESSURE_NOTIFY_SET_FAIL ="AA550800C2017C4B";//压力校准值设置失败反馈

    public static  String SLEEP_WRITE_START ="AA55070039FBC9";//深睡眠测试

    public static  String MODELINFO_WRITE_START ="AA550700C7FB37";//血压模组信息查询指令
}
