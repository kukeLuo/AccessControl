package com.brc.acctrl.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.brc.acctrl.R;
import com.brc.acctrl.bean.AccessFail;
import com.brc.acctrl.bean.AccessLog;
import com.brc.acctrl.bean.AccessUser;
import com.brc.acctrl.bean.BaseRsp;
import com.brc.acctrl.bean.RspImagePath;
import com.brc.acctrl.bean.UploadFailReq;
import com.brc.acctrl.bean.UploadFailRsp;
import com.brc.acctrl.camera.CameraControl;
import com.brc.acctrl.camera.source.CameraImageSource;
import com.brc.acctrl.camera.source.ImageFrame;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.db.UserDatabase;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.listener.OnFrameAvailableListener;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.utils.AccessFailUtil;
import com.brc.acctrl.utils.AccessRecordUtil;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.FaceSDKUtil;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.view.TexturePreviewView;
import com.deepglint.hri.face.FaceSDKManager;
import com.deepglint.hri.facesdk.FaceInfo;
import com.deepglint.hri.facesdk.FaceTracker;
import com.deepglint.hri.facesdk.SearchResult;
import com.deepglint.hri.utils.ImageUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.Calendar;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhendan on 5/8/2016.
 */
public abstract class BaseCameraActivity extends BasePreviewActivity {
    public VideoView baseVideoView;
    // RGB CAMERA
    public TexturePreviewView baseRGBPreviewView;
    public TextureView baseRGBTextureView;
    public Mat bgrFrame;
    public FaceInfo maxRGBDetectFaceInfo;
    public FaceTracker mRGBTracker;
    public CameraImageSource cameraRGBImageSource;
    public OnFrameAvailableListener frameRGBAvailableListener;
    public ImageFrame lastRGBFrame;
    private Bitmap matchFaceBmp;
    public int rgbPreviewWdh;
    private String strErrFileName = "%1$d_%2$s_%3$d_%4$s.jpg";

