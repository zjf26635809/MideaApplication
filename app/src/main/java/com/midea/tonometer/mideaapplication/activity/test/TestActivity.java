package com.midea.tonometer.mideaapplication.activity.test;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.BluetoothUtils;
import com.inuker.bluetooth.library.utils.ByteUtils;
import com.midea.tonometer.mideaapplication.R;
import com.midea.tonometer.mideaapplication.activity.MipcaActivityCapture;
import com.midea.tonometer.mideaapplication.activity.base.BaseActivity;
import com.midea.tonometer.mideaapplication.model.DevInfo;
import com.midea.tonometer.mideaapplication.model.http.DevTestResult;
import com.midea.tonometer.mideaapplication.okhttp.IResponseCallback;
import com.midea.tonometer.mideaapplication.okhttp.OkClient;
import com.midea.tonometer.mideaapplication.okhttp.bean.OkError;
import com.midea.tonometer.mideaapplication.okhttp.bean.RequestParam;
import com.midea.tonometer.mideaapplication.tools.BaseTypeConvert;
import com.midea.tonometer.mideaapplication.tools.CMDConstant;
import com.midea.tonometer.mideaapplication.tools.ClientManager;
import com.midea.tonometer.mideaapplication.tools.CommonUtils;
import com.midea.tonometer.mideaapplication.tools.MideaConstant;
import com.midea.tonometer.mideaapplication.tools.StringUtils;
import com.midea.tonometer.mideaapplication.view.MyTestContentView;
import com.midea.tonometer.mideaapplication.view.MyTestTitleView;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.midea.tonometer.mideaapplication.okhttp.NetManager.POST;

public class TestActivity extends BaseActivity implements View.OnClickListener, MyTestContentView.OnContentClickListener {
    private final static int REQUEST_SCANNIN_GREQUEST_CODE = 3;
    private DevInfo devInfo;

    private MyTestTitleView myTestTitleView;
    private MyTestContentView myTestContentView;
    private String mideaTestType;
    private int pos;


    private boolean mConnected;

    private BluetoothDevice mDevice;


    private BleGattService bleGattService;
    private BleGattCharacter rwBleGattCharacter;
    private BleGattCharacter notifyBleGattCharacter;

    private String WRITE_START_CMD = null;
    private String WRITE_STOP_CMD = null;

    private String NOTIFY_STOP_CMD = null;

    private String currentWriteCMD;//当前的写入指令

    private boolean isNotify = false;
    private boolean isTesting = false;
    private boolean isTiming = false;

    private boolean isSetConnectStatusListener = false;
    private boolean isSleep = false;
    Intent resultIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        devInfo = (DevInfo) getIntent().getSerializableExtra("DEVINFO");

        mideaTestType = getIntent().getStringExtra("TYPE");
        pos = getIntent().getIntExtra("POSITION", -1);

