package com.brc.acctrl.activity;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.bean.RspUpdate;
import com.brc.acctrl.bean.UpdateDetail;
import com.brc.acctrl.download.DownloadHelper;
import com.brc.acctrl.download.DownloadManager;
import com.brc.acctrl.download.InstallApkUtils;
import com.brc.acctrl.download.OnDownloadListener;
import com.brc.acctrl.retrofit.BaseObserver;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.MD5Util;
import com.brc.acctrl.utils.SPUtil;

import java.util.ArrayList;

import io.reactivex.schedulers.Schedulers;

/**
 * @author zhengdan
 * @date 2019-08-14
 * @Description:
 */
public abstract class BaseUpdateAPPActivity extends BaseDateTimeActivity {
    private DownloadManager mDownloadManager;
    public int versionCode;

    private final static int CHECK_VERSION_UPDATE = 0;
    private final static int MOCK_CLICK_INSTALL = 1;
    private final static int MOCK_CLICK_REOPEN = 2;
    private final static long DELAY_CLICK = 12000L;
    private final static long DELAY_REOPEN = 20000L;

    private Handler mockClickHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case CHECK_VERSION_UPDATE:
                    checkVersion();
                    break;
                case MOCK_CLICK_INSTALL:
                    mockClickPos("su -s sh -c input tap 620 960");
                    mockClickHandler.sendEmptyMessageDelayed(MOCK_CLICK_REOPEN, DELAY_CLICK);
                    break;
                case MOCK_CLICK_REOPEN:
                    mockClickPos("su -s sh -c input tap 620 960");
                    break;
            }
            super.dispatchMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mockClickHandler.removeCallbacksAndMessages(null);
    }

    private void mockClickPos(String clickStr) {
        try {
            Runtime.getRuntime().exec(clickStr); // hide dlg
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ANR", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        versionCode = CommonUtil.getVersion();
        mDownloadManager = DownloadManager.getInstance();
        mDownloadManager.clearAllCacheFile();
    }

    // for update
    private void checkVersion() {
        BaseObserver<RspUpdate> updateBaseObserver =
                new BaseObserver<RspUpdate>(this, true, false) {
                    @Override
                    public void onSuccess(RspUpdate responseData) {
                        LogUtil.e(MainApplication.getAPPInstance().gson.toJson(responseData));
                        ArrayList<UpdateDetail> versionInfo =
                                responseData.getData();
                        if (versionInfo != null && versionInfo.size() > 0) {
                            for (UpdateDetail singleUpdate : versionInfo) {
                                if (singleUpdate.getStatus() == 1 && singleUpdate.getVersionCode() > versionCode) {
                                    dlNewVersion(singleUpdate);
                                    return;
                                }
                            }
                        }
                    }
                };

        long curTimeSec = System.currentTimeMillis();
        long random = curTimeSec % 123247;
        RetrofitConfig.createService().checkUpdate(
                Constants.APP_KEY, curTimeSec, random + "", CommonUtil.getVersionName()
                , getSignStr(curTimeSec, random), getPackageName()).subscribeOn(Schedulers.io())
                .subscribe(updateBaseObserver);
    }

    private void dlNewVersion(UpdateDetail singleUpdate) {
        SPUtil.getInstance().setValue("KEY_START_DOWNLOAD", true);
        mDownloadManager.startDownload(singleUpdate.getInstallUrl(),
                new OnDownloadListener() {
                    @Override
                    public void onException() {
                        mDownloadManager.clearAllCacheFile();
                    }

                    @Override
                    public void onProgress(int progress) {
                    }

                    @Override
                    public void onSuccess() {
                        LogUtil.e("download onSuccess");

                        SPUtil.getInstance().setValue("KEY_START_DOWNLOAD", false);
                        String filePath =
                                DownloadHelper.getDownloadFile(singleUpdate.getInstallUrl());
                        InstallApkUtils.silentInstall(filePath);
                    }

                    @Override
                    public void onFailed() {
                        mDownloadManager.clearAllCacheFile();
                    }

                    @Override
                    public void onPaused() {
                        LogUtil.e("download onPaused: ");
                    }

                    @Override
                    public void onCanceled() {
                        LogUtil.e("download onCanceled: ");
                        mDownloadManager.clearAllCacheFile();
                    }
                });
    }

    private String getSignStr(long timeSec, long seqId) {
        String httpUrl = RetrofitConfig.BASE_HOST + "oss/api/app/version/list";
        return MD5Util.getMD5(httpUrl + seqId + Constants.APP_SECRET + timeSec).toUpperCase();
    }

    @Override
    public void tryToCheckUpdate() {
        LogUtil.e("CHECK UPDATE");
        mockClickHandler.sendEmptyMessage(CHECK_VERSION_UPDATE);
    }
}