    // base video
    public void loadVideo(VideoView videoView) {
        this.baseVideoView = videoView;
        baseVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                baseVideoView.start();
            }
        });
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/" +
                rawVideoResource());
        baseVideoView.setVideoURI(uri);
        baseVideoView.start();
    }

    private void stopPlaybackVideo() {
        if (baseVideoView != null) {
            try {
                baseVideoView.stopPlayback();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void showPwdInputSuc() {
        resetShowFailDlg();

        View inputSucView = getInputSucView();
        if (inputSucView != null) {
            inputSucView.setVisibility(View.VISIBLE);
        }

        hideScanSucFace();

        GoldenEyesUtils.setGoldenEyesStateGreedLED71();
    }

    // 因为服务器无法返回结果，所以这个函数应该没地方调用
    public void showCardInputSuc() {
        View inputSucView = getInputSucView();
        TextView tvSucView = getTextSucView();
        if (inputSucView != null) {
            inputSucView.setVisibility(View.VISIBLE);
        }

        if (tvSucView != null) {
            tvSucView.setText(R.string.str_door_open_card);
        }

        // TODO 这里可以记录到数据库中保存对应的信息
        GoldenEyesUtils.setGoldenEyesStateGreedLED71();
    }

    @Override
    public void hideInputSuc() {
        View inputSucView = getInputSucView();
        if (inputSucView != null) {
            inputSucView.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideScanErrFace() {
        View scanFaceErrView = getScanErrView();
        if (scanFaceErrView != null) {
            scanFaceErrView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showWaitVideo() {
//        if (baseVideoView != null && baseVideoView.getAlpha() < 0.5) {
        if (baseVideoView != null) {
            baseVideoView.setAlpha(1);
            baseVideoView.pause();
            baseVideoView.start();
        }

        showVideoMask();
        stopOtherCameras();
    }

    @Override
    public void hideWaitVideo() {
        // 这里会发生小概率的ANR异常，所以调整方式处理
//        if (baseVideoView != null && baseVideoView.getAlpha() > 0.5) {
        if (baseVideoView != null) {
            baseVideoView.setAlpha(0);
            baseVideoView.pause();
        }

        hideVideoMask();
    }

    public abstract View getInputSucView();

    public abstract TextView getTextSucView();

    public abstract TextView getInfoTitleView();

    public abstract TextView getInfoDescView();

    public abstract TextView getInfoDescMoreView();

    // life cycle
    @Override
    protected void onResume() {
        super.onResume();
        if (baseVideoView != null) {
            baseVideoView.start();
        }

        getInfoTitleView().setText(SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_TITLE, getString(R.string.str_mode_default_title)));
        getInfoDescView().setText(SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_DESC, ""));
        if (getInfoDescMoreView() != null) {
            getInfoDescMoreView().setText(SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_DESC_MORE, ""));
        }

        if (faceInitSuc) {
            startCamera();
        } else {
            initFaceSDK();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (baseVideoView != null) {
            baseVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settingHandler.removeCallbacksAndMessages(null);
        FaceSDKManager.getInstance().release();
        stopPlaybackVideo();
    }

    private static final int RESET_CLOSE_CAMERA = 0;
    private static final int RESET_START_CAMERA = 1;
    private Handler resetCameraHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case RESET_CLOSE_CAMERA:
                    canDetectFace = false;
                    showWaitVideo();
                    stopCamera();
                    resetCameraHandler.sendEmptyMessageDelayed(RESET_START_CAMERA, 1000L);
                    break;
                case RESET_START_CAMERA:
                    startCamera();
                    canDetectFace = true;
                    break;
            }
            super.dispatchMessage(msg);
        }
    };

    private Runnable processRGBRunnable = new Runnable() {
        @Override
        public void run() {
            if (lastRGBFrame == null) {
                return;
            }
            Mat bgrImage;
            synchronized (lastRGBFrame) {
                // 提取上一帧的mat矩阵数据
                bgrImage = lastRGBFrame.getBgrMat();
                lastRGBFrame = null;
            }
//            LogUtil.i("RGB检测人脸，并去绘制人脸框");
            // 针对RGB数据做处理
            detectFaceInRgbFrame(bgrImage);
        }
    };

    public void initSDK() {
        initCardSound();
        // 2个部分: 1.时间刷新 2.软件狗20s
        initTimeClockAction();
        GoldenEyesUtils.hideSystemBar(this);
        faceSDKManager = FaceSDKManager.getInstance();
        initCamera();

        initPaintStyle();
    }

    public void initRGBCamera() {
        cameraRGBImageSource = initCameraSource(baseRGBPreviewView, baseRGBTextureView, CameraControl.CAMERA_RGB);
        frameRGBAvailableListener = new OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(ImageFrame frame) {
//                LogUtil.i("RGB CAMERA 抓取有效Frame");
                resetCameraHandler.removeMessages(RESET_CLOSE_CAMERA);
                if (!isResumeState) {
                    return;
                }
                resetCameraHandler.sendEmptyMessageDelayed(RESET_CLOSE_CAMERA, 2000L);

                if (canDetectFace) {
                    lastRGBFrame = frame;
                    processRGBRunnable.run();
                } else {
                    clearBeforeMarker(baseRGBTextureView);
                }
            }
        };
    }

    private void stopCamera() {
        resetCameraHandler.removeMessages(RESET_CLOSE_CAMERA);
        stopCameraImgSource(cameraRGBImageSource, frameRGBAvailableListener);
        stopOtherCameras();
    }

    // detect face
    private void detectFaceInRgbFrame(Mat image) {
        if (mRGBTracker == null) {
            return;
        }

        try {
            // 针对不同的旋转做处理
            Core.transpose(image, image);
            Core.flip(image, image, 0);

            rgbPreviewWdh = (int) image.size().width;
            mRGBTracker.update(image, System.currentTimeMillis(), false);
            // 提取页面中检测的人脸
            FaceInfo[] faceInfos = mRGBTracker.getTrackedFaceInfos();
            // 先标记处人脸再处理
            drawFaceInfos(baseRGBPreviewView, baseRGBTextureView, faceInfos);

            if (!isAlwaysOpenDoor) {
                doDetectRGBFaceAction(faceInfos, image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Mat checkMat;
    private FaceInfo checkFace;

    public void checkPersonHasPermit(final Mat mat, final FaceInfo faceInfo) {
        checkMat = mat;
        checkFace = faceInfo;
        executorPool.execute(matchFaceInDB);
    }

    private Runnable matchFaceInDB = new Runnable() {
        @Override
        public void run() {
            // 提取RGB人脸特征数据
            Calendar dayStartCalendar = Calendar.getInstance();
            try {
                Mat bgrFaceImage = new Mat();
                faceSDKManager.alignFace(checkMat, checkFace, bgrFaceImage);
                Mat bgrFeature = new Mat();
                faceSDKManager.extractFeature(bgrFaceImage, bgrFeature);
                int hourMinute = dayStartCalendar.get(Calendar.MINUTE);
                int dayHour = dayStartCalendar.get(Calendar.HOUR_OF_DAY);
                if(TextUtils.equals(Constants.INDENTIFY_ON,SPUtil.getInstance().getValue(SPUtil.SHOW_FILL_LIGHTS_STATUS, Constants.OFTEN_OFF))){
                    Constants.checkTopLightStatus(dayHour,hourMinute);
                }
                // 现在开始找最匹配的人脸了
                SearchResult searchResult = FaceSDKManager.getInstance()
                        .searchHighestScoreFace(bgrFeature);

                if (searchResult.score > Constants.THRESH_DETECT_FACE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View inputSucView = getInputSucView();
                            if (inputSucView != null) {
                                inputSucView.setVisibility(View.GONE);
                            }
                        }
                    });

                    detectMatchedFace(searchResult.index,searchResult,bgrFaceImage);

                } else {
                    if (canShowFailDlg) {
                        resetShowFailDlg();
                        showNoMatchFaceFail(searchResult,bgrFaceImage);
                       /* // save error file
                        String userInfo = featureIDs.get(searchResult.index);
                        if (TextUtils.isEmpty(userInfo)) {
                            return;
                        }

                        String[] splitStrs = userInfo.split("-");
                        long curMillsTime = System.currentTimeMillis();
                        String fileName = String.format(strErrFileName, curMillsTime,
                                splitStrs[0], (int) searchResult.score, NetworkUtil.ethernetMac());
                        FaceSDKUtil.getInstance().saveErrBitmap2File(ImageUtils.Mat2Bitmap(bgrFaceImage), fileName);

                        // send err log 2 sever
                        AccessFail failRec = new AccessFail(fileName, splitStrs[2], splitStrs[0],
                                (int) searchResult.score, curMillsTime);
                        if (NetworkUtil.isNetworkAvailable(BaseCameraActivity.this)) {
                            AccessFailUtil.tryToUploadFailLog2Server(failRec);
                        } else {
                            DBStore.getInstance().insertRegFail(failRec);
                        }*/
                    } else {
                        // 如果之前没有消息的话，则可以用于发送
                        if (!settingHandler.hasMessages(MSG_CAN_SHOW_DETECT_FAIL)) {
                            settingHandler.sendEmptyMessageDelayed(MSG_CAN_SHOW_DETECT_FAIL, 1000);
                        }
                    }
                }
            } catch (Exception e) {

            } finally {
                doFinalActionCheckPermit();
            }
        }
    };

    public void resetShowFailDlg() {
        canShowFailDlg = false;
        settingHandler.removeMessages(MSG_CAN_SHOW_DETECT_FAIL);
        settingHandler.removeMessages(MSG_RESET_SHOW_DETECT_FAIL);
    }

    public void showNoMatchFaceFail(SearchResult searchResult,Mat bgrFaceImage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // 如果之前有人脸检测成功，则不成功不显示
                View scanSucView = getScanSucView();
                if (scanSucView != null && scanSucView.getVisibility() == View.VISIBLE) {
                    return;
                }

                settingHandler.sendEmptyMessage(MSG_SHOW_NOMATCHFACE);
                GoldenEyesUtils.setGoldenEyesStateRedLED71();

                View scanErrView = getScanErrView();
                if (scanErrView != null) {
                    scanErrView.setVisibility(View.VISIBLE);
                }
                if(searchResult!=null){
                    uploadRecord2Service(searchResult,bgrFaceImage,1);
                }
            }
        });
    }

    public void detectMatchedFace(int resultIdx,SearchResult searchResult,Mat bgrFaceImage) {
        // 重新设置检测失败时3s后显示
        resetShowFailDlg();

        LogUtil.i("detectMatchedFace resultIdx = " + resultIdx);
        String userInfo = featureIDs.get(resultIdx);
        if (TextUtils.isEmpty(userInfo)) {
            showNoMatchFaceFail(searchResult,bgrFaceImage);
            return;
        }

        String[] splitStrs = userInfo.split("-");
        LogUtil.i("detectMatchedFace userInfo = " + userInfo);

        int mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
        if (mode == SPUtil.ACCESS_TYPE_BOSS) {
            UserDatabase.getInstance().getAccessUserDao().fetchUserByUserId(splitStrs[0])
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<AccessUser>() {

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(AccessUser accessUser) {
                            matchName = accessUser.getUsername();
                            String userBitmapFilePath = accessUser.getDeviceBmpPath();

                            showScanFaceSuc(splitStrs[0], matchName, accessUser.getAvatarUrl(),
                                    userBitmapFilePath,searchResult,bgrFaceImage);
                        }

                        @Override
                        public void onError(Throwable e) {
                            showNoMatchFaceFail(searchResult,bgrFaceImage);
                        }
                    });
        } else {
            showScanFaceSuc(splitStrs[0], splitStrs[2], "", "",searchResult,bgrFaceImage);
        }
    }

    public void showScanFaceSuc(String userId, String userName, String avatarUrl,
                                String userBmpPath,SearchResult searchResult,Mat bgrFaceImage) {
        // 这里需要在UI界面呈现出效果
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View scanSucView = getScanSucView();
                if (scanSucView != null) {
                    scanSucView.setVisibility(View.VISIBLE);
                }

                hideInputSuc();
                TextView tvSucView = getTextScanSucView();
                if (tvSucView != null) {
                    if(SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_NAME, true)){
                        tvSucView.setText(StringUtil.CpStrStrPara(R.string.str_face_recognition_sus, userName));
                    }else{
                        tvSucView.setText(StringUtil.CpStrPara(R.string.str_recognition_sus_hide));
                    }
                }

                GoldenEyesUtils.setGoldenEyesStateGreedLED71();

                ImageView circleImageView = getImageView();
                if (circleImageView != null) {
                    if(!TextUtils.isEmpty(userBmpPath)&&SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_NAME, true)){
                        matchFaceBmp = BitmapFactory.decodeFile(userBmpPath);
                        circleImageView.setImageBitmap(matchFaceBmp);
                    }else if(bgrFaceImage!=null){
                        circleImageView.setImageBitmap(ImageUtils.Mat2Bitmap(bgrFaceImage));
                    }
                }
            }
        });

        if (canDetectFace) {
            uploadRecord2Service(searchResult,bgrFaceImage,0);
        }
        settingHandler.sendEmptyMessage(MSG_SHOW_MATCHFACE);
    }

    public void insertIdentificationLog(String captureFaceFile, String avatarUrl,
                                        String userName, String userId,
                                        int ratio, long time,int type,int comparison){
        AccessLog accessLog=new AccessLog(captureFaceFile,avatarUrl,userName,userId,ratio,time,type,comparison);
        DBStore.getInstance().insertAccessLog(accessLog);
        AccessRecordUtil.fetchUrlByFile(this,accessLog,comparison);


    }

    /**
     *  save success and fail file
     *  comparison 0代表成功，1代表失败
     */
    public void uploadRecord2Service(SearchResult searchResult,Mat bgrFaceImage,int comparison){

        String userInfo = featureIDs.get(searchResult.index);
        if (TextUtils.isEmpty(userInfo)) {
            return;
        }
        String[] splitStrs = userInfo.split("-");
        long curMillsTime = System.currentTimeMillis();
        String fileName = String.format(strErrFileName, curMillsTime,
                splitStrs[0], (int) searchResult.score, NetworkUtil.ethernetMac());
        //此处保存，上传成功后，该文件下的图片会删除
//        FaceSDKUtil.getInstance().saveErrBitmap2File(ImageUtils.Mat2Bitmap(bgrFaceImage), fileName);
        //此处保存，每两周删除一次
        FaceSDKUtil.getInstance().saveLogBitmap2File(ImageUtils.Mat2Bitmap(bgrFaceImage), fileName);
        UserDatabase.getInstance().getAccessUserDao().fetchUserByUserId(splitStrs[0])
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<AccessUser>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(AccessUser accessUser) {
                        String deviceBmpPath=accessUser.getDeviceBmpPath();
                        String targeImageUr=accessUser.getAvatarUrl();
                        // 该方法是用于上传识别记录(包括成功和失败)，由于之前只上传失败人脸，所以取名上有偏端(只考虑到了失败)，但是不影响上传
                        AccessFail failRec = new AccessFail(fileName,deviceBmpPath, splitStrs[2], splitStrs[0],
                                (int) searchResult.score, curMillsTime);
                        if (NetworkUtil.isNetworkAvailable(BaseCameraActivity.this)) {
                            AccessFailUtil.tryToUploadFailLog2Server(failRec);
                        } else {
                            DBStore.getInstance().insertRegFail(failRec);
                        }
                        insertIdentificationLog(fileName,targeImageUr, splitStrs[2], splitStrs[0],
                                (int) searchResult.score, curMillsTime,0,comparison);
                    }

                    @Override
                    public void onError(Throwable e) {
                        insertIdentificationLog(fileName,"", splitStrs[2], splitStrs[0],
                                0, curMillsTime,0,comparison);
                    }
                });

    }
    @Override
    public void hideScanSucFace() {
        View scanSucView = getScanSucView();
        if (scanSucView != null) {
            scanSucView.setVisibility(View.GONE);
        }

        ImageView circleImageView = getImageView();
        if (circleImageView != null) {
            circleImageView.setImageBitmap(null);

            if (matchFaceBmp != null && !matchFaceBmp.isRecycled()) {
                matchFaceBmp.recycle();
                matchFaceBmp = null;
            }
        }

    }

    public abstract View getScanSucView();

    public abstract View getScanErrView();

    public abstract ImageView getImageView();

    public abstract TextView getTextScanSucView();

    @Override
    public void initFaceSDKSucAction() {
        faceInitSuc = true;

        initCameraTrack();
        startCamera();
        // 加载人脸组
        refreshFaceByType(true);
    }

    private void initCameraTrack() {
        FaceTracker.FaceTrackerOptions options = new FaceTracker.FaceTrackerOptions();
        options.detectStrategy = FaceTracker.DetectStrategy.DETECT_HIGH_PRECISION;
        options.detectIntervalTimeMS = 500;
        options.minTrackingSize = 100;
        options.minConfidence = 0.7f;

        newCameraTrack(options);
    }

    public void stopOtherCameras() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvANREvent(RefreshEvents.ANREvent event) {
        settingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
//                    Runtime.getRuntime().exec("su -s sh -c input tap 400 650"); // close app
                    Runtime.getRuntime().exec("su -s sh -c input tap 400 700"); // hide dlg
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("ANR", e.getMessage());
                }
            }
        }, 3000L);
    }

    public void shouldShowDescMore() {
        TextView tvInfoDescmore = getInfoDescMoreView();
        if (tvInfoDescmore == null) {
            return;
        }
        int mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
        String moreDesc = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_DESC_MORE, "");
        if (TextUtils.isEmpty(moreDesc)) {
            if (mode == SPUtil.ACCESS_TYPE_MEETING) {
                tvInfoDescmore.setVisibility(View.VISIBLE);
                tvInfoDescmore.setText(R.string.str_group_name);
            } else {
                tvInfoDescmore.setVisibility(View.GONE);
            }
        } else {
            tvInfoDescmore.setVisibility(View.VISIBLE);
            tvInfoDescmore.setText(moreDesc);
        }
    }

    public void showVideoMask() {

    }

    public void hideVideoMask() {

    }

    public abstract int rawVideoResource();

    public abstract void newCameraTrack(FaceTracker.FaceTrackerOptions options);

    public abstract void startCamera();

    public abstract void initCamera();

    public abstract void doFinalActionCheckPermit();

    public abstract void doDetectRGBFaceAction(FaceInfo[] faceInfos, Mat rgbImage);
}
