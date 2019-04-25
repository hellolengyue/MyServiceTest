package com.example.myservicetest;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author hel
 * @date 2019/4/23
 * 文件 MyServiceTest
 * 参数1:执行AsyncTask时传入String作为参数
 * 参数2:使用Integer显示进度
 * 参数3:适应Integer反馈执行结果
 */
public class DownloadAsyncTask extends AsyncTask<String, Integer, Integer> {
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAIL = 1;
    public static final int TYPE_PAUSE = 2;
    public static final int TYPE_CANCEL = 3;
    private DownloadListener downloadListener;
    private boolean isPause;
    private boolean isCancel;
    private int lastProgress;

    public DownloadAsyncTask(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile randomAccessFile = null;
        File file = null;

        try {
            long downloadLentgh = 0;
            String downloadUrl = strings[0];
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);
            if (file.exists()) {
                downloadLentgh = file.length();
            }
            long contentLentgh = getContentLentgh(downloadUrl);
            if (contentLentgh == 0) {
                return TYPE_FAIL;
            } else if (contentLentgh == downloadLentgh) {
                //下载完成
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    //断点下载，指定从哪个字节开始下载
                    .addHeader("RANGE", "bytes = " + downloadLentgh + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(downloadLentgh);//跳过已下载的字节
                byte[] bytes = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(bytes)) != -1) {
                    if (isCancel) {
                        return TYPE_CANCEL;
                    } else if (isPause) {
                        return TYPE_PAUSE;
                    } else {
                        total += len;
                        randomAccessFile.write(bytes, 0, len);
                        int progress = (int) ((downloadLentgh + total) * 100 / contentLentgh);
                        publishProgress(progress);

                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (isCancel && file != null) {
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAIL;
    }

    private long getContentLentgh(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long l = response.body().contentLength();
            response.close();
            return l;
        }

        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0];
        if (progress > lastProgress) {
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }


    @Override
    protected void onPostExecute(Integer integer) {
//        super.onPostExecute(integer);
        switch (integer) {
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_FAIL:
                downloadListener.onFail();
                break;
            case TYPE_PAUSE:
                downloadListener.onPause();
                break;
            case TYPE_CANCEL:
                downloadListener.onCancel();
                break;
        }
    }

    public void pauseDownload() {
        isPause = true;
    }

    public void cancelDownload() {
        isCancel = true;
    }


}
