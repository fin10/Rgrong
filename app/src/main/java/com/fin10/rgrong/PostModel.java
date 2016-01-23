package com.fin10.rgrong;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PostModel implements Parcelable {

    public static final Creator<PostModel> CREATOR = new Creator<PostModel>() {
        @Override
        public PostModel createFromParcel(Parcel in) {
            return new PostModel(in);
        }

        @Override
        public PostModel[] newArray(int size) {
            return new PostModel[size];
        }
    };

    private static final String URL = "http://te31.com/m/mlist.php?id=";
    private final String mBoardId;
    private final String mId;
    private final String mTitle;
    private final int mCommentCount;
    private final boolean mHasImages;
    private final String mAuthor;
    private final String mDate;
    private List<String> mThumbnailLinks;

    private PostModel(@NonNull String boardId, @NonNull Element page, boolean hasImages) throws NullPointerException, IndexOutOfBoundsException {
        mBoardId = boardId;
        mHasImages = hasImages;
        Elements tds = page.getElementsByTag("td");
        mTitle = tds.get(1).text();
        mAuthor = tds.get(2).text();
        mDate = tds.get(3).text();

        int commentCount;
        try {
            commentCount = !TextUtils.isEmpty(tds.get(0).text()) ? Integer.parseInt(tds.get(0).text()) : 0;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            commentCount = -1;
        }
        mCommentCount = commentCount;

        Element td = tds.get(1);
        Elements links = td.getElementsByTag("a");
        Element link = links.get(0);
        String href = link.attr("href");
        mId = href.substring(href.lastIndexOf("no=") + "no=".length());
    }

    private PostModel(@NonNull Parcel in) {
        mBoardId = in.readString();
        mId = in.readString();
        mTitle = in.readString();
        mCommentCount = in.readInt();
        mHasImages = in.readByte() != 0;
        mAuthor = in.readString();
        mDate = in.readString();
        mThumbnailLinks = in.createStringArrayList();
    }

    @NonNull
    public static List<PostModel> getPosts(@NonNull final String id, final int page, Filter filter) {
        try {
            String url = URL
                    + id
                    + "&page="
                    + page;

            Connection connection = Jsoup.connect(url);
            Document doc = connection.get();

            List<PostModel> posts = new ArrayList<>();
            Elements tables = doc.getElementsByTag("table");
            int count = tables.size();
            for (int i = 0; i < count; ++i) {
                Element table = tables.get(i);
                String value = table.attr("onclick");
                if (value == null) {
                    Log.e("[%d] not found onclick.", i);
                    continue;
                }

                Elements tds = table.getElementsByTag("td");
                if (tds == null || tds.size() < 2) {
                    continue;
                }

                Element td = tds.get(1);
                Log.d("[%d] %s", i, td.text());
                Elements imgs = td.getElementsByTag("img");
                if (imgs == null || imgs.size() == 0) {
                    continue;
                }

                try {
                    boolean hasImages = false;
                    int c = imgs.size();
                    for (int j = 0; j < c; ++j) {
                        Element img = imgs.get(j);
                        if ("image_up.gif".equalsIgnoreCase(img.attr("src"))) {
                            hasImages = true;
                            break;
                        }
                    }

                    switch (filter) {
                        case NONE:
                            posts.add(new PostModel(id, table, hasImages));
                            break;
                        case ONLY_WITH_IMAGES:
                            if (hasImages) posts.add(new PostModel(id, table, true));
                            break;
                    }
                } catch (NullPointerException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

            return posts;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PostModel postModel = (PostModel) o;

        if (mBoardId != null ? !mBoardId.equals(postModel.mBoardId) : postModel.mBoardId != null) return false;
        return mId != null ? mId.equals(postModel.mId) : postModel.mId == null;

    }

    @Override
    public int hashCode() {
        int result = mBoardId != null ? mBoardId.hashCode() : 0;
        result = 31 * result + (mId != null ? mId.hashCode() : 0);
        return result;
    }

    @NonNull
    public String getDate() {
        return mDate;
    }

    public int getCommentCount() {
        return mCommentCount;
    }

    @NonNull
    public String getAuthor() {
        return mAuthor;
    }

    public boolean hasImages() {
        return mHasImages;
    }

    @NonNull
    public synchronized Collection<String> getThumbnailLinks() {
        if (mThumbnailLinks != null) {
            return mThumbnailLinks;
        }

        try {
            Connection connection = Jsoup.connect(getUrl());
            Document doc = connection.get();

            mThumbnailLinks = new ArrayList<>();
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
                        Log.d("src:%s", src);
                        mThumbnailLinks.add(src);
                    }
                }
            }

            return mThumbnailLinks;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @NonNull
    public String getUrl() {
        return Constants.Url.VIEW + "?id=" + mBoardId + "&no=" + mId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mBoardId);
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeInt(mCommentCount);
        dest.writeByte((byte) (mHasImages ? 1 : 0));
        dest.writeString(mAuthor);
        dest.writeString(mDate);
        dest.writeStringList(mThumbnailLinks);
    }

    public enum Filter {
        NONE, ONLY_WITH_IMAGES,
    }
}
