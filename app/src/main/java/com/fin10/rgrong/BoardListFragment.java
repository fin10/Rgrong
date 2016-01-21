package com.fin10.rgrong;

import android.app.Fragment;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class BoardListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    private SwipeRefreshLayout mRefreshLayout;
    private BoardAdapter mBoardAdapter;
    private OnItemClickListener mListener;

    private DataSetObserver mObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            mRefreshLayout.setRefreshing(false);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_board_list, container, false);
        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mBoardAdapter = new BoardAdapter(inflater);
        mBoardAdapter.registerDataSetObserver(mObserver);

        ListView listView = (ListView) root.findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);
        listView.setAdapter(mBoardAdapter);

        return root;
    }

    @Override
    public void onRefresh() {
        mBoardAdapter.refresh();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            BoardModel board = (BoardModel) parent.getItemAtPosition(position);
            mListener.onItemClicked(board);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClicked(@NonNull BoardModel board);
    }

    private static class BoardAdapter extends BaseAdapter {

        private final List<BoardModel> mItems = new ArrayList<>();
        private final LayoutInflater mLayoutInflater;

        public BoardAdapter(@NonNull LayoutInflater inflater) {
            mLayoutInflater = inflater;
            refresh();
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
                convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            BoardModel board = (BoardModel) getItem(position);
            TextView nameView = (TextView) convertView.findViewById(android.R.id.text1);
            nameView.setText(board.getName());

            return convertView;
        }

        public void refresh() {
            mItems.clear();
            new AsyncTask<Void, Void, List<BoardModel>>() {

                @Override
                protected List<BoardModel> doInBackground(Void... params) {
                    return BoardModel.getBoards();
                }

                @Override
                protected void onPostExecute(List<BoardModel> boards) {
                    mItems.addAll(boards);
                    notifyDataSetChanged();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}
