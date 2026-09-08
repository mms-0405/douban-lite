package io.github.doubanlite;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import android.app.Activity;

public class DetailActivity extends Activity {
    public static final String EXTRA_URL = "url";
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContent());
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null || url.isEmpty()) {
            url = "https://m.douban.com/";
        }
        webView.loadUrl(url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private View createContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(0, insets.getSystemWindowInsetTop(), 0,
                    insets.getSystemWindowInsetBottom());
            return insets;
        });

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setPadding(dp(6), dp(4), dp(6), dp(4));
        toolbar.setGravity(android.view.Gravity.CENTER_VERTICAL);

        Button back = new Button(this);
        back.setText("返回");
        back.setOnClickListener(v -> finish());
        toolbar.addView(back, new LinearLayout.LayoutParams(dp(80), dp(48)));

        Button clearLogin = new Button(this);
        clearLogin.setText("退出登录");
        clearLogin.setOnClickListener(v -> clearCookies());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(dp(110), dp(48));
        clearParams.leftMargin = dp(6);
        toolbar.addView(clearLogin, clearParams);
        root.addView(toolbar, new LinearLayout.LayoutParams(-1, dp(56)));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        root.addView(progressBar, new LinearLayout.LayoutParams(-1, dp(3)));

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(115);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(false);
        settings.setSupportZoom(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        // Use a current mobile browser identity so the login page chooses its touch layout.
        settings.setUserAgentString(WebSettings.getDefaultUserAgent(this));
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setWebViewClient(new SimpleClient());
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setOnTouchListener((view, event) -> {
            // Prevent the toolbar/container from stealing the horizontal drag used by captcha sliders.
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                    event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        return root;
    }

    private void clearCookies() {
        CookieManager.getInstance().removeAllCookies(value ->
                Toast.makeText(this, "已清除登录状态", Toast.LENGTH_SHORT).show());
        CookieManager.getInstance().flush();
        webView.clearCache(true);
        webView.reload();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

        private class SimpleClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            boolean loginPage = url != null && url.contains("accounts.douban.com");
            String css = readAsset("mobile.css");
            String enhance = readAsset("enhance.js");
            String safeCss = css.replace("\\", "\\\\").replace("`", "\\`");
            String script = "(function(){var s=document.createElement('style');s.textContent=`" + safeCss + "`;document.head.appendChild(s);" +
                    (loginPage ? "var c=document.createElement('style');c.textContent='.geetest_panel,.captcha-container{z-index:2147483647!important;overflow:visible!important;} .geetest_box,.geetest_holder,.geetest_widget{max-width:calc(100vw - 16px)!important;max-height:none!important;overflow:visible!important;} .geetest_btn,.geetest_slider,.geetest_radar{touch-action:none!important;pointer-events:auto!important;} iframe[src*=captcha],iframe[src*=geetest]{width:100%!important;height:420px!important;min-height:320px!important;max-height:none!important;}';document.head.appendChild(c);" : "") +
                    (loginPage ? "document.documentElement.style.fontSize='100%';" :
                            "document.documentElement.style.fontSize='110%';document.body.style.fontSize='16px';") +
                    "try{" + enhance + "}catch(e){} })()";
            view.evaluateJavascript(script, null);
        }

        private String readAsset(String name) {
            try (InputStream input = getAssets().open(name);
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
                return new String(output.toByteArray(), StandardCharsets.UTF_8);
            } catch (IOException error) {
                return "";
            }
        }
    }
}
