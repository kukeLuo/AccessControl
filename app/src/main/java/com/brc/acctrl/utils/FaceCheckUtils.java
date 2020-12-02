package com.brc.acctrl.utils;

import android.graphics.RectF;

import com.brc.acctrl.view.TexturePreviewView;
import com.deepglint.hri.face.FaceSDKManager;
import com.deepglint.hri.facesdk.FaceInfo;
import com.deepglint.hri.log.Logger;

import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * @author zhengdan
 * @date 2019-07-16
 * @Description:
 */
public class FaceCheckUtils {
    private final static int DISTANCE_FACE_SAME_WIDTH = 75;
    private final static int DISTANCE_FACE_SAME_HEIGHT = 40;
    private static int xDistanceRbg2Ir = 75;//根据相机成像进行调整
    private static int yDistanceRgb2Ir = 40;//根据相机成像进行调整

    public static boolean checkRgbIrMaxFaceFeature(Mat bgrFrame, Mat irImage, FaceInfo maxRGBDetectFaceInfo, FaceInfo irMaxFaceInfo) {
        Mat irFaceImage = new Mat();
        FaceSDKManager.getInstance().alignFace(irImage, irMaxFaceInfo, irFaceImage);
        Mat irFeature = new Mat();
        FaceSDKManager.getInstance().extractFeature(irFaceImage, irFeature);

        Mat bgrFaceImage = new Mat();
        FaceSDKManager.getInstance().alignFace(bgrFrame, maxRGBDetectFaceInfo, bgrFaceImage);
        Mat bgrFeature = new Mat();
        FaceSDKManager.getInstance().extractFeature(bgrFaceImage, bgrFeature);

        float score = FaceSDKManager.getInstance().matchFeature(irFeature, bgrFeature);
        return score > Constants.THRESH_DETECT_FACE;
    }

    public static boolean checkRgbIrSameFaceVer1(TexturePreviewView rgbView, FaceInfo maxRGBDetectFaceInfo,
                                                 TexturePreviewView irView, FaceInfo irMaxFaceInfo) {
        // 通过比对得到, RGB的center.x要比IR的center.x 大 0< (centerRGB.x - centerIR.x) < 50
        // 同时y也是这种情况 0< (centerRGB.y - centerIR.y) < 50
        RectF innerRect = new RectF(maxRGBDetectFaceInfo.rect[0],
                maxRGBDetectFaceInfo.rect[1],
                maxRGBDetectFaceInfo.rect[2] + maxRGBDetectFaceInfo.rect[0],
                maxRGBDetectFaceInfo.rect[3] + maxRGBDetectFaceInfo.rect[1]);
        rgbView.mapFromOriginalRect(innerRect);

        int centerRGBX = (int) (innerRect.left + innerRect.width() / 2);
        int centerRGBY = (int) (innerRect.top + innerRect.height() / 2);

        RectF innerIrRect = new RectF(irMaxFaceInfo.rect[0],
                irMaxFaceInfo.rect[1],
                irMaxFaceInfo.rect[2] + irMaxFaceInfo.rect[0],
                irMaxFaceInfo.rect[3] + irMaxFaceInfo.rect[1]);
        irView.mapFromOriginalRect(innerIrRect);

        int centerIRX = (int) (innerIrRect.left + innerIrRect.width() / 2);
        int centerIRY = (int) (innerIrRect.top + innerIrRect.height() / 2);

        int difCenterX = Math.abs(centerRGBX - centerIRX);
        int difCenterY = Math.abs(centerRGBY - centerIRY);
        LogUtil.e("detectAndMatchIRFace difX = " + difCenterX + ":diffY = " + difCenterY);

        return (difCenterX > 0 && difCenterX < DISTANCE_FACE_SAME_WIDTH) && (difCenterY < DISTANCE_FACE_SAME_HEIGHT);
    }
    public static boolean isSameFace(FaceInfo rgbFaceInfo, FaceInfo irFaceInfo,
                               TexturePreviewView rgbPreviewView, TexturePreviewView irPreviewView, Size irPreviewSize,int rgbPreviewWdh) {
        boolean rgbMirrored = rgbPreviewView.isMirrored();
        boolean irMirrored = irPreviewView.isMirrored();
        float rgbCenterX = rgbMirrored ? (float)rgbPreviewWdh - (rgbFaceInfo.rect[0] + rgbFaceInfo.rect[2] / 2):rgbFaceInfo.rect[0] + rgbFaceInfo.rect[2] / 2;
        float rgbCenterY = rgbFaceInfo.rect[1] + rgbFaceInfo.rect[3] / 2;
        float irCenterX = irMirrored ? (float)irPreviewSize.width - (irFaceInfo.rect[0] + irFaceInfo.rect[2] /2): irFaceInfo.rect[0] + irFaceInfo.rect[2] /2;
        float irCenterY = irFaceInfo.rect[1] + irFaceInfo.rect[3] / 2;
        if(Math.abs(irCenterX - rgbCenterX) > Math.abs( xDistanceRbg2Ir) || Math.abs(irCenterY - rgbCenterY) > Math.abs(yDistanceRgb2Ir)) {
            return false;
        }
        return true;
    }
    public static boolean checkRgbIrSameFace(TexturePreviewView rgbView, FaceInfo rgbFaceInfo,
                                             int rgbWdh, TexturePreviewView irView,
                                             FaceInfo irFaceInfo, int irWdh) {
        boolean rgbMirrored = rgbView.isMirrored();
        boolean irMirrored = irView.isMirrored();
        int rgbCenterX = rgbMirrored ? rgbWdh - (rgbFaceInfo.rect[0] + rgbFaceInfo.rect[2] / 2) :
                rgbFaceInfo.rect[0] + rgbFaceInfo.rect[2] / 2;
        int rgbCenterY = rgbFaceInfo.rect[1] + rgbFaceInfo.rect[3] / 2;
        int irCenterX = irMirrored ? irWdh - (irFaceInfo.rect[0] + irFaceInfo.rect[2] / 2) :
                irFaceInfo.rect[0] + irFaceInfo.rect[2] / 2;
        int irCenterY = irFaceInfo.rect[1] + irFaceInfo.rect[3] / 2;

        int difCenterX = Math.abs(rgbCenterX - irCenterX);
        int difCenterY = Math.abs(rgbCenterY - irCenterY);
        LogUtil.i("face distance x = " + difCenterX + ":y = " + difCenterY);

        if (difCenterX < DISTANCE_FACE_SAME_WIDTH &&
                difCenterY < DISTANCE_FACE_SAME_HEIGHT) {
            return true;
        }

        return false;
    }

    public static boolean checkFaceIsGood(FaceInfo faceInfo) {
        float[] orientation = faceInfo.orientation;
        float yaw = orientation[0];
        float pitch = orientation[1];
        float roll = orientation[2];
        if (Math.abs(yaw) > 30 || Math.abs(pitch) > 15 || Math.abs(roll) > 20) {
            LogUtil.e("人脸角度太大，请正对屏幕");
            return false;
        }

        if (faceInfo.faceQuality > 0.0 && faceInfo.faceQuality < 0.5) {
            LogUtil.e("人脸质量过低");
            return false;
        }

        return true;
    }
}
