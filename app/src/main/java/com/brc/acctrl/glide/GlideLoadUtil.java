package com.brc.acctrl.glide;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.brc.acctrl.utils.LogUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by zhd on 2018/3/28.
 */

public class GlideLoadUtil {
    private String TAG = "GlideImageLoader";

    /**
     * 借助内部类 实现线程安全的单例模式
     * 属于懒汉式单例，因为Java机制规定，内部类SingletonHolder只有在getInstance()
     * 方法第一次调用的时候才会被加载（实现了lazy），而且其加载过程是线程安全的。
     * 内部类加载的时候实例化一次instance。
     */
    public GlideLoadUtil() {
    }

    private static class GlideLoadUtilsHolder {
        private final static GlideLoadUtil INSTANCE = new GlideLoadUtil();
    }

    public static GlideLoadUtil getInstance() {
        return GlideLoadUtilsHolder.INSTANCE;
    }

    // context

    /**
     * Glide 加载 简单判空封装 防止异步加载数据时调用Glide 抛出异常
     *
     * @param context
     * @param url             加载图片的url地址  String
     * @param imageView       加载图片的ImageView 控件
     * @param defaultDrawable 图片展示错误的本地图片 id
     */
    public void loadContext(Context context, String url, ImageView imageView, int
            defaultDrawable, boolean isCircle, boolean isRoundCorner) {
        if (context != null) {
            if (context instanceof Activity) {
                Activity curActivity = (Activity) context;
                if (curActivity.isDestroyed()) {
                    LogUtil.trackLogDebug("Picture loading failed,activity is destroyed");
                    return;
                }
            }

//            RequestBuilder<Drawable> request = Glide.with(context).load(url);
            RequestOptions options = new RequestOptions();
            if (defaultDrawable > 0) {
                options.placeholder(defaultDrawable);
            }

            if (isCircle) {
                options.transform(new GlideCircleTransform(context));
            } else if (isRoundCorner) {
                options.transform(new CenterCropRoundTransform(context));
            }

//            request.transition(withCrossFade()).into(imageView);
            Glide.with(context).load(url).transition(withCrossFade()).apply(options).into
                    (imageView);
        } else {
            LogUtil.trackLogDebug("Picture loading failed,context is null");
        }
    }

    public void load(Context context, String url, ImageView imageView) {
        loadContext(context, url, imageView, 0, false, false);
    }

    public void load(Context context, String url, ImageView imageView, int defaultDrawable) {
        loadContext(context, url, imageView, defaultDrawable, false, false);
    }

    public void loadCircle(Context context, String url, ImageView imageView) {
        loadContext(context, url, imageView, 0, true, false);
    }

    public void loadCircle(Context context, String url, ImageView imageView, int defaultDrawable) {
        loadContext(context, url, imageView, defaultDrawable, true, false);
    }

    public void loadRoundCorner(Context context, String url, ImageView imageView) {
        loadContext(context, url, imageView, 0, false, true);
    }

    public void loadRoundCorner(Context context, String url, ImageView imageView, int
            defaultDrawable) {
        loadContext(context, url, imageView, defaultDrawable, false, true);
    }

    // activity
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadActivity(Activity activity, String url, ImageView imageView, int
            defaultDrawable, boolean isCircle, boolean isRoundCorner) {
        if (activity != null && !activity.isDestroyed()) {
//            RequestBuilder<Drawable> request = Glide.with(activity).load(url);
            RequestOptions options = new RequestOptions();
            if (defaultDrawable > 0) {
                options.placeholder(defaultDrawable);
            }

            if (isCircle) {
                options.transform(new GlideCircleTransform(activity));
            } else if (isRoundCorner) {
                options.transform(new CenterCropRoundTransform(activity));
            }

//            request.transition(withCrossFade()).into(imageView);
            Glide.with(activity).load(url).transition(withCrossFade()).apply(options).into
                    (imageView);
        } else {
            LogUtil.trackLogDebug("Picture loading failed, activity is Destroyed");
        }
    }

