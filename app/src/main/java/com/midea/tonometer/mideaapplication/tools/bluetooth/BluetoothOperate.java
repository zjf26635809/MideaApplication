package com.midea.tonometer.mideaapplication.tools.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙BLE BluetoothLeService 对接操作类
 *
 * @author Administrator
 */
public class BluetoothOperate {

    private static final String TAG = "BluetoothOperat";
    // 蓝牙BLE接受通知特征
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    // 蓝牙BLE 读特征
    private BluetoothGattCharacteristic readCharacteristic;
    // 蓝牙BLE 写特征
    private BluetoothGattCharacteristic writeCharacteristic;
    // BLE蓝牙主服务类
    private BluetoothLeService mBluetoothLeService = null;
    private BluetoothGattCharacteristic mGattCharacteristics;
    private Context context;
    // 根据该状态判断 蓝牙链接
    private boolean mConnected = false;
    //要链接蓝牙设备地址
    private String mDeviceAddress;
    //主activity 传过来的handler
    private Handler mHandler;
    // 接受到RSSI what
    private static final int READRSSI = 0;
    //蓝牙链接上设备 what
    private static final int CONNECTED = 1;
    //蓝牙链接断开 what
    private static final int DISCONNECT = 2;

    private ReadRSSI readRssi;

    private boolean isReadRssi = false;

    private int pos;

