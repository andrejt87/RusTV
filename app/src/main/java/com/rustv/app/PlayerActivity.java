package com.rustv.app;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout fullscreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private List<Channel> channels;
    private int currentIndex = 0;
    private TextView channelOverlay;
    private Handler overlayHandler = new Handler();

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_player);

        webView = findViewById(R.id.webview);
        fullscreenContainer = findViewById(R.id.fullscreen_container);

        // Load channel list
        channels = loadChannels();
        currentIndex = getIntent().getIntExtra("channelIndex", 0);
        if (currentIndex < 0 || currentIndex >= channels.size()) {
            currentIndex = 0;
        }

        // Channel name overlay
        channelOverlay = new TextView(this);
        channelOverlay.setTextColor(0xFFFFFFFF);
        channelOverlay.setTextSize(24);
        channelOverlay.setBackgroundColor(0xAA000000);
        channelOverlay.setPadding(32, 16, 32, 16);
        channelOverlay.setVisibility(View.GONE);
        FrameLayout root = findViewById(android.R.id.content);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.topMargin = 40;
        root.addView(channelOverlay, lp);

        // WebView settings
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportMultipleWindows(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // For tas-ix.tv: hide everything except the player, make it fullscreen
                if (url != null && url.contains("tas-ix.tv")) {
                    view.evaluateJavascript(
                        "(function() {" +
                        "  var css = document.createElement('style');" +
                        "  css.textContent = '" +
                        "    body > *:not(#playerjs1):not(script) { display: none !important; } " +
                        "    body { margin: 0 !important; padding: 0 !important; background: #000 !important; overflow: hidden !important; } " +
                        "    #playerjs1 { position: fixed !important; top: 0 !important; left: 0 !important; width: 100vw !important; height: 100vh !important; z-index: 99999 !important; } " +
                        "    .header, .sidebar, .footer, nav, .ad, .reklama, #reklama, .menu { display: none !important; } " +
                        "  ';" +
                        "  document.head.appendChild(css);" +
                        "  var p = document.getElementById('playerjs1');" +
                        "  if (!p) {" +
                        "    var allDivs = document.querySelectorAll('div[id*=player]');" +
                        "    if (allDivs.length > 0) { p = allDivs[0]; }" +
                        "  }" +
                        "  if (p) { document.body.appendChild(p); }" +
                        "})();", null);
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                customView = view;
                customViewCallback = callback;
                fullscreenContainer.addView(view);
                fullscreenContainer.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                fullscreenContainer.removeView(customView);
                fullscreenContainer.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }
                customView = null;
                customViewCallback = null;
            }
        });

        String url = getIntent().getStringExtra("url");
        if (url != null) {
            webView.loadUrl(url);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_CHANNEL_UP:
                switchChannel(-1);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                switchChannel(1);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void switchChannel(int direction) {
        if (channels == null || channels.isEmpty()) return;

        currentIndex = (currentIndex + direction + channels.size()) % channels.size();
        Channel ch = channels.get(currentIndex);

        // Show overlay with channel name
        showChannelOverlay(ch.title);

        // Load new channel
        webView.loadUrl(ch.url);
    }

    private void showChannelOverlay(String name) {
        channelOverlay.setText(name);
        channelOverlay.setVisibility(View.VISIBLE);
        channelOverlay.bringToFront();

        overlayHandler.removeCallbacksAndMessages(null);
        overlayHandler.postDelayed(() -> {
            channelOverlay.setVisibility(View.GONE);
        }, 3000);
    }

    private List<Channel> loadChannels() {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("channels.json")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            Type listType = new TypeToken<List<Channel>>(){}.getType();
            return new Gson().fromJson(sb.toString(), listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            fullscreenContainer.removeView(customView);
            fullscreenContainer.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
            }
            customView = null;
            customViewCallback = null;
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }
}
