package com.midea.tonometer.mideaapplication.app;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothContext;

/**
 * Created by ex_zhongjf on 2016-12-3.
 */

public class MWApplication extends Application {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        BluetoothContext.set(this);
    }


}
