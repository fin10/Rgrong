package com.fin10.rgrong;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BoardModel implements Parcelable {

    public static final Creator<BoardModel> CREATOR = new Creator<BoardModel>() {
        @Override
        public BoardModel createFromParcel(Parcel in) {
            return new BoardModel(in);
        }

        @Override
        public BoardModel[] newArray(int size) {
            return new BoardModel[size];
        }
    };

    private static final String PREF_KEY_LAST_BOARD_ITEM = "pref_key_last_board_item";
    private final String mName;
    private final String mId;
    private final int mNewPostCount;

    private BoardModel(@NonNull Element td) {
        String name = td.text().replace(" -", "");
        String[] results = name.split("[^0-9]+");
        mName = results.length == 2 ? name.replace(results[1], "").trim() : name;

        int count = 0;
        try {
            count = results.length == 2 ? Integer.parseInt(results[1]) : 0;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        mNewPostCount = count;

        Elements links = td.getElementsByTag("a");
        Element link = links.get(0);
        String href = link.attr("href");
        mId = href.substring(href.lastIndexOf("id=") + "id=".length());
    }

    private BoardModel(@NonNull String name, @NonNull String id) {
        mName = name;
        mId = id;
        mNewPostCount = 0;
    }

    private BoardModel(@NonNull String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        mName = jsonObject.getString("name");
        mId = jsonObject.getString("id");
        mNewPostCount = 0;
    }

    private BoardModel(@NonNull Parcel in) {
        mName = in.readString();
        mId = in.readString();
        mNewPostCount = in.readInt();
    }

    @NonNull
    public static List<BoardModel> getBoards() {
        try {
            Connection connection = Jsoup.connect(Constants.Url.MAIN);
            Document doc = connection.get();

            List<BoardModel> items = new ArrayList<>();
            Elements tds = doc.getElementsByTag("td");
            int count = tds.size();
            for (int i = 0; i < count; ++i) {
                Element td = tds.get(i);
                String value = td.attr("onclick");
                if (value == null || !value.startsWith("location.href='mlist.php?id=")) {
                    continue;
                }

                try {
                    items.add(new BoardModel(td));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            return items;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @Nullable
    public static BoardModel getLastItem(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(PREF_KEY_LAST_BOARD_ITEM, null);
        if (TextUtils.isEmpty(value)) {
            return new BoardModel("호기심해결", "rgrong");
        }

        try {
            return new BoardModel(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void save(@NonNull Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString(PREF_KEY_LAST_BOARD_ITEM, toJsonString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    public int getNewPostCount() {
        return mNewPostCount;
    }

    @NonNull
    private String toJsonString() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", mName);
        jsonObject.put("id", mId);

        return jsonObject.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mId);
        dest.writeInt(mNewPostCount);
    }
}
