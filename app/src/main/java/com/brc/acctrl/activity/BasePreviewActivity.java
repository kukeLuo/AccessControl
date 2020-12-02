package com.brc.acctrl.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.TextureView;

import com.brc.acctrl.R;
import com.brc.acctrl.bean.AccessHistory;
import com.brc.acctrl.bean.AccessUser;
import com.brc.acctrl.bean.BaseRsp;
import com.brc.acctrl.camera.ICameraControl;
import com.brc.acctrl.camera.source.CameraImageSource;
import com.brc.acctrl.db.UserDatabase;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.listener.OnFrameAvailableListener;
import com.brc.acctrl.mqtt.MqttService;
import com.brc.acctrl.retrofit.BaseObserver;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.retrofit.RxJavaAction;
import com.brc.acctrl.utils.AccessRecordUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.DrawLineUtils;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.view.CameraView;
import com.brc.acctrl.view.PreviewView;
import com.brc.acctrl.view.TexturePreviewView;
import com.deepglint.hri.face.FaceSDKManager;
import com.deepglint.hri.facesdk.FaceInfo;
import com.deepglint.hri.facesdk.FaceSDKOptions;
import com.deepglint.hri.facesdk.FaceTracker;
import com.deepglint.hri.utils.ArrayUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public abstract class BasePreviewActivity extends BaseNetworkActivity {
    public int cameraRotation = 270;
    public boolean isMatching = false;
    public String matchName;
    public FaceSDKManager faceSDKManager;

    public HashMap<Integer, String> featureIDs = new HashMap<>();
    public int dbGroupFaceCnt;

    public ExecutorService executorPool = Executors.newSingleThreadExecutor();
    public String matchGroupId;
    public SoundPool soundPool;
    public int soundId;

    public boolean hasDetectedFace = false;
    public boolean canDetectFace = true;
    public int DoorOpenTime = 0;
    public boolean canShowFailDlg = false;

    public boolean faceInitSuc = false;
    public boolean isAlwaysOpenDoor = false;

    @Override
    protected void onResume() {
        super.onResume();
        DoorOpenTime = SPUtil.getInstance().getValue(SPUtil.DOOR_OPEN_TIME, 5) * 1000;
        isAlwaysOpenDoor = SPUtil.getInstance().getValue(SPUtil.KEEP_DOOR_OPEN, false);
        showAlwaysOpenDoorUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvDoorOpenEvent(RefreshEvents.KeepDoorOpenEvent event) {
        isAlwaysOpenDoor = event.isKeepOpen();
        showAlwaysOpenDoorUI();
    }

    // paint
    public Paint boldLinePaint, thinLinePaint;
    private int boldLineWdh, thinLineWdh;

    public void initPaintStyle() {
        boldLineWdh = 5;
        thinLineWdh = 2;
        boldLinePaint = createPaint(boldLineWdh);
        thinLinePaint = createPaint(thinLineWdh);
    }

    private Paint createPaint(int width) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        return paint;
    }

    // handler
    public static final int REQUEST_PWD = 1;
    public int settingClickCnt = 0;
    public static final long DELAY_TIME = 3000L;

    public static final int CNT_GOTO_SETTING = 5;
    public static final int MSG_RESET_SETTING_CLICK = 0;
    public static final int MSG_SHOW_CIRCLEVIEW = 1;
    public static final int MSG_HIDE_CIRCLEVIEW = 2;
    public static final int MSG_SHOW_MATCHFACE = 3;
    public static final int MSG_HIDE_MATCHFACE = 4;
    public static final int MSG_SHOW_VIDEO = 5;
    public static final int MSG_HIDE_VIDEO = 6;
    public static final int MSG_RELOAD_FACE_DETECT = 7;
    public static final int MSG_CLOSE_DOOR = 8;
    // 如果一开始检测到不符合要求，则3秒后再次检测是否符合要求
    // 防止人一开始从外部进入检测失败提示后马上又提示成功情况
    public static final int MSG_CAN_SHOW_DETECT_FAIL = 9;
    public static final int MSG_RESET_SHOW_DETECT_FAIL = 10;
    public static final int MSG_FETCH_AUTH_CODE = 11;
    public static final int MSG_SHOW_NOMATCHFACE = 12;
    public static final int MSG_HIDE_NOMATCHFACE = 13;

    public Handler settingHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESET_SETTING_CLICK:
                    settingClickCnt = 0;
                    break;
                case MSG_SHOW_CIRCLEVIEW:
                    canDetectFace = false;
                    settingHandler.sendEmptyMessageDelayed(MSG_HIDE_CIRCLEVIEW, DELAY_TIME);
                    break;
                case MSG_HIDE_CIRCLEVIEW:
                    resetBtmLightOFF();
                    canDetectFace = true;
                    GoldenEyesUtils.setGoldenEyesCloseDoor();
                    hideInputSuc();

                    settingHandler.removeMessages(MSG_CAN_SHOW_DETECT_FAIL);
                    canShowFailDlg = false;
                    break;
                case MSG_SHOW_MATCHFACE:
                    canDetectFace = false;
                    doOpenDoorAction();
                    settingHandler.sendEmptyMessageDelayed(MSG_HIDE_MATCHFACE
                            , DELAY_TIME);
                    break;
                case MSG_HIDE_MATCHFACE:
                    resetBtmLightOFF();
                    canDetectFace = true;
                    hideScanSucFace();
                    break;
                case MSG_SHOW_NOMATCHFACE:
                    canDetectFace = false;
                    settingHandler.sendEmptyMessageDelayed(MSG_HIDE_NOMATCHFACE, DELAY_TIME);
                    break;
                case MSG_HIDE_NOMATCHFACE:
                    resetBtmLightOFF();
                    canDetectFace = true;
                    hideScanErrFace();

                    settingHandler.removeMessages(MSG_CAN_SHOW_DETECT_FAIL);
                    canShowFailDlg = false;
                    break;
                case MSG_SHOW_VIDEO:
                    showWaitVideo();
                    canDetectFace = true;
                    break;
                case MSG_HIDE_VIDEO:
                    hideWaitVideo();
                    break;
                case MSG_RELOAD_FACE_DETECT:
                    canDetectFace = true;
                    break;
                case MSG_CLOSE_DOOR:
                    GoldenEyesUtils.setGoldenEyesCloseDoor();
                    break;
                case MSG_CAN_SHOW_DETECT_FAIL:
                    canShowFailDlg = true;
                    settingHandler.sendEmptyMessageDelayed(MSG_RESET_SHOW_DETECT_FAIL, 2000L);
                    break;
                case MSG_RESET_SHOW_DETECT_FAIL:
                    canShowFailDlg = false;
                    break;
                case MSG_FETCH_AUTH_CODE:
                    checkAuthCode();
                    break;
                default:
                    break;
            }
            super.dispatchMessage(msg);
        }
    };

    private void resetBtmLightOFF() {
        GoldenEyesUtils.setGoldenEyesStateGreedLED_OFF71();
        GoldenEyesUtils.setGoldenEyesStateRedLED_OFF71();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvFinishPageEvent(RefreshEvents.FinishPageEvent event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvRestartEvent(RefreshEvents.RestartAPPEvent event) {
        try {
            Intent intent = new Intent(getApplicationContext(),
                    MqttService.class);
            stopService(intent);
        } catch (Exception e) {

        }

        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    public CameraImageSource initCameraSource(TexturePreviewView basePreviewView, TextureView baseTextureView, int cameraIdx) {
        CameraImageSource cameraImageSource = new CameraImageSource(this);
        cameraImageSource.getCameraControl().setPreferredPreviewSize(1280,
                720);
        cameraImageSource.setPreviewView(basePreviewView);
        baseTextureView.setOpaque(false);
        baseTextureView.setKeepScreenOn(true);
        basePreviewView.setMirrored(false);

        boolean isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            basePreviewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            basePreviewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }
        cameraImageSource.getCameraControl().setCameraIndex(cameraIdx);
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);

        return cameraImageSource;
    }

    public void tryInitFaceSDK() {
        FaceSDKOptions options = new FaceSDKOptions();
        options.photoTrackerOptions.detectStrategy = FaceTracker.DetectStrategy.DETECT_BALANCE;
        options.photoTrackerOptions.minTrackingSize = 50;
        options.photoTrackerOptions.minConfidence = 0.5f;

        options.videoTrackerOptions.minTrackingSize = 100;
        options.videoTrackerOptions.minConfidence = 0.7f;

        String faceCode = SPUtil.getInstance().getValue(SPUtil.FACE_AUTH_CODE, "");
        if (TextUtils.isEmpty(faceCode)) {
            setErrTextContent(R.string.str_sdk_init_fail);
            settingHandler.sendEmptyMessageDelayed(MSG_FETCH_AUTH_CODE, DELAY_TIME);
            return;
        }

        FaceSDKManager.getInstance().init(BasePreviewActivity.this, faceCode, options);
        int status = FaceSDKManager.getInstance().getActivationStatus();
        LogUtil.d("ACTIVE CODE = " + faceCode + ":STATUS = " + status);
        if (status == 0) {
            if (NetworkUtil.networkStatus == NetworkUtil.NETWORK_OK) {
                setErrTextContent(0);
            }
            LogUtil.e(StringUtil.CpStrPara(R.string.str_sdk_init_suc));
            // 打开摄像头
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initFaceSDKSucAction();
                    uploadCodeStatus(1);
                }
            });

        } else if (status > 0 || status == -1 || status == -2) {
            // 如果有问题，这里就直接提示即可
            setErrTextContent(R.string.str_sdk_init_fail);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uploadCodeStatus(2);
                }
            });
        } else if (status == -3) {
            setErrTextContent(R.string.str_sdk_init_fail_model);
        }
    }

    public void checkSettingAction() {
        settingClickCnt++;
        if (settingClickCnt == 1) {
            settingHandler.sendEmptyMessageDelayed(MSG_RESET_SETTING_CLICK,
                    DELAY_TIME);
        } else if (settingClickCnt == CNT_GOTO_SETTING) {
            startActivity(new Intent(BasePreviewActivity.this,
                    SettingPWDActivity.class));
        } else if (settingClickCnt > CNT_GOTO_SETTING) {
            settingClickCnt = 2;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PWD) {
                doOpenDoorAction();
                settingHandler.sendEmptyMessage(MSG_SHOW_CIRCLEVIEW);
                showPwdInputSuc();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doOpenDoorAction() {
        GoldenEyesUtils.setGoldenEyesOpenDoor();
        settingHandler.removeMessages(MSG_CLOSE_DOOR);
        settingHandler.sendEmptyMessageDelayed(MSG_CLOSE_DOOR, DoorOpenTime);
    }

   /* public void saveAccessRecord(String userId, String userName, String avatarUrl) {
        // 这里应该先直接上传，而不是先检测人脸. 成功后再处理
        AccessHistory record = new AccessHistory();
        record.setUserId(userId + "");
        record.setUserName(userName);
        record.setAccessTime(System.currentTimeMillis());
        record.setType(AccessHistory.TYPE_FACE);
        record.setUserAvatar(avatarUrl);

        AccessRecordUtil.uploadSingleRecord(BasePreviewActivity.this, record);
    }*/

    public void startCameraImgSource(CameraImageSource cameraImageSource, OnFrameAvailableListener frameAvailableListener, int cameraRotation) {
        if (cameraImageSource != null) {
            cameraImageSource.getListeners().clear();
            cameraImageSource.addOnFrameAvailableListener(
                    frameAvailableListener
            );
            cameraImageSource.getCameraControl().setCameraRotation(cameraRotation);
            cameraImageSource.start();
        }
    }

    public void stopCameraImgSource(CameraImageSource cameraImageSource, OnFrameAvailableListener frameAvailableListener) {
        if (cameraImageSource != null) {
            cameraImageSource.getListeners().clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cameraImageSource.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void gotoEnterPWDActivity() {
        Intent pwdIntent = new Intent(BasePreviewActivity.this,
                OfficePWDActivity.class);
        startActivityForResult(pwdIntent, REQUEST_PWD);
    }

    public void initCardSound() {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        // 加载声音资源
        soundId = soundPool.load(this, R.raw.alert, 1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvCardEvent(RefreshEvents.CatchCardEvent event) {
        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvRefreshFaceEvent(RefreshEvents.RefreshFaceEvent event) {
        refreshFaceByType(event.shouleRefresh());
    }

    protected void refreshFaceByType(boolean bforceRefresh) {
        if (!faceInitSuc) {
            return;
        }
        String currentFaceGroupId = fetchFaceGroupId();
        // 如果相等则不处理
        if (bforceRefresh || !matchGroupId.equals(currentFaceGroupId)) {
            // 有可能一开始没有
            settingHandler.removeMessages(MSG_RELOAD_FACE_DETECT);
            canDetectFace = false;
            matchGroupId = currentFaceGroupId;
            loadFaceDBByGroup();
        } else {
            canDetectFace = true;
        }
    }

    private String fetchFaceGroupId() {
        return "0";
//        int mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
//        if (mode == SPUtil.ACCESS_TYPE_MEETING) {
//            return MeetingUtils.getInstance().fetchCurrentMeetingId();
//        } else {
//            return "0";
//        }
    }

    private void loadFaceDBByGroup() {
        LogUtil.e("matchGroupId = " + matchGroupId);
        if (TextUtils.isEmpty(matchGroupId)) {
            canDetectFace = true;
            dbGroupFaceCnt = 0;
        } else {
            loadFaceDB();
        }
    }

    public void initFaceSDK() {
        executorPool.execute(new Runnable() {
            @Override
            public void run() {
//                checkAuthCode();
                tryInitFaceSDK();
            }
        });
    }

    @Override
    protected void checkAuthCode() {
        if (NetworkUtil.isNetworkAvailable(this)) {
            RetrofitConfig.createService().fetchAuthCode(
                    Constants.APP_KEY, Constants.APP_SECRET, NetworkUtil.ethernetMac(), Constants.PRODUCT_KEY)
                    .compose(RxJavaAction.<BaseRsp<String>>setThread())
                    .subscribe(new BaseObserver<BaseRsp<String>>(this, true,
                            true) {
                        @Override
                        public void onSuccess(BaseRsp<String> responseData) {
                            if (responseData.status == 200) {
                                SPUtil.getInstance().setValue(SPUtil.FACE_AUTH_CODE, responseData.data);
                                tryInitFaceSDK();
                            }
                        }

                        @Override
                        protected void onErrorAction(String msg) {
                        }
                    });
        }
    }

    // 加载数据库，如果有人员变动需要再次读取
    public void loadFaceDB() {
        featureIDs.clear();
        try {
            FaceSDKManager.getInstance().clearFaceFeature();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 这里其实还是使用meetingId来判别的。由于会议人脸数据不准，故调整处理
        UserDatabase.getInstance().getAccessUserDao().fetchUsersFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LogUtil.e("loadFaceDB finish");
                        settingHandler.sendEmptyMessageDelayed(MSG_RELOAD_FACE_DETECT, DELAY_TIME);
                    }
                })
                .subscribe(new SingleObserver<List<AccessUser>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<AccessUser> accessUsers) {
                        int count = 0;
                        for (AccessUser singleUser : accessUsers) {
                            try {
                                byte[] byteFeature = singleUser.getFeatureBytes();
                                float[] floatFeature = ArrayUtils.toFloatArray(byteFeature);
                                Mat matFeature = new Mat(1, floatFeature.length, CvType.CV_32FC1);
                                matFeature.put(0, 0, floatFeature);

                                FaceSDKManager.getInstance().importFaceFeature(
                                        matFeature
                                );

                                String content = singleUser.getUserId() + "-" +
                                        singleUser.getPermissionId() + "-" + singleUser.getUsername();
                                featureIDs.put(count, content);

                                ++count;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        dbGroupFaceCnt = count;
                        loadFaceResult();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        featureIDs.clear();
                        dbGroupFaceCnt = 0;
                        loadFaceResult();
                    }
                });
    }

    private void loadFaceResult() {
//        LogUtil.e("人脸库加载完成 featureIDs:" + MainApplication.getAPPInstance().gson.toJson(featureIDs));
        LogUtil.e("人脸库加载完成，共:" + dbGroupFaceCnt + "个人脸");
        settingHandler.sendEmptyMessageDelayed(MSG_RELOAD_FACE_DETECT, DELAY_TIME);
    }

    public void drawFaceInfos(TexturePreviewView previewView, TextureView textureView, FaceInfo[] faceInfos) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        // 如果没有检测到人脸
        if (faceInfos == null || faceInfos.length == 0) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);

            // 如果前面检测到人脸，而现在没有，则说明人已经走了，这个时候关闭回显，显示动效
            if (hasDetectedFace) {
                hasDetectedFace = false;
                settingHandler.removeMessages(MSG_SHOW_VIDEO);
                settingHandler.sendEmptyMessageDelayed(MSG_SHOW_VIDEO, 3000L);
            }
            return;
        }

        // 检测到人脸, 关闭视频
        settingHandler.removeMessages(MSG_SHOW_VIDEO);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkShouldHideVideo();
            }
        });

        hasDetectedFace = true;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (isAlwaysOpenDoor) {
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        // 检测到脸的信息,后续需要在这个地方做处理，比如添加检测到人脸的图.可以对图做缩放处理
        for (FaceInfo faceInfo : faceInfos) {
            int[] faceRect = faceInfo.rect;

            RectF innerRect = new RectF(faceRect[0],
                    faceRect[1],
                    faceRect[2] + faceRect[0],
                    faceRect[3] + faceRect[1]);
            previewView.mapFromOriginalRect(innerRect);

            float[] drawLinesFloat = DrawLineUtils.createPaintLines(innerRect, 5f);
            canvas.drawLines(drawLinesFloat, boldLinePaint);

            RectF innerLineRect = new RectF(innerRect.left,
                    innerRect.top,
                    innerRect.right,
                    innerRect.bottom);
            canvas.drawRect(innerLineRect, thinLinePaint);
        }

        textureView.unlockCanvasAndPost(canvas);
    }

    public void clearBeforeMarker(TextureView baseTextureView) {
        Canvas canvas = baseTextureView.lockCanvas();
        if (canvas == null) {
            baseTextureView.unlockCanvasAndPost(canvas);
            return;
        }

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        baseTextureView.unlockCanvasAndPost(canvas);
    }

    private void uploadCodeStatus(int status) {
        RetrofitConfig.createService().checkCodeValidateStatus(
                Constants.APP_KEY, Constants.APP_SECRET, NetworkUtil.ethernetMac(),
                Constants.PRODUCT_KEY, status)
                .compose(RxJavaAction.<BaseRsp>setThread())
                .subscribe(new BaseObserver<BaseRsp>(this, true,
                        false) {
                    @Override
                    public void onSuccess(BaseRsp responseData) {
                    }
                });
    }

    public abstract void hideInputSuc();

    public abstract void hideScanSucFace();

    public abstract void hideScanErrFace();

    public abstract void showWaitVideo();

    public abstract void hideWaitVideo();

    public abstract void showPwdInputSuc();

    public abstract void initFaceSDKSucAction();

    public abstract void checkShouldHideVideo();

    public abstract void showAlwaysOpenDoorUI();
}
