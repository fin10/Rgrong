package com.fin10.rgrong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collection;

public final class ImagePreviewPopupWindow {

    private final WebView mWebView;
    private final ImageView mImageView;
    private final PopupWindow mPopupWindow;
    private final TextView mTitleView;

    public ImagePreviewPopupWindow(@NonNull Context context) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.image_preview, null);
        mWebView = (WebView) view.findViewById(R.id.web_view);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        mImageView = (ImageView) view.findViewById(R.id.image_view);
        mTitleView = (TextView) view.findViewById(R.id.file_name_view);

        int size = context.getResources().getDimensionPixelSize(R.dimen.preview_size);
        mPopupWindow = new PopupWindow(view, size, size);
    }

    public void show(@NonNull View anchor, @NonNull PostModel post) {
        mPopupWindow.showAsDropDown(anchor);
        mWebView.setVisibility(View.GONE);
        mImageView.setVisibility(View.GONE);

        new AsyncTask<PostModel, Void, Collection<String>>() {

            @Override
            protected Collection<String> doInBackground(PostModel... params) {
                return params[0].getThumbnailLinks();
            }

            @Override
            protected void onPostExecute(Collection<String> imageLinks) {
                if (!imageLinks.isEmpty()) {
                    String link = imageLinks.iterator().next();
                    mTitleView.setText(link);

                    if (link.endsWith(".gif") || link.endsWith(".GIF")) {
                        mImageView.setVisibility(View.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                        mWebView.loadUrl(link);
                    } else {
                        mImageView.setVisibility(View.VISIBLE);
                        mWebView.setVisibility(View.GONE);
                        Glide.with(mImageView.getContext())
                                .load(link)
                                .crossFade()
                                .centerCrop()
                                .into(mImageView);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, post);
    }

    public void dismiss() {
        mPopupWindow.dismiss();
        mTitleView.setText("");
    }
}