    public void loadFileActivity(Activity activity, File avatarFile, ImageView imageView) {
        if (activity != null && !activity.isDestroyed()) {
//            RequestBuilder<Drawable> request = Glide.with(activity).load(url);
            RequestOptions options = new RequestOptions();

            options.transform(new GlideCircleTransform(activity));
            Glide.with(activity).load(avatarFile).transition(withCrossFade()).apply(options).into
                    (imageView);
        } else {
            LogUtil.trackLogDebug("Picture loading failed, activity is Destroyed");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void load(Activity activity, String url, ImageView imageView) {
        loadActivity(activity, url, imageView, 0, false, false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void load(Activity activity, String url, ImageView imageView, int defaultDrawable) {
        loadActivity(activity, url, imageView, defaultDrawable, false, false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadCircle(Activity activity, String url, ImageView imageView) {
        loadActivity(activity, url, imageView, 0, true, false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadCircle(Activity activity, String url, ImageView imageView, int
            defaultDrawable) {
        loadActivity(activity, url, imageView, defaultDrawable, true, false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadRoundCorner(Activity activity, String url, ImageView imageView) {
        loadActivity(activity, url, imageView, 0, false, true);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadRoundCorner(Activity activity, String url, ImageView imageView, int
            defaultDrawable) {
        loadActivity(activity, url, imageView, defaultDrawable, false, true);
    }

    //// Fragment part
    public void loadFragment(Fragment fragment, String url, ImageView imageView, int
            defaultDrawable, boolean isCircle, boolean isRoundCorner) {
        if (fragment != null && fragment.getActivity() != null && !fragment.getActivity()
                .isDestroyed()) {
//            RequestBuilder<Drawable> request = Glide.with(fragment).load(url);
            RequestOptions options = new RequestOptions();
            if (defaultDrawable > 0) {
                options.placeholder(defaultDrawable);
            }

            if (isCircle) {
                options.transform(new GlideCircleTransform(fragment.getActivity()));
            } else if (isRoundCorner) {
                options.transform(new CenterCropRoundTransform(fragment.getActivity()));
            }

//            request.transition(withCrossFade()).into(imageView);
            Glide.with(fragment).load(url).transition(withCrossFade()).apply(options).into
                    (imageView);
        } else {
            LogUtil.trackLogDebug("Picture loading failed,fragment is null");
        }
    }

    public void load(Fragment fragment, String url, ImageView imageView) {
        loadFragment(fragment, url, imageView, 0, false, false);
    }

    public void load(Fragment fragment, String url, ImageView imageView, int defaultDrawable) {
        loadFragment(fragment, url, imageView, defaultDrawable, false, false);
    }

    public void loadCircle(Fragment fragment, String url, ImageView imageView) {
        loadFragment(fragment, url, imageView, 0, true, false);
    }

    public void loadCircle(Fragment fragment, String url, ImageView imageView, int
            defaultDrawable) {
        loadFragment(fragment, url, imageView, defaultDrawable, true, false);
    }

    public void loadRoundCorner(Fragment fragment, String url, ImageView imageView) {
        loadFragment(fragment, url, imageView, 0, false, true);
    }

    public void loadRoundCorner(Fragment fragment, String url, ImageView imageView, int
            defaultDrawable) {
        loadFragment(fragment, url, imageView, defaultDrawable, false, true);
    }

    // app.Fragment
    public void loadAPPFragment(android.app.Fragment fragment, String url, ImageView imageView,
                                int defaultDrawable, boolean isCircle, boolean isRoundCorner) {
        if (fragment != null && fragment.getActivity() != null && !fragment.getActivity()
                .isDestroyed()) {
//            RequestBuilder<Drawable> request = Glide.with(fragment).load(url);
            RequestOptions options = new RequestOptions();
            if (defaultDrawable > 0) {
                options.placeholder(defaultDrawable);
            }

            if (isCircle) {
                options.transform(new GlideCircleTransform(fragment.getActivity()));
            } else if (isRoundCorner) {
                options.transform(new CenterCropRoundTransform(fragment.getActivity()));
            }

//            request.transition(withCrossFade()).into(imageView);
            Glide.with(fragment.getActivity()).load(url).transition(withCrossFade()).apply
                    (options).into(imageView);
        } else {
            LogUtil.trackLogDebug("Picture loading failed, app fragment is null");
        }
    }

    public void load(android.app.Fragment fragment, String url, ImageView imageView) {
        loadAPPFragment(fragment, url, imageView, 0, false, false);
    }

    public void load(android.app.Fragment fragment, String url, ImageView imageView, int
            defaultDrawable) {
        loadAPPFragment(fragment, url, imageView, defaultDrawable, false, false);
    }

    public void loadCircle(android.app.Fragment fragment, String url, ImageView imageView) {
        loadAPPFragment(fragment, url, imageView, 0, true, false);
    }

    public void loadCircle(android.app.Fragment fragment, String url, ImageView imageView, int
            defaultDrawable) {
        loadAPPFragment(fragment, url, imageView, defaultDrawable, true, false);
    }

    public void loadRoundCorner(android.app.Fragment fragment, String url, ImageView imageView) {
        loadAPPFragment(fragment, url, imageView, 0, false, true);
    }

    public void loadRoundCorner(android.app.Fragment fragment, String url, ImageView imageView,
                                int defaultDrawable) {
        loadAPPFragment(fragment, url, imageView, defaultDrawable, false, true);
    }
}
