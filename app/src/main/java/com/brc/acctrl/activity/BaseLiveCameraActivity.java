package com.brc.acctrl.activity;

import android.view.TextureView;

import com.brc.acctrl.camera.CameraControl;
import com.brc.acctrl.listener.OnFrameAvailableListener;
import com.brc.acctrl.camera.source.CameraImageSource;
import com.brc.acctrl.camera.source.ImageFrame;
import com.brc.acctrl.utils.FaceCheckUtils;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.view.TexturePreviewView;
import com.deepglint.hri.facesdk.FaceInfo;
import com.deepglint.hri.facesdk.FaceTracker;
import com.deepglint.hri.facesdk.LivenessChecker;
import com.deepglint.hri.utils.FaceUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by zhendan on 5/8/2016.
 */
public abstract class BaseLiveCameraActivity extends BaseCameraActivity {
    // IR CAMERA
    private TexturePreviewView baseIRPreviewView;
    private TextureView baseIRTextureView;
    private FaceTracker mIRTracker;
    private CameraImageSource cameraIRImageSource;
    private OnFrameAvailableListener frameIRAvailableListener;
    private ImageFrame lastIRFrame;
    private int irPreviewWdh;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private boolean isLiveChecking = false, isIrStartCapture = false;

    private FaceInfo bgrFaceInfo;
    private int liveBgrTrackingID = -1;

    @Override
    public void doDetectRGBFaceAction(FaceInfo[] faceInfos, Mat rgbImage) {
        if (faceInfos != null && faceInfos.length > 0) {
            // 这里应该根据有了IR再来获取RGB人脸来检测而不是根据一开始检测到人脸的RGB来处理
            // 主要是用来排除掉人从外面进来的情况
            if (isIrStartCapture) {
                // 找到最大区域的人脸
                maxRGBDetectFaceInfo = FaceUtils.findMaxFaceInfo(faceInfos);
                // 最大的人脸并且角度质量合适
                if (maxRGBDetectFaceInfo != null) {
//                    LogUtil.trackLogDebug("maxRGBDetectFaceInfo not null");
                    bgrFrame = rgbImage;
                } else {
                    LogUtil.trackLogDebug("maxRGBDetectFaceInfo is null");
                }
            }
        }
    }

    public void initCameraPreviewLayout(TexturePreviewView cameraView,
                                        TextureView cameraTexture,
                                        TexturePreviewView irCameraView,
                                        TextureView irCameraTexture) {
        this.baseRGBPreviewView = cameraView;
        this.baseRGBTextureView = cameraTexture;

        this.baseIRPreviewView = irCameraView;
        this.baseIRTextureView = irCameraTexture;
    }

    @Override
    public void newCameraTrack(FaceTracker.FaceTrackerOptions options) {
        mRGBTracker = new FaceTracker(options);
        mIRTracker = new FaceTracker(options);
    }

    @Override
    public void initCamera() {
        initRGBCamera();
        initIRCamera();
    }

