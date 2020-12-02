package com.brc.acctrl.camera;

import android.support.annotation.IntDef;
import android.view.View;

import com.brc.acctrl.view.PreviewView;
import com.brc.acctrl.view.TexturePreviewView;

/**
 * Created by koo on 05/09/18.
 */

public interface ICameraControl<T> {

    interface OnFrameListener<T> {
        void onPreviewFrame(T data, int rotation, int width, int height);
    }

    int FLASH_MODE_OFF = 0;

    int FLASH_MODE_TORCH = 1;

    int FLASH_MODE_AUTO = 2;

    @IntDef({FLASH_MODE_TORCH, FLASH_MODE_OFF, FLASH_MODE_AUTO})
    @interface FlashMode {

    }

    int CAMERA_FACING_BACK = 0;

    int CAMERA_FACING_FRONT = 1;

    int CAMERA_USB = 2;

    @IntDef({CAMERA_FACING_FRONT, CAMERA_FACING_BACK, CAMERA_USB})
    @interface CameraFacing {

    }

    interface OnTakePictureCallback {
        void onPictureTaken(byte[] data);
    }


    void start();

    void stop();

    void pause();

    void resume();

    void setOnFrameListener(OnFrameListener listener);

    void setPreferredPreviewSize(int width, int height);

    View getDisplayView();

    void setPreviewView(TexturePreviewView previewView);

    PreviewView getPreviewView();

    void setDisplayOrientation(int displayOrientation);
    void setCameraRotation(int rotation);

    @FlashMode
    int getFlashMode();

    void setCameraFacing(@CameraFacing int cameraFacing);

    void setCameraIndex(int index);
}
