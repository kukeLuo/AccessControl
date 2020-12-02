package com.brc.acctrl.retrofit;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.text.TextUtils;

import com.brc.acctrl.R;
import com.brc.acctrl.activity.BaseActivity;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.StringUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class BaseObserver<T> implements Observer<T> {
    private WeakReference<Activity> reference;
    protected boolean shouldHideProgressBar = false;
    protected boolean showErrorMsg = true;

    private Disposable disposable;

    public Disposable getDisposable() {
        return disposable;
    }

    public BaseObserver(Activity cxt) {
        reference = new WeakReference<>(cxt);
    }

    public BaseObserver(Activity cxt, boolean hideProgress, boolean showError) {
        reference = new WeakReference<>(cxt);
        this.shouldHideProgressBar = hideProgress;
        this.showErrorMsg = showError;
    }

    public BaseObserver(Activity cxt, boolean showError) {
        reference = new WeakReference<>(cxt);
        this.showErrorMsg = showError;
    }

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        onRequestStart();
    }

    @Override
    public void onNext(T response) {
        onRequestEnd();

        if (reference.get() == null) {
            return;
        }
        if (response == null) {
            showFailMessage(StringUtil.CpStrGet(R.string.str_error_network));
            hideProgress();
            return;
        }

        onSuccess(response);
        hideProgress();
    }

    @Override
    public void onError(Throwable e) {
        onRequestEnd();
        e.printStackTrace();
        if (reference.get() == null) {
            return;
        }

        hideProgress();

        try {
            if (e instanceof ConnectException || e instanceof TimeoutException || e instanceof
                    SocketTimeoutException || e instanceof RuntimeException || e instanceof
                    NetworkErrorException || e instanceof UnknownHostException) {
                doErrorAction(StringUtil.CpStrGet(R.string.str_error_no_network));
            } else {
                doErrorAction(StringUtil.CpStrGet(R.string.str_error_network));
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            doErrorAction(e1.getMessage());
        }
    }

    private void hideProgress() {
        if (!shouldHideProgressBar) {
            if (reference.get() != null && reference.get() instanceof BaseActivity) {
                ((BaseActivity) (reference.get())).setProgressVisibility(false);
            }
        }
    }

    private void showProgress() {
        if (!shouldHideProgressBar) {
            if (reference.get() != null && reference.get() instanceof BaseActivity) {
                ((BaseActivity) (reference.get())).setProgressVisibility(true);
            }
        }
    }

    @Override
    public void onComplete() {
    }

    /**
     * 返回成功
     *
     * @param t
     * @throws Exception
     */
    protected abstract void onSuccess(T t);

    protected void doFailAction(T t) {
        onFailAction();
    }

    /**
     * 返回失败
     *
     * @throws Exception
     */
    protected void onFailAction() {
        onErrorAction("");
    }

    protected void onRequestStart() {
        if (reference.get() == null) {
            return;
        }

        showProgress();
    }

    protected void onRequestEnd() {
    }

    protected void showErrorMessage(String msg) {
        if (showErrorMsg) {
            CommonUtil.showToast(reference.get(), msg);
        }
    }

    protected void showFailMessage(String msg) {
        if (showErrorMsg) {
            CommonUtil.showToast(reference.get(), msg);
        }
    }

    protected void doErrorAction(String msg) {
        if (showErrorMsg) {
            CommonUtil.showToast(reference.get(), msg);
        }
        onErrorAction(msg);
    }

    protected void onErrorAction(String msg) {
        if(!TextUtils.isEmpty(msg)){
            LogUtil.e("network error :"+msg);
        }
    }

}
