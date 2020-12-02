package com.brc.acctrl.activity;

import android.view.TextureView;

import com.brc.acctrl.bean.AccessFail;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.utils.AccessFailUtil;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.view.TexturePreviewView;
import com.deepglint.hri.facesdk.FaceInfo;
import com.deepglint.hri.facesdk.FaceTracker;

import org.opencv.core.Mat;

/**
 * Created by zhendan on 5/8/2016.
 */
public abstract class BaseRGBCameraActivity extends BaseCameraActivity {
    public void initCameraPreviewLayout(TexturePreviewView cameraView,
                                        TextureView cameraTexture) {
        this.baseRGBPreviewView = cameraView;
        this.baseRGBTextureView = cameraTexture;
    }

    @Override
    public void doDetectRGBFaceAction(FaceInfo[] faceInfos, Mat rgbImage) {
        if (dbGroupFaceCnt > 0) {
            // 将检测到的人脸和数据库中比对
            checkQualityMatchFace(faceInfos, rgbImage);
        } else if (faceInfos.length > 0) {
            if (canShowFailDlg) {
                resetShowFailDlg();
                showNoMatchFaceFail(null,rgbImage);

                AccessFail failRec = new AccessFail(null, "0", "0", 0);
                if (NetworkUtil.isNetworkAvailable(this)) {
                    AccessFailUtil.tryToUploadFailLog2Server(failRec);
                } else {
                    DBStore.getInstance().insertRegFail(failRec);
                }
            } else {
                // 如果之前没有消息的话，则可以用于发送
                if (!settingHandler.hasMessages(MSG_CAN_SHOW_DETECT_FAIL)) {
                    settingHandler.sendEmptyMessageDelayed(MSG_CAN_SHOW_DETECT_FAIL, DELAY_TIME);
                }
            }
        }
    }

    private void checkQualityMatchFace(FaceInfo[] faceInfos, Mat image) {
        if (faceInfos != null && faceInfos.length > 0) {
            // 针对买个人脸来匹配判别
            for (FaceInfo faceInfo : faceInfos) {
//                checkSingleFaceQuality(faceInfo, image);

                // 这里就开始匹配人脸
                if (!isMatching) {
                    isMatching = true;
                    checkPersonHasPermit(image, faceInfo);
                }
            }
        }
    }

    private void checkSingleFaceQuality(FaceInfo faceInfo, Mat image) {
        float[] orientation = faceInfo.orientation;
        float yaw = orientation[0];
        float pitch = orientation[1];
        float roll = orientation[2];
        if (Math.abs(yaw) > 30 || Math.abs(pitch) > 15 || Math.abs(roll) > 20) {
            LogUtil.e("calMatchDegree degree err");
            return;
        }
        if (faceInfo.faceQuality < 0.5) {
            LogUtil.e("calMatchDegree face quality low");
            return;
        }

        // 这里就开始匹配人脸
        if (!isMatching) {
            checkPersonHasPermit(image, faceInfo);
        }
    }

    @Override
    public void newCameraTrack(FaceTracker.FaceTrackerOptions options) {
        mRGBTracker = new FaceTracker(options);
    }

    @Override
    public void initCamera() {
        initRGBCamera();
    }

    @Override
    public void startCamera() {
        startCameraImgSource(cameraRGBImageSource, frameRGBAvailableListener, cameraRotation);
    }

    @Override
    public void doFinalActionCheckPermit() {
        isMatching = false;
    }

    @Override
    public void checkShouldHideVideo() {
        hideWaitVideo();
    }
}
