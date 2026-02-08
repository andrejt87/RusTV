package com.rustv.app;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout fullscreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

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
