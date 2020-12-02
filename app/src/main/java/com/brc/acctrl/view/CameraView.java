package com.brc.acctrl.view;

/**
 * Created by koo on 05/09/18.
 */

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.brc.acctrl.camera.CameraControl;
import com.brc.acctrl.camera.ICameraControl;


public class CameraView extends FrameLayout {

    public static final int ORIENTATION_PORTRAIT = 0;

    public static final int ORIENTATION_HORIZONTAL = 1;

    public static final int ORIENTATION_INVERT = 2;

    @IntDef({ORIENTATION_PORTRAIT, ORIENTATION_HORIZONTAL, ORIENTATION_INVERT})
    public @interface Orientation {

    }

    private CameraViewTakePictureCallback cameraViewTakePictureCallback = new CameraViewTakePictureCallback();

    private ICameraControl cameraControl;


    private View displayView;

    private ImageView hintView;

    public ICameraControl getCameraControl() {
        return cameraControl;
    }

    public void setOrientation(@Orientation int orientation) {
        cameraControl.setDisplayOrientation(orientation);
    }

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void start() {
        cameraControl.start();
        setKeepScreenOn(true);
    }

    public void stop() {
        cameraControl.stop();
        setKeepScreenOn(false);
    }


    private void init() {
        cameraControl = new CameraControl(getContext());
        displayView = cameraControl.getDisplayView();
        addView(displayView);


        hintView = new ImageView(getContext());
        addView(hintView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        displayView.layout(left, 0, right, bottom - top);
    }


    private class CameraViewTakePictureCallback implements ICameraControl.OnTakePictureCallback {

        @Override
        public void onPictureTaken(final byte[] data) {
        }
    }
}
