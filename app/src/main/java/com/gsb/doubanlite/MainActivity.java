package io.github.doubanlite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private EditText searchInput;
    private TextView loginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginStatus();
    }

    private View createContent() {
        int padding = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, dp(24), padding, padding);
        root.setBackgroundColor(Color.rgb(250, 250, 250));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.TOP);

        TextView title = new TextView(this);
        title.setText("\u8c46\u74e3\u7b80\u6d01\u7248");
        title.setTextColor(Color.rgb(30, 30, 30));
        title.setTextSize(28);
        title.setTypeface(null, 1);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        loginStatus = new TextView(this);
        loginStatus.setTextSize(15);
        loginStatus.setGravity(Gravity.END);
        loginStatus.setPadding(dp(10), dp(8), 0, 0);
        header.addView(loginStatus, new LinearLayout.LayoutParams(-2, -2));
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("\u53ea\u770b\u8bc4\u5206\u548c\u8bc4\u8bba\uff0c\u5feb\u901f\u6807\u8bb0\u770b\u8fc7\u3001\u5b8c\u6210\u8bc4\u5206");
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setTextSize(16);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(8);
        root.addView(subtitle, subtitleParams);

        searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setTextSize(18);
        searchInput.setHint("\u8f93\u5165\u7535\u5f71\u540d\u79f0");
        searchInput.setPadding(dp(14), 0, dp(14), 0);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(-1, dp(56));
        inputParams.topMargin = dp(28);
        root.addView(searchInput, inputParams);

        Button searchButton = new Button(this);
        searchButton.setText("\u641c\u7d22\u7535\u5f71");
        searchButton.setTextSize(17);
        searchButton.setOnClickListener(v -> search());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, dp(56));
        buttonParams.topMargin = dp(12);
        root.addView(searchButton, buttonParams);

        Button loginButton = new Button(this);
        loginButton.setText("\u767b\u5f55\u8c46\u74e3");
        loginButton.setTextSize(16);
        loginButton.setOnClickListener(v -> openPage("https://accounts.douban.com/passport/login"));
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(-1, dp(52));
        loginParams.topMargin = dp(24);
        root.addView(loginButton, loginParams);

        Button watchedButton = new Button(this);
        watchedButton.setText("\u6211\u7684\u5df2\u770b\u8fc7");
        watchedButton.setTextSize(16);
        watchedButton.setOnClickListener(v -> openPage("https://m.douban.com/mine/movie?status=collect"));
        LinearLayout.LayoutParams watchedParams = new LinearLayout.LayoutParams(-1, dp(52));
        watchedParams.topMargin = dp(10);
        root.addView(watchedButton, watchedParams);

        Button nowPlayingButton = new Button(this);
        nowPlayingButton.setText("\u6700\u8fd1\u4e0a\u6620");
        nowPlayingButton.setTextSize(16);
        nowPlayingButton.setOnClickListener(v -> openPage("https://movie.douban.com/cinema/nowplaying/"));
        LinearLayout.LayoutParams nowPlayingParams = new LinearLayout.LayoutParams(-1, dp(52));
        nowPlayingParams.topMargin = dp(10);
        root.addView(nowPlayingButton, nowPlayingParams);

        TextView hint = new TextView(this);
        hint.setText("\u767b\u5f55\u5728\u8c46\u74e3\u7f51\u9875\u4e2d\u5b8c\u6210\uff0c\u672c\u5e94\u7528\u4e0d\u4f1a\u8bfb\u53d6\u6216\u4fdd\u5b58\u5bc6\u7801\u3002\n\u8bc4\u5206\u548c\u201c\u770b\u8fc7\u201d\u64cd\u4f5c\u5c06\u5728\u7535\u5f71\u9875\u9762\u4e2d\u5b8c\u6210\u3002");
        hint.setTextColor(Color.GRAY);
        hint.setTextSize(14);
        hint.setLineSpacing(4, 1.0f);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(-1, -2);
        hintParams.topMargin = dp(14);
        root.addView(hint, hintParams);
        return root;
    }

    private void updateLoginStatus() {
        if (loginStatus == null) {
            return;
        }
        CookieManager cookieManager = CookieManager.getInstance();
        boolean loggedIn = hasCookie(cookieManager.getCookie("https://www.douban.com/"), "dbcl2")
                || hasCookie(cookieManager.getCookie("https://m.douban.com/"), "dbcl2")
                || hasCookie(cookieManager.getCookie("https://movie.douban.com/"), "dbcl2");
        loginStatus.setText(loggedIn ? "\u5df2\u767b\u5f55" : "\u672a\u767b\u5f55");
        loginStatus.setTextColor(loggedIn ? Color.rgb(0, 145, 88) : Color.rgb(117, 117, 117));
    }

    private boolean hasCookie(String cookies, String name) {
        if (cookies == null || cookies.isEmpty()) {
            return false;
        }
        String prefix = name + "=";
        for (String cookie : cookies.split(";")) {
            if (cookie.trim().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void search() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            searchInput.setError("\u8bf7\u8f93\u5165\u7535\u5f71\u540d\u79f0");
            return;
        }
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        openPage("https://m.douban.com/search/?query=" + Uri.encode(query));
    }

    private void openPage(String url) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_URL, url);
        startActivity(intent);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
