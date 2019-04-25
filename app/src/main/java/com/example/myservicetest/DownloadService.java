package com.example.myservicetest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {
    private DownloadAsyncTask myAsyncTask;
    private String downloadUrl;
    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("downloading", progress));
        }

        @Override
        public void onSuccess() {
            myAsyncTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("download success", -1));
            Toast.makeText(DownloadService.this, "download success", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onFail() {
            myAsyncTask = null;
            getNotificationManager().notify(1, getNotification("download fail", -1));
            Toast.makeText(DownloadService.this, "download fail", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {
            myAsyncTask = null;
            Toast.makeText(DownloadService.this, "download pause", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            myAsyncTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "download cancel", Toast.LENGTH_SHORT).show();
        }
    };
    private DownloadBinder mBinder = new DownloadBinder();

    public DownloadService() {
        Log.e("hel", "DownloadService: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("hel", "onBind: ");
        return mBinder;
    }


    class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (myAsyncTask == null) {
                downloadUrl = url;
                myAsyncTask = new DownloadAsyncTask(downloadListener);
                myAsyncTask.execute(downloadUrl);
                startForeground(1, getNotification("downloading", 0));
                Toast.makeText(DownloadService.this, "downloading", Toast.LENGTH_SHORT).show();
            }

        }

        public void pauseDownload() {
            if (myAsyncTask != null) {
                myAsyncTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (myAsyncTask != null) {
                myAsyncTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String fileDiretory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(fileDiretory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "cancel", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}
