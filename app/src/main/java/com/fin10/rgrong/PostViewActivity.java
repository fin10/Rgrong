package com.fin10.rgrong;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public final class PostViewActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;
    private PostModel mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPost = getIntent().getParcelableExtra("post");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mPost.getAuthor());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mPost.getUrl());
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
                return true;
            case R.id.menu_refresh:
                mRefreshLayout.setRefreshing(true);
                onRefresh();
                return true;
        }

        return false;
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }
}
