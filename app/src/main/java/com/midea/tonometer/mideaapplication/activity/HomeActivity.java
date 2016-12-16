package com.midea.tonometer.mideaapplication.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.Code;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleReadRssiResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.midea.tonometer.mideaapplication.R;
import com.midea.tonometer.mideaapplication.activity.base.BaseActivity;
import com.midea.tonometer.mideaapplication.activity.test.TestActivity;
import com.midea.tonometer.mideaapplication.adapter.HomeAdapter;
import com.midea.tonometer.mideaapplication.model.DevInfo;
import com.midea.tonometer.mideaapplication.model.http.DevDetail;
import com.midea.tonometer.mideaapplication.model.http.DevListResult;
import com.midea.tonometer.mideaapplication.model.index.IndexInfo;
import com.midea.tonometer.mideaapplication.model.index.IndexList;
import com.midea.tonometer.mideaapplication.okhttp.IResponseCallback;
import com.midea.tonometer.mideaapplication.okhttp.OkClient;
import com.midea.tonometer.mideaapplication.okhttp.bean.OkError;
import com.midea.tonometer.mideaapplication.okhttp.bean.RequestParam;
import com.midea.tonometer.mideaapplication.tools.ClientManager;
import com.midea.tonometer.mideaapplication.tools.DensityUtil;
import com.midea.tonometer.mideaapplication.tools.MideaConstant;
import com.midea.tonometer.mideaapplication.tools.Shareference;
import com.midea.tonometer.mideaapplication.tools.StringUtils;
import com.midea.tonometer.mideaapplication.view.MyTestIntoView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class HomeActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, HomeAdapter.OnDevInfoClickListener, HomeAdapter.OnLineGetDateListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_TEST = 2;
    private final static int REQUEST_SCANNIN_GREQUEST_CODE = 3;

    private RelativeLayout mLayout_found;

    private BluetoothAdapter mBluetoothAdapter;

    private HomeAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private List<SearchResult> mDevices = new ArrayList<>();
    private List<DevInfo> devInfos = new ArrayList<>();

    private boolean StopGetRSSI = false;
    private boolean isSearchDev = false;

    private int index = 0;

    private String mideaTestType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initBlueToothPermission();

        initUI();
        searchDevice();
    }

    private void initCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        11);
            } else {
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_SCANNIN_GREQUEST_CODE);
            }
        }
    }

    private void initBlueToothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this, permissions, 10);
                return;
            }
        }

        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "当前手机不支持ble ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (SearchResult searchResult : mDevices) {
            ClientManager.getClient().disconnect(searchResult.getAddress());
        }
        stopGetRSSI();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_SCANNIN_GREQUEST_CODE);

            }
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            searchDevice();
        } else if (requestCode == REQUEST_TEST) {

            searchDevice();
            /**
             if (data == null)
             return;
             int pos = resultCode;

             int fristPos = linearLayoutManager.findFirstVisibleItemPosition();
             int lastPos = linearLayoutManager.findLastVisibleItemPosition();

             String result = data.getStringExtra("TEST_RESULT");
             String sn = data.getStringExtra("BOUND_SN");

             if (pos >= fristPos && pos <= lastPos) {
             View view = mRecyclerView.getChildAt(pos);
             if (view == null)
             return;
             if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LEAK))
             ((MyTestIntoView) mRecyclerView.getChildAt(pos).findViewById(R.id.item_devices_testview_leak)).setState(result);
             if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LIFE))
             ((MyTestIntoView) mRecyclerView.getChildAt(pos).findViewById(R.id.item_devices_testview_life)).setState(result);
             if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_PRESSURE))
             ((MyTestIntoView) mRecyclerView.getChildAt(pos).findViewById(R.id.item_devices_testview_pressure)).setState(result);
             if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE))
             ((MyTestIntoView) mRecyclerView.getChildAt(pos).findViewById(R.id.item_devices_testview_static_pressure)).setState(result);
             if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP))
             ((MyTestIntoView) mRecyclerView.getChildAt(pos).findViewById(R.id.item_devices_testview_sleep)).setState(result);
             if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO))
             ((MyTestIntoView) mRecyclerView.getChildAt(pos).findViewById(R.id.item_devices_testview_modelinfo)).setState(result);

             }**/
        } else if (requestCode == REQUEST_SCANNIN_GREQUEST_CODE)

        {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                //显示扫描到的内容:bundle.getString("result")
                String sn = bundle.getString("result");
                for (int i = 0; i < devInfos.size(); i++) {
                    if (sn.equals(devInfos.get(i).getDevSn())) {
                        DevInfo scanDevInfo = devInfos.get(i);
                        devInfos.clear();
                        devInfos.add(scanDevInfo);
                        mAdapter.setData(devInfos);
                        mAdapter.notifyDataSetChanged();
                        ClientManager.getClient().connect(scanDevInfo.getDevMac(), new BleConnectResponse() {//链接蓝牙
                            @Override
                            public void onResponse(int code, BleGattProfile data) {
                                startGetRSSI();
                            }
                        });
                    }
                }

            }

        }

    }


    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(5000, 1).build();

        ClientManager.getClient().search(request, mSearchResponse);
    }


    private final SearchResponse mSearchResponse = new SearchResponse() {

        int oldDevNum = 0;

        @Override
        public void onSearchStarted() {
            BluetoothLog.w("MainActivity.onSearchStarted");
            isSearchDev = true;
            devInfos.clear();
            mDevices.clear();
            mAdapter.setData(devInfos);
            mAdapter.notifyDataSetChanged();
            index = 0;
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());


            if (!mDevices.contains(device) || mDevices.size() == 0) {
                if (!device.getName().contains("MW-BPM"))
                    return;

                mDevices.add(device);
                filterDevInfos(device);
                mAdapter.setData(devInfos);
                mAdapter.notifyDataSetChanged();

                if (mDevices.size() < 6) {
                    ClientManager.getClient().connect(device.getAddress(), new BleConnectResponse() {//链接蓝牙
                        @Override
                        public void onResponse(int code, BleGattProfile data) {

                        }
                    });
                }
            }

            if (mDevices.size() > 0 && oldDevNum != mDevices.size()) {
                addTipView(mDevices.size());
            }
            oldDevNum = mDevices.size();
        }

        @Override
        public void onSearchStopped() {
            BluetoothLog.w("MainActivity.onSearchStopped");
            isSearchDev = false;
            if (devInfos.size() != 0) {
                startGetRSSI();
                StopGetRSSI = false;
            }
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onSearchCanceled() {
            BluetoothLog.w("MainActivity.onSearchCanceled");
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().stopSearch();
    }


    @Override
    public void onRefresh() {

        swipeRefreshLayout.setRefreshing(false);

        if (isSearchDev)
            ClientManager.getClient().stopSearch();
        for (SearchResult searchResult : mDevices)
            ClientManager.getClient().disconnect(searchResult.getAddress());

        searchDevice();
    }


    @Override
    public void onLine(int pos) {
        startGetDevInfo(pos, devInfos.get(pos));
    }

    @Override
    public void onLinkClick(int pos) {

    }

    @Override
    public void onTestLeakClick(int pos, DevInfo devInfo) {

        getRSSIHandler.removeMessages(0);
        StopGetRSSI = true;
        Intent it = new Intent(this, TestActivity.class);
        it.putExtra("DEVINFO", devInfo);
        it.putExtra("TYPE", mideaTestType = MideaConstant.MIDEA_TEST_TYPE_LEAK);
        it.putExtra("POSITION", pos);
        startActivityForResult(it, REQUEST_TEST);

    }

    @Override
    public void onTestLifeClick(int pos, DevInfo devInfo) {
        getRSSIHandler.removeMessages(0);
        StopGetRSSI = true;

        Intent it = new Intent(this, TestActivity.class);
        it.putExtra("DEVINFO", devInfo);
        it.putExtra("TYPE", mideaTestType = MideaConstant.MIDEA_TEST_TYPE_LIFE);
        it.putExtra("POSITION", pos);
        startActivityForResult(it, REQUEST_TEST);
    }

    @Override
    public void onTestPressureClick(int pos, DevInfo devInfo) {
        getRSSIHandler.removeMessages(0);
        StopGetRSSI = true;

        Intent it = new Intent(this, TestActivity.class);
        it.putExtra("DEVINFO", devInfo);
        it.putExtra("TYPE", mideaTestType = MideaConstant.MIDEA_TEST_TYPE_PRESSURE);
        it.putExtra("POSITION", pos);
        startActivityForResult(it, REQUEST_TEST);
    }

    @Override
    public void onTestStaticPressureClick(int pos, DevInfo devInfo) {
        getRSSIHandler.removeMessages(0);
        StopGetRSSI = true;

        Intent it = new Intent(this, TestActivity.class);
        it.putExtra("DEVINFO", devInfo);
        it.putExtra("TYPE", mideaTestType = MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE);
        it.putExtra("POSITION", pos);
        startActivityForResult(it, REQUEST_TEST);
    }

    @Override
    public void onTestSleepClick(int pos, DevInfo devInfo) {
        getRSSIHandler.removeMessages(0);
        StopGetRSSI = true;

        Intent it = new Intent(this, TestActivity.class);
        it.putExtra("DEVINFO", devInfo);
        it.putExtra("TYPE", mideaTestType = MideaConstant.MIDEA_TEST_TYPE_SLEEP);
        it.putExtra("POSITION", pos);
        startActivityForResult(it, REQUEST_TEST);
    }

    @Override
    public void onTestModelInfoClick(int pos, DevInfo devInfo) {
        getRSSIHandler.removeMessages(0);
        StopGetRSSI = true;

        Intent it = new Intent(this, TestActivity.class);
        it.putExtra("DEVINFO", devInfo);
        it.putExtra("TYPE", mideaTestType = MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO);
        it.putExtra("POSITION", pos);
        startActivityForResult(it, REQUEST_TEST);
    }

    private LinearLayoutManager linearLayoutManager;

    private void initUI() {
        findViewById(R.id.include_title_iv_back).setVisibility(View.GONE);

        mLayout_found = (RelativeLayout) findViewById(R.id.activity_home_layout_found);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_home_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = findView(R.id.activity_home_recyclerview);

        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter = new HomeAdapter(this, devInfos));
        mAdapter.setOnDevInfoClickListener(this);
        mAdapter.setOnLineGetDateListener(this);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        findViewById(R.id.include_title_tv_sao).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initCameraPermission();
            }
        });

    }


    private void addTipView(int size) {
        if (mLayout_found.getChildAt(0) == null) {
            TextView textView = new TextView(this);
            textView.setText(" 共发现" + size + "设备");
            textView.setTextColor(this.getResources().getColor(R.color.list_background_dark));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setPadding(DensityUtil.px2dip(this, 10), DensityUtil.px2dip(this, 5), DensityUtil.px2dip(this, 10), DensityUtil.px2dip(this, 5));
            textView.setLayoutParams(params);
            mLayout_found.addView(textView, 0);
        } else {
            ((TextView) mLayout_found.getChildAt(0)).setText(" 共发现" + size + "设备");
        }
        closeTipViewHandler.sendEmptyMessageDelayed(0, 8000);

    }

    private Handler closeTipViewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                View view = mLayout_found.getChildAt(0);
                if (view != null)
                    ((TextView) view).setText("");
                closeTipViewHandler.sendEmptyMessageDelayed(1, 500);
            } else if (msg.what == 1)
                mLayout_found.removeAllViews();
        }
    };

    private int rssiPos = 0;
    private boolean isStopGetRSSI = false;

    private void startGetRSSI() {
        isStopGetRSSI = false;
        getRSSIHandler.sendEmptyMessageDelayed(rssiPos, 100);
    }

    private void stopGetRSSI() {
        isStopGetRSSI = true;
    }


    private void startGetDevInfo(int pos, DevInfo devInfo) {
        Log.d("NET", devInfo.getDevMac() + "  正在连入服务器");
        RequestParam requestParam = new RequestParam(0, "/query?getBloodpressureList");
        requestParam.putHeader("X-Recipe-AppId", "1000");
        requestParam.putHeader("X-Recipe-AppKey", "uh79l871a38adf41aab3be3837e39123");

        requestParam.put("macAddress", devInfo.getDevMac());
        requestParam.put("deviceCode", "");
        requestParam.put("testItem", "");
        requestParam.put("testResult", "");
        requestParam.put("sn", "");
        requestParam.put("testTimes", "1");
        requestParam.put("start", 0);
        requestParam.put("rows", 10);


        OkClient.request(requestParam, DevListResult.class, new ResponseCallback(pos));
    }

    private class ResponseCallback implements IResponseCallback {
        private int pos;

        ResponseCallback(int pos) {
            this.pos = pos;
        }

        @Override
        public void onSuccess(int tag, Object object) {

            if (isFinishing() || isDestroyed())
                return;

            if (pos >= devInfos.size())
                return;


            DevListResult devListResult = (DevListResult) object;
            Log.v("NET", devInfos.get(pos).getDevMac() + " 返回结果===" + devListResult.toString());
            if (devListResult != null)
                if (devListResult.getDevDetails() != null) {
                    devInfos.get(pos).setOnLined(true);

                    for (int i = 0; i < devListResult.getDevDetails().size(); i++) {
                        DevDetail devDetail = devListResult.getDevDetails().get(i);
                        Log.v("NET", devInfos.get(pos).getDevMac() + " 返回结果===" + devDetail.toString());

                        pairTestItem(pos, devDetail);

                        if (!StringUtils.isEmpty(devDetail.getDeviceCode()))
                            if (!devDetail.getDeviceCode().equals("0")) {
                                ((TextView) mRecyclerView.getChildAt(pos).findViewById(R.id.item_devices_tv_index)).setText("No." + devDetail.getDeviceCode());
                                devInfos.get(pos).setDevIndex(Integer.valueOf(devDetail.getDeviceCode()));
                            }
                        mAdapter.setData(devInfos);
                    }
                }
        }

        @Override
        public void onError(int tag, OkError error) {

            if (isFinishing() || isDestroyed())
                return;
            if (pos >= devInfos.size())
                return;

            Log.v("NET", devInfos.get(pos).getDevMac() + " 返回错误===" + error.getMsg());
        }
    }


    private Handler getRSSIHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            if (devInfos.size() == 0)
                return;

            if (rssiPos > devInfos.size() - 1) rssiPos = 0;
            final String mac = devInfos.get(rssiPos).getDevMac();

            ClientManager.getClient().readRssi(mac, new BleReadRssiResponse() {//链接成功之后获取RSSI
                @Override
                public void onResponse(int code, Integer data) {
                    if (StopGetRSSI)
                        return;

                    pairSignalImage(rssiPos, data); //对应的更改图片

                    rssiPos = rssiPos + 1;
                    if (rssiPos > devInfos.size() - 1) rssiPos = 0;
                    if (!isStopGetRSSI)
                        startGetRSSI();
                }
            });
        }
    };

    private boolean filterDevInfos(SearchResult btDevice) {
        Log.d("TAG", "扫描到蓝牙设备 蓝牙名字=" + btDevice.getName() + "MAC地址="
                + btDevice.getAddress());
        if (devInfos.size() == 0) {
            devInfos.add(pairDevInfo(btDevice));
            return true;
        }
        boolean isExist = false;
        for (int i = 0; i < devInfos.size(); i++) {
            if (devInfos.get(i).getDevMac().equals(btDevice.getAddress())) {
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            devInfos.add(pairDevInfo(btDevice));
            return true;
        }
        return false;
    }

    private DevInfo pairDevInfo(SearchResult btDevice) {
        DevInfo devInfo = new DevInfo();
        devInfo.setDevName(btDevice.getName());
        devInfo.setDevMac(btDevice.getAddress());
        devInfo.setDevRSSI(btDevice.getRssi());
        devInfo.setDevIndex(0);

        return devInfo;
    }

    private void pairTestItem(int pos, DevDetail devDetail) {
        int fristPos = linearLayoutManager.findFirstVisibleItemPosition();
        int lastPos = linearLayoutManager.findLastVisibleItemPosition();

        if (pos >= fristPos && pos <= lastPos) {
            View view = mRecyclerView.getChildAt(pos);
            if (view == null)
                return;

            if (devDetail.getTestItem().equals(MideaConstant.MIDEA_TEST_TYPE_LEAK)) {
                devInfos.get(pos).setTestLeak(devDetail.getTestResult());
                ((MyTestIntoView) view.findViewById(R.id.item_devices_testview_leak)).setState(devDetail.getTestResult());

            }
            if (devDetail.getTestItem().equals(MideaConstant.MIDEA_TEST_TYPE_LIFE)) {
                devInfos.get(pos).setTestLife(devDetail.getTestResult());
                ((MyTestIntoView) view.findViewById(R.id.item_devices_testview_life)).setState(devDetail.getTestResult());
            }
            if (devDetail.getTestItem().equals(MideaConstant.MIDEA_TEST_TYPE_PRESSURE)) {
                devInfos.get(pos).setTestPressure(devDetail.getTestResult());
                ((MyTestIntoView) view.findViewById(R.id.item_devices_testview_pressure)).setState(devDetail.getTestResult());

            }
            if (devDetail.getTestItem().equals(MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE)) {
                devInfos.get(pos).setTestStaticPressure(devDetail.getTestResult());
                ((MyTestIntoView) view.findViewById(R.id.item_devices_testview_static_pressure)).setState(devDetail.getTestResult());

            }
            if (devDetail.getTestItem().equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP)) {
                devInfos.get(pos).setTestSleep(devDetail.getTestResult());
                ((MyTestIntoView) view.findViewById(R.id.item_devices_testview_sleep)).setState(devDetail.getTestResult());

            }
            if (devDetail.getTestItem().equals(MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO)) {
                devInfos.get(pos).setTestModelInfo(devDetail.getTestResult());
                ((MyTestIntoView) view.findViewById(R.id.item_devices_testview_modelinfo)).setState(devDetail.getTestResult());

            }
            devInfos.get(pos).setDevSn(devDetail.getSn());
            if (!StringUtils.isEmpty(devDetail.getSn()))
                view.findViewById(R.id.item_devices_iv_link).setVisibility(View.VISIBLE);
            else
                view.findViewById(R.id.item_devices_iv_link).setVisibility(View.GONE);
        }
    }

    private void pairSignalImage(int position, int rssi) {

        int fristPos = linearLayoutManager.findFirstVisibleItemPosition();
        int lastPos = linearLayoutManager.findLastVisibleItemPosition();

        if (position >= fristPos && position <= lastPos) {
            View view = mRecyclerView.getChildAt(position);
            if (view == null)
                return;

            if (getSignalLever(rssi) == 0)
                ((ImageView) view.findViewById(R.id.item_devices_iv_signal)).setImageResource(R.mipmap.icon_signal_0);
            else if (getSignalLever(rssi) == 1)
                ((ImageView) view.findViewById(R.id.item_devices_iv_signal)).setImageResource(R.mipmap.icon_signal_1);
            else if (getSignalLever(rssi) == 2)
                ((ImageView) view.findViewById(R.id.item_devices_iv_signal)).setImageResource(R.mipmap.icon_signal_2);
            else if (getSignalLever(rssi) == 3)
                ((ImageView) view.findViewById(R.id.item_devices_iv_signal)).setImageResource(R.mipmap.icon_signal_3);
            else if (getSignalLever(rssi) == 4)
                ((ImageView) view.findViewById(R.id.item_devices_iv_signal)).setImageResource(R.mipmap.icon_signal_4);
            else if (getSignalLever(rssi) == 5)
                ((ImageView) view.findViewById(R.id.item_devices_iv_signal)).setImageResource(R.mipmap.icon_signal_5);
        }
    }

    private int getSignalLever(int rssi) {
        int lever;
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


}