    private void initIRCamera() {
        cameraIRImageSource = initCameraSource(baseIRPreviewView, baseIRTextureView, CameraControl.CAMERA_IR);

        frameIRAvailableListener = new OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(ImageFrame frame) {
                if (!isResumeState) {
                    return;
                }

                if (canDetectFace) {
                    lastIRFrame = frame;
                    processIRRunnable.run();
                }
            }
        };
    }

    @Override
    public void startCamera() {
        startCameraImgSource(cameraRGBImageSource, frameRGBAvailableListener, cameraRotation);
    }

    private void startIrCameraCapture() {
        if (cameraIRImageSource.getListeners().size() == 0) {
            startCameraImgSource(cameraIRImageSource, frameIRAvailableListener, cameraRotation);
        }
    }

    private Runnable processIRRunnable = new Runnable() {
        @Override
        public void run() {
            // 初始化及采集到的数据有效
            if (mIRTracker == null || lastIRFrame == null) {
                return;
            }

            // 如果正在处理上一帧图像则跳过
            if (isLiveChecking || isAlwaysOpenDoor) {
                return;
            }

            isIrStartCapture = true;

//            LogUtil.e("processIRRunnable");
            Mat irImage;
            synchronized (lastIRFrame) {
                // 提取上一帧的mat矩阵数据
                irImage = lastIRFrame.getBgrMat();
                lastIRFrame = null;
            }

            // 针对数据做处理
            detectFaceInIrFrame(irImage);
        }
    };

    private void detectFaceInIrFrame(Mat irImage) {
        if (isLiveChecking) {
            return;
        }

        isLiveChecking = true;

        try {
            Core.transpose(irImage, irImage);
            Core.flip(irImage, irImage, 0);
            /*FaceInfo currentBgrFace = new FaceInfo();
            currentBgrFace.trackingId = bgrFaceInfo.trackingId;*/
            irPreviewWdh = (int) irImage.size().width;
            mIRTracker.update(irImage, System.currentTimeMillis(), false);
            FaceInfo[] faceInfos = mIRTracker.getTrackedFaceInfos();
            FaceInfo irMaxFaceInfo = FaceUtils.findMaxFaceInfo(faceInfos);
            if (irMaxFaceInfo == null || maxRGBDetectFaceInfo == null) {
                LogUtil.e("detectAndMatchIRFace irMaxFaceInfo or maxRGBDetectFaceInfo null");
                isLiveChecking = false;
                return;
            }
           /* if (FaceCheckUtils.checkRgbIrSameFace(baseRGBPreviewView, maxRGBDetectFaceInfo,
                    rgbPreviewWdh, baseIRPreviewView, irMaxFaceInfo, irPreviewWdh)) {
                    asyncLiveness(irImage, irMaxFaceInfo);
            }*/
            /*if(currentBgrFace.trackingId != liveBgrTrackingID && FaceCheckUtils.isSameFace(currentBgrFace, irMaxFaceInfo,baseRGBPreviewView,baseIRPreviewView,irImage.size(),rgbPreviewWdh)) {
                LogUtil.i("-----------------------------------");
                asyncLiveness(irImage, irMaxFaceInfo);
            }*/

            // 先判别是同一个人脸再做活体检测，因为这个判别人脸位置快
            if (FaceCheckUtils.checkRgbIrSameFace(baseRGBPreviewView, maxRGBDetectFaceInfo,
                    rgbPreviewWdh, baseIRPreviewView, irMaxFaceInfo, irPreviewWdh)) {
                LivenessChecker.LivenessResult livenessResult = faceSDKManager.checkIrLiveness(irImage, irMaxFaceInfo);
                if (livenessResult == LivenessChecker.LivenessResult.LIVE) {
                    LogUtil.i("判断为活体");
                    checkPersonHasPermit(bgrFrame, maxRGBDetectFaceInfo);
                    return;
                } else if (livenessResult == LivenessChecker.LivenessResult.NOT_LIVE) {
                    LogUtil.i("判断为非活体");
                } else if (livenessResult == LivenessChecker.LivenessResult.NO_FACE) {
                    LogUtil.i("红外相机未检测到有效人脸");
                } else if (livenessResult == LivenessChecker.LivenessResult.MULTI_FACE) {
                    LogUtil.i("红外相机检测到多个人脸");
                }
                LogUtil.i("livenessResult: " + livenessResult);
            }
        } catch (Exception e) {
        }


        isLiveChecking = false;
    }

    @Override
    public void stopOtherCameras() {
        stopCameraImgSource(cameraIRImageSource, frameIRAvailableListener);
        isIrStartCapture = false;
    }

    @Override
    public void doFinalActionCheckPermit() {
        isLiveChecking = false;
    }

    @Override
    public void checkShouldHideVideo() {
        if (isVideoMaskShowing() && !settingHandler.hasMessages(MSG_HIDE_VIDEO)) {
            // 这里之所以要延迟800ms是为了降低用户等待感受。因为红外开启需要600~700ms时间
            settingHandler.sendEmptyMessageDelayed(MSG_HIDE_VIDEO, 600L);
            // 红外不能一直打开尝试打开
            if (!isAlwaysOpenDoor) {
                startIrCameraCapture();
            }
        }
    }

    public boolean isVideoMaskShowing() {
//        if (baseVideoView == null) {
//            return isMeetingStatusShow();
//        } else {
//            return baseVideoView.getAlpha() > 0.5;
//        }
        return true;
    }

    public boolean isMeetingStatusShow() {
        return false;
    }

   /* private void asyncLiveness(final Mat irImage,final FaceInfo irFaceInfo) {
        if(isLiveChecking) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    float[] orientation = irFaceInfo.orientation;
                    isLiveChecking =false;
                    long now = System.currentTimeMillis();
                    float livenessScore = faceSDKManager.getIrLivenessScore(irImage, irFaceInfo);
                    if (livenessScore >= 0.95) {
//                        liveBgrTrackingID = bgrFaceInfo.trackingId;
                        checkPersonHasPermit(bgrFrame, maxRGBDetectFaceInfo);
                    } else if (livenessScore >0 && livenessScore < 0.95) {
                        liveBgrTrackingID = -1;
                    }else{
                    }
                    isLiveChecking =true;
                }
            });
        }

    }*/
}
