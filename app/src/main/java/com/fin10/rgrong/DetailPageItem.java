package com.fin10.rgrong;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DetailPageItem {

    private static final String TAG = "DetailPageItem";
    private static final String DETAIL_URL = "http://te31.com/m/view.php?id=rgrong";

    private final String mLink;

    DetailPageItem(@NonNull String src) {
        mLink = src;
    }

    public static void fetch(@NonNull final String id, @NonNull final ResultListener listener) {
        new AsyncTask<Void, Void, DetailPageItem>() {

            @Override
            protected DetailPageItem doInBackground(Void... params) {

                String url = DETAIL_URL
                        + "&no="
                        + id;

                return new DetailPageItem(url);
            }

            @Override
            protected void onPostExecute(DetailPageItem detailPageItem) {
                listener.onResult(detailPageItem);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void fetchImages(@NonNull final PageItem item, @NonNull final ImageLinkResultListener listener) {
        new AsyncTask<Void, Void, List<String>>() {

            @Override
            protected List<String> doInBackground(Void... params) {
                try {
                    String url = DETAIL_URL
                            + "&no="
                            + item.getId();

                    Connection connection = Jsoup.connect(url);
                    Document doc = connection.get();

                    List<String> links = new ArrayList<>();
                    Elements tables = doc.getElementsByTag("table");
                    int count = tables.size();
                    for (int i = 0; i < count; ++i) {
                        Element table = tables.get(i);
                        Elements imgs = table.getElementsByTag("img");
                        if (imgs == null || imgs.size() == 0) {
                            continue;
                        }

                        int imgCount = imgs.size();
                        for (int j = 0; j < imgCount; ++j) {
                            Element img = imgs.get(j);
                            String src = img.attr("src");
                            if (!TextUtils.isEmpty(src) && src.startsWith("http://te31.com/rgr/data")) {
                                Log.d(TAG, String.format("src:%s", src));
                                links.add(src);
                            }
                        }
                    }

                    return links;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return Collections.emptyList();
            }

            @Override
            protected void onPostExecute(List<String> imageLinks) {
                listener.onResult(imageLinks);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public String getLink() {
        return mLink;
    }

    public interface ImageLinkResultListener {
        void onResult(@NonNull List<String> imageLinks);
    }

    public interface ResultListener {
        void onResult(@NonNull DetailPageItem item);
    }
}
