package com.brc.acctrl.camera.source;

import android.content.Context;

import com.brc.acctrl.camera.CameraControl;
import com.brc.acctrl.camera.ICameraControl;
import com.brc.acctrl.listener.OnFrameAvailableListener;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.view.PreviewView;
import com.brc.acctrl.view.TexturePreviewView;

import java.util.ArrayList;

/**
 * Created by koo on 05/09/18.
 */

public class CameraImageSource extends ImageSource {
    private static final String TAG = "CameraImageSource";

    private ICameraControl cameraControl;
    private Context context;

    public ICameraControl getCameraControl() {
        return cameraControl;
    }

    private int cameraFaceType = ICameraControl.CAMERA_FACING_FRONT;

    public CameraImageSource(Context context) {
        this.context = context;
        cameraControl = new CameraControl(getContext());
        cameraControl.setCameraFacing(cameraFaceType);
        cameraControl.setOnFrameListener(new ICameraControl.OnFrameListener<byte[]>() {
            @Override
            public void onPreviewFrame(byte[] data, int rotation, int width, int height) {
                ImageFrame frame = new ImageFrame();
                frame.setRawData(data, width, height);
                ArrayList<OnFrameAvailableListener> listeners = getListeners();
                for (OnFrameAvailableListener listener : listeners) {
                    listener.onFrameAvailable(frame);
                }
            }
        });
    }

    @Override
    public void start() {
        super.start();
        cameraControl.start();
    }

    @Override
    public void stop() {
        super.stop();
        cameraControl.stop();
    }

    private Context getContext() {
        return context;
    }

    @Override
    public void setPreviewView(PreviewView previewView) {
        cameraControl.setPreviewView((TexturePreviewView) previewView);
    }
}
