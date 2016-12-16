package com.midea.tonometer.mideaapplication.tools;

import android.widget.Toast;

import com.midea.tonometer.mideaapplication.app.MWApplication;

/**
 * Created by dingjikerbo on 2016/9/6.
 */
public class CommonUtils {

    public static void toast_s(String text) {
        Toast.makeText(MWApplication.getContext(), text, Toast.LENGTH_SHORT).show();
    }
    public static void toast_l(String text) {
        Toast.makeText(MWApplication.getContext(), text, Toast.LENGTH_LONG).show();
    }
}
