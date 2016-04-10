package com.fin10.rgrong.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fin10.rgrong.PostModel;
import com.fin10.rgrong.R;

import java.util.Collection;

public final class ImagePreviewPopupWindow {

    private final ImageView mImageView;
    private final PopupWindow mPopupWindow;
    private final TextView mTitleView;

    public ImagePreviewPopupWindow(@NonNull Context context) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.image_preview, null);

        mImageView = (ImageView) view.findViewById(R.id.image_view);
        mTitleView = (TextView) view.findViewById(R.id.file_name_view);

        int size = context.getResources().getDimensionPixelSize(R.dimen.preview_size);
        mPopupWindow = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, size);
    }

    public void show(@NonNull View anchor, @NonNull PostModel post) {
        mPopupWindow.showAsDropDown(anchor);
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

                    mImageView.setVisibility(View.VISIBLE);
                    Glide.with(mImageView.getContext())
                            .load(link)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .centerCrop()
                            .into(mImageView);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, post);
    }

    public void dismiss() {
        mPopupWindow.dismiss();
        mTitleView.setText("");
    }
}
