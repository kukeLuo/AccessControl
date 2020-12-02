package com.brc.acctrl.activity;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.view.MeetingInfoView;

import butterknife.BindView;
import butterknife.OnClick;

public class TestDeviceActivity extends BaseActivity {
    @BindView(R.id.btn_top_light)
    Button btnTopLight;
    @BindView(R.id.btn_btm_red_light)
    Button btnBtmRedLight;
    @BindView(R.id.btn_btm_green_light)
    Button btnBtmGreenLight;
    @BindView(R.id.btn_btm_yellow_light)
    Button btnBtmYellowLight;
    @BindView(R.id.btn_open_door)
    Button btnOpenDoor;
    @BindView(R.id.text_cardId)
    TextView textCardId;
    @BindView(R.id.meeting_view)
    MeetingInfoView meetingView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_test;
    }

    @Override
    public void initViews() {
        initCommDevice();

        meetingView.setMeetingAuthor("发起者：Daniel Wu");
        meetingView.setMeetingDesc("智能研发部：智能研发会议");
        meetingView.setMeetingTime("14:00 - 15:30");
    }

    private void initCommDevice() {
        GoldenEyesUtils.openCommPort(new GoldenEyesUtils.CommPortListener() {
            @Override
            public void Success() {
                GoldenEyesUtils.initGoldenEyesDoorAccessCommManager(new GoldenEyesUtils.ICCardResponseListener() {
                    @Override
                    public void data(String type, String data) {
                        textCardId.setText(data);
                    }
                });

                GoldenEyesUtils.setGoldenEyesWatchdog(GoldenEyesUtils.WACTHDOG_OPEN);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GoldenEyesUtils.releaseGoldenEyesDoorAccessCommManager();
    }

    private boolean topLightOn = false;
    private boolean topDoorOpen = false;
    private boolean btmGreenLightOn = false;
    private boolean btmRedLightOn = false;
    private boolean btmYellowLightOn = false;

    @OnClick({R.id.btn_top_light, R.id.btn_btm_red_light,
            R.id.btn_btm_green_light, R.id.btn_btm_yellow_light,
            R.id.btn_open_door, R.id.btn_openandclose_door})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_top_light:
                topLightOn = !topLightOn;
                if (topLightOn) {
                    GoldenEyesUtils.setGoldenEyesTopLED71();
                } else {
                    GoldenEyesUtils.setGoldenEyesTopLED_OFF71();
                }
                break;
            case R.id.btn_btm_red_light:
                btmGreenLightOn = false;
                btmYellowLightOn = false;
                btmRedLightOn = !btmRedLightOn;
                GoldenEyesUtils.setGoldenEyesStateGreedLED_OFF71();
                GoldenEyesUtils.setGoldenEyesStateRedLED_OFF71();
                if (btmRedLightOn) {
                    GoldenEyesUtils.setGoldenEyesStateRedLED71();
                }
                break;
            case R.id.btn_btm_green_light:
                btmRedLightOn = false;
                btmYellowLightOn = false;
                btmGreenLightOn = !btmGreenLightOn;
                GoldenEyesUtils.setGoldenEyesStateGreedLED_OFF71();
                GoldenEyesUtils.setGoldenEyesStateRedLED_OFF71();
                if (btmGreenLightOn) {
                    GoldenEyesUtils.setGoldenEyesStateGreedLED71();
                }
                break;
            case R.id.btn_btm_yellow_light:
                btmGreenLightOn = false;
                btmRedLightOn = false;
                btmYellowLightOn = !btmYellowLightOn;
                GoldenEyesUtils.setGoldenEyesStateGreedLED_OFF71();
                GoldenEyesUtils.setGoldenEyesStateRedLED_OFF71();
                if (btmYellowLightOn) {
                    GoldenEyesUtils.setGoldenEyesStateRedLED71();
                    GoldenEyesUtils.setGoldenEyesStateGreedLED71();
                }
                break;
            case R.id.btn_open_door:
                topDoorOpen = !topDoorOpen;
                if (topDoorOpen) {
                    GoldenEyesUtils.setGoldenEyesOpenDoor();
                } else {
                    GoldenEyesUtils.setGoldenEyesCloseDoor();
                }
                break;
            case R.id.btn_openandclose_door:
//                GoldenEyesUtils.setGoldenEyesAutoOpenCloseDoor();
//                textCardId.setText(getScreenHeight(this) + ":" + getScreenWidth(this));
                startActivity(new Intent(TestDeviceActivity.this, DemoActivity.class));
                break;
        }
    }

    /**
     * 获取屏幕的宽
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}
