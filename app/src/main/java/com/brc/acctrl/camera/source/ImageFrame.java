package com.brc.acctrl.camera.source;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by koo on 05/09/18.
 */

public class ImageFrame {

    private static  final String TAG = "ImageFrame";

    private int width;

    private int height;
    private Mat bgrMat;
    private byte[] rawData;
    private Mat yuyvData;
    public ImageFrame() {

    }


    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public void release() {
    }

    public void setRawData(byte[] rawData, int width, int height) {
        this.rawData = rawData;
        if (yuyvData == null || yuyvData.size().area() != (height + height / 2) * width) {
            yuyvData = new Mat(height + height / 2, width , CvType.CV_8UC1);
        }
        yuyvData.put(0, 0, rawData);
    }

    public Mat getBgrMat() {
        if(null == bgrMat) {
            bgrMat = new Mat();
        }
        Imgproc.cvtColor(
                yuyvData,
                bgrMat,
                Imgproc.COLOR_YUV2BGR_NV21,
                3
        );
        return bgrMat;
    }
}
