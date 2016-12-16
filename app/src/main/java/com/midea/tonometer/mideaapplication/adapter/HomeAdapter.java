package com.midea.tonometer.mideaapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.midea.tonometer.mideaapplication.R;
import com.midea.tonometer.mideaapplication.activity.HomeActivity;
import com.midea.tonometer.mideaapplication.model.DevInfo;
import com.midea.tonometer.mideaapplication.model.index.IndexInfo;
import com.midea.tonometer.mideaapplication.model.index.IndexList;
import com.midea.tonometer.mideaapplication.tools.Shareference;
import com.midea.tonometer.mideaapplication.tools.StringUtils;
import com.midea.tonometer.mideaapplication.tools.TextUtils;
import com.midea.tonometer.mideaapplication.view.MyTestIntoView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex_zhongjf on 2016-12-3.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    private List<DevInfo> devInfos;

    private OnDevInfoClickListener onDevInfoClickListener;

    private OnLineGetDateListener onLineGetDateListener;

    private Context context;


    public HomeAdapter(Context context, List<DevInfo> devInfos) {
        this.devInfos = devInfos;
        this.context = context;
    }

    public void setData(List<DevInfo> devInfos) {

        this.devInfos = devInfos;
    }

    @Override
    public int getItemCount() {
        return devInfos.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(
                context).inflate(R.layout.item_devices, parent,
                false));

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        if (TextUtils.isEmpte(devInfos.get(position).getDevName()))
            holder.tv_name.setText("未找到设备");
        else
            holder.tv_name.setText(devInfos.get(position).getDevName());


        if (devInfos.get(position).getDevIndex() == 0) {
            if (!devInfos.get(position).isOnLined()) {
                int index = saveIndexToLocal(devInfos.get(position));
                holder.tv_index.setText("No." + index);
                devInfos.get(position).setDevIndex(index);
            }
        } else
            holder.tv_index.setText("No." + devInfos.get(position).getDevIndex());

        holder.tv_mac.setText("MAC  " + devInfos.get(position).getDevMac());
        holder.testView_leak.setState(devInfos.get(position).getTestLeak());
        holder.testView_life.setState(devInfos.get(position).getTestLife());
        holder.testView_pressure.setState(devInfos.get(position).getTestPressure());
        holder.testView_static_pressure.setState(devInfos.get(position).getTestStaticPressure());
        holder.testView_sleep.setState(devInfos.get(position).getTestSleep());
        holder.testView_modelInfo.setState(devInfos.get(position).getTestModelInfo());


        if (StringUtils.isEmpty(devInfos.get(position).getDevSn()))
            holder.iv_link.setVisibility(View.GONE);
        else
            holder.iv_link.setVisibility(View.VISIBLE);


        if (getSignalLever(devInfos.get(position).getDevRSSI()) == 0)
            holder.iv_signal.setImageResource(R.mipmap.icon_signal_0);
        else if (getSignalLever(devInfos.get(position).getDevRSSI()) == 1)
            holder.iv_signal.setImageResource(R.mipmap.icon_signal_1);
        else if (getSignalLever(devInfos.get(position).getDevRSSI()) == 2)
            holder.iv_signal.setImageResource(R.mipmap.icon_signal_2);
        else if (getSignalLever(devInfos.get(position).getDevRSSI()) == 3)
            holder.iv_signal.setImageResource(R.mipmap.icon_signal_3);
        else if (getSignalLever(devInfos.get(position).getDevRSSI()) == 4)
            holder.iv_signal.setImageResource(R.mipmap.icon_signal_4);
        else if (getSignalLever(devInfos.get(position).getDevRSSI()) == 5)
            holder.iv_signal.setImageResource(R.mipmap.icon_signal_5);


        holder.iv_link.setOnClickListener(new ClickListener(holder.iv_link.getId(), position));

        holder.testView_leak.setOnClickListener(new ClickListener(holder.testView_leak.getId(), position));
        holder.testView_life.setOnClickListener(new ClickListener(holder.testView_life.getId(), position));
        holder.testView_pressure.setOnClickListener(new ClickListener(holder.testView_pressure.getId(), position));
        holder.testView_static_pressure.setOnClickListener(new ClickListener(holder.testView_static_pressure.getId(), position));
        holder.testView_sleep.setOnClickListener(new ClickListener(holder.testView_sleep.getId(), position));
        holder.testView_modelInfo.setOnClickListener(new ClickListener(holder.testView_modelInfo.getId(), position));

        if (onLineGetDateListener != null && !devInfos.get(position).isOnLined())
            onLineGetDateListener.onLine(position);

        holder.testView_leak.setState(devInfos.get(position).getTestLeak());

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_name;
        TextView tv_mac;
        TextView tv_index;
        ImageView iv_link;
        ImageView iv_signal;

        MyTestIntoView testView_leak;
        MyTestIntoView testView_life;
        MyTestIntoView testView_pressure;
        MyTestIntoView testView_static_pressure;
        MyTestIntoView testView_sleep;
        MyTestIntoView testView_modelInfo;

        public MyViewHolder(View view) {
            super(view);
            tv_name = (TextView) view.findViewById(R.id.item_devices_tv_name);
            tv_mac = (TextView) view.findViewById(R.id.item_devices_tv_mac);
            tv_index = (TextView) view.findViewById(R.id.item_devices_tv_index);
            iv_link = (ImageView) view.findViewById(R.id.item_devices_iv_link);
            iv_signal = (ImageView) view.findViewById(R.id.item_devices_iv_signal);


            testView_leak = (MyTestIntoView) view.findViewById(R.id.item_devices_testview_leak);
            testView_life = (MyTestIntoView) view.findViewById(R.id.item_devices_testview_life);
            testView_pressure = (MyTestIntoView) view.findViewById(R.id.item_devices_testview_pressure);
            testView_static_pressure = (MyTestIntoView) view.findViewById(R.id.item_devices_testview_static_pressure);
            testView_sleep = (MyTestIntoView) view.findViewById(R.id.item_devices_testview_sleep);
            testView_modelInfo = (MyTestIntoView) view.findViewById(R.id.item_devices_testview_modelinfo);

        }
    }

    private int getSignalLever(int rssi) {
        //return signalStrength;
        int lever = 0;
        if (rssi < -90) {
            lever = 1;
        } else if (rssi >= -90 && rssi < -80) {
            lever = 2;
        } else if (rssi >= -80 && rssi < -70) {
            lever = 3;
        } else if (rssi >= -70 && rssi < -60) {
            lever = 4;
        } else if (rssi >= -60 && rssi < -50) {
            lever = 5;
        } else if (rssi >= -50 && rssi < 0) {
            lever = 6;
        } else {
            lever = 1;
        }

        return lever;
    }

    private IndexList indexList;

    private int getIndexFromLocal(DevInfo devInfo) {
        indexList = (IndexList) Shareference.readObject(context, "INDEX_LIST");

        if (indexList == null) {
            indexList = new IndexList();
            if (indexList.getIndexInfos() == null)
                indexList.setIndexInfos(new ArrayList<IndexInfo>());
        } else {
            if (indexList.getIndexInfos() != null) {
                for (int i = 0; i < indexList.getIndexInfos().size(); i++) {
                    if (devInfo.getDevMac().equals(indexList.getIndexInfos().get(i).getMacAddress()))
                        return indexList.getIndexInfos().get(i).getIndex();
                }
            } else
                indexList.setIndexInfos(new ArrayList<IndexInfo>());
        }

        return -1;
    }

    private int saveIndexToLocal(DevInfo devInfo) {
        int pos = getIndexFromLocal(devInfo);
        if (pos == -1) {
            IndexInfo indexInfo = new IndexInfo();
            indexInfo.setMacAddress(devInfo.getDevMac());
            int index;
            if (indexList.getIndexInfos().size() == 0)
                index = 1;
            else
                index = indexList.getIndexInfos().size() + 1;
            indexInfo.setIndex(index);
            indexList.getIndexInfos().add(indexInfo);
            Shareference.saveObject(context, "INDEX_LIST", indexList);
            return index;
        }
        return pos;
    }

    public class ClickListener implements View.OnClickListener {//在这里我们重写了点击事件
        private int id, pos;

        public ClickListener(int id, int pos) {
            this.id = id;
            this.pos = pos;
        }

        @Override
        public void onClick(View view) {
            if (onDevInfoClickListener != null) {
                switch (id) {
                    case R.id.item_devices_iv_link:
                        onDevInfoClickListener.onLinkClick(pos);
                        break;
                    case R.id.item_devices_testview_leak:
                        onDevInfoClickListener.onTestLeakClick(pos, devInfos.get(pos));
                        break;
                    case R.id.item_devices_testview_life:
                        onDevInfoClickListener.onTestLifeClick(pos, devInfos.get(pos));
                        break;
                    case R.id.item_devices_testview_pressure:
                        onDevInfoClickListener.onTestPressureClick(pos, devInfos.get(pos));
                        break;
                    case R.id.item_devices_testview_static_pressure:
                        onDevInfoClickListener.onTestStaticPressureClick(pos, devInfos.get(pos));
                        break;
                    case R.id.item_devices_testview_sleep:
                        onDevInfoClickListener.onTestSleepClick(pos, devInfos.get(pos));
                        break;
                    case R.id.item_devices_testview_modelinfo:
                        onDevInfoClickListener.onTestModelInfoClick(pos, devInfos.get(pos));
                        break;
                }
            }
        }
    }

    public void setOnLineGetDateListener(OnLineGetDateListener onLineGetDateListener) {
        this.onLineGetDateListener = onLineGetDateListener;
    }

    public void setOnDevInfoClickListener(OnDevInfoClickListener onDevInfoClickListener) {
        this.onDevInfoClickListener = onDevInfoClickListener;
    }

    public interface OnLineGetDateListener {
        void onLine(int pos);
    }

    public interface OnDevInfoClickListener {

        void onLinkClick(int pos);

        void onTestLeakClick(int pos, DevInfo devInfo);

        void onTestLifeClick(int pos, DevInfo devInfo);

        void onTestPressureClick(int pos, DevInfo devInfo);

        void onTestStaticPressureClick(int pos, DevInfo devInfo);

        void onTestSleepClick(int pos, DevInfo devInfo);

        void onTestModelInfoClick(int pos, DevInfo devInfo);

    }

}
