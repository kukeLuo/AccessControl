package com.brc.acctrl.camera;

/**
 * Created by koo on 05/09/18.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.view.CameraView;
import com.brc.acctrl.view.PreviewView;
import com.brc.acctrl.view.TexturePreviewView;
import com.deepglint.hri.log.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CameraControl implements ICameraControl {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public static final int CAMERA_RGB = 0;
    public static final int CAMERA_IR = 1;

    private int displayOrientation = 0;
    private int cameraId = 0;
    private int cameraRotation = 0;
    private Camera.CameraInfo cameraInfo;
    private int flashMode;
    private AtomicBoolean takingPicture = new AtomicBoolean(false);

    private Context context;
    private Camera camera;
    private HandlerThread cameraHandlerThread = null;
    private Handler cameraHandler = null;
    private Handler uiHandler = null;

    private Camera.Parameters parameters;
    private PermissionCallback permissionCallback;
    private Rect previewFrame = new Rect();

    private int preferredWidth = 1280;
    private int preferredHeight = 720;

    @CameraFacing
    private int cameraFacing = CAMERA_FACING_FRONT;

    private int cameraIndex = -1;

    @Override
    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    @Override
    public void setCameraRotation(int rotation) {
        this.cameraRotation = rotation;
    }

    @Override
    public int getFlashMode() {
        return flashMode;
    }

    @Override
    public void setCameraFacing(@CameraFacing int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    @Override
    public void setCameraIndex(int index) {
        this.cameraIndex = index;
    }

    @Override
    public void start() {
        postStartCamera();
    }

    private SurfaceTexture surfaceTexture;

    private void postStartCamera() {
        if (cameraHandlerThread == null || !cameraHandlerThread.isAlive()) {
            cameraHandlerThread = new HandlerThread("camera");
            cameraHandlerThread.start();
            cameraHandler = new Handler(cameraHandlerThread.getLooper());
            uiHandler = new Handler(Looper.getMainLooper());
        }

        if (cameraHandler == null) {
            return;
        }

        cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    startCamera();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void startCamera() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (permissionCallback != null) {
                permissionCallback.onRequestPermission();
            }
            return;
        }
        Logger.d("Camera", "cameraIndex:" + cameraIndex);
        if (cameraIndex >= 0) {
            if (cameraIndex >= Camera.getNumberOfCameras()) {
                Logger.e("Camera", "try to open camera: " + cameraIndex + " ," +
                        "but only " + Camera.getNumberOfCameras() + " cameras" +
                        " attached!");
                cameraId = 0;
            } else {
                cameraId = cameraIndex;
            }
        } else {
            if (camera == null) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == cameraFacing) {
                        cameraId = i;
                        this.cameraInfo = cameraInfo;
                    }
                }
            }
        }
        if (cameraInfo == null) {
            cameraInfo = new Camera.CameraInfo();
        }
        Camera.getCameraInfo(cameraId, cameraInfo);
        camera = Camera.open(cameraId);
        int detectRotation = 0;
        if (cameraFacing == ICameraControl.CAMERA_FACING_FRONT) {
            int rotation = ORIENTATIONS.get(displayOrientation);
            rotation = getCameraDisplayOrientation(rotation, cameraId, camera);
            camera.setDisplayOrientation(rotation);
            detectRotation = rotation;
            if (displayOrientation == CameraView.ORIENTATION_PORTRAIT) {
                if (detectRotation == 90 || detectRotation == 270) {
                    detectRotation = (detectRotation + 180) % 360;
                }
            }
        } else if (cameraFacing == ICameraControl.CAMERA_FACING_BACK) {
            int rotation = ORIENTATIONS.get(displayOrientation);
            rotation = getCameraDisplayOrientation(rotation, cameraId, camera);
            camera.setDisplayOrientation(rotation);
            detectRotation = rotation;
        } else if (cameraFacing == ICameraControl.CAMERA_USB) {
            if (cameraInfo.facing == CAMERA_FACING_FRONT) {
                if (cameraRotation == 90) {
                    cameraRotation = 270;
                } else if (cameraRotation == 270) {
                    cameraRotation = 90;
                }
                camera.setDisplayOrientation(cameraRotation);
                detectRotation = cameraRotation;
            } else {
                camera.setDisplayOrientation(cameraRotation);
                detectRotation = cameraRotation;
            }
        }
        Logger.d("Camera", "detectRotation:" + detectRotation + " cameraInfo" +
                ":" + cameraInfo.facing + " " + cameraInfo.orientation);
        if (cameraInfo.facing == CAMERA_FACING_FRONT) {
            previewView.setMirrored(true);
        } else {
            previewView.setMirrored(false);
        }
        opPreviewSize(preferredWidth, preferredHeight);
        final Camera.Size size = camera.getParameters().getPreviewSize();
        if (detectRotation % 180 == 90) {
            previewView.setPreviewSize(size.height, size.width);
        } else {
            previewView.setPreviewSize(size.width, size.height);
        }
        final int temp = detectRotation;
        try {
            if (cameraFacing == ICameraControl.CAMERA_USB) {
                camera.setPreviewTexture(textureView.getSurfaceTexture());
            } else {
                surfaceTexture = new SurfaceTexture(11);
                camera.setPreviewTexture(surfaceTexture);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (textureView != null) {
                            surfaceTexture.detachFromGLContext();
                            textureView.setSurfaceTexture(surfaceTexture);
                        }
                    }
                });
            }
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    onFrameListener.onPreviewFrame(data, temp, size.width,
                            size.height);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private TextureView textureView;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
        if (surfaceTexture != null) {
            surfaceTexture.detachFromGLContext();
            textureView.setSurfaceTexture(surfaceTexture);
        }
    }

    private int getCameraDisplayOrientation(int degrees, int cameraId,
                                            Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360;
        } else {
            rotation = (info.orientation - degrees + 360) % 360;
        }
        return rotation;
    }

    @Override
    public void stop() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        if (cameraHandlerThread != null) {
            cameraHandlerThread.quit();
            cameraHandlerThread = null;
        }
    }

    @Override
    public void pause() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void resume() {
        takingPicture.set(false);
        if (camera == null) {
            postStartCamera();
        }
    }

    private OnFrameListener onFrameListener;

    @Override
    public void setOnFrameListener(OnFrameListener listener) {
        this.onFrameListener = listener;
    }

    @Override
    public void setPreferredPreviewSize(int width, int height) {
        this.preferredWidth = Math.max(width, height);
        this.preferredHeight = Math.min(width, height);
    }

    @Override
    public View getDisplayView() {
        return null;
    }


    private TexturePreviewView previewView;

    @Override
    public void setPreviewView(TexturePreviewView previewView) {
        this.previewView = previewView;
        setTextureView(previewView.getTextureView());
    }

    @Override
    public PreviewView getPreviewView() {
        return previewView;
    }

    public CameraControl(Context context) {
        this.context = context;
    }

    private void startPreview(boolean checkPermission) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (checkPermission && permissionCallback != null) {
                permissionCallback.onRequestPermission();
            }
            return;
        }
        camera.startPreview();
    }

    private void opPreviewSize(int width, int height) {
        if (camera != null && width > 0) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size optSize = getOptimalSize(width, height,
                        camera.getParameters().getSupportedPreviewSizes());
                LogUtil.d("WDH = " + optSize.width + ":HGT = " + optSize.height);
                parameters.setPreviewSize(optSize.width, optSize.height);
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private Camera.Size getOptimalSize(int width, int height,
                                       List<Camera.Size> sizes) {

        Camera.Size pictureSize = sizes.get(0);

        List<Camera.Size> candidates = new ArrayList<>();

        for (Camera.Size size : sizes) {
            if (size.width >= width && size.height >= height && size.width * height == size.height * width) {
                candidates.add(size);
            } else if (size.height >= width && size.width >= height && size.width * width == size.height * height) {
                candidates.add(size);
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.min(candidates, sizeComparator);
        }

        for (Camera.Size size : sizes) {
            if (size.width >= width && size.height >= height) {
                return size;
            }
        }

        return pictureSize;
    }

    private Comparator<Camera.Size> sizeComparator =
            new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size lhs, Camera.Size rhs) {
                    return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
                }
            };

    private int getSurfaceOrientation() {
        @CameraView.Orientation
        int orientation = displayOrientation;
        switch (orientation) {
            case CameraView.ORIENTATION_PORTRAIT:
                return 90;
            case CameraView.ORIENTATION_HORIZONTAL:
                return 0;
            case CameraView.ORIENTATION_INVERT:
                return 180;
            default:
                return 90;
        }
    }
}
