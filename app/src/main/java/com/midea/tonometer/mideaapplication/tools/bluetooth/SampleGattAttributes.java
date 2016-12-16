package com.midea.tonometer.mideaapplication.tools.bluetooth;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 * 该类包含用于示范标准GATT属性的一个小的子集
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
   public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
  public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    //hjt给的UUID
    public static String ISSC_SERVICE_UUID  = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String ISSC_CHAR_RX_UUID  = "0000fff1-0000-1000-8000-00805f9b34fb";   //Notify
    public static String ISSC_CHAR_TX_UUID  = "0000fff2-0000-1000-8000-00805f9b34fb";   //Write


    static {
        // Sample Services.
        //例子 服务
//        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
//        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(ISSC_SERVICE_UUID, "ISSC SERVICE");
        // Sample Characteristics.
        //例子 特征
//        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(ISSC_CHAR_RX_UUID, "ISSC_char_RX Notify");
        attributes.put(ISSC_CHAR_TX_UUID, "ISSC_char_TX");
    }
    //在记录的UUID查找对应名字的服务
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
    /**
     * 通过UUID判断是否在记录中
     * @param uuid UUID
     * @return 在记录中返回true 不在记录中 返回false
     */
    public static boolean  lookupUUID(String uuid) {
        String name = attributes.get(uuid);
        return name == null ? false : true;
    }
    
}
