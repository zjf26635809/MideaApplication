package com.midea.tonometer.mideaapplication.view;


import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.midea.tonometer.mideaapplication.R;
import com.midea.tonometer.mideaapplication.model.DevInfo;
import com.midea.tonometer.mideaapplication.tools.MideaConstant;
import com.midea.tonometer.mideaapplication.tools.TextUtils;


public class MyTestContentView extends LinearLayout {


    private Context mContext;
    private View root = null;

    private DevInfo devInfo;

    private ScrollView mScrollView;
    private TextView mTv_name;
    private TextView mTv_value;
    private TextView mTv_des;
    private TextView mTv_des1;

    private TextView mTv_correct, mTv_content;
    private EditText mEd_correct;

    private TextView mTv_start;
    private TextView mTv_stop;
    private String testName;

    private OnContentClickListener onContentClickListener;
    private Handler handler = new Handler();

    public MyTestContentView(Context context, AttributeSet attrs) {
        // TODO Auto-generated constructor stub
        super(context, attrs);
        root = LayoutInflater.from(context).inflate(R.layout.view_test_content, this,
                true);
        initUI();
    }

    public void setDes(String str) {
        mTv_des.setText(str);
    }

    public void setDes1(String str) {
        mTv_des1.setText(str);
    }

    public void setCorrectLintener(OnClickListener onClickListener) {
        mTv_correct.setOnClickListener(onClickListener);
    }

    public int getCorrectValue() {
        if (TextUtils.isEmpte(mEd_correct.getText().toString()))
            return -1;
        else
            return Integer.valueOf(mEd_correct.getText().toString());
    }

    public void setValue(String value) {
        mTv_value.setText(value);
    }

    public void setOnContentClickListener(OnContentClickListener onContentClickListener) {
        this.onContentClickListener = onContentClickListener;
    }

    public void setType(String testType) {

        if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_LEAK)) {
            this.testName = "压力值: ";
            mTv_des.setVisibility(VISIBLE);
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_LIFE)) {
            this.testName = "压力值: ";
            mTv_des.setVisibility(VISIBLE);
            mTv_des1.setVisibility(VISIBLE);
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_PRESSURE)) {
            this.testName = "压力值: ";
            mTv_des.setVisibility(GONE);
            showCorrect();
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE)) {
            this.testName = "压力值: ";
            mTv_des.setVisibility(GONE);
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP)) {
            this.testName = "";
            mTv_name.setVisibility(GONE);
            mTv_stop.setVisibility(GONE);
            disappearDateView();
        } else if (testType.equals(MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO)) {
            this.testName = "";
            mTv_des.setVisibility(GONE);
            mTv_stop.setVisibility(GONE);
        }

        mTv_name.setText(testName);

    }

    public void writeIntoText(String str) {
        if (mTv_content != null)
            mTv_content.append(str + "\n");

        handler.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    private void initUI() {
        mScrollView = (ScrollView) root
                .findViewById(R.id.view_test_content_scroll);

        mTv_name = (TextView) root
                .findViewById(R.id.view_test_content_tv_name);
        mTv_value = (TextView) root
                .findViewById(R.id.view_test_content_tv_value);
        mTv_des = (TextView) root
                .findViewById(R.id.view_test_content_tv_des);
        mTv_des1 = (TextView) root
                .findViewById(R.id.view_test_content_tv_des1);

        mTv_content = (TextView) root
                .findViewById(R.id.view_test_content_tv_content);

        mTv_start = (TextView) root
                .findViewById(R.id.view_test_content_tv_start);
        mTv_start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onContentClickListener != null)
                    onContentClickListener.onStartAction();
            }
        });
        mTv_stop = (TextView) root
                .findViewById(R.id.view_test_content_tv_stop);
        mTv_stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onContentClickListener != null)
                    onContentClickListener.onStopAction();
            }
        });
    }

    private void disappearDateView() {
        mTv_name.setVisibility(GONE);
        mTv_value.setVisibility(GONE);
    }

    private void showCorrect() {
        root.findViewById(R.id.view_test_content_layout_correct).setVisibility(VISIBLE);

        mTv_correct = (TextView) root
                .findViewById(R.id.view_test_content_tv_correct);
        mEd_correct = (EditText) root
                .findViewById(R.id.view_test_content_ed_correct);
        mEd_correct.clearFocus();
    }


    public interface OnContentClickListener {
        void onStartAction();

        void onStopAction();
    }
}
