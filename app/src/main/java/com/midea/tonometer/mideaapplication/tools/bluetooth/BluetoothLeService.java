package com.midea.tonometer.mideaapplication.tools.bluetooth;

import java.util.List;
import java.util.UUID;






import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	private BluetoothManager mBluetoothManager;// 蓝牙管理器
	private BluetoothAdapter mBluetoothAdapter;// 蓝牙适配器
	private String mBluetoothDeviceAddress;// 蓝牙设备地址
	private BluetoothGatt mBluetoothGatt;
	// 记录蓝牙链接状态
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;// 设备无法连接
	private static final int STATE_CONNECTING = 1;// 设备正在连接状态
	private static final int STATE_CONNECTED = 2;// 设备连接完毕

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String ACTION_READ_DATA = "com.example.bluetooth.le.READ_DATA";
	public final static String ACTION_WRITE_DATA = "com.example.bluetooth.le.WRITE_DATA";
	public final static String ACTION_READ_ALL_DATA = "com.example.bluetooth.le.READ_ALL_DATA";
	// 蓝牙接收到数据段
	private static final int READMRSSAGE = 1;
	//是否将蓝牙数据读完了
	public static boolean readMessageFinish=false;
	//记录着整块读完的蓝牙回馈数据
	public static byte [] readMessage;
	//BLE回调得到的蓝牙RSSI值
	private static int BLERSSI=0;

	
	public final static UUID ISSC_SERVICE_UUID = UUID
			.fromString(SampleGattAttributes.ISSC_SERVICE_UUID);
	// 蓝牙的读取UUID
	public final static UUID ISSC_CHAR_RX_UUID = UUID
			.fromString(SampleGattAttributes.ISSC_CHAR_RX_UUID);
	//  蓝牙的发送UUID
	public final static UUID ISSC_CHAR_TX_UUID = UUID
			.fromString(SampleGattAttributes.ISSC_CHAR_TX_UUID);
	//传输数据主服务
	private BluetoothGattService gattService;
	//传输数据读特性
	private BluetoothGattCharacteristic readCharacteristic;
	//传输数据写特性
	private BluetoothGattCharacteristic writeCharacteristic;

	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	// 通过BLE API的不同类型的回调方法
	@SuppressLint("NewApi")
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		// 当连接状态发生改变
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			// 当蓝牙设备已经连接
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i(TAG, "Connected to GATT server.已经链接上GATT服务");
				// Attempts to discover services after successful connection.
				// 试图发现服务连接成功后。
				Log.i(TAG, "Attempting to start service discovery试图发现服务连接成功后。:"
						+ mBluetoothGatt.discoverServices());
				
				// 当设备无法连接
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				close();
				Log.i(TAG, "Disconnected from GATT server.不能链接上GATT服务");
				broadcastUpdate(intentAction);
			}
		}

		// 发现新服务端
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		// 读取特征值
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicRead(gatt, characteristic, status);
			Log.d(TAG, "读到数据了的");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastReadUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		// 写出特征值
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			super.onCharacteristicWrite(gatt, characteristic, status);
			Log.d(TAG, "写出数据了~~");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastWriteUpdate(ACTION_WRITE_DATA, characteristic);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
		
			broadcastReadUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			// TODO Auto-generated method stub
			super.onReadRemoteRssi(gatt, rssi, status);
			//将回调的RSSI值赋值
			BLERSSI=rssi;
		}
		
	};
	
	// 广播更新
	private void broadcastUpdate(final String action) {
		// 创建意图 并封装action
		final Intent intent = new Intent(action);
		
		// 发送广播
		sendBroadcast(intent);
	}

	

	// 广播更新
	@SuppressLint("NewApi")
	private void broadcastReadUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);

		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (ISSC_CHAR_RX_UUID.equals(characteristic.getUuid())) {
		
			// 获得蓝牙读到的数组
			final byte[] data = characteristic.getValue();
			// 如果读到数据不为空且读到数据数组长度大于0
			if (data != null && data.length > 0) {
				
			}
		} else {
			// For all other profiles, writes the data formatted in HEX.
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				
			}
		}
		sendBroadcast(intent);
	}

	// 广播更新
	private void broadcastWriteUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);

		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (ISSC_CHAR_TX_UUID.equals(characteristic.getUuid())) {
			
		}
		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// 解除绑定
	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example, close() is
		// invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	/**
	 * 初始化一个参照当地的蓝牙适配器。
	 *
	 * [url=home.php?mod=space&uid=7300]@return[/url] 如果初始化成功，返回true。 初始化蓝牙服务类
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		// 对于API等级18以上，打通BluetoothManager引用BluetoothAdapter。
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "无法初始化BluetoothManager.");
				return false;
			}
		}
		// 实例化蓝牙适配器
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "无法取得BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @param address
	 *            The device address of the destination device.
	 *
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	/**
	 * 连接到托管蓝牙LE设备上的GATT服务器。
	 */
	public boolean connect(final String address) {
		// 如果蓝牙适配器为空 链接地址为空
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG, "BluetoothAdapter未初始化或未指定地址.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		// 先前已连接的设备。尝试重新连接。
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(TAG, "尝试使用现有mBluetoothGatt连接.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}
		//通过蓝牙地址实例化蓝牙设备
		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		
		//如果蓝牙设无法实例化
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect没有发现设备。无法连接.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		// 我们要直接连接到设备，所以我们的自动连接参数设置为false。
		// 连接GATT Server，你需要调用BluetoothDevice的connectGatt()方法。此函数带三个参数：Context、autoConnect(boolean)和BluetoothGattCallback对象。
	
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

		Log.d(TAG, "Trying to create a new connection试图创建一个新的连接.");
		// 蓝牙设备地址 为 传过来的蓝牙地址
		mBluetoothDeviceAddress = address;
		// 蓝牙链接状态 更新为 STATE_CONNECTING
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	/**
	 * 断开现有连接或取消挂起的连接。
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
		Log.d(TAG, "断开蓝牙连接");
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	/**
	 * 使用给定的BLE装置后，应用程序必须调用这个方法来确保资源被正确释放。
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
		
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	/**
	 * 读取特征值
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	// 写入特征值 原方法
	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter未初始化");
			return;
		}
		mBluetoothGatt.writeCharacteristic(characteristic);

	}

	// 写入特征值
	public boolean writeCharacteristic(
			BluetoothGattCharacteristic characteristic, byte[] writeData) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter未初始化");
			return false;
		}
		if (writeData == null) {
			return false;
		} else {
			characteristic.setValue(writeData);
		}

		// 写入方式要视情况而定
		characteristic
				.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}

	

	
	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	/**
	 * 启用或禁用特性通知。
	 *
	 * 如果为true，启用通知。否则为false。
	 */
	@SuppressLint("NewApi")
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		Log.d(TAG, "设置接受特征!");
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		// // This is specific to Heart Rate Measurement.
		// // 这是特定的心率测量。
		if (ISSC_CHAR_RX_UUID.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID
							.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}

	
	//获取写特征值
	public BluetoothGattCharacteristic getWriteCharacteristic() {
		return writeCharacteristic;
	}
	//设置写特征值
	public void setWriteCharacteristic(
			BluetoothGattCharacteristic writeCharacteristic) {
		this.writeCharacteristic = writeCharacteristic;
	}
	//获取已经得到的RSSI值
	public static int getBLERSSI() {
		return BLERSSI;
	}
	//是都能读取到已连接设备的RSSI值
	//执行该方法一次，获得蓝牙回调onReadRemoteRssi（）一次
	 /** 
     * Read the RSSI for a connected remote device. 
     * */
    public boolean getRssiVal() { 
        if (mBluetoothGatt == null) 
            return false; 
        return mBluetoothGatt.readRemoteRssi(); 
       
    } 

}
