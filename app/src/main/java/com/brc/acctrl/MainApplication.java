package com.brc.acctrl;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import com.brc.acctrl.activity.SplashActivity;
import com.brc.acctrl.bean.AccessHistory;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.CrashHandler;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.LogUtil;
import com.facebook.stetho.Stetho;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.google.gson.Gson;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Map;

public class MainApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    private static MainApplication application;
    public static long APP_START_TIME = 0L;
    private ArrayList<Activity> activityHistorys = new ArrayList<>();

    public static MainApplication getAPPInstance() {
        return application;
    }

    public Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        APP_START_TIME = System.currentTimeMillis();
        application = this;
        gson = new Gson();

        Constants.initOpenTopLights();
        registerActivityLifecycleCallbacks(this);
        initCommDevice();
        Stetho.initializeWithDefaults(this);
        initBugly();
        catchANRAndReboot();
        initLeakCanary();
//        initCrashHandler();
    }

    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

    private void initBugly() {
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
            @Override
            public Map<String, String> onCrashHandleStart(int crashType, String errorType,
                                                          String errorMessage, String errorStack) {
                LogUtil.e("BUGLY ：" + errorMessage);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                return null;
            }

            @Override
            public byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType,
                                                           String errorMessage, String errorStack) {
                return null;
            }

        });

        CrashReport.initCrashReport(getApplicationContext(), "118e13b7dc",
                BuildConfig.DEBUG, strategy);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtil.e("APP onTerminate");
    }

    private void initCommDevice() {
        GoldenEyesUtils.openCommPort(new GoldenEyesUtils.CommPortListener() {
            @Override
            public void Success() {
                LogUtil.d("OPEN COMM SUC");
                GoldenEyesUtils.initGoldenEyesDoorAccessCommManager(new GoldenEyesUtils.ICCardResponseListener() {
                    @Override
                    public void data(String type, String data) {
                        // 这里需要对数据做一个反转才行
                        // 由于无法从服务器获取刷卡结果，所以只是记录，不做展示
                        int len = data.length();
                        StringBuilder builder = new StringBuilder();
                        for (int idx = len - 2; idx >= 0; idx -= 2) {
                            builder.append(data.substring(idx, idx + 2));
                        }

                        String converData = builder.toString();
                        LogUtil.d("CARD: " + data + "-" + converData);

                        EventBus.getDefault().post(new RefreshEvents.CatchCardEvent());

                        AccessHistory cardRecord = new AccessHistory();
                        cardRecord.setType(AccessHistory.TYPE_CARD);
                        cardRecord.setCardNo(converData);
                        cardRecord.setAccessTime(System.currentTimeMillis());
                        DBStore.getInstance().insertAccessRecord(cardRecord);
                    }
                });
            }
        });
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activityHistorys.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity,
                                            Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activityHistorys.remove(activity);
    }

    public boolean isStackBtmSplashActivity() {
        return activityHistorys.size() > 0 && activityHistorys.get(0) instanceof SplashActivity;
    }

    private void initCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(
                getApplicationContext(), Thread.getDefaultUncaughtExceptionHandler()));
    }

    private void catchANRAndReboot() {
        new ANRWatchDog().setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                LogUtil.e("ANR:" + error.getMessage());
                EventBus.getDefault().post(new RefreshEvents.ANREvent());
//                CrashReport.postCatchedException(error);
            }
        }).start();
    }
/*
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }*/
}
