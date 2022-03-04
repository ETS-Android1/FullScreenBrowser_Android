package com.xSavior_of_God.fullscreenbrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootUpReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent arg1) {
    Toast.makeText(context, "STARTING...", Toast.LENGTH_LONG).show();
    try {
      // monkey -p com.xSavior_of_God.fullscreenbrowser -c android.intent.category.LAUNCHER 1
      // am start -n com.xSavior_of_God.fullscreenbrowser/com.xSavior_of_God.fullscreenbrowser.FullscreenActivity
      //Runtime.getRuntime().exec(new String[] {"su", "am start com.xSavior_of_God.fullscreenbrowser/.FullscreenActivity"});
      //Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.android.tv.settings");
      //launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      //context.startActivity(launchIntent);

      Intent myIntent = new Intent(context, FullscreenActivity.class);
      myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(myIntent);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Toast.makeText(context, "START DONE!", Toast.LENGTH_LONG).show();
  }



}

