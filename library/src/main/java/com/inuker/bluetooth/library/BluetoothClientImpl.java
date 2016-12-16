package com.inuker.bluetooth.library;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleReadRssiResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.connect.response.BluetoothResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.receiver.BluetoothReceiver;
import com.inuker.bluetooth.library.receiver.listener.BleCharacterChangeListener;
import com.inuker.bluetooth.library.receiver.listener.BleConnectStatusChangeListener;
import com.inuker.bluetooth.library.receiver.listener.BluetoothStateChangeListener;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.BluetoothUtils;
import com.inuker.bluetooth.library.utils.ListUtils;
import com.inuker.bluetooth.library.utils.proxy.ProxyBulk;
import com.inuker.bluetooth.library.utils.proxy.ProxyInterceptor;
import com.inuker.bluetooth.library.utils.proxy.ProxyUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static com.inuker.bluetooth.library.Constants.ACTION_CHARACTER_CHANGED;
import static com.inuker.bluetooth.library.Constants.ACTION_CONNECT_STATUS_CHANGED;
import static com.inuker.bluetooth.library.Constants.CODE_CONNECT;
import static com.inuker.bluetooth.library.Constants.CODE_DISCONNECT;
import static com.inuker.bluetooth.library.Constants.CODE_INDICATE;
import static com.inuker.bluetooth.library.Constants.CODE_NOTIFY;
import static com.inuker.bluetooth.library.Constants.CODE_READ;
import static com.inuker.bluetooth.library.Constants.CODE_READ_DESCRIPTOR;
import static com.inuker.bluetooth.library.Constants.CODE_READ_RSSI;
import static com.inuker.bluetooth.library.Constants.CODE_SEARCH;
import static com.inuker.bluetooth.library.Constants.CODE_STOP_SESARCH;
import static com.inuker.bluetooth.library.Constants.CODE_UNNOTIFY;
import static com.inuker.bluetooth.library.Constants.CODE_WRITE;
import static com.inuker.bluetooth.library.Constants.CODE_WRITE_DESCRIPTOR;
import static com.inuker.bluetooth.library.Constants.CODE_WRITE_NORSP;
import static com.inuker.bluetooth.library.Constants.DEVICE_FOUND;
import static com.inuker.bluetooth.library.Constants.EXTRA_BYTE_VALUE;
import static com.inuker.bluetooth.library.Constants.EXTRA_CHARACTER_UUID;
import static com.inuker.bluetooth.library.Constants.EXTRA_DESCRIPTOR_UUID;
import static com.inuker.bluetooth.library.Constants.EXTRA_GATT_PROFILE;
import static com.inuker.bluetooth.library.Constants.EXTRA_MAC;
import static com.inuker.bluetooth.library.Constants.EXTRA_OPTIONS;
import static com.inuker.bluetooth.library.Constants.EXTRA_REQUEST;
import static com.inuker.bluetooth.library.Constants.EXTRA_RSSI;
import static com.inuker.bluetooth.library.Constants.EXTRA_SEARCH_RESULT;
import static com.inuker.bluetooth.library.Constants.EXTRA_SERVICE_UUID;
import static com.inuker.bluetooth.library.Constants.EXTRA_STATUS;
import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.SEARCH_CANCEL;
import static com.inuker.bluetooth.library.Constants.SEARCH_START;
import static com.inuker.bluetooth.library.Constants.SEARCH_STOP;
import static com.inuker.bluetooth.library.Constants.SERVICE_UNREADY;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

/**
 * Created by dingjikerbo on 16/4/8.
 */
public class BluetoothClientImpl implements IBluetoothClient, ProxyInterceptor, Callback {

    private static final int MSG_INVOKE_PROXY = 1;

    private static final String TAG = BluetoothClientImpl.class.getSimpleName();

    private Context mContext;

    private IBluetoothService mBluetoothService;

    private static IBluetoothClient sInstance;

    private CountDownLatch mCountDownLatch;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;

    private HashMap<String, HashMap<String, List<BleNotifyResponse>>> mNotifyResponses;
    private HashMap<String, List<BleConnectStatusListener>> mConnectStatusListeners;
    private List<BluetoothStateListener> mBluetoothStateListener;

