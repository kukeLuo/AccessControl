package com.brc.acctrl.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.brc.acctrl.BuildConfig;
import com.brc.acctrl.R;
import com.brc.acctrl.bean.BaseRsp;
import com.brc.acctrl.bean.ReqRegister;
import com.brc.acctrl.bean.RspRegister;
import com.brc.acctrl.mqtt.MqttService;
import com.brc.acctrl.retrofit.BaseObserver;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.retrofit.RxJavaAction;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SplashActivity extends BaseActivity {
    @BindView(R.id.icon_setting)
    View iconSetting;
    @BindView(R.id.tv_network_err)
    TextView tvNetworkErr;
    @BindView(R.id.surface_play)
    SurfaceView surfacePlay;
    @BindView(R.id.img_bg)
    ImageView imgBg;

    private boolean isFirstTimeOpen = true;
    private int settingClickCnt = 0;
    private Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    gotoNextActivity();
                    break;
                case 1:
                    settingClickCnt = 0;
                    break;
            }
            super.dispatchMessage(msg);
        }
    };

    private MediaPlayer player;

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public void initViews() {
        resetLights();
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        player = MediaPlayer.create(this, R.raw.splash);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgBg.setVisibility(View.VISIBLE);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                alphaAnimation.setDuration(1000L);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        player.stop();
                        player.release();
                        player = null;
                        surfacePlay.setVisibility(View.GONE);
                        checkPermissions(permissions);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imgBg.startAnimation(alphaAnimation);
            }
        });

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                player.start();
            }
        });
        SurfaceHolder mSurfaceHolder = surfacePlay.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player.setDisplay(holder);//给mMediaPlayer添加预览的SurfaceHolder

                try {
                    player.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void resetLights() {
        GoldenEyesUtils.setGoldenEyesTopLED_OFF71();
        GoldenEyesUtils.setGoldenEyesStateRedLED_OFF71();
        GoldenEyesUtils.setGoldenEyesStateGreedLED_OFF71();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTimeOpen) {
            isFirstTimeOpen = !isFirstTimeOpen;
        } else {
            checkPermissions(permissions);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    // for permission
    /**
     * 需要获得权限的List
     */
    private List<String> permissionList = new ArrayList<String>();
    private String[] permissions =
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE};

    /**
     * 6.0及以上版本获取动态权限
     */
    private void checkPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            permissionList.clear();
            for (int i = 0; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permissions[i]);
                }
            }
            //请求权限
            if (permissionList.size() > 0) {
                String[] perStrs = new String[permissionList.size()];
                permissionList.toArray(perStrs);
                requestPermissions(perStrs, 1);
            } else {
                handler.sendEmptyMessageDelayed(0, 3000L);
            }
        } else {
            handler.sendEmptyMessageDelayed(0, 3000L);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        if (requestCode == 1) {
            if (grantResults.length == permissionList.size()) {
                handler.sendEmptyMessageDelayed(0, 3000L);
            }
        }
    }

    private void gotoNextActivity() {
        boolean hasConfig =
                SPUtil.getInstance().getValue(SPUtil.HAS_CONFIG_NAME, false);
        if (hasConfig) {
            // check register
            if (NetworkUtil.isNetworkAvailable(this)) {
                checkRegister();
            } else {
                NetworkUtil.networkStatus = NetworkUtil.NETWORK_NO_DISCONNECT;
                startNextActivity();
            }
        } else {
            gotoSettingActivity();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        tvNetworkErr.setVisibility(View.GONE);
    }

    private static final boolean SDK_CODE_FOR_DEVELOP = BuildConfig.DEBUG;
    private void checkRegister() {
        // 测试直接使用CODE，开发从服务器获取
        if (SDK_CODE_FOR_DEVELOP) {
            SPUtil.getInstance().setValue(SPUtil.FACE_AUTH_CODE, Constants.DEEPLINT_CODE);
        }
        tvNetworkErr.setVisibility(View.GONE);
        // 一开始判别是否有有线网络，但是还是暂时不用。以防后续需要使用无线
        ArrayList<ReqRegister> bodyData = new ArrayList<>();
        bodyData.add(new ReqRegister());
        RetrofitConfig.createService().registerDevice(
                Constants.APP_KEY, Constants.APP_SECRET, bodyData).compose(RxJavaAction.<RspRegister>setThread())
                .subscribe(new BaseObserver<RspRegister>(this, true,
                        true) {
                    @Override
                    public void onSuccess(RspRegister responseData) {
                        if (responseData.status == 200) {
                            Constants.hasRegisterServerSuc = true;
                            NetworkUtil.networkStatus = NetworkUtil.NETWORK_OK;

                            // start mqtt service
                            Intent intent = new Intent(getApplicationContext(), MqttService.class);
                            startService(intent);
                            if (SDK_CODE_FOR_DEVELOP) {
                                startNextActivity();
                            } else {
                                checkAuthCode();
                            }
                        } else {
                            NetworkUtil.networkStatus = NetworkUtil.NETWORK_SERVER_ERROR;
                            startNextActivity();
                        }
                    }

                    @Override
                    protected void onErrorAction(String msg) {
                        NetworkUtil.networkStatus = NetworkUtil.NETWORK_SERVER_ERROR;
                        startNextActivity();
                    }
                });
    }

    private void checkAuthCode() {
        RetrofitConfig.createService().fetchAuthCode(
                Constants.APP_KEY, Constants.APP_SECRET, NetworkUtil.ethernetMac(), Constants.PRODUCT_KEY)
                .compose(RxJavaAction.<BaseRsp<String>>setThread())
                .subscribe(new BaseObserver<BaseRsp<String>>(this, true,
                        true) {
                    @Override
                    public void onSuccess(BaseRsp<String> responseData) {
                        if (responseData.status == 200) {
                            SPUtil.getInstance().setValue(SPUtil.FACE_AUTH_CODE, responseData.data);
                        }
                        startNextActivity();
                    }

                    @Override
                    protected void onErrorAction(String msg) {
                        startNextActivity();
                    }
                });
    }

    private void startNextActivity() {
        // check machine mode
        int mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
        Class<?> className = null;
        if (mode == SPUtil.ACCESS_TYPE_MEETING) {
            if (SPUtil.getInstance().getValue(SPUtil.SUPPORT_LIVECHECK, false)) {
                className = OfficeMeetingLiveActivity.class;
            } else {
                className = OfficeMeetingActivity.class;
            }
        } else {
            if (SPUtil.getInstance().getValue(SPUtil.SUPPORT_LIVECHECK, false)) {
                className = OfficeAccessLiveActivity.class;
            } else {
                className = OfficeAccessActivity.class;
            }
        }

        startActivity(new Intent(SplashActivity.this,
                className));
        finish();
    }

    private void gotoSettingActivity() {
        startActivity(new Intent(SplashActivity.this, SettingFunctionActivity.class));
    }

    @OnClick(R.id.icon_setting)
    public void onViewClicked() {
        settingClickCnt++;
        if (settingClickCnt == 1) {
            handler.sendEmptyMessageDelayed(1, 2000L);
        } else if (settingClickCnt == 5) {
            startActivity(new Intent(SplashActivity.this, SettingPWDActivity.class));
        } else if (settingClickCnt > 5) {
            settingClickCnt = 2;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
