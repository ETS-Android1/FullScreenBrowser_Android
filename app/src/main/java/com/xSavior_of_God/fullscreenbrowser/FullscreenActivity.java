package com.xSavior_of_God.fullscreenbrowser;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.xSavior_of_God.fullscreenbrowser.databinding.ActivityFullscreenBinding;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class FullscreenActivity extends AppCompatActivity implements View.OnTouchListener, NetworkStateReceiver.NetworkStateReceiverListener {
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
  private static final int UI_ANIMATION_DELAY = 300;
  private final Handler mHideHandler = new Handler();
  private View mContentView;
  private View mControlsView;
  private boolean mVisible;
  private ActivityFullscreenBinding binding;
  public WebView webView;
  private WebViewClient client;
  public static FullscreenActivity instance = null;
  private NetworkStateReceiver networkStateReceiver;
  public int refresh;
  private SharedPreferences mPrefs = null;
  private TimerTask mTimerTask;
  private Timer t = new Timer();
  public boolean viewOpen = false;
  public boolean restartActivity = true;

  /*
  Qui chiamo l'evento che innesca l'apertura delle impostazioni.
   */
  private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (FullscreenActivity.instance.viewOpen == false) {
        FullscreenActivity.instance.viewOpen = true;
        settingsDialog dialog = new settingsDialog();
        dialog.show(getSupportFragmentManager(), "Settings");
      }
      return true;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    String ipAddress = null;
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress()) {
            ipAddress = inetAddress.getHostAddress().toString();
          }
        }
      }
    } catch (SocketException ex) {
    }

    String display = "Network Info\n" + "IP: " + ipAddress;
    Toast.makeText(getApplicationContext(), display, Toast.LENGTH_LONG).show();
    super.onCreate(savedInstanceState);
    instance = this;
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

      @Override
      public void uncaughtException(Thread t, Throwable e) {
        System.out.println("CRASHED!!!");
        reboot();
        System.out.println("CRASHED!!!");
      }
    });

    mPrefs = getSharedPreferences("label", 0);

    networkStateReceiver = new NetworkStateReceiver();
    networkStateReceiver.addListener(this);
    this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    mVisible = true;
    mControlsView = binding.fullscreenContentControls;
    mContentView = binding.fullscreenContentControls;

    webView = findViewById(R.id.site);
    webView.setOnTouchListener(this);
    client = new WebViewClient() {
      public boolean timeout;

      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {

        new Thread(new Runnable() {
          @Override
          public void run() {
            timeout = true;

            try {
              Thread.sleep(300000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            if (timeout) {
              view.stopLoading();
              FullscreenActivity.instance.refreshUrl(FullscreenActivity.instance.mPrefs.getString("url", "http://10.4.1.9/televisori/@17"));
              System.out.println("URL TIMEOUT - Auto Refresh");
            } else {
              System.out.println("OK");
            }
          }
        }).start();
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        timeout = false;
      }

      @Override
      public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (error.getDescription().equals("net::ERR_FAILED")) {
          System.out.println("ERROR " + error.getErrorCode() + " - " + error.getDescription() + " URL: '" + request.getUrl() + "'");
          Toast.makeText(FullscreenActivity.instance, "ERROR IGNORED! " + error.getErrorCode() + " - " + error.getDescription(), Toast.LENGTH_LONG).show();

        } else {
          System.out.println("ERROR " + error.getErrorCode() + " - " + error.getDescription() + " URL: '" + request.getUrl() + "'");
          Toast.makeText(FullscreenActivity.instance, "ERROR " + error.getErrorCode() + " - " + error.getDescription() + " URL: '" + request.getUrl() + "'", Toast.LENGTH_LONG).show();
          FullscreenActivity.instance.reboot();

        }
      }
    };
    webView.setWebViewClient(client);
    webView.setVerticalScrollBarEnabled(false);
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    webSettings.setSafeBrowsingEnabled(false);
    webSettings.setJavaScriptEnabled(true);
    String url = mPrefs.getString("url", "http://10.4.1.9/televisori/@17");
    this.refreshUrl(url);
    this.refresh = mPrefs.getInt("refresh", 43200); //60 * 60 * 12
    binding.settingsButton.setOnTouchListener(mDelayHideTouchListener);
  }

  @Override
  public void onDestroy() {
    System.out.println("Destroy!");
    Toast.makeText(getApplicationContext(), "Fase di chiusura!", Toast.LENGTH_LONG).show();
    super.onDestroy();
    this.stopTask();
    networkStateReceiver.removeListener(this);
    this.unregisterReceiver(networkStateReceiver);
    if(this.restartActivity)
      startActivity(new Intent(this.getBaseContext(), FullscreenActivity.class));
  }

  private final Runnable mHidePart2Runnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {
      if (Build.VERSION.SDK_INT >= 30) {
        mContentView.getWindowInsetsController().hide(
            WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
      } else {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
      }
    }
  };

  private final Runnable mShowPart2Runnable = new Runnable() {
    @Override
    public void run() {
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.show();
      }
      mControlsView.setVisibility(View.VISIBLE);
    }
  };

  private final Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    if (v.getId() == R.id.site && event.getAction() == MotionEvent.ACTION_DOWN) {
      toggle();
    }
    return false;
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    delayedHide(100);
  }

  private void toggle() {
    if (mVisible) {
      hide();
    } else {
      show();
      delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }
  }

  private void hide() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
    mControlsView.setVisibility(View.GONE);
    mVisible = false;
    mHideHandler.removeCallbacks(mShowPart2Runnable);
    mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
  }

  private void show() {
    if (Build.VERSION.SDK_INT >= 30) {
      mContentView.getWindowInsetsController().show(
          WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
    } else {
      mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    mVisible = true;

    // Schedule a runnable to display UI elements after a delay
    mHideHandler.removeCallbacks(mHidePart2Runnable);
    mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
  }

  private void delayedHide(int delayMillis) {
    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }

  @Override
  public void onPointerCaptureChanged(boolean hasCapture) {
  }

  @Override
  public void networkAvailable() {
    this.webView.clearCache(true);
    this.webView.loadUrl(this.webView.getUrl());
  }

  @Override
  public void networkUnavailable() {
    Toast.makeText(this, "Network not avalible...", Toast.LENGTH_LONG).show();
    this.reboot();
  }

  public void doTimerTask() {
    Handler handler = new Handler(Looper.getMainLooper());
    mTimerTask = new TimerTask() {
      public void run() {
        handler.post(new Runnable() {
          public void run() {
            FullscreenActivity.instance.webView.clearCache(true);
            FullscreenActivity.instance.webView.loadUrl(FullscreenActivity.instance.webView.getUrl());
          }
        });
      }
    };
    t.schedule(mTimerTask, 500, refresh * 1000);
  }

  public void stopTask() {
    if (mTimerTask != null) {
      mTimerTask.cancel();
    }
    mTimerTask = null;
  }

  public void refreshUrl(String URL) {
    webView.clearCache(true);
    webView.clearHistory();
    webView.clearFormData();
    webView.clearSslPreferences();

    SharedPreferences.Editor mEditor = mPrefs.edit();
    mEditor.putString("url", URL).commit();
    webView.loadUrl(URL);
  }

  public void updateRefresh(int refresh) {
    stopTask();
    this.refresh = refresh;
    SharedPreferences.Editor mEditor = mPrefs.edit();
    mEditor.putInt("refresh", this.refresh).commit();
    doTimerTask();
  }

  public void reboot() {
    try {
      Toast.makeText(this, "Restarting...", Toast.LENGTH_LONG).show();
      Runtime.getRuntime().exec("reboot");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onStop() {
    System.out.println("Stopped!");
    super.onStop();
    Toast.makeText(this, "Stopped... "+this.restartActivity, Toast.LENGTH_LONG).show();
    if(this.restartActivity)
      startActivity(new Intent(this.getBaseContext(), FullscreenActivity.class));
  }


  public void closeApp(View view) {
    this.restartActivity = false;
    finish();
    System.exit(0);
  }
}