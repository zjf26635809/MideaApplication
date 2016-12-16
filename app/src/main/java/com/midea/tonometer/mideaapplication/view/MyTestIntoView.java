package com.midea.tonometer.mideaapplication.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.midea.tonometer.mideaapplication.R;
import com.midea.tonometer.mideaapplication.tools.MideaConstant;


public class MyTestIntoView extends LinearLayout {


    private Context mContext;
    private View root = null;

    private ImageView mIv_state;
    private TextView mTv_name;

    private String textValue;
    private String state;

    public MyTestIntoView(Context context, AttributeSet attrs) {
        // TODO Auto-generated constructor stub
        super(context, attrs);
        root = LayoutInflater.from(context).inflate(R.layout.view_test_into, this,
                true);
        init(attrs);
        initUI();
    }

    private void init(AttributeSet attrs) {

        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.MyTest);
        textValue = t.getString(R.styleable.MyTest_textValue);
        state = t.getString(R.styleable.MyTest_state);
        t.recycle();
    }


    private void initUI() {

        mIv_state = (ImageView) root
                .findViewById(R.id.view_test_into_iv);
        mTv_name = (TextView) root
                .findViewById(R.id.view_test_into_tv);
        mTv_name.setText(textValue);
        setState(state);

    }

    public void setState(String state) {
        Log.d("haha", "state=" + state);
        if (state == null)
            mIv_state.setVisibility(View.INVISIBLE);
        else if (state.equals(MideaConstant.MIDEA_TEST_RESULT_FINISH)) {
            mIv_state.setVisibility(VISIBLE);
            mIv_state.setImageResource(R.mipmap.icon_finish);
        } else if (state.equals(MideaConstant.MIDEA_TEST_RESULT_UNFINISH)) {
            mIv_state.setVisibility(VISIBLE);
            mIv_state.setImageResource(R.mipmap.icon_finish_un);
        }

    }


}
