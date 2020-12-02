package com.brc.acctrl.listener;

import com.brc.acctrl.camera.source.ImageFrame;

/**
 * Created by koo on 05/09/18.
 */

public interface OnFrameAvailableListener {
    void onFrameAvailable(ImageFrame frame);
}
