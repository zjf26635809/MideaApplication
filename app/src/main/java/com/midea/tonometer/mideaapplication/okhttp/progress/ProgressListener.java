package com.midea.tonometer.mideaapplication.okhttp.progress;

/**
 * Created by h2h on 2015/9/8.
 */
public interface ProgressListener {
        void onProgress(long bytesWritten, long contentLength, long percent);
}