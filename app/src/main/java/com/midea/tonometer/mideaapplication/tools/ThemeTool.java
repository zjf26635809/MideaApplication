package com.midea.tonometer.mideaapplication.tools;

import android.app.Activity;

import com.midea.tonometer.mideaapplication.R;


/**
 * 改变主题工具类
 * 全局变量可以利用 类静态变量 或 preference
 */
public class ThemeTool {


    public static void changeTheme(Activity activity) {
        if (PrefUtils.isDarkMode()) {
            activity.setTheme(R.style.AppThemeDark);
        }
    }
}