    private BluetoothClientImpl(Context context) {
        mContext = context.getApplicationContext();
        BluetoothContext.set(mContext);

        mWorkerThread = new HandlerThread(TAG);
        mWorkerThread.start();

        mWorkerHandler = new Handler(mWorkerThread.getLooper(), this);

        mNotifyResponses = new HashMap<String, HashMap<String, List<BleNotifyResponse>>>();
        mConnectStatusListeners = new HashMap<String, List<BleConnectStatusListener>>();
        mBluetoothStateListener = new LinkedList<BluetoothStateListener>();

        registerBluetoothReceiver();

//        BluetoothHooker.hook();
    }

    public static IBluetoothClient getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BluetoothClientImpl.class) {
                if (sInstance == null) {
                    BluetoothClientImpl client = new BluetoothClientImpl(context);
                    sInstance = ProxyUtils.getProxy(client, IBluetoothClient.class, client);
                }
            }
        }
        return sInstance;
    }

    private IBluetoothService getBluetoothService() {
        if (mBluetoothService == null) {
            bindServiceSync();
        }
        return mBluetoothService;
    }

    private void bindServiceSync() {
        mCountDownLatch = new CountDownLatch(1);

        Intent intent = new Intent();
        intent.setClass(mContext, BluetoothService.class);

        if (mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
            BluetoothLog.v(String.format("BluetoothService registered"));
            waitBluetoothManagerReady();
        } else {
            BluetoothLog.v(String.format("BluetoothService not registered"));
            mBluetoothService = BluetoothServiceImpl.getInstance();
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = IBluetoothService.Stub.asInterface(service);
            notifyBluetoothManagerReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };


    @Override
    public void connect(String mac, BleConnectOptions options, final BleConnectResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putParcelable(EXTRA_OPTIONS, options);
        safeCallBluetoothApi(CODE_CONNECT, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    data.setClassLoader(getClass().getClassLoader());
                    BleGattProfile profile = data.getParcelable(EXTRA_GATT_PROFILE);
                    response.onResponse(code, profile);
                }
            }
        });
    }

    @Override
    public void disconnect(String mac) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        safeCallBluetoothApi(CODE_DISCONNECT, args, null);
        clearNotifyListener(mac);
    }

    @Override
    public void registerConnectStatusListener(String mac, BleConnectStatusListener listener) {
        List<BleConnectStatusListener> listeners = mConnectStatusListeners.get(mac);
        if (listeners == null) {
            listeners = new ArrayList<BleConnectStatusListener>();
            mConnectStatusListeners.put(mac, listeners);
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterConnectStatusListener(String mac, BleConnectStatusListener listener) {
        List<BleConnectStatusListener> listeners = mConnectStatusListeners.get(mac);
        if (!ListUtils.isEmpty(listeners)) {
            listeners.clear();
        }
    }

    @Override
    public void read(String mac, UUID service, UUID character, final BleReadResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putSerializable(EXTRA_SERVICE_UUID, service);
        args.putSerializable(EXTRA_CHARACTER_UUID, character);
        safeCallBluetoothApi(CODE_READ, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    response.onResponse(code, data.getByteArray(EXTRA_BYTE_VALUE));
                }
            }
        });
    }

    @Override
    public void write(String mac, UUID service, UUID character, byte[] value, final BleWriteResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putSerializable(EXTRA_SERVICE_UUID, service);
        args.putSerializable(EXTRA_CHARACTER_UUID, character);
        args.putByteArray(EXTRA_BYTE_VALUE, value);
        safeCallBluetoothApi(CODE_WRITE, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, final BleReadResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putSerializable(EXTRA_SERVICE_UUID, service);
        args.putSerializable(EXTRA_CHARACTER_UUID, character);
        args.putSerializable(EXTRA_DESCRIPTOR_UUID, descriptor);
        safeCallBluetoothApi(CODE_READ_DESCRIPTOR, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    response.onResponse(code, data.getByteArray(EXTRA_BYTE_VALUE));
                }
            }
        });
    }

    @Override
    public void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, final BleWriteResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putSerializable(EXTRA_SERVICE_UUID, service);
        args.putSerializable(EXTRA_CHARACTER_UUID, character);
        args.putSerializable(EXTRA_DESCRIPTOR_UUID, descriptor);
        args.putByteArray(EXTRA_BYTE_VALUE, value);
        safeCallBluetoothApi(CODE_WRITE_DESCRIPTOR, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void writeNoRsp(String mac, UUID service, UUID character, byte[] value, final BleWriteResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putSerializable(EXTRA_SERVICE_UUID, service);
        args.putSerializable(EXTRA_CHARACTER_UUID, character);
        args.putByteArray(EXTRA_BYTE_VALUE, value);
        safeCallBluetoothApi(CODE_WRITE_NORSP, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    private void saveNotifyListener(String mac, UUID service, UUID character, BleNotifyResponse response) {
        HashMap<String, List<BleNotifyResponse>> listenerMap = mNotifyResponses.get(mac);
        if (listenerMap == null) {
            listenerMap = new HashMap<String, List<BleNotifyResponse>>();
            mNotifyResponses.put(mac, listenerMap);
        }

        String key = generateCharacterKey(service, character);
        List<BleNotifyResponse> responses = listenerMap.get(key);
        if (responses == null) {
            responses = new ArrayList<BleNotifyResponse>();
            listenerMap.put(key, responses);
        }

        responses.add(response);
    }

    private void removeNotifyListener(String mac, UUID service, UUID character) {
        HashMap<String, List<BleNotifyResponse>> listenerMap = mNotifyResponses.get(mac);
        if (listenerMap != null) {
            String key = generateCharacterKey(service, character);
            listenerMap.remove(key);
        }
    }

    private void clearNotifyListener(String mac) {
        mNotifyResponses.remove(mac);
    }

    private String generateCharacterKey(UUID service, UUID character) {
        return String.format("%s_%s", service, character);
    }

    @Override
    public void notify(final String mac, final UUID service, final UUID character, final BleNotifyResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putSerializable(EXTRA_SERVICE_UUID, service);
        args.putSerializable(EXTRA_CHARACTER_UUID, character);
        safeCallBluetoothApi(CODE_NOTIFY, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    if (code == REQUEST_SUCCESS) {
                        saveNotifyListener(mac, service, character, response);
                    }
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void unnotify(final String mac, final UUID service, final UUID character, final BleUnnotifyResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putSerializable(EXTRA_SERVICE_UUID, service);
        args.putSerializable(EXTRA_CHARACTER_UUID, character);
        safeCallBluetoothApi(CODE_UNNOTIFY, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    response.onResponse(code);
                }

                if (code == REQUEST_SUCCESS) {
                    removeNotifyListener(mac, service, character);
                }
            }
        });
    }

    @Override
    public void indicate(final String mac, final UUID service, final UUID character, final BleNotifyResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        args.putSerializable(EXTRA_SERVICE_UUID, service);
        args.putSerializable(EXTRA_CHARACTER_UUID, character);
        safeCallBluetoothApi(CODE_INDICATE, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    if (code == REQUEST_SUCCESS) {
                        saveNotifyListener(mac, service, character, response);
                    }
                    response.onResponse(code);
                }
            }
        });
    }

    @Override
    public void unindicate(String mac, UUID service, UUID character, BleUnnotifyResponse response) {
        unnotify(mac, service, character, response);
    }

    @Override
    public void readRssi(String mac, final BleReadRssiResponse response) {
        Bundle args = new Bundle();
        args.putString(EXTRA_MAC, mac);
        safeCallBluetoothApi(CODE_READ_RSSI, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response != null) {
                    response.onResponse(code, data.getInt(EXTRA_RSSI, 0));
                }
            }
        });
    }

    @Override
    public void search(SearchRequest request, final SearchResponse response) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_REQUEST, request);
        safeCallBluetoothApi(CODE_SEARCH, args, new BluetoothResponse() {
            @Override
            public void onResponse(int code, Bundle data) throws RemoteException {
                if (response == null) {
                    return;
                }

                data.setClassLoader(getClass().getClassLoader());

                switch (code) {
                    case SEARCH_START:
                        response.onSearchStarted();
                        break;

                    case SEARCH_CANCEL:
                        response.onSearchCanceled();
                        break;

                    case SEARCH_STOP:
                        response.onSearchStopped();
                        break;

                    case DEVICE_FOUND:
                        SearchResult device = data.getParcelable(EXTRA_SEARCH_RESULT);
                        response.onDeviceFounded(device);
                        break;

                    default:
                        throw new IllegalStateException("unknown code");
                }
            }
        });
    }

    @Override
    public void stopSearch() {
        safeCallBluetoothApi(CODE_STOP_SESARCH, null, null);
    }

    @Override
    public void registerBluetoothStateListener(BluetoothStateListener listener) {
        if (listener != null && !mBluetoothStateListener.contains(listener)) {
            mBluetoothStateListener.add(listener);
        }
    }

    @Override
    public void unregisterBluetoothStateListener(BluetoothStateListener listener) {
        if (listener != null) {
            mBluetoothStateListener.remove(listener);
        }
    }

    private void safeCallBluetoothApi(int code, Bundle args, final BluetoothResponse response) {
        try {
            IBluetoothService service = getBluetoothService();
            if (service != null) {
                args = (args != null ? args : new Bundle());
                service.callBluetoothApi(code, args, response);
            } else {
                response.onResponse(SERVICE_UNREADY, null);
            }
        } catch (Throwable e) {
            BluetoothLog.e(e);
        }
    }

    @Override
    public boolean onIntercept(final Object object, final Method method, final Object[] args) {
        mWorkerHandler.obtainMessage(MSG_INVOKE_PROXY, new ProxyBulk(object, method, args))
                .sendToTarget();
        return true;
    }

    private void notifyBluetoothManagerReady() {
        if (mCountDownLatch != null) {
            mCountDownLatch.countDown();
            mCountDownLatch = null;
        }
    }

    private void waitBluetoothManagerReady() {
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INVOKE_PROXY:
                ProxyBulk.safeInvoke(msg.obj);
                break;
        }
        return true;
    }

    private void dispatchCharacterNotify(String mac, UUID service, UUID character, byte[] value) {
        HashMap<String, List<BleNotifyResponse>> notifyMap = mNotifyResponses.get(mac);
        if (notifyMap != null) {
            String key = generateCharacterKey(service, character);
            List<BleNotifyResponse> responses = notifyMap.get(key);
            if (responses != null) {
                for (final BleNotifyResponse response : responses) {
                    response.onNotify(service, character, value);
                }
            }
        }
    }

    private void dispatchConnectionStatus(final String mac, final int status) {
        List<BleConnectStatusListener> listeners = mConnectStatusListeners.get(mac);
        if (!ListUtils.isEmpty(listeners)) {
            for (final BleConnectStatusListener listener : listeners) {
                BluetoothUtils.post(new Runnable() {

                    @Override
                    public void run() {
                        listener.onConnectStatusChanged(mac, status);
                    }
                });
            }
        }
    }

    private void dispatchBluetoothStateChanged(int previousState, final int currentState) {
        if (currentState == Constants.STATE_OFF || currentState == Constants.STATE_ON) {
            for (final BluetoothStateListener listener : mBluetoothStateListener) {
                BluetoothUtils.post(new Runnable() {

                    @Override
                    public void run() {
                        listener.onBluetoothStateChanged(currentState == Constants.STATE_ON);
                    }
                });
            }
        }
    }

    private void registerBluetoothReceiver() {
        registerBluetoothStateReceiver();
        registerBleConnectStatusReceiver();
        registerBleCharacterChangeReceiver();
    }

    private void registerBluetoothStateReceiver() {
        BluetoothReceiver.getInstance().register(new BluetoothStateChangeListener() {
            @Override
            public void onBluetoothStateChanged(final int previousState, final int currentState) {
                if (currentState == Constants.STATE_OFF || currentState == Constants.STATE_TURNING_OFF) {
                    stopSearch();
                }

                mWorkerHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        dispatchBluetoothStateChanged(previousState, currentState);
                    }
                });
            }
        });
    }

    private void registerBleConnectStatusReceiver() {
        BluetoothReceiver.getInstance().register(new BleConnectStatusChangeListener() {
            @Override
            public void onConnectStatusChanged(final String mac, final int status) {
                if (status == Constants.STATUS_DISCONNECTED) {
                    clearNotifyListener(mac);
                }

                mWorkerHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        dispatchConnectionStatus(mac, status);
                    }
                });
            }
        });
    }

    private void registerBleCharacterChangeReceiver() {
        BluetoothReceiver.getInstance().register(new BleCharacterChangeListener() {
            @Override
            public void onCharacterChanged(final String mac, final UUID service, final UUID character, final byte[] value) {
                mWorkerHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        dispatchCharacterNotify(mac, service, character, value);
                    }
                });
            }
        });
    }
}
