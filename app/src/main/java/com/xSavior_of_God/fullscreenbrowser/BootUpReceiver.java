package com.xSavior_of_God.fullscreenbrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.io.IOException;

public class BootUpReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent arg1) {
    Toast.makeText(context, "STARTING...", Toast.LENGTH_LONG).show();
    if (arg1.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
      Toast.makeText(context, "arg1.getAction().equals(Intent.ACTION_BOOT_COMPLETED)", Toast.LENGTH_LONG).show();

    }
    Intent intent = new Intent(context,FullscreenActivity.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startForegroundService(intent);
      Toast.makeText(context, "TRUE!", Toast.LENGTH_LONG).show();
    } else {
      context.startService(intent);
      Toast.makeText(context, "FALSE!", Toast.LENGTH_LONG).show();
    }


    Toast.makeText(context, "START DONE!", Toast.LENGTH_LONG).show();
  }



}

