package com.xSavior_of_God.fullscreenbrowser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;


public class service extends Service {
  private static final String TAG = "MyService";
  int i = 0;

  @Override
  public void onCreate() {
    super.onCreate();

    String NOTIFICATION_CHANNEL_ID = "com.xSavior_of_God.fullscreenbrowser";
    String channelName = "My Background Service";
    NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
    chan.setLightColor(Color.BLUE);
    chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    assert manager != null;
    manager.createNotificationChannel(chan);

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
    Notification notification = notificationBuilder.setOngoing(true)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("App is running in background")
        .setPriority(NotificationManager.IMPORTANCE_MIN)
        .setCategory(Notification.CATEGORY_SERVICE)
        .build();

    startForeground(2, notification);
  }

  @Override
  public IBinder onBind(Intent intent) {
    Toast.makeText(this, "Service Binded!", Toast.LENGTH_LONG).show();
    return null;
  }

  @Override
  public void onDestroy() {
    Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onStart(Intent intent, int startid) {
    i++;
    Toast.makeText(this, "Service Starting... " + i, Toast.LENGTH_LONG).show();
    Intent intents = new Intent(getApplicationContext(), FullscreenActivity.class);
    Toast.makeText(this, "A0 " + i, Toast.LENGTH_LONG).show();
    intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    Toast.makeText(this, "B0 " + i, Toast.LENGTH_LONG).show();
    getApplicationContext().startActivity(intents);
    Toast.makeText(this, "C0 " + i, Toast.LENGTH_LONG).show();
    Toast.makeText(this, "D0 " + i, Toast.LENGTH_LONG).show();

    Toast.makeText(this, "Service Started " + i, Toast.LENGTH_LONG).show();
  }

}
