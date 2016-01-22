package com.fin10.rgrong;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public final class MainActivity extends AppCompatActivity implements View.OnClickListener, BoardListFragment.OnItemClickListener, AccountController.LoginStateListener {

    private BoardFragment mBoardFragment;
    private BoardModel mBoard;

    private DrawerLayout mDrawer;
    private View mNewPostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AccountController.addListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        mBoard = BoardModel.getLastItem(this);
        if (mBoard != null && actionBar != null) {
            actionBar.setTitle(mBoard.getName());
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.open, R.string.close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mBoardFragment = (BoardFragment) getFragmentManager().findFragmentById(R.id.board_fragment);
        mBoardFragment.setBoard(mBoard);

        BoardListFragment boardListFragment = (BoardListFragment) getFragmentManager().findFragmentById(R.id.board_list_fragment);
        boardListFragment.setOnItemClickListener(this);

        mNewPostButton = findViewById(R.id.fab);
        mNewPostButton.setOnClickListener(this);
        mNewPostButton.setVisibility(AccountController.isLogin() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountController.removeListener(this);
        if (mBoard != null) {
            mBoard.save(this);
        }
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
                mBoardFragment.onRefresh();
                return true;
            case R.id.menu_filter:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClicked(@NonNull BoardModel board) {
        mDrawer.closeDrawer(GravityCompat.START);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mBoard.getName());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {
                Intent intent = new Intent(this, NewPostActivity.class);
                intent.putExtra("board", mBoard);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onLoginStateChanged(boolean login) {
        mNewPostButton.setVisibility(AccountController.isLogin() ? View.VISIBLE : View.GONE);
    }
}
