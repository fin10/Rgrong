package com.fin10.rgrong;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class DetailPageActivity extends Activity implements DetailPageItem.ResultListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "DetailPageActivity";
    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            String title = intent.getStringExtra("title");
            actionBar.setTitle(title);
        }

        String id = intent.getStringExtra("id");
        Log.d(TAG, String.format("id:%s", id));
        if (!TextUtils.isEmpty(id)) {
            DetailPageItem.fetch(id, this);
        }

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_refresh:
                mRefreshLayout.setRefreshing(true);
                onRefresh();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult(@Nullable DetailPageItem item) {
        mRefreshLayout.setRefreshing(false);

        if (item != null) {
            mWebView.loadUrl(item.getLink());
        }
    }

    @Override
    public void onRefresh() {
        String id = getIntent().getStringExtra("id");
        if (!TextUtils.isEmpty(id)) {
            DetailPageItem.fetch(id, this);
        }
    }
}