    public BluetoothOperate init(Context context, Handler handler, int pos) {
        this.context = context;
        mHandler = handler;
        this.pos = pos;
        return this;
    }

    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }

    /**
     * 获取蓝牙服务类
     *
     * @return
     */
    public BluetoothLeService getmService() {
        return mBluetoothLeService;
    }

    /**
     * 设置蓝牙服务类
     *
     * @param mService
     */
    public void setmService(BluetoothLeService mService) {
        this.mBluetoothLeService = mService;
    }

    /**
     * 启动蓝牙服务
     */
    public void OpenBluetoothService() {

        // 判断蓝牙服务是否打开
        if (mBluetoothLeService == null) {
            // 创建绑定服务意图
            Intent gattServiceIntent = new Intent(context,
                    BluetoothLeService.class);
            // 绑定蓝牙服务
            context.bindService(gattServiceIntent, mServiceConnection,
                    Context.BIND_AUTO_CREATE);

            // 注册广播 并绑定多个action
            context.registerReceiver(mGattUpdateReceiver,
                    makeGattUpdateIntentFilter());
            if (mBluetoothLeService != null) {
                // 根据地址链接 并查看是否链接完成
                final boolean result = mBluetoothLeService
                        .connect(mDeviceAddress);
                Log.d(TAG, "Connect request result蓝牙是否链接上=" + result);
            }

        }

    }

    /**
     * 关闭蓝牙服务
     */
    public void CloseBluetoothService() {
        // TODO Auto-generated method stub
        isReadRssi = false;

        if (readRssi.isAlive()) {
            readRssi.interrupt();
        }

        // 解绑广播
        mBluetoothLeService.disconnect();
        context.unregisterReceiver(mGattUpdateReceiver);
        // 关闭蓝牙服务
        if (mBluetoothLeService != null) {
            // 解绑蓝牙服务
            context.unbindService(mServiceConnection);
            // 蓝夜服务释放
            mBluetoothLeService = null;
            readCharacteristic = null;
            writeCharacteristic = null;
            Log.d(TAG, "停掉所有蓝牙服务");


        }
    }

    // Code to manage Service lifecycle.
    // 代码 管理服务 生命周期
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            Log.d(TAG, "服务开始绑定");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            // 初始化蓝牙服务，并判断是否能实例化
            if (!mBluetoothLeService.initialize()) {
                // 不能初始化蓝牙
                Log.e(TAG, "Unable to initialize Bluetooth 不能初始化蓝牙");

            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            // 自动连接到装置上成功启动初始化
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.d(TAG, "停掉蓝牙服务");
        }
    };


    /**
     * IntentFilter对象负责过滤掉组件无法响应和处理的Intent，只将自己关心的Intent接收进来进行处理。
     * IntentFilter实行“白名单”管理，即只列出组件乐意接受的Intent，但IntentFilter只会过滤隐式Intent，
     * 显式的Intent会直接传送到目标组件。 Android组件可以有一个或多个IntentFilter，每个IntentFilter之间相互独立
     * ，只需要其中一个验证通过则可。除了用于过滤广播的IntentFilter可以在代码中创建外，
     * 其他的IntentFilter必须在AndroidManifest.xml文件中进行声明。
     *
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_READ_ALL_DATA);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        return intentFilter;
    }


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // 链接上GATT服务
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // 断掉链接GATT服务
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // 发现GATT服务
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read
    // or notification operations.
    // 从设备接收的数据。这是一个读或通知操作的结果

    // 通过服务控制不同的事件
    // ACTION_GATT_CONNECTED: 连接到GATT服务器。
    // ACTION_GATT_DISCONNECTED: 从GATT服务器断开连接。
    // ACTION_GATT_SERVICES_DISCOVERED: 发现GATT服务。
    // ACTION_DATA_AVAILABLE: 从设备接收到的数据。这可以是读或通知操作的结果。
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        private int time = 0, lost = 0, OK = 0;

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Log.d(TAG, "mGattUpdateReceiver 收到action " + action);
            // ACTION_GATT_CONNECTED: 连接到GATT服务器。
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                // 链接状态 连上
                mConnected = true;
                Log.d(TAG, "mGattUpdateReceiver 连接到GATT服务器。 ");
                isReadRssi = true;


                readRssi = new ReadRSSI();
                readRssi.start();
                // ACTION_GATT_DISCONNECTED: 从GATT服务器断开连接。
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                // 链接状态 断开
                mConnected = false;
                isReadRssi = false;
                Log.d(TAG, "mGattUpdateReceiver 从GATT服务器断开连接。 ");
                // ACTION_GATT_SERVICES_DISCOVERED: 发现GATT服务。
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                //从蓝牙服务中获取写特征 在发送数据的时候需要些特征
                writeCharacteristic = mBluetoothLeService.getWriteCharacteristic();
                Log.i(TAG, "writeCharacteristic是否等于null " + (writeCharacteristic == null));
                Log.d(TAG, "mGattUpdateReceiver 发现GATT服务。 ");


                // ACTION_DATA_AVAILABLE: 从设备接收到的数据。这可以是读或通知操作的结果。
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String str = intent
                        .getStringExtra(BluetoothLeService.ACTION_READ_DATA);
                Log.d(TAG, "mGattUpdateReceiver 从设备接收到的数据。这可以是读或通知操作的结果。");

            } else if (BluetoothLeService.ACTION_WRITE_DATA.equals(action)) {
                Log.d(TAG, "mGattUpdateReceiver 收到写的广播了的");
                String str = intent
                        .getStringExtra(BluetoothLeService.ACTION_WRITE_DATA);

                Log.d(TAG, str);
            } else if (BluetoothLeService.ACTION_READ_ALL_DATA.equals(action)) {
                byte[] read = intent.getByteArrayExtra(BluetoothLeService.ACTION_READ_ALL_DATA);
                StringBuffer str = new StringBuffer();
                for (byte item : read) {
                    str.append(String.format("%02X ", item));
                }
                Log.d(TAG, "mGattUpdateReceiver接收到整段数据了 数据" + str);
                //当设备开始扫描时。
            }
        }
    };

    public  void writeBlueToothCharacteristic(byte[] writeData){
        mBluetoothLeService.writeCharacteristic(writeCharacteristic,writeData);
    }

    public  void setCharacteristicNotification(boolean enable){
        mBluetoothLeService.setCharacteristicNotification(writeCharacteristic,enable);
    }

    /**
     * 读取蓝牙RSSi线程
     */

    class ReadRSSI extends Thread {
        int Rssi = 0;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            while (isReadRssi) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //如果读取蓝牙RSSi回调成功
                if (mBluetoothLeService.getRssiVal()) {
                    //获取已经读到的RSSI值
                    Rssi = BluetoothLeService.getBLERSSI();

                    mHandler.obtainMessage(pos, Rssi).sendToTarget();
                }

            }

        }
    }

    //设置要链接蓝牙设备的地址
    public String getmDeviceAddress() {
        return mDeviceAddress;
    }

}
