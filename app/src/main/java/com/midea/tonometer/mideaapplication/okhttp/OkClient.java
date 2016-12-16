package com.midea.tonometer.mideaapplication.okhttp;


import com.midea.tonometer.mideaapplication.okhttp.bean.OkTag;
import com.midea.tonometer.mideaapplication.okhttp.bean.RequestParam;
import com.midea.tonometer.mideaapplication.okhttp.progress.ProgressListener;

/**
 * Created by Paul on 15/12/8.
 */
public class OkClient {
    private static String TAG = OkClient.class.getSimpleName();

    private OkClient() {
    }

    /**
     * 普通post和get请求
     *
     * @param param    参数封装，不能为空
     * @param cls      返回的数据封装的类型，如果为null，则返回String
     * @param callback 回调接口，每一个回调接口与tag绑定
     */
    public static void request(RequestParam param, Class<?> cls, IResponseCallback callback) {
        OkTag OkTag = new OkTag(param.getTag());
        NetManager.getInstance().request(OkTag, param, cls, callback, null);
    }

    /**
     * 普通post和get请求
     *
     * @param param    参数封装，不能为空
     * @param cls      返回的数据封装的类型，如果为null，则返回String
     * @param callback 回调接口，每一个回调接口与tag绑定
     */
    public static void upload(RequestParam param, Class<?> cls, IResponseCallback callback) {
        OkTag OkTag = new OkTag(param.getTag());
        NetManager.getInstance().request(OkTag, param, cls, callback, null);
    }

    /**
     * 文件上传
     *
     * @param param    参数封装，不能为空
     * @param cls      返回的数据封装的类型，如果为null，则返回String
     * @param callback 回调接口，每一个回调接口与tag绑定
     * @param listener 上传进度监听  可为空
     */
    public static void upload(RequestParam param, Class<?> cls, IResponseCallback callback, ProgressListener listener) {
        OkTag OkTag = new OkTag(param.getTag());
        NetManager.getInstance().request(OkTag, param, cls, callback, listener);
    }

    /**
     * 文件下载
     *
     * @param param    参数封装，不能为空
     * @param cls      返回的数据封装的类型，如果为null，则返回String
     * @param callback 回调接口，每一个回调接口与tag绑定
     */
    public static void download(RequestParam param, Class<?> cls, IResponseCallback callback) {
        OkTag OkTag = new OkTag(param.getTag());
        NetManager.getInstance().request(OkTag, param, cls, callback, null);
    }

    /**
     * 文件下载
     *
     * @param param    参数封装，不能为空
     * @param cls      返回的数据封装的类型，如果为null，则返回String
     * @param callback 回调接口，每一个回调接口与tag绑定
     * @param listener 下载进度监听
     */
    public static void download(RequestParam param, Class<?> cls, IResponseCallback callback, ProgressListener listener) {
        OkTag OkTag = new OkTag(param.getTag());
        NetManager.getInstance().request(OkTag, param, cls, callback, listener);
    }

    /**
     * 取消请求
     *
     * @param tag
     */
    public static void cancelRequest(int... tag) {
        NetManager.getInstance().cancelRequest(tag);
    }

    public static void cancelAllRequest() {
        NetManager.getInstance().cancelAllRequest();

    }
}
