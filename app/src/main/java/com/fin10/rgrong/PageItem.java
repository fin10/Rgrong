package com.fin10.rgrong;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
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

public final class PageItem {

    private static final String TAG = "PageItem";
    private static final String URL = "http://te31.com/m/mlist.php?id=rgrong";

    private final String mText;
    private final String mId;
    private final String mComments;
    private final String mAuthor;
    private final String mDate;

    PageItem(@NonNull Element page) throws NullPointerException {
        Elements tds = page.getElementsByTag("td");
        mComments = !TextUtils.isEmpty(tds.get(0).text()) ? tds.get(0).text() : "0";
        mText = tds.get(1).text();
        mAuthor = tds.get(2).text();
        mDate = tds.get(3).text();

        Element td = tds.get(1);
        Elements links = td.getElementsByTag("a");
        Element link = links.get(0);
        String href = link.attr("href");
        mId = href.substring(href.lastIndexOf("no=") + "no=".length());
    }

    public static void fetch(final int page, @NonNull final ResultListener listener) {
        new AsyncTask<Void, Void, List<PageItem>>() {

            @Override
            protected List<PageItem> doInBackground(Void... params) {

                try {
                    String url = URL
                            + "&page="
                            + page;

                    Connection connection = Jsoup.connect(url);
                    Document doc = connection.get();

                    List<PageItem> pageItems = new ArrayList<>();
                    Elements tables = doc.getElementsByTag("table");
                    int count = tables.size();
                    for (int i = 0; i < count; ++i) {
                        Element table = tables.get(i);
                        String value = table.attr("onclick");
                        if (value == null) {
                            Log.e(TAG, String.format("[%d] not found onclick.", i));
                            continue;
                        }

                        Elements tds = table.getElementsByTag("td");
                        if (tds == null || tds.size() < 2) {
                            Log.e(TAG, String.format("[%d] td size:%d.", i, tds != null ? tds.size() : -1));
                            continue;
                        }

                        Element td = tds.get(1);
                        Log.d(TAG, String.format("[%d] %s", i, td.text()));
                        Elements imgs = td.getElementsByTag("img");
                        if (imgs == null || imgs.size() == 0) {
                            continue;
                        }

                        Element img = imgs.get(0);
                        String src = img.attr("src");
                        Log.d(TAG, String.format("src:%s", src));
                        if ("image_up.gif".equalsIgnoreCase(src)) {
                            try {
                                pageItems.add(new PageItem(table));
                            } catch (NullPointerException e) {
                            }
                        }
                    }

                    return pageItems;

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return Collections.emptyList();
            }

            @Override
            protected void onPostExecute(List<PageItem> pageItems) {
                Log.d(TAG, String.format("size:%d", pageItems.size()));
                listener.onResult(page, pageItems);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getTitle() {
        return mText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageItem pageItem = (PageItem) o;

        return mId.equals(pageItem.mId);
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @NonNull
    public String getDate() {
        return mDate;
    }

    @NonNull
    public String getComments() {
        return mComments;
    }

    @NonNull
    public String getAuthor() {
        return mAuthor;
    }

    public interface ResultListener {
        void onResult(int page, @NonNull List<PageItem> pageItems);
    }

}
