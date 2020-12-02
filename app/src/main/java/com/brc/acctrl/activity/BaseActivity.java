package com.brc.acctrl.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.brc.acctrl.R;
import com.brc.acctrl.utils.EventBusUtil;

import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;


/**
 * Created by zhendan on 5/8/2016.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public ProgressDialog mProgressDialog;
    public boolean isResumeState = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Window _window = getWindow();
        WindowManager.LayoutParams params = _window.getAttributes();
        params.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        _window.setAttributes(params);

        setContentView(getLayoutId());
        ButterKnife.bind(this);
        EventBusUtil.registerEventBus(this);
        initViews();
    }

    public abstract int getLayoutId();

    public abstract void initViews();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtil.unregisterEventBus(this);
        setProgressVisibility(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumeState = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumeState = false;
    }

    public void setProgressVisibility(boolean visible) {
        if (visible) {
            if (isResumeState) {
                checkAndCreateProgressDlg();
                if (mProgressDialog != null) {
                    mProgressDialog.show();
                }
            }
        } else {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

    public void checkAndCreateProgressDlg() {
        if (mProgressDialog == null) {
            mProgressDialog = newProgressDlg();
        }
    }

    public void setProgressMsg(int msgId) {
        setProgressMsg(getString(msgId));
    }

    public void setProgressMsg(String strMsg) {
        checkAndCreateProgressDlg();
        mProgressDialog.setMessage(strMsg);
        setProgressVisibility(true);
    }

    public boolean isHomeActivityClass() {
        return false;
    }

    @Subscribe
    public void onRecvMsg(String msg) {

    }

    private ProgressDialog newProgressDlg() {
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage(this.getResources().getString(R.string.loading_prompt));
        pDialog.setCancelable(false);
        return pDialog;
    }
}
