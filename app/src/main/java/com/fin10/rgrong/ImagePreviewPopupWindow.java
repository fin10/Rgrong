package com.fin10.rgrong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public final class ImagePreviewPopupWindow implements DetailPageItem.ImageLinkResultListener {

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

    public void show(@NonNull View anchor, @NonNull PageItem item) {
        mPopupWindow.showAsDropDown(anchor);
        DetailPageItem.fetchImages(item, this);
        mWebView.setVisibility(View.GONE);
        mImageView.setVisibility(View.GONE);
    }

    public void dismiss() {
        mPopupWindow.dismiss();
        mTitleView.setText("");
    }

    @Override
    public void onResult(@NonNull List<String> imageLinks) {
        if (!imageLinks.isEmpty()) {
            String link = imageLinks.get(0);
            mTitleView.setText(link);

            if (link.endsWith(".gif")) {
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
}
