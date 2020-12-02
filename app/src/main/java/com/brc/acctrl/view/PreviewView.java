package com.brc.acctrl.view;

import android.graphics.RectF;
import android.view.TextureView;

/**
 * Created by koo on 05/09/18.
 */

public interface PreviewView {

    enum ScaleType{
        FIT_WIDTH,
        FIT_HEIGHT,
        CROP_INSIDE,
    }

    TextureView getTextureView();

    void setPreviewSize(int width, int height);

    void mapToOriginalRect(RectF rect);

    void mapFromOriginalRect(RectF rectF);


    void setScaleType(ScaleType scaleType);

    ScaleType getScaleType();
}
