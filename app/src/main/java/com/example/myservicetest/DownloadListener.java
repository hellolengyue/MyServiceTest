package com.example.myservicetest;

/**
 * @author hel
 * @date 2019/4/24
 * 文件 MyServiceTest
 * 描述
 */
public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFail();
    void onPause();
    void onCancel();
}
