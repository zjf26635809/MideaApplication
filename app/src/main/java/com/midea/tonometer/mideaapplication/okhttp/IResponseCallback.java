package com.midea.tonometer.mideaapplication.okhttp;


import com.midea.tonometer.mideaapplication.okhttp.bean.OkError;

public interface IResponseCallback {

	public void onSuccess(int tag, Object object);

	public void onError(int tag, OkError error);

}
