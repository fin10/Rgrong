package com.fin10.rgrong;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fin10.rgrong.widget.EndlessScrollListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, PageItem.ResultListener, AdapterView.OnItemLongClickListener, BoardListView.BoardItemClickListener {

    private static final String TAG = "MainActivity";

    private int mPageCount = 1;
    private RgrongListAdapter mRgrongListAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private ImagePreviewPopupWindow mPopupWindow;
    private Drawer mDrawer;
    private BoardItem mBoardItem;
    private final EndlessScrollListener mEndlessScrollListener = new EndlessScrollListener() {

        @Override
        public boolean onLoadMore(int page, int totalItemsCount) {
            Log.d(TAG, String.format("page:%d, count:%d", page, totalItemsCount));
            PageItem.fetch(mBoardItem.getId(), mPageCount, MainActivity.this);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBoardItem = BoardItem.getLastItem(this);
        if (mBoardItem != null) {
            setTitle(mBoardItem.getName());
        }

        BoardListView boardListView = new BoardListView(this);
        boardListView.setOnItemClickListener(this);

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(false)
                .withStatusBarColorRes(android.R.color.transparent)
                .withCustomView(boardListView)
                .build();

        mPopupWindow = new ImagePreviewPopupWindow(this);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mRgrongListAdapter = new RgrongListAdapter(this);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mRgrongListAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setOnScrollListener(mEndlessScrollListener);

        onRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBoardItem.save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                mRefreshLayout.setRefreshing(true);
                onRefresh();
                return true;
            case R.id.menu_filter:
                Toast.makeText(this, "not ready yet.", Toast.LENGTH_LONG).show();
                return true;
            case android.R.id.home:
                if (mDrawer.isDrawerOpen()) {
                    mDrawer.closeDrawer();
                } else {
                    mDrawer.openDrawer();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PageItem pageItem = (PageItem) mRgrongListAdapter.getItem(position);
        Intent intent = new Intent(this, DetailPageActivity.class);
        intent.putExtra("title", pageItem.getAuthor());
        intent.putExtra("board_id", pageItem.getBoardId());
        intent.putExtra("id", pageItem.getId());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, int position, long id) {
        PageItem pageItem = (PageItem) mRgrongListAdapter.getItem(position);
        mPopupWindow.show(view, pageItem);

        parent.setOnTouchListener(new View.OnTouchListener() {

            private static final float THRESHOLD = 10.f;

            private float mPrevX = -1;
            private float mPrevY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (mPrevX < 0 || mPrevY < 0) {
                            mPrevX = event.getX();
                            mPrevY = event.getY();
                        } else {
                            float diffX = Math.abs(mPrevX - event.getX());
                            float diffY = Math.abs(mPrevY - event.getY());
                            if (diffX > THRESHOLD || diffY > THRESHOLD) {
                                mPopupWindow.dismiss();
                                parent.setOnTouchListener(null);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mPopupWindow.dismiss();
                        parent.setOnTouchListener(null);
                        break;
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public void onRefresh() {
        mPageCount = 1;
        mRgrongListAdapter.clear();
        if (mBoardItem != null) {
            PageItem.fetch(mBoardItem.getId(), mPageCount, this);
        }
    }

    @Override
    public void onResult(int page, @NonNull List<PageItem> pageItems) {
        mRefreshLayout.setRefreshing(false);
        mRgrongListAdapter.addItems(pageItems);
        mPageCount = page + 1;
    }

    @Override
    public void onBoardItemClicked(@NonNull BoardItem item) {
        mDrawer.closeDrawer();
        mBoardItem = item;
        setTitle(mBoardItem.getName());
        onRefresh();
    }

    private static class RgrongListAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private List<PageItem> mPageItems = new ArrayList<>();

        public RgrongListAdapter(@NonNull Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mPageItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mPageItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_rgrong_item, parent, false);
                convertView.setTag(R.id.title_text_view, convertView.findViewById(R.id.title_text_view));
                convertView.setTag(R.id.sub_title_text_view, convertView.findViewById(R.id.sub_title_text_view));
                convertView.setTag(R.id.comments_text_view, convertView.findViewById(R.id.comments_text_view));
                convertView.setTag(R.id.date_text_view, convertView.findViewById(R.id.date_text_view));
            }

            PageItem pageItem = (PageItem) getItem(position);
            TextView titleView = (TextView) convertView.getTag(R.id.title_text_view);
            titleView.setText(pageItem.getTitle());

            TextView subTitleVIew = (TextView) convertView.getTag(R.id.sub_title_text_view);
            subTitleVIew.setText(pageItem.getAuthor());

            TextView commentsView = (TextView) convertView.getTag(R.id.comments_text_view);
            commentsView.setText(pageItem.getComments());

            TextView dateView = (TextView) convertView.getTag(R.id.date_text_view);
            dateView.setText(pageItem.getDate());

            return convertView;
        }

        public void addItems(@NonNull List<PageItem> pageItems) {
            for (PageItem pageItem : pageItems) {
                if (!mPageItems.contains(pageItem)) {
                    mPageItems.add(pageItem);
                }
            }
            notifyDataSetChanged();
        }

        public void clear() {
            mPageItems.clear();
            notifyDataSetChanged();
        }
    }
}
