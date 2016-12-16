package com.midea.tonometer.mideaapplication.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

@SuppressLint("CommitPrefEdits")
public class Shareference {

	static final String name = "com.midea.tonometer";

	static final String modelname = "com.midea.tonometer.model";

	public static void save(Context context, String key, String value) {
		if (context != null) {

			SharedPreferences mSharedPreferences = context
					.getSharedPreferences(name, Context.MODE_PRIVATE);
			Editor editor = mSharedPreferences.edit();
			editor.putString(key, value);
			editor.commit();
		}

	}

	public static void save(Context context, String key, boolean value) {
		if (context != null) {

			SharedPreferences mSharedPreferences = context
					.getSharedPreferences(name, Context.MODE_PRIVATE);
			Editor editor = mSharedPreferences.edit();
			editor.putBoolean(key, value);
			editor.commit();
		}

	}

	public static void save(Context context, String key, List<String> values) {
		if (context != null) {

			SharedPreferences mSharedPreferences = context
					.getSharedPreferences(modelname, Context.MODE_PRIVATE);

			Editor editor = mSharedPreferences.edit();
			
			editor.clear().commit();
			
			editor.putInt(key + "_size", values.size());

			for (int i = 0; i < values.size(); i++) {
				editor.putString(key + "_" + i, values.get(i));
			}

			editor.commit();
		}

	}

	public static void save(Context context, String key, int value) {
		if (context != null) {

			SharedPreferences mSharedPreferences = context
					.getSharedPreferences(name, Context.MODE_PRIVATE);
			Editor editor = mSharedPreferences.edit();
			editor.putString(key, String.valueOf(value));
			editor.commit();
		}

	}

	public static int getInt(Context context, String key) {
		if (context != null) {

			SharedPreferences mSharedPreferences = context
					.getSharedPreferences(name, Context.MODE_PRIVATE);
			String value = mSharedPreferences.getString(key, "");
			if (!value.equals("")) {
				return Integer.parseInt(value);
			} else {
				return 0;

			}
		}
		return 0;

	}

	public static String get(Context context, String key) {
		if (context != null) {

			SharedPreferences mSharedPreferences = context
					.getSharedPreferences(name, Context.MODE_PRIVATE);
			String value = mSharedPreferences.getString(key, "");
			if (!value.equals("")) {
				return value;
			} else {
				return "";

			}
		}
		return "0";
	}

	public static String[] getStringArray(Context context, String key) {
		if (context != null) {

			SharedPreferences mSharedPreferences = context
					.getSharedPreferences(modelname, Context.MODE_PRIVATE);

			int size = mSharedPreferences.getInt(key + "_size", 0);
			String[] values = new String[size];
			for (int i = 0; i < size; i++) {
				values[i] = mSharedPreferences.getString(key + "_" + i, null);

			}
			return values;
		}
		return null;
	}

	public static boolean getBoolean(Context context, String key) {

		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				name, Context.MODE_PRIVATE);
		boolean value = mSharedPreferences.getBoolean(key, false);
		return value;
	}

	public static boolean getBoolean(Context context, String key,
			boolean defaults) {

		SharedPreferences mSharedPreferences = context.getSharedPreferences(
				name, Context.MODE_PRIVATE);
		boolean value = mSharedPreferences.getBoolean(key, defaults);
		return value;
	}

	public static void saveObject(Context context, String key, Object obj) {
		try {
			// 保存对象
			Editor sharedata = context.getSharedPreferences(
					name, 0).edit();
			// 先将序列化结果写到byte缓存中，其实就分配一个内存空间
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bos);
			// 将对象序列化写入byte缓存
			os.writeObject(obj);
			// 将序列化的数据转为16进制保存
			String bytesToHexString = bytesToHexString(bos.toByteArray());
			// 保存该16进制数组
			sharedata.putString(key, bytesToHexString);
			sharedata.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * desc:将数组转为16进制
	 * 
	 * @param bArray
	 * @return modified:
	 */
	public static String bytesToHexString(byte[] bArray) {
		if (bArray == null) {
			return null;
		}
		if (bArray.length == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * desc:获取保存的Object对象
	 * 
	 * @param context
	 * @param key
	 * @return modified:
	 */
	public static Object readObject(Context context, String key) {
		try {
			SharedPreferences sharedata = context.getSharedPreferences(name, 0);
			if (sharedata.contains(key)) {
				String string = sharedata.getString(key, "");
				if (TextUtils.isEmpty(string)) {
					return null;
				} else {
					// 将16进制的数据转为数组，准备反序列化
					byte[] stringToBytes = StringToBytes(string);
					ByteArrayInputStream bis = new ByteArrayInputStream(
							stringToBytes);
					ObjectInputStream is = new ObjectInputStream(bis);
					// 返回反序列化得到的对象
					Object readObject = is.readObject();
					return readObject;
				}
			}
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 所有异常返回null
		return null;

	}

	/**
	 * desc:将16进制的数据转为数组
	 * <p>
	 * 创建人：聂旭阳 , 2014-5-25 上午11:08:33
	 * </p>
	 * 
	 * @param data
	 * @return modified:
	 */
	public static byte[] StringToBytes(String data) {
		String hexString = data.toUpperCase().trim();
		if (hexString.length() % 2 != 0) {
			return null;
		}
		byte[] retData = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i++) {
			int int_ch; // 两位16进制数转化后的10进制数
			char hex_char1 = hexString.charAt(i); // //两位16进制数中的第一位(高位*16)
			int int_ch1;
			if (hex_char1 >= '0' && hex_char1 <= '9')
				int_ch1 = (hex_char1 - 48) * 16; // // 0 的Ascll - 48
			else if (hex_char1 >= 'A' && hex_char1 <= 'F')
				int_ch1 = (hex_char1 - 55) * 16; // // A 的Ascll - 65
			else
				return null;
			i++;
			char hex_char2 = hexString.charAt(i); // /两位16进制数中的第二位(低位)
			int int_ch2;
			if (hex_char2 >= '0' && hex_char2 <= '9')
				int_ch2 = (hex_char2 - 48); // // 0 的Ascll - 48
			else if (hex_char2 >= 'A' && hex_char2 <= 'F')
				int_ch2 = hex_char2 - 55; // // A 的Ascll - 65
			else
				return null;
			int_ch = int_ch1 + int_ch2;
			retData[i / 2] = (byte) int_ch;// 将转化后的数放入Byte里
		}
		return retData;
	}
	
	public static void clean(Context context){
		// 保存对象
		Editor sharedata = context.getSharedPreferences(
				name, 0).edit();
		
		sharedata.clear();
		  sharedata.commit();
	}
	
	public static void clean(Context context, String key) {
		if (context != null) {

			SharedPreferences mSharedPreferences = context
					.getSharedPreferences(modelname, Context.MODE_PRIVATE);

			Editor editor = mSharedPreferences.edit();
			
			editor.clear().commit();
			
		}

	}
}
