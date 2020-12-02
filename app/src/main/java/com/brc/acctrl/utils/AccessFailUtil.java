package com.brc.acctrl.utils;

import android.text.TextUtils;

import com.brc.acctrl.activity.BaseActivity;
import com.brc.acctrl.bean.AccessFail;
import com.brc.acctrl.bean.RspRegister;
import com.brc.acctrl.bean.UploadLogReq;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.db.FailRecordDatabase;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.retrofit.RetryWithDelay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AccessFailUtil {
    public static void checkUnuploadRecord(BaseActivity activity,long expireTime) {
        LogUtil.e("checkUnuploadRecord");
        if (!NetworkUtil.isNetworkAvailable(activity)) {
            return;
        }
        // 检测未上传记录， 找到errJpg对应的错误图片文件夹，删除记录不存在的图片
        FailRecordDatabase.getInstance().getAccessFailDao().fetchAllFailRegFiles(expireTime)
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<List<AccessFail>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<AccessFail> failHistories) {
                        ArrayList<String> jpgFileNames = listFaceFail();
                        if (failHistories != null && failHistories.size() > 0) {
                            for (AccessFail failReq : failHistories) {
                                jpgFileNames.remove(failReq.getErrJpgName());
                                tryToUploadFailLog2Server(failReq, false);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        for (String jpgName : jpgFileNames) {
                            File jpgFile = new File(FaceSDKUtil.errJpgDirectory(), jpgName);
                            try {
                                jpgFile.delete();
                            } catch (Exception e) {

                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private static ArrayList<String> listFaceFail() {
        ArrayList<String> jpgFileName = new ArrayList<>();
        File[] errFiles = FaceSDKUtil.errJpgDirectory().listFiles();
        if (errFiles != null && errFiles.length > 0) {
            for (File singleErrFile : errFiles) {
                if (singleErrFile.getName().endsWith(".jpg")) {
                    jpgFileName.add(singleErrFile.getName());
                } else {
                    try {
                        singleErrFile.delete();
                    } catch (Exception e) {

                    }
                }
            }
        }

        return jpgFileName;
    }

    public static void tryToUploadFailLog2Server(AccessFail failRec) {
        tryToUploadFailLog2Server(failRec, true);
    }

    public static void tryToUploadFailLog2Server(AccessFail failRec, boolean shouldSaveRecord) {
        RetrofitConfig.createService().uploadLogData(new UploadLogReq(failRec))
                .subscribeOn(Schedulers.io()).retryWhen(new RetryWithDelay())
                .subscribe(new Observer<RspRegister>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(RspRegister rsp) {
                        // 如果本来就是读取DB来的,上传成功后删除
                        if (!shouldSaveRecord) {
                            DBStore.getInstance().delRegFail(failRec);
                        }

                        if (!TextUtils.isEmpty(failRec.getErrJpgName())) {
                            File tempFile = new File(FaceSDKUtil.errJpgDirectory(), failRec.getErrJpgName());
                            if (tempFile.exists()) {
                                tempFile.delete();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // 如果本来就是读取DB来的就不需要保存，只有没成功才保存
                        if (shouldSaveRecord) {
                            DBStore.getInstance().insertRegFail(failRec);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
