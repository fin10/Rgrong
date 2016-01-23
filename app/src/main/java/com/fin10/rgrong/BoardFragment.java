package com.fin10.rgrong;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fin10.rgrong.widget.EndlessScrollListener;
import com.fin10.rgrong.widget.ImagePreviewPopupWindow;

import java.util.ArrayList;
import java.util.List;

public final class BoardFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private int mPageCount = 1;
    private BoardModel mBoard;
    private PostModel.Filter mFilter = PostModel.Filter.NONE;
    private ImagePreviewPopupWindow mPopupWindow;
    private SwipeRefreshLayout mRefreshLayout;
    private PostAdapter mPostAdapter;
    private View mFooterView;

    private final EndlessScrollListener mEndlessScrollListener = new EndlessScrollListener() {

        @Override
        public boolean onLoadMore(int page, int totalItemsCount) {
            Log.d("page:%d, count:%d", page, totalItemsCount);
            mFooterView.setVisibility(View.VISIBLE);

            new AsyncTask<Void, Void, List<PostModel>>() {

                @Override
                protected List<PostModel> doInBackground(Void... params) {
                    return PostModel.getPosts(mBoard.getId(), mPageCount, mFilter);
                }

                @Override
                protected void onPostExecute(List<PostModel> result) {
                    mRefreshLayout.setRefreshing(false);
                    mFooterView.setVisibility(View.GONE);
                    mPostAdapter.addPosts(result);
                    ++mPageCount;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return true;
        }
    };

    public void setBoard(@NonNull BoardModel board, PostModel.Filter filter) {
        mFilter = filter;
        mBoard = board;
        onRefresh();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_board, container, false);
        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_layout);
        mRefreshLayout.setOnRefreshListener(this);

        Context context = root.getContext();
        mPopupWindow = new ImagePreviewPopupWindow(context);
        mPostAdapter = new PostAdapter(context);

        ListView listView = (ListView) root.findViewById(R.id.list_view);
        listView.setAdapter(mPostAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setOnScrollListener(mEndlessScrollListener);

        mFooterView = View.inflate(context, R.layout.list_loading_footer, null);
        listView.addFooterView(mFooterView, null, false);
        mFooterView.setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onRefresh() {
        mPageCount = 1;
        mPostAdapter.clear();
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PostModel post = (PostModel) parent.getItemAtPosition(position);
        Intent intent = new Intent(view.getContext(), PostViewActivity.class);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, int position, long id) {
        PostModel post = (PostModel) parent.getItemAtPosition(position);
        if (!post.hasImages()) return false;

        mPopupWindow.show(view, post);

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

    private static final class PostAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private List<PostModel> mPosts = new ArrayList<>();

        public PostAdapter(@NonNull Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mPosts.size();
        }

        @Override
        public Object getItem(int position) {
            return mPosts.get(position);
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
                convertView.setTag(R.id.image_icon_view, convertView.findViewById(R.id.image_icon_view));
            }

            PostModel post = (PostModel) getItem(position);
            TextView titleView = (TextView) convertView.getTag(R.id.title_text_view);
            titleView.setText(post.getTitle());

            TextView subTitleVIew = (TextView) convertView.getTag(R.id.sub_title_text_view);
            subTitleVIew.setText(post.getAuthor());

            TextView commentsView = (TextView) convertView.getTag(R.id.comments_text_view);
            commentsView.setText(String.valueOf(post.getCommentCount()));

            TextView dateView = (TextView) convertView.getTag(R.id.date_text_view);
            dateView.setText(post.getDate());

            View imageIconView = (View) convertView.getTag(R.id.image_icon_view);
            imageIconView.setVisibility(post.hasImages() ? View.VISIBLE : View.GONE);

            return convertView;
        }

        public void addPosts(@NonNull List<PostModel> posts) {
            for (PostModel post : posts) {
                if (!mPosts.contains(post)) {
                    mPosts.add(post);
                }
            }
            notifyDataSetChanged();
        }

        public void clear() {
            mPosts.clear();
            notifyDataSetChanged();
        }
    }
}
