package com.fin10.rgrong;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BoardListView extends FrameLayout implements BoardItem.ResultListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "BoardListView";

    private BoardItemListAdapter mBoardItemListAdapter;
    private BoardItemClickListener mItemClickListener;
    private View mEmptyView;
    private SwipeRefreshLayout mRefreshLayout;

    public BoardListView(Context context) {
        super(context);
        init(context);
    }

    public BoardListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BoardListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.board_list_view, this, false);
        addView(root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mEmptyView = root.findViewById(R.id.empty_text_view);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mBoardItemListAdapter = new BoardItemListAdapter(inflater);

        ListView listView = (ListView) root.findViewById(R.id.list_view);
        listView.setAdapter(mBoardItemListAdapter);
        listView.setOnItemClickListener(this);

        mRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    public void setOnItemClickListener(@Nullable BoardItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public void onResult(@NonNull List<BoardItem> items) {
        mBoardItemListAdapter.setItems(items);
        mEmptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mItemClickListener != null) {
            BoardItem item = (BoardItem) mBoardItemListAdapter.getItem(position);
            Log.d(TAG, String.format("[%d] %s", position, item));
            mItemClickListener.onBoardItemClicked(item);
        }
    }

    @Override
    public void onRefresh() {
        BoardItem.fetch(this);
        mBoardItemListAdapter.setItems(Collections.<BoardItem>emptyList());
        mEmptyView.setVisibility(View.GONE);
    }

    public interface BoardItemClickListener {
        void onBoardItemClicked(@NonNull BoardItem item);
    }

    private static class BoardItemListAdapter extends BaseAdapter {

        private final List<BoardItem> mItems = new ArrayList<>();
        private final LayoutInflater mLayoutInflater;

        public BoardItemListAdapter(@NonNull LayoutInflater inflater) {
            mLayoutInflater = inflater;
        }

        public void setItems(@NonNull List<BoardItem> items) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.board_list_view_item, parent, false);
                convertView.setTag(R.id.item_name_view, convertView.findViewById(R.id.item_name_view));
            }

            BoardItem item = (BoardItem) getItem(position);
            TextView nameView = (TextView) convertView.getTag(R.id.item_name_view);
            nameView.setText(item.getName());

            return convertView;
        }
    }
}
