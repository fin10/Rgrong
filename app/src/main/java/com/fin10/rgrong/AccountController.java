package com.fin10.rgrong;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.LinkedList;
import java.util.List;

public final class AccountController {

    private static final List<LoginStateListener> sListeners = new LinkedList<>();

    public static void addListener(@NonNull LoginStateListener listener) {
        if (!sListeners.contains(listener)) {
            sListeners.add(listener);
        }
    }

    public static void removeListener(@NonNull LoginStateListener listener) {
        sListeners.remove(listener);
    }

    public static boolean isLogin() {
        String cookie = CookieManager.getInstance().getCookie(Constants.Url.MAIN);
        return !TextUtils.isEmpty(cookie);
    }

    public static void login(@NonNull Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void logout() {
        CookieManager.getInstance().removeAllCookie();
        if (sListeners.isEmpty()) return;

        Handler handler = new Handler();
        for (final LoginStateListener listener : sListeners) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onLoginStateChanged(false);
                }
            });
        }
    }

    public interface LoginStateListener {
        void onLoginStateChanged(boolean login);
    }

    public static final class LoginActivity extends AppCompatActivity {

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_webview);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.login);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            WebView webView = (WebView) findViewById(R.id.web_view);
            webView.loadUrl(Constants.Url.LOGIN);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d(String.valueOf(url));
                    if (url != null && url.startsWith(Constants.Url.MAIN_NEW)) {
                        finish();
                        if (!sListeners.isEmpty()) {
                            Handler handler = new Handler();
                            for (final LoginStateListener listener : sListeners) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onLoginStateChanged(true);
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }
}