        if (devInfo == null || mideaTestType == null || pos == -1) {
            finish();
            return;
        }
        initUI();
        initCMD();
        isSleep = false;
        mDevice = BluetoothUtils.getRemoteDevice(devInfo.getDevMac());


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bleGattService == null) {
            if (!isSetConnectStatusListener) {
                ClientManager.getClient().registerConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
            }
        }
    }

    @Override
    protected void onDestroy() {
//        ClientManager.getClient().unregisterConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        super.onDestroy();
        isNotify = false;
        if (bleGattService == null || rwBleGattCharacter == null)
            return;

        if (bleGattService.getUUID() != null && rwBleGattCharacter.getUuid() != null) {
            if (isSetConnectStatusListener) {
                isSetConnectStatusListener = false;
                ClientManager.getClient().unregisterConnectStatusListener(devInfo.getDevMac(), mConnectStatusListener);
            }
            //ClientManager.getClient().disconnect(devInfo.getDevMac());
            ClientManager.getClient().unnotify(devInfo.getDevMac(), bleGattService.getUUID(), rwBleGattCharacter.getUuid(), mUnnotifyRsp);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(pos, resultIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCANNIN_GREQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                String sn = bundle.getString("result");

                startNetSaveDevSn(sn);

            }
        }
    }

    private void initCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        11);
            } else {
                Intent intent = new Intent();
                intent.setClass(TestActivity.this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_SCANNIN_GREQUEST_CODE);
            }
        }
    }

    private void initUI() {
        ((TextView) findViewById(R.id.include_title_tv)).setText("设备号No." + devInfo.getDevIndex());
        findViewById(R.id.include_title_iv_back).setOnClickListener(this);
        findViewById(R.id.activity_test_tv_success).setOnClickListener(this);
        findViewById(R.id.activity_test_tv_fail).setOnClickListener(this);


        waittingDialog.setTip("连接设备中...");

        myTestTitleView = (MyTestTitleView) findViewById(R.id.activity_test_testtitle);
        myTestTitleView.setDevInfo(mideaTestType, devInfo);


        if (StringUtils.isEmpty(devInfo.getDevSn())) {
            if(devInfo.isOnLined())
            myTestTitleView.setBoundDes("还没绑定SN");
            else
            myTestTitleView.setBoundDes("还没连上服务器");
        }
        else
            myTestTitleView.setBoundSN(true);

        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LEAK))
            myTestTitleView.setState(devInfo.getTestLeak());
        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LIFE))
            myTestTitleView.setState(devInfo.getTestLife());
        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_PRESSURE))
            myTestTitleView.setState(devInfo.getTestPressure());
        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE))
            myTestTitleView.setState(devInfo.getTestStaticPressure());
        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP))
            myTestTitleView.setState(devInfo.getTestSleep());
        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO))
            myTestTitleView.setState(devInfo.getTestModelInfo());


        myTestContentView = (MyTestContentView) findViewById(R.id.activity_test_testcontent);
        myTestContentView.setOnContentClickListener(this);
        myTestContentView.setType(mideaTestType);

        findViewById(R.id.include_title_tv_sao).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StringUtils.isEmpty(devInfo.getDevSn()))
                    initCameraPermission();
                else
                    CommonUtils.toast_s("已绑定SN");
            }
        });
    }

    private void initCMD() {
        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LEAK)) {
            WRITE_START_CMD = CMDConstant.LEAK_WRITE_START;
            WRITE_STOP_CMD = CMDConstant.LEAK_WRITE_STOP;
            NOTIFY_STOP_CMD = CMDConstant.LEAK_NOTIFY_STOP;
        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LIFE)) {
            WRITE_START_CMD = CMDConstant.LIFE_WRITE_START;
            WRITE_STOP_CMD = CMDConstant.LIFE_WRITE_STOP;
        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_PRESSURE)) {
            WRITE_START_CMD = CMDConstant.PRESSURE_WRITE_START;
            WRITE_STOP_CMD = CMDConstant.PRESSURE_WRITE_STOP;

            myTestContentView.setCorrectLintener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (myTestContentView.getCorrectValue() == -1)
                        CommonUtils.toast_s("请输入校准值");
                    else {

                        int correctValue = myTestContentView.getCorrectValue();

                        if (correctValue < 0 || correctValue > 255) {
                            CommonUtils.toast_s("请输入0到255范围的值");
                            return;
                        }
                        String hex;
                        if (correctValue <= 15)
                            hex = "0" + Integer.toHexString(correctValue);
                        else
                            hex = Integer.toHexString(correctValue);


                        String body = "AA550800C2" + hex;
                        byte[] byteBody = ByteUtils.stringToBytes(body);
                        short crc16 = CRC16(byteBody, byteBody.length);

                        byte[] byteAll = new byte[byteBody.length + 2];
                        for (int i = 0; i < byteBody.length; i++) byteAll[i] = byteBody[i];
                        byteAll[byteBody.length] = (byte) (crc16 >> 8);
                        byteAll[byteBody.length + 1] = (byte) crc16;

                        currentWriteCMD = ByteUtils.byteToString(byteAll);

                        ClientManager.getClient().write(devInfo.getDevMac(), bleGattService.getUUID(), rwBleGattCharacter.getUuid(),
                                byteAll, mWriteRsp);
                    }
                }
            });
        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE)) {
            WRITE_START_CMD = CMDConstant.PRESSURE_WRITE_START;
            WRITE_STOP_CMD = CMDConstant.PRESSURE_WRITE_STOP;
        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP)) {
            WRITE_START_CMD = CMDConstant.SLEEP_WRITE_START;
        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO)) {
            WRITE_START_CMD = CMDConstant.MODELINFO_WRITE_START;
        }
    }

    private void startNetSaveDevSn(final String sn) {

        if (StringUtils.isEmpty(sn)) {
            CommonUtils.toast_s("SN扫码失败");
            return;
        }

        devInfo.setDevSn(sn);

        waittingDialog.setTip("设备绑定SN中...");
        waittingDialog.onContentChanged();

        if (!waittingDialog.isShowing()) {
            waittingDialog.show();

        }

        RequestParam requestParam = new RequestParam(0, "/addsn?addSn");

        requestParam.setMethod(POST);
        requestParam.putHeader("X-Recipe-AppId", "1000");
        requestParam.putHeader("X-Recipe-AppKey", "uh79l871a38adf41aab3be3837e39123");

        requestParam.put("macAddress", devInfo.getDevMac());
        requestParam.put("sn", devInfo.getDevSn());

        OkClient.request(requestParam, DevTestResult.class, new IResponseCallback() {
            @Override
            public void onSuccess(int tag, Object object) {

                if (isFinishing() || isDestroyed())
                    return;
                if (waittingDialog.isShowing())
                    waittingDialog.dismiss();

                DevTestResult devResule = (DevTestResult) object;
                Log.v("NET", "返回结果===" + devResule.toString());
                if (devResule.getRet() == 0) {
                    CommonUtils.toast_s("绑定SN成功");

                    myTestTitleView.setBoundSN(true);
                    resultIntent.putExtra("BOUND_SN", devInfo.getDevSn());
                    setResult(pos, resultIntent);

                } else {
                    myTestTitleView.setBoundSN(false);
                    CommonUtils.toast_s("绑定SN失败：" + devResule.getDetailMsg());
                }

            }

            @Override
            public void onError(int tag, OkError error) {
                if (isFinishing() || isDestroyed())
                    return;

                CommonUtils.toast_s("保存失败：" + error.getMsg());
                Log.v("NET", "返回错误===" + error.getMsg());
                myTestTitleView.setBoundSN(false);
                if (waittingDialog.isShowing())
                    waittingDialog.dismiss();
            }
        });

    }


    private void startNetSaveDevTestInfos(final String testResult) {

        waittingDialog.setTip("正在保存数据...");
        waittingDialog.onContentChanged();


        if (!waittingDialog.isShowing()) {
            waittingDialog.show();

        }

        RequestParam requestParam = new RequestParam(0, "/add?addBloodPressure");

        requestParam.setMethod(POST);
        requestParam.putHeader("X-Recipe-AppId", "1000");
        requestParam.putHeader("X-Recipe-AppKey", "uh79l871a38adf41aab3be3837e39123");

        requestParam.put("macAddress", devInfo.getDevMac());
        requestParam.put("deviceCode", devInfo.getDevIndex());
        requestParam.put("testItem", mideaTestType);
        requestParam.put("testResult", testResult);
        requestParam.put("sn", devInfo.getDevSn());
        requestParam.put("testTimes", "1");

        OkClient.request(requestParam, DevTestResult.class, new IResponseCallback() {
            @Override
            public void onSuccess(int tag, Object object) {
                if (isFinishing() || isDestroyed())
                    return;

                if (waittingDialog.isShowing())
                    waittingDialog.dismiss();

                DevTestResult devResule = (DevTestResult) object;
                Log.v("NET", "返回结果===" + devResule.toString());
                if (devResule.getRet() == 0) {
                    CommonUtils.toast_s("保存成功");
                    myTestTitleView.setState(testResult);


                    resultIntent.putExtra("TEST_RESULT", testResult);
                    setResult(pos, resultIntent);

                } else {
                    CommonUtils.toast_s("保存失败：" + devResule.getDetailMsg());
                }

            }

            @Override
            public void onError(int tag, OkError error) {
                if (isFinishing() || isDestroyed())
                    return;

                CommonUtils.toast_s("保存失败：" + error.getMsg());
                Log.v("NET", "返回错误===" + error.getMsg());
                if (waittingDialog.isShowing())
                    waittingDialog.dismiss();
            }
        });

    }

    private String getTime() {
        long time = System.currentTimeMillis();
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        String hour_str = null, minute_str = null, second_str = null;
        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);
        if (hour == 0)
            hour_str = "12";
        else if (hour < 10 && hour > 0)
            hour_str = "0" + hour;
        else
            hour_str = "" + hour;

        if (minute < 10 && minute >= 0)
            minute_str = "0" + minute;
        else
            minute_str = "" + minute;

        if (second < 10 && second >= 0)
            second_str = "0" + second;
        else
            second_str = "" + second;

        return hour_str + ":" + minute_str + ":" + second_str;

    }

    private BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            isSetConnectStatusListener = true;

            if (isFinishing() || isDestroyed())
                return;
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == STATUS_CONNECTED);

            if (status == STATUS_CONNECTED) isSleep = false;

            connectDeviceIfNeeded();

        }
    };


    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            connectDevice();
        }
    }

    private void connectDevice() {
        if (this.isFinishing()) {
            return;
        }
        if (waittingDialog != null && !waittingDialog.isShowing())
            waittingDialog.show();

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(0)
                .setConnectTimeout(8000)
                .setServiceDiscoverRetry(0)
                .setServiceDiscoverTimeout(8000)
                .build();

        ClientManager.getClient().connect(mDevice.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                BluetoothLog.v(String.format("onResponse code = %d", code));
                if (waittingDialog != null && waittingDialog.isShowing())
                    waittingDialog.cancel();

                if (code == REQUEST_SUCCESS) {
                    BluetoothLog.v(String.format("Profiles: \n%s", profile));
                    pairUUID(profile);
                }
            }
        });
    }

    private void pairUUID(BleGattProfile profile) {
        List<BleGattService> services = profile.getServices();

        for (int i = 0; i < services.size(); i++) {
//            items.add(new DetailItem(DetailItem.TYPE_SERVICE, service.getUUID(), null));
            String serviceUUID_str = String.format("%s", services.get(i).getUUID());
            BluetoothLog.w(devInfo.getDevMac() + "  :  " + "serviceUUID:" + serviceUUID_str);
            if (serviceUUID_str.contains("00001000-0000-1000")) {
                bleGattService = services.get(i);

                if (bleGattService != null) {
                    List<BleGattCharacter> characters = bleGattService.getCharacters();
                    for (int j = 0; j < characters.size(); j++) {
                        BluetoothLog.v(devInfo.getDevMac() + "  :  " + "charactersUUID:" + characters.get(j).getUuid());
                        if (String.format("%s", characters.get(j).getUuid()).contains("00001002"))
                            rwBleGattCharacter = characters.get(j);
                        if (String.format("%s", characters.get(j).getUuid()).contains("00001001"))
                            notifyBleGattCharacter = characters.get(j);
                    }
                }
            }
        }
        if (bleGattService == null) {
            Toast.makeText(this, "获取指定的Service_UUID失败", Toast.LENGTH_SHORT).show();
            finish();
        } else if (rwBleGattCharacter == null) {
            Toast.makeText(this, "获取指定的Character_UUID失败", Toast.LENGTH_SHORT).show();
            finish();
        } else if (notifyBleGattCharacter == null) {
            Toast.makeText(this, "获取指定的notify_UUID失败", Toast.LENGTH_SHORT).show();
            finish();
        } else
            Toast.makeText(this, "获取指定的UUID成功", Toast.LENGTH_SHORT).show();

    }

    private void pairNotifyCMD(byte[] value) {

        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LEAK)) {
            if (value.length == 8) {

                if (CMDConstant.LEAK_CALLBACK_RESULE_LEAK.equals(ByteUtils.byteToString(value))) {
                    myTestContentView.setDes("漏气");
                    isTesting = false;
                } else if (CMDConstant.LEAK_CALLBACK_RESULE_NO_LEAK.equals(ByteUtils.byteToString(value))) {
                    myTestContentView.setDes("不漏气");
                    isTesting = false;
                }

            } else if (value.length == 11)
                myTestContentView.setValue(BaseTypeConvert.bytesToInt(value, 6) + "");

        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LIFE)) {
            if (value.length == 10)
                myTestContentView.setDes("循环：" + BaseTypeConvert.bytesToInt(value, 6) + "次");
            else if (value.length == 11)
                myTestContentView.setValue(BaseTypeConvert.bytesToInt(value, 6) + "");
        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_PRESSURE)) {
            if (value.length == 8) {
                String s = ByteUtils.byteToString(value);
                if (s.equals(CMDConstant.PRESSURE_NOTIFY_SET_SUCCESS))
                    CommonUtils.toast_s("设置校准成功");

                else if (s.equals(CMDConstant.PRESSURE_NOTIFY_SET_FAIL))
                    CommonUtils.toast_s("设置校准失败");


            } else if (value.length == 11) {
                String s = ByteUtils.byteToString(value);
                if (s.contains("AA550B001400"))
                    myTestContentView.setValue(BaseTypeConvert.bytesToInt(value, 6) + "");

            }
        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE)) {
            if (value.length == 11) {
                String s = ByteUtils.byteToString(value);
                if (s.contains("AA550B001400"))
                    myTestContentView.setValue(BaseTypeConvert.bytesToInt(value, 6) + "");

            }

        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP)) {
        } else if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO)) {
            if (value.length == 18) {
                String s = ByteUtils.byteToString(value);
                if (s.contains("AA551200C7")) { //血压模组信息检测

                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("软件版本号：" + BaseTypeConvert.byteToInt(value[5]) + "." + BaseTypeConvert.byteToInt(value[6]) + "." + BaseTypeConvert.byteToInt(value[7]) + "\n");
                    stringBuffer.append("算法版本号：" + BaseTypeConvert.byteToInt(value[8]) + "." + BaseTypeConvert.byteToInt(value[9]) + "." + BaseTypeConvert.byteToInt(value[10]) + "\n");
                    stringBuffer.append("协议版本号：" + BaseTypeConvert.byteToInt(value[11]) + "." + BaseTypeConvert.byteToInt(value[12]) + "." + BaseTypeConvert.byteToInt(value[13]));
                    myTestContentView.setValue(stringBuffer.toString());
                }
            }
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP) && isSleep) {
                CommonUtils.toast_s("请先唤醒设备才能退出");
                return false;
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.include_title_iv_back:
                if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP) && isSleep)
                    CommonUtils.toast_s("请先唤醒设备才能退出");
                else
                    finish();
                break;

            case R.id.activity_test_tv_success:
                if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LEAK) || mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_PRESSURE) || mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_STATIC_PRESSURE)) {
                    if (isTesting) {
                        Toast.makeText(TestActivity.this, "请先停止测试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                startNetSaveDevTestInfos(MideaConstant.MIDEA_TEST_RESULT_FINISH);
                break;
            case R.id.activity_test_tv_fail:
                if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LEAK)) {
                    if (isTesting) {
                        Toast.makeText(TestActivity.this, "请先停止测试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                startNetSaveDevTestInfos(MideaConstant.MIDEA_TEST_RESULT_UNFINISH);
                break;
        }
    }

    int time = 0;
    private Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isTiming)
                if (msg.what == 0) {
                    time++;
                    myTestContentView.setDes1("运行" + time + "秒");
                    timerHandler.sendEmptyMessageDelayed(0, 1000);
                }
        }
    };

    public void onStartAction() {
        if (bleGattService == null || rwBleGattCharacter == null || bleGattService.getUUID() == null || rwBleGattCharacter.getUuid() == null) {
            CommonUtils.toast_s("连接失败，请重试");
            finish();
            return;
        }
        if (!isTesting) {
            currentWriteCMD = WRITE_START_CMD;
            if (isSleep)
                CommonUtils.toast_s("设备进入深睡眠状态");
            else {
                BluetoothLog.w("currentWriteCMD=" + formatCMD(currentWriteCMD));
                ClientManager.getClient().write(devInfo.getDevMac(), bleGattService.getUUID(), rwBleGattCharacter.getUuid(),
                        ByteUtils.stringToBytes(currentWriteCMD), mWriteRsp);

            }
            myTestContentView.setDes("");

            isTesting = true;

            if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP) || mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_MODULE_INFO))
                isTesting = false;

            if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LIFE)) {
                isTiming = true;
                timerHandler.sendEmptyMessageDelayed(0, 1000);
            }

            if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_SLEEP))
                isSleep = true;

            myTestContentView.setDes("");
        } else
            Toast.makeText(this, "正在测试中...", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStopAction() {
        if (bleGattService == null || rwBleGattCharacter == null || bleGattService.getUUID() == null || rwBleGattCharacter.getUuid() == null) {
            CommonUtils.toast_s("连接失败，请重试");
            finish();
            return;
        }

        currentWriteCMD = WRITE_STOP_CMD;
        ClientManager.getClient().write(devInfo.getDevMac(), bleGattService.getUUID(), rwBleGattCharacter.getUuid(),
                ByteUtils.stringToBytes(currentWriteCMD), mWriteRsp);

        //ClientManager.getClient().unnotify(devInfo.getDevMac(), bleGattService.getUUID(), rwBleGattCharacter.getUuid(), mUnnotifyRsp);

        if (mideaTestType.equals(MideaConstant.MIDEA_TEST_TYPE_LIFE))
            isTiming = false;

    }

    private final BleWriteResponse mWriteRsp = new BleWriteResponse() {
        @Override
        public void onResponse(int code) {
            if (TestActivity.this.isFinishing())
                return;
            if (code == REQUEST_SUCCESS) {
                myTestContentView.writeIntoText(getTime() + "【发送数据】：成功");
                ClientManager.getClient().read(devInfo.getDevMac(), bleGattService.getUUID(), rwBleGattCharacter.getUuid(), mReadRsp);
                if (!isNotify)
                    ClientManager.getClient().notify(devInfo.getDevMac(), bleGattService.getUUID(), notifyBleGattCharacter.getUuid(), mNotifyRsp);
            } else {
                myTestContentView.writeIntoText(getTime() + "【发送数据】：失败");
            }
        }
    };

    private final BleReadResponse mReadRsp = new BleReadResponse() {
        @Override
        public void onResponse(int code, byte[] data) {
            if (TestActivity.this.isFinishing())
                return;

            if (code == REQUEST_SUCCESS) {
                myTestContentView.writeIntoText(getTime() + "【接收命令】：成功");

                if ((ByteUtils.byteToString(data).contains(CMDConstant.LEAK_WRITE_STOP)))
                    isTesting = false;

                if ((ByteUtils.byteToString(data).contains(CMDConstant.LEAK_NOTIFY_STOP))) {
                    isNotify = false;
                }
                if ((ByteUtils.byteToString(data).contains(CMDConstant.LIFE_WRITE_STOP))) {
                    isTesting = false;
                }

                if ((ByteUtils.byteToString(data).contains(CMDConstant.PRESSURE_WRITE_STOP))) {
                    isTesting = false;
                }

            } else {
                myTestContentView.writeIntoText(getTime() + "【接收命令】：失败");
            }
        }
    };

    private final BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
//            myTestContentView.writeIntoText("设备反馈：" + formatCMD(ByteUtils.byteToString(value)));
            if (TestActivity.this.isFinishing())
                return;
            if (service.equals(bleGattService.getUUID()) && character.equals(notifyBleGattCharacter.getUuid())) {
//                myTestContentView.writeIntoText("设备反馈：" + formatCMD(ByteUtils.byteToString(value)));
                BluetoothLog.v("设备反馈：" + formatCMD(ByteUtils.byteToString(value)));
                pairNotifyCMD(value);
            }
        }


        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                isNotify = true;
            } else {
            }
        }
    };

    private final BleUnnotifyResponse mUnnotifyRsp = new BleUnnotifyResponse() {
        @Override
        public void onResponse(int code) {

        }
    };


    private String formatCMD(String cmd) {
        String input = cmd;
        String regex = "(.{2})";
        input = input.replaceAll(regex, "$1 ");
        return input;

    }

    short CRC16(byte[] buff, int length) {
        int message;
        byte temp;
        int i, j;
        message = 0;
        message = ((message ^ (buff[0])) << 8) ^ (buff[1]);
        for (j = 2; j < length; j++) {
            temp = (byte) buff[j];
            for (i = 8; i > 0; i--) {
                if ((message & 0x8000) == 0x8000) {
                    if ((temp & 0x80) == 0x80)
                        message = (((message << 1) | 0x01) ^ 0x1021);
                    else
                        message = ((message << 1) ^ 0x1021);
                } else {
                    if ((temp & 0x80) == 0x80)
                        message = ((message << 1) | 0x01);
                    else
                        message = (message << 1);
                }
                temp = (byte) (temp << 1);
            }

        }
        return (short) message;
    }
}
