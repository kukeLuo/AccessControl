package com.brc.acctrl.camera.source;

import com.brc.acctrl.listener.OnFrameAvailableListener;
import com.brc.acctrl.view.PreviewView;

import java.util.ArrayList;

/**
 * Created by koo on 05/09/18.
 */

public class ImageSource {
    public ImageFrame borrowImageFrame() {
        return new ImageFrame();
    }

    private ArrayList<OnFrameAvailableListener> listeners = new ArrayList<>();

    /** 注册监听器，当有图片帧时会回调。*/
    public void addOnFrameAvailableListener(OnFrameAvailableListener listener) {
        this.listeners.add(listener);
    }

    /** 删除监听器*/
    public void removeOnFrameAvailableListener(OnFrameAvailableListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /** 获取监听器列表 */
    public ArrayList<OnFrameAvailableListener> getListeners() {
        return listeners;
    }

    /** 打开图片源。*/
    public void start() {

    }

    /** 停止图片源。*/
    public void stop() {

    }

    /** 设置预览View用于显示预览图。*/
    public void setPreviewView(PreviewView previewView) {
    }
}
