package com.brc.acctrl.utils;

import android.text.TextUtils;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.activity.BaseActivity;
import com.brc.acctrl.bean.AccessHistory;
import com.brc.acctrl.bean.AccessLog;
import com.brc.acctrl.bean.AccessRecordParams;
import com.brc.acctrl.bean.BaseFileInfo;
import com.brc.acctrl.bean.BaseRsp;
import com.brc.acctrl.bean.ReqUploadModelBean;
import com.brc.acctrl.bean.ReqUploadModels;
import com.brc.acctrl.bean.ReqUploadRecord;
import com.brc.acctrl.bean.RspImagePath;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.db.RecordDatabase;
import com.brc.acctrl.retrofit.BaseObserver;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.retrofit.RxJavaAction;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AccessRecordUtil {
    public static final int MAX_DAY_ACCESS_RECORD_DURATION = 7;
    public static final int MAX_DAY_ACCESS_LOG_DURATION = 1;

    public static void checkUnuploadRecord(BaseActivity activity) {
        if (!NetworkUtil.isNetworkAvailable(activity)) {
            return;
        }
        RecordDatabase.getInstance().getAccessHistoryDao().fetchUnuploadRecordFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<AccessHistory>>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<AccessHistory> accessHistories) {
                        if (accessHistories != null && accessHistories
                                .size() > 0) {
                            ReqUploadModels<ReqUploadRecord> uploadModels =
                                    new ReqUploadModels<>();
                            ReqUploadModelBean<ReqUploadRecord> recordData =
                                    new ReqUploadModelBean<>();
                            for (AccessHistory record : accessHistories) {
                                ReqUploadRecord uploadBean = new
                                        ReqUploadRecord();

                                uploadBean.msgTime = record.getAccessTime();
                                AccessRecordParams params =
                                        new AccessRecordParams();
                                params.personId = record.getUserId();
                                params.personName = record.getUserName();
                                params.groupId = record.getGroupId();
                                params.targetImageUrl=record.getUserAvatar();
                                params.captureFaceUrl=record.getCaptureFaceurl();
                                params.threshold=record.getRatio();


                                uploadBean.eventParams =
                                        MainApplication.getAPPInstance().gson.toJson(params);

                                recordData.deviceEvents.add(uploadBean);
                            }
                            uploadModels.deviceModels.add(recordData);

                            // try to upload server
                            uploadAccessRecords(activity, uploadModels,
                                    accessHistories);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void uploadSingleRecord(BaseActivity activity, final AccessHistory record,int comparison) {
        if (!NetworkUtil.isNetworkAvailable(activity)) {
            DBStore.getInstance().insertAccessRecord(record);
            return;
        }

        ReqUploadModels<ReqUploadRecord> uploadModels = new ReqUploadModels<>();
        ReqUploadModelBean<ReqUploadRecord> recordData = new ReqUploadModelBean<>();

        ReqUploadRecord uploadBean = new ReqUploadRecord();

        uploadBean.msgTime = record.getAccessTime();
        AccessRecordParams params = new AccessRecordParams();
        params.personId = record.getUserId();
        params.personName = record.getUserName();
        params.targetImageUrl=record.getUserAvatar();
        params.captureFaceUrl=record.getCaptureFaceurl();
        params.threshold=record.getRatio();
        if(comparison==0){
            params.direction=1;
        }else{
            params.direction=3;
        }

        uploadBean.eventParams = MainApplication.getAPPInstance().gson.toJson(params);

        recordData.deviceEvents.add(uploadBean);

        uploadModels.deviceModels.add(recordData);

        RetrofitConfig.createService().uploadAccessRecord(
                Constants.APP_KEY, Constants.APP_SECRET, uploadModels).compose(RxJavaAction.<BaseRsp>setThread())
                .subscribe(new BaseObserver<BaseRsp>(activity, true,
                        false) {
                    @Override
                    public void onSuccess(BaseRsp responseData) {
                        if (responseData.status == 200) {
                            record.setUploaded(1);
                        }
                        DBStore.getInstance().insertAccessRecord(record);
                    }

                    @Override
                    protected void onErrorAction(String msg) {
                        DBStore.getInstance().insertAccessRecord(record);
                    }
                });
    }

    private static void uploadAccessRecords(BaseActivity activity,
                                            ReqUploadModels<ReqUploadRecord> recordData, List<AccessHistory> accessHistories) {
        if (!NetworkUtil.isNetworkAvailable(activity)) {
            return;
        }
        // api 请求上传，成功后修改对应的id
        RetrofitConfig.createService().uploadAccessRecord(
                Constants.APP_KEY, Constants.APP_SECRET, recordData).compose(RxJavaAction.<BaseRsp>setThread())
                .subscribe(new BaseObserver<BaseRsp>(activity, true,
                        false) {
                    @Override
                    public void onSuccess(BaseRsp responseData) {
                        if (responseData.status == 200) {
                            for (AccessHistory record : accessHistories) {
                                record.setUploaded(1);
                            }
                            DBStore.getInstance().updateAccessRecords(accessHistories);
                        }
                    }

                    @Override
                    protected void onErrorAction(String msg) {
                    }
                });
    }

    public static void deleteExpireRerod(long expireTime) {
        DBStore.getInstance().deleteExpireRecord(expireTime);
    }
    public static void deleteAccessLog(long expireTime){
        DBStore.getInstance().deleteAccessLogByTime(expireTime);
        FaceSDKUtil.getInstance().deleteIndetifiFace();
    }

    public static void fetchUrlByFile(BaseActivity activity, AccessLog accessLog,int comparison){

        MultipartBody.Builder builder = new MultipartBody.Builder();
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), new File(FaceSDKUtil.getInstance().getFaceFolderPath(),accessLog.getErrJpgName()));
        builder.addFormDataPart("file", new File(FaceSDKUtil.getInstance().getFaceFolderPath(),accessLog.getErrJpgName()).getName(), requestBody);
        builder.setType(MultipartBody.FORM);
        MultipartBody multipartBody = builder.build();
        RetrofitConfig.createService().fetchUrlByFile(multipartBody).compose(RxJavaAction.<BaseRsp<RspImagePath>>setThread())
            .subscribe(new BaseObserver<BaseRsp<RspImagePath>>(activity, true,
                    false) {
                @Override
                public void onSuccess(BaseRsp<RspImagePath> responseData) {
                    LogUtil.i("response:"+responseData);
                    if (responseData.status == 200) {
                        AccessHistory record = new AccessHistory();
                        record.setUserId(accessLog.getMatchUserId());
                        record.setUserName(accessLog.getMatchUserName());
                        record.setAccessTime(System.currentTimeMillis());
                        record.setType(AccessHistory.TYPE_FACE);
                        record.setUserAvatar(accessLog.getAvatarUrl());
                        record.setCaptureFaceurl(responseData.data.getUrl());
                        record.setRatio(accessLog.getMatchRatio());
                        uploadSingleRecord(activity,record,comparison);
                    }
                }

                @Override
                protected void onErrorAction(String msg) {
                    LogUtil.i("msg"+msg);
                }
            });
    }
}
