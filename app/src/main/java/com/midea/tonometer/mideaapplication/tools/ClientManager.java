package com.midea.tonometer.mideaapplication.tools;

import com.inuker.bluetooth.library.BluetoothClient;
import com.midea.tonometer.mideaapplication.app.MWApplication;

/**
 * Created by dingjikerbo on 2016/8/27.
 */
public class ClientManager {

    private static BluetoothClient mClient;

    public static BluetoothClient getClient() {
        if (mClient == null) {
            synchronized (ClientManager.class) {
                if (mClient == null) {
                    mClient = new BluetoothClient(MWApplication.getContext());
                }
            }
        }
        return mClient;
    }
}
