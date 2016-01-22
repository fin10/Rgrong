package com.fin10.rgrong;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class AccountFragment extends Fragment implements View.OnClickListener, AccountController.LoginStateListener {

    private View mLoginButton;
    private View mLogoutButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        mLoginButton = root.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);
        mLogoutButton = root.findViewById(R.id.logout_button);
        mLogoutButton.setOnClickListener(this);

        boolean login = AccountController.isLogin();
        mLoginButton.setVisibility(login ? View.GONE : View.VISIBLE);
        mLogoutButton.setVisibility(login ? View.VISIBLE : View.GONE);

        AccountController.addListener(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AccountController.removeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button: {
                AccountController.login(view.getContext());
                break;
            }
            case R.id.logout_button: {
                AccountController.logout();
                break;
            }
        }
    }

    @Override
    public void onLoginStateChanged(boolean login) {
        mLoginButton.setVisibility(login ? View.GONE : View.VISIBLE);
        mLogoutButton.setVisibility(login ? View.VISIBLE : View.GONE);
    }
}
