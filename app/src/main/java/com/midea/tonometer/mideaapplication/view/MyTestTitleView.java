package com.midea.tonometer.mideaapplication.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.midea.tonometer.mideaapplication.R;
import com.midea.tonometer.mideaapplication.model.DevInfo;
import com.midea.tonometer.mideaapplication.tools.MideaConstant;


public class MyTestTitleView extends LinearLayout {


    private Context mContext;
    private View root = null;

    private DevInfo devInfo;

    private TextView mTv_name;
    private TextView mTv_mac;
    private TextView mTv_bound;
    private ImageView mIv_state;

    private String testName;
    private String testState;


    public MyTestTitleView(Context context, AttributeSet attrs) {
        // TODO Auto-generated constructor stub
        super(context, attrs);
        root = LayoutInflater.from(context).inflate(R.layout.view_test_title, this,
                true);
        initUI();
    }

    public void setDevInfo(String testType, DevInfo devInfo) {
        this.devInfo = devInfo;

        if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_LEAK)) {
            this.testName = "漏气测试";
            testState = devInfo.getTestLeak();
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_LIFE)) {
            this.testName = "寿命测试";
            testState = devInfo.getTestLife();
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_PRESSURE)) {
            this.testName = "压力校准";
            testState = devInfo.getTestPressure();
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE)) {
            this.testName = "静态压力测试";
            testState = devInfo.getTestStaticPressure();
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP)) {
            this.testName = "深睡眠测试";
            testState = devInfo.getTestSleep();
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO)) {
            this.testName = "模组信息测试";
            testState = devInfo.getTestModelInfo();
        }

        mTv_name.setText(testName);
        mTv_mac.setText("MAC " + devInfo.getDevMac());
        setState(testState);
    }

    public void setBoundSN(boolean isBoundSN) {
        if (isBoundSN)
            mTv_bound.setText("已绑定SN");
        else
            mTv_bound.setText("绑定SN失败");

    }

    public void setBoundDes(String str) {
            mTv_bound.setText(str);
    }

    private void initUI() {

        mTv_name = (TextView) root
                .findViewById(R.id.view_test_title_tv_name);
        mTv_bound = (TextView) root
                .findViewById(R.id.view_test_title_tv_bound);
        mTv_mac = (TextView) root
                .findViewById(R.id.view_test_title_tv_mac);
        mIv_state = (ImageView) root
                .findViewById(R.id.view_test_title_iv_state);


    }

    public void setState(String state) {
        if (state == null)
            mIv_state.setVisibility(View.INVISIBLE);
        else if (state.equals(MideaConstant.MIDEA_TEST_RESULT_FINISH))
            mIv_state.setImageResource(R.mipmap.icon_finish);
        else if (state.equals(MideaConstant.MIDEA_TEST_RESULT_UNFINISH))
            mIv_state.setImageResource(R.mipmap.icon_finish_un);
        else
            mIv_state.setVisibility(View.INVISIBLE);
    }

}
