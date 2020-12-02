package com.brc.acctrl.activity;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.widget.TextView;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.R;
import com.brc.acctrl.bean.BaseDeviceInfo;
import com.brc.acctrl.bean.BaseRsp;
import com.brc.acctrl.bean.RspWeather;
import com.brc.acctrl.bean.WeatherShow;
import com.brc.acctrl.db.DBStore;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.retrofit.BaseObserver;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.utils.AccessFailUtil;
import com.brc.acctrl.utils.AccessRecordUtil;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.DateTimeUtil;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.MeetingUtils;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by zhendan on 5/8/2016.
 */
public abstract class BaseDateTimeActivity extends BaseActivity {
    private TextView timeTextView;
    private static long lastUpdateWeather = 0;
    private static final long WEATHER_GAP = 60 * 60 * 1000L;
    private static final long UPDATE_REBOOT_GAP = 60 * 60 * 1000L;

    private CompositeDisposable mCompositeDisposable;
    private Disposable disposable, watchDogDisposable;
    public boolean isSettingTopShow = false;

    public void initTimeClockAction() {
        updateMinuteTimeText();
        minuteTimeAction();
        watchDogAction();
    }

    private void minuteTimeAction() {
        try {
            Observable.interval(1, TimeUnit.MINUTES).subscribeOn(Schedulers.computation())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable dis) {
                            disposable = dis;
                            addDisposable(dis);
                        }

                        @Override
                        public void onNext(@NonNull Long number) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateMinuteTimeText();
                                }
                            });

                            Calendar dayStartCalendar = Calendar.getInstance();
                            int hourMinute = dayStartCalendar.get(Calendar.MINUTE);
                            int dayHour = dayStartCalendar.get(Calendar.HOUR_OF_DAY); // 24h
                            // 凌晨0-8点整点上传未通行记录，但是因为每次都会上传，所以这里只上传之前没有成功的
                            if (hourMinute == 0 && dayHour < 9) {
                                if (dayHour < 3) {
                                    tryDeleteUnusedData();
                                }

                                AccessRecordUtil.checkUnuploadRecord(BaseDateTimeActivity.this);
                            }



                            // 在3点-4点之间进行版本检测更新
                            long updateGap = System.currentTimeMillis() - MainApplication.APP_START_TIME;
                            if (dayHour == 3 && updateGap > UPDATE_REBOOT_GAP && hourMinute % 10 == 0) {
                                tryToCheckUpdate();
                            }

                            // 默认开启早上4点-5点重启设备
                            if (dayHour == 4 && updateGap > UPDATE_REBOOT_GAP &&
                                    SPUtil.getInstance().getValue(SPUtil.SUPPORT_REBOOT_TIMELY, true)) {
//                                    GoldenEyesUtils.reboot();
                                CommonUtil.reboot();
                                return;
                            }

                            // 删除超过一周的通行记录，否则太多了
                            if (dayHour == 0 && hourMinute == 0) {
                                int dayYear =
                                        dayStartCalendar.get(Calendar.DAY_OF_YEAR);
                                dayStartCalendar.set(Calendar.SECOND, 0);
                                dayStartCalendar.set(Calendar.MILLISECOND, 0);
                                dayStartCalendar.set(Calendar.DAY_OF_YEAR,
                                        dayYear - AccessRecordUtil.MAX_DAY_ACCESS_RECORD_DURATION);
                                AccessRecordUtil.deleteExpireRerod(
                                        dayStartCalendar.getTimeInMillis());
                            }
                            // 一点-删除超过两周的识别记录
                            if (dayHour == 1 && hourMinute == 0) {
                                int dayYear = dayStartCalendar.get(Calendar.DAY_OF_YEAR);
//                                int minute=dayStartCalendar.get(Calendar.MINUTE);
                                dayStartCalendar.set(Calendar.SECOND, 0);
                                dayStartCalendar.set(Calendar.MILLISECOND, 0);
                                dayStartCalendar.set(Calendar.MINUTE,
                                        dayYear - AccessRecordUtil.MAX_DAY_ACCESS_LOG_DURATION);
                                AccessRecordUtil.deleteAccessLog(
                                        dayStartCalendar.getTimeInMillis());
                                AccessFailUtil.checkUnuploadRecord(BaseDateTimeActivity.this,dayStartCalendar.getTimeInMillis());

                            }
                            // 每小时请求天气数据
                            if (SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE,
                                    SPUtil.ACCESS_TYPE_MEETING) == SPUtil.ACCESS_TYPE_BOSS) {
                                long gapTime = System.currentTimeMillis() - lastUpdateWeather;
                                if (gapTime > WEATHER_GAP) {
                                    fetchWeather();
                                }
                            }

                            // 设置顶灯亮起时间
                            checkShowTopLight(dayHour, hourMinute);
                            // 刷新会议信息
                            if (CommonUtil.bMeetingMode()) {
                                MeetingUtils.getInstance().refreshShowMeetings(false);
                            }

                            // send heart-beat
                            if (hourMinute % 2 == 0) {
                                sendHearBeat();
                            }
                            if(hourMinute % 59==0){
                                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                CommonUtil.detectSound(mAudioManager);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            rmDispose(disposable);
                        }

                        @Override
                        public void onComplete() {
                            rmDispose(disposable);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkShowTopLight(int dayHour, int hourMinute) {
        if (Constants.checkTopLightStatus(dayHour, hourMinute)&&TextUtils.equals(Constants.TIMING_ON,SPUtil.getInstance().getValue(SPUtil.SHOW_FILL_LIGHTS_STATUS, Constants.OFTEN_OFF))) {
            isSettingTopShow = true;
            GoldenEyesUtils.setGoldenEyesTopLED71();
        } else {
            isSettingTopShow = false;
            GoldenEyesUtils.setGoldenEyesTopLED_OFF71();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Calendar dayStartCalendar = Calendar.getInstance();
        int hourMinute = dayStartCalendar.get(Calendar.MINUTE);
        int dayHour = dayStartCalendar.get(Calendar.HOUR_OF_DAY); // 24h
        checkShowTopLight(dayHour, hourMinute);
    }

    private void watchDogAction() {
        try {
            Observable.interval(20, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).subscribe(new Observer<Long>() {
                @Override
                public void onSubscribe(@NonNull Disposable dis) {
                    watchDogDisposable = dis;
                    addDisposable(watchDogDisposable);
                }

                @Override
                public void onNext(@NonNull Long number) {
                    GoldenEyesUtils.setGoldenEyesWatchdog(GoldenEyesUtils.WACTHDOG_OPEN);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    rmDispose(watchDogDisposable);
                }

                @Override
                public void onComplete() {
                    rmDispose(watchDogDisposable);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void addDisposable(Disposable subscription) {
        if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
            mCompositeDisposable = new CompositeDisposable();
        }

        if (subscription != null) {
            mCompositeDisposable.add(subscription);
        }
    }

    protected void rmDispose(Disposable subscription) {
        if (mCompositeDisposable != null && subscription != null) {
            mCompositeDisposable.remove(subscription);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
    }

    public void updateMinuteTimeText() {
        if (timeTextView == null) {
            timeTextView = fetchTimeText();
        }
        if (timeTextView != null) {
            timeTextView.setText(DateTimeUtil.curTimeFormat());
        }
    }

    private void tryDeleteUnusedData() {
        // 1.无用的会议, 无用的人, 无用的头像数据
        DBStore.getInstance().deleteUnusedData();
    }

    public abstract TextView fetchTimeText();

    private void sendHearBeat() {
        LogUtil.e("sendHearBeat");
        RetrofitConfig.createService().sendBase2Server(
                Constants.APP_KEY, Constants.APP_SECRET, new BaseDeviceInfo("heartBeatTime",
                        System.currentTimeMillis())).subscribeOn(Schedulers.io())
                .subscribe(new BaseObserver<BaseRsp>(this, true,
                        false) {
                    @Override
                    public void onSuccess(BaseRsp responseData) {
                        LogUtil.e("responseData.status=="+responseData.status);
                        if (responseData.status == 200) {
                            if (!Constants.hasRegisterServerSuc) {
                                tryToRegisterServer();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }

    public void fetchWeather() {
        LogUtil.e("fetchWeather");
        lastUpdateWeather = System.currentTimeMillis();
        RetrofitConfig.createService().fetchWeather().subscribeOn(Schedulers.io())
                .subscribe(new BaseObserver<BaseRsp<RspWeather>>(this, true,
                        false) {
                    @Override
                    public void onSuccess(BaseRsp<RspWeather> responseData) {
                        if (responseData.status == 200) {
                            RspWeather rsp = responseData.data;
                            WeatherShow weather = new WeatherShow();
                            weather.iconUrl = rsp.now.weather_pic;
                            weather.weatherDesc = rsp.now.weather;
                            weather.pm25 = rsp.now.aqiDetail.pm2_5;
                            weather.weatherTemp = StringUtil.CpStrInt2Para(R.string.str_weather_temp,
                                    rsp.f1.night_air_temperature, rsp.f1.day_air_temperature);

                            EventBus.getDefault().post(new RefreshEvents.WeatherEvent(weather));
                        }
                    }
                });
    }

    public void tryToCheckUpdate() {

    }

    public abstract void tryToRegisterServer();
}
