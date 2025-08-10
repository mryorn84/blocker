package com.example.youtubeblocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MonitorService extends Service {
    private static final String TAG = "MonitorService";
    private static final int NOTIF_ID = 1;
    private static final String CHANNEL_ID = "yt_blocker_channel";

    private volatile boolean running = false;
    private HandlerThread thread;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());

        thread = new HandlerThread("monitor-thread");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        handler.post(this::monitorLoop);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        if (thread != null) thread.quitSafely();
        super.onDestroy();
    }

    private void monitorLoop() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        String targetPackage = "com.google.android.youtube";

        while (running) {
            try {
                long end = System.currentTimeMillis();
                long begin = end - 2000;
                UsageEvents events = usm.queryEvents(begin, end);
                UsageEvents.Event event = new UsageEvents.Event();

                while (events.hasNextEvent()) {
                    events.getNextEvent(event);
                    if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND &&
                            targetPackage.equals(event.getPackageName())) {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(homeIntent);
                        Log.i(TAG, "Detected YouTube in foreground — sending HOME");
                    }
                }

                Thread.sleep(500);
            } catch (Exception e) {
                Log.e(TAG, "Error in monitorLoop:", e);
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("YouTube Blocker")
                .setContentText("Monitoring apps…")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "YouTube Blocker", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
