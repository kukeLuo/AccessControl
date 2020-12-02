package com.brc.acctrl.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.R;
import com.brc.acctrl.bean.RspUpdate;
import com.brc.acctrl.bean.UpdateDetail;
import com.brc.acctrl.download.DownloadHelper;
import com.brc.acctrl.download.DownloadManager;
import com.brc.acctrl.download.InstallApkUtils;
import com.brc.acctrl.download.OnDownloadListener;
import com.brc.acctrl.retrofit.BaseObserver;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.retrofit.RxJavaAction;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.MD5Util;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;

public class UpdateVersionActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_latest_title)
    TextView tvLatestTitle;
    @BindView(R.id.tv_latest_version)
    TextView tvLatestVersion;
    @BindView(R.id.layout_version_latest)
    RelativeLayout layoutVersionLatest;
    @BindView(R.id.tv_current_title)
    TextView tvCurrentTitle;
    @BindView(R.id.tv_current_version)
    TextView tvCurrentVersion;
    @BindView(R.id.layout_version_current)
    RelativeLayout layoutVersionCurrent;
    @BindView(R.id.tv_update)
    TextView tvUpdate;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.tv_dl_progress)
    TextView tvDlProgress;
    @BindView(R.id.progress_dl)
    ProgressBar progressDl;
    @BindView(R.id.layout_dl)
    LinearLayout layoutDl;

    private int versionCode = 0;
    private String versionName = "";
    private UpdateDetail waitUpdate;
    private DownloadManager mDownloadManager;
    private boolean isDownloadSuc = false;
    private boolean isFirstCheck = true;

    private CompositeDisposable mCompositeDisposable =
            new CompositeDisposable();

    @Override
    public int getLayoutId() {
        return R.layout.activity_update;
    }

    @Override
    public void initViews() {
        versionCode = CommonUtil.getVersion();
        versionName = CommonUtil.getVersionName();
        tvCurrentVersion.setText(versionName);

        mDownloadManager = DownloadManager.getInstance();
        mDownloadManager.clearAllCacheFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstCheck) {
            isFirstCheck = false;
            checkVersion();
        }
    }

    private void checkVersion() {
        BaseObserver<RspUpdate> updateBaseObserver =
                new BaseObserver<RspUpdate>(this) {
                    @Override
                    public void onSuccess(RspUpdate responseData) {
                        LogUtil.e(MainApplication.getAPPInstance().gson.toJson(responseData));
                        ArrayList<UpdateDetail> versionInfo =
                                responseData.getData();
                        if (versionInfo != null && versionInfo.size() > 0) {
                            for (UpdateDetail singleUpdate : versionInfo) {
                                if (singleUpdate.getStatus() == 1 && singleUpdate.getVersionCode() > versionCode) {
                                    waitUpdate = singleUpdate;
                                    refreshUpdateView();
                                    return;
                                }
                            }
                        }
                        tvCurrentTitle.setText(R.string.str_update_lateast);
                    }
                };

        long curTimeSec = System.currentTimeMillis();
        long random = curTimeSec % 123247;
        RetrofitConfig.createService().checkUpdate(
                Constants.APP_KEY, curTimeSec, random + "", CommonUtil.getVersionName()
                , getSignStr(curTimeSec, random), getPackageName()).compose(RxJavaAction.<RspUpdate>setThread())
                .subscribe(updateBaseObserver);

        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            if (updateBaseObserver.getDisposable() != null)
                mCompositeDisposable.add(updateBaseObserver.getDisposable());
        }
    }

    private String getSignStr(long timeSec, long seqId) {
        String httpUrl = RetrofitConfig.BASE_HOST + "oss/api/app/version/list";
        return MD5Util.getMD5(httpUrl + seqId + Constants.APP_SECRET + timeSec).toUpperCase();
    }

    private void refreshUpdateView() {
        layoutVersionLatest.setVisibility(View.VISIBLE);
        tvUpdate.setVisibility(View.VISIBLE);

        tvLatestVersion.setText(waitUpdate.getAppVersion());
    }

    @OnClick({R.id.img_back, R.id.tv_update})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_update:
                if (isDownloadSuc) {
                    String filePath =
                            DownloadHelper.getDownloadFile(waitUpdate.getInstallUrl());
                    InstallApkUtils.install(this, filePath);
                } else {
                    layoutDl.setVisibility(View.VISIBLE);
                    tvUpdate.setText("");
                    progressDl.setProgress(0);
                    startDownLoad();
                }
                break;
        }
    }

    public void startDownLoad() {
        tvDlProgress.setText(StringUtil.CpStrIntPara(R.string.str_dl_progress
                , 0));
        progressDl.setProgress(0);
        tvUpdate.setText(R.string.str_update_action_dling);
        tvUpdate.setEnabled(false);
        SPUtil.getInstance().setValue("KEY_START_DOWNLOAD", true);
        mDownloadManager.startDownload(waitUpdate.getInstallUrl(),
                new OnDownloadListener() {
                    @Override
                    public void onException() {
                        CommonUtil.showToast(UpdateVersionActivity.this, R.string.str_update_action_err);
                        resetDLStatus(R.string.str_update_action);
                    }

                    @Override
                    public void onProgress(int progress) {
                        LogUtil.e("download onProgress: " + progress);
                        tvDlProgress.setText(StringUtil.CpStrIntPara(R.string.str_dl_progress, progress));
                        progressDl.setProgress(progress);
                        tvUpdate.setEnabled(false);
                    }

                    @Override
                    public void onSuccess() {
                        SPUtil.getInstance().setValue("KEY_START_DOWNLOAD", false);
                        LogUtil.e("download onSuccess: ");
                        isDownloadSuc = true;
                        resetDLStatus(R.string.str_update_action_dled);
                    }

                    @Override
                    public void onFailed() {
                        CommonUtil.showToast(UpdateVersionActivity.this, R.string.str_update_action_err);
                        resetDLStatus(R.string.str_update_action);
                    }

                    @Override
                    public void onPaused() {
                        LogUtil.e("download onPaused: ");
                    }

                    @Override
                    public void onCanceled() {
                        LogUtil.e("download onCanceled: ");
                        resetDLStatus(R.string.str_update_action);
                    }
                });
    }

    private void resetDLStatus(int strId) {
        LogUtil.e("resetDLStatus:" + getString(strId));
        tvUpdate.setText(strId);
        tvUpdate.setEnabled(true);

        if (!isDownloadSuc) {
            mDownloadManager.clearAllCacheFile();
        }
        layoutDl.setVisibility(View.GONE);
        SPUtil.getInstance().setValue("KEY_START_DOWNLOAD", false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isDownloadSuc) {
            DownloadManager.getInstance().cancelDownload();
            DownloadManager.getInstance().clearAllCacheFile();
        }

        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
    }
}
