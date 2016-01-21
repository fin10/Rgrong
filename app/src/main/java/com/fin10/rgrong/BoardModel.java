package com.fin10.rgrong;

import android.content.Context;
import android.content.SharedPreferences;
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

public final class BoardModel {

    private static final String PREF_KEY_LAST_BOARD_ITEM = "pref_key_last_board_item";

    private static final String URL = "http://te31.com/m/main.php";

    private final String mName;
    private final String mId;

    private BoardModel(@NonNull Element td) {
        mName = td.text();

        Elements links = td.getElementsByTag("a");
        Element link = links.get(0);
        String href = link.attr("href");
        mId = href.substring(href.lastIndexOf("id=") + "id=".length());
    }

    private BoardModel(@NonNull String name, @NonNull String id) {
        mName = name;
        mId = id;
    }

    private BoardModel(@NonNull String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        mName = jsonObject.getString("name");
        mId = jsonObject.getString("id");
    }

    @NonNull
    public static List<BoardModel> getBoards() {
        try {
            Connection connection = Jsoup.connect(URL);
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

    @NonNull
    private String toJsonString() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", mName);
        jsonObject.put("id", mId);

        return jsonObject.toString();
    }
}