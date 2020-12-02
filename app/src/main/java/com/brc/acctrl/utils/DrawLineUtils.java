package com.brc.acctrl.utils;

import android.graphics.RectF;

/**
 * @author zhengdan
 * @date 2019-07-16
 * @Description:
 */
public class DrawLineUtils {
    public static float[] createTrianglePaintLines(RectF drawRect, float gapWdh) {
        float[] linesFloats = new float[32];
        // left
        linesFloats[0] = linesFloats[4] = drawRect.left + gapWdh;
        linesFloats[1] = linesFloats[5] = drawRect.top + drawRect.height() / 2;
        linesFloats[2] = linesFloats[6] = drawRect.left;
        linesFloats[7] = linesFloats[1] - gapWdh;
        linesFloats[3] = linesFloats[1] + gapWdh;

        // top
        linesFloats[8] = linesFloats[12] = drawRect.left + drawRect.width() / 2;
        linesFloats[9] = linesFloats[13] = drawRect.top + gapWdh;
        linesFloats[11] = linesFloats[15] = drawRect.top;
        linesFloats[10] = linesFloats[8] - gapWdh;
        linesFloats[14] = linesFloats[8] + gapWdh;

        // right
        linesFloats[16] = linesFloats[20] = drawRect.right - gapWdh;
        linesFloats[17] = linesFloats[21] = drawRect.top + drawRect.height() / 2;
        linesFloats[18] = linesFloats[22] = drawRect.right;
        linesFloats[19] = linesFloats[17] - gapWdh;
        linesFloats[23] = linesFloats[17] + gapWdh;

        // btm
        linesFloats[24] = linesFloats[28] = drawRect.left + drawRect.width() / 2;
        linesFloats[25] = linesFloats[29] = drawRect.bottom - gapWdh;
        linesFloats[27] = linesFloats[31] = drawRect.bottom;
        linesFloats[26] = linesFloats[24] - gapWdh;
        linesFloats[30] = linesFloats[24] + gapWdh;

        return linesFloats;
    }

    public static float[] createPaintLines(RectF drawRect, float ratio) {
        float[] linesFloats = new float[32];
        // left-btm
        linesFloats[0] = linesFloats[2] = linesFloats[4] = drawRect.left;
        linesFloats[1] = drawRect.bottom - drawRect.height() / ratio;
        linesFloats[3] = linesFloats[5] = linesFloats[7] = drawRect.bottom;
        linesFloats[6] = drawRect.left + drawRect.width() / ratio;

        // right-top
        linesFloats[9] = linesFloats[11] = linesFloats[13] = drawRect.top;
        linesFloats[8] = drawRect.right - drawRect.width() / ratio;
        linesFloats[10] = linesFloats[12] = linesFloats[14] = drawRect.right;
        linesFloats[15] = drawRect.top + drawRect.height() / ratio;

        // left-top
        linesFloats[17] = linesFloats[19] = linesFloats[21] = drawRect.top;
        linesFloats[16] = drawRect.left + drawRect.width() / ratio;
        linesFloats[18] = linesFloats[20] = linesFloats[22] = drawRect.left;
        linesFloats[23] = drawRect.top + drawRect.height() / ratio;

        // right-btm
        linesFloats[24] = linesFloats[26] = linesFloats[28] = drawRect.right;
        linesFloats[25] = drawRect.bottom - drawRect.height() / ratio;
        linesFloats[27] = linesFloats[29] = linesFloats[31] = drawRect.bottom;
        linesFloats[30] = drawRect.right - drawRect.width() / ratio;

        return linesFloats;
    }
}
