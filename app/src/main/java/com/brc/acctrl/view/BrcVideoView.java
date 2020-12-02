package com.brc.acctrl.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class BrcVideoView extends VideoView {
    private int maxHeight;
    private int maxWidth;

    public BrcVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BrcVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrcVideoView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize(0, widthMeasureSpec);//得到默认的大小（0，宽度测量规范）
        int height = getDefaultSize(0, heightMeasureSpec);//得到默认的大小（0，高度度测量规范）
//        if (width > maxWidth) {
//            maxWidth = width;
//        }
//        if (height > maxHeight) {
//            maxHeight = height;
//        }
//        setMeasuredDimension(maxWidth, maxHeight); //设置测量尺寸,将高和宽放进去
//        setMeasuredDimension(800, 536);
        setMeasuredDimension(width, height);
    }
}
