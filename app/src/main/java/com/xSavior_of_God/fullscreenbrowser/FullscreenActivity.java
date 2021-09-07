package com.xSavior_of_God.fullscreenbrowser;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.xSavior_of_God.fullscreenbrowser.databinding.ActivityFullscreenBinding;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FullscreenActivity extends AppCompatActivity implements View.OnTouchListener, NetworkStateReceiver.NetworkStateReceiverListener {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
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
    private View mControlsView;
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
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            settingsDialog dialog = new settingsDialog();
            dialog.show(getSupportFragmentManager(), "Settings");
            return true;
        }
    };
    private ActivityFullscreenBinding binding;
    public WebView webView;
    private WebViewClient client;
    public static FullscreenActivity instance = null;
    private NetworkStateReceiver networkStateReceiver;
    public int refresh;
    private SharedPreferences mPrefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        mPrefs = getSharedPreferences("label", 0);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContentControls;

        webView = (WebView)findViewById(R.id.site);
        webView.setOnTouchListener(this);
        client = new WebViewClient();
        webView.setWebViewClient(client);
        webView.setVerticalScrollBarEnabled(false);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSafeBrowsingEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        String url = mPrefs.getString("url", "http://10.4.1.9/televisori/");
        this.refreshUrl(url);
        this.refresh = mPrefs.getInt("refresh",1000 * 60 * 60 * 12);

        binding.dummyButton.setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopTask();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.site && event.getAction() == MotionEvent.ACTION_DOWN){
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
    public void onPointerCaptureChanged(boolean hasCapture) { }

    @Override
    public void networkAvailable() {
        this.webView.clearCache(true);
        this.webView.loadUrl(this.webView.getUrl());
    }

    @Override
    public void networkUnavailable() { }



    private TimerTask mTimerTask;
    private Timer t = new Timer();

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

        t.schedule(mTimerTask, 500, refresh);

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

}