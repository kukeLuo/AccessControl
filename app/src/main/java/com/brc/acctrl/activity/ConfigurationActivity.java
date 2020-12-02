package com.brc.acctrl.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.utils.TimePickerUtil;
import com.brc.acctrl.view.ThresholdSeekBar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfigurationActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.tv_network)
    TextView tvNetwork;
    @BindView(R.id.tv_door_open_time)
    TextView tvDoorOpenTime;
    @BindView(R.id.layout_door_open)
    RelativeLayout layoutDoorOpen;
    @BindView(R.id.switch_livecheck)
    ImageView switchLivecheck;
    @BindView(R.id.layout_livecheck)
    RelativeLayout layoutLivecheck;
    @BindView(R.id.switch_display_name)
    ImageView switchDisplayName;
    @BindView(R.id.layout_display_name)
    RelativeLayout layoutDisplayName;
    @BindView(R.id.switch_openlight)
    ImageView switchOpenlight;
    @BindView(R.id.layout_toplight)
    RelativeLayout layoutToplight;
    @BindView(R.id.tv_light_start_time)
    TextView tvLightStartTime;
    @BindView(R.id.layout_light_starttime)
    RelativeLayout layoutLightStarttime;
    @BindView(R.id.tv_light_end_time)
    TextView tvLightEndTime;
    @BindView(R.id.layout_light_endtime)
    RelativeLayout layoutLightEndtime;
    @BindView(R.id.tv_threshold)
    TextView tvThreshold;
    @BindView(R.id.rb_regular_opening)
    RadioButton rbRegularOpening;
    @BindView(R.id.rb_timing_identification_on)
    RadioButton rbTimingIdentificationOn;
    @BindView(R.id.rb_long_guan)
    RadioButton rbLongGuan;
    @BindView(R.id.rg_fill_light)
    RadioGroup rgFillLight;
    @BindView(R.id.tv_start_time)
    TextView tvNewLightStartTime;
    @BindView(R.id.rl_light_starttime)
    RelativeLayout rlLightStartTime;
    @BindView(R.id.tv_end_time)
    TextView tvNewLightEndTime;
    @BindView(R.id.rl_light_endtime)
    RelativeLayout rlLightEndTime;
    @BindView(R.id.tv_threshold_size)
    TextView tvThresholdSize;
    @BindView(R.id.tv_voice)
    TextView tvVoice;
    @BindView(R.id.tv_voice_size)
    TextView tvVoiceSize;


    private int trySelectDoorTime = 0;
    private int openDoorTime = 0;
    private int voiceSize=30;
    private boolean shouldSupportLiveCheck = true;
    private ArrayList<TextView> doorOpenTexts = new ArrayList<>();
    private final static int[] DoorOpenTimeText = {R.id.tv_time_2, R.id.tv_time_3, R.id.tv_time_4,
            R.id.tv_time_5, R.id.tv_time_6, R.id.tv_time_7, R.id.tv_time_8, R.id.tv_time_9};

    private boolean shouldSupportOpenFillLight = false;
    private boolean shouldSupportOpenLight = true;
    private boolean shouldSupportDisplayName = true;
    private String fillLightStatus = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_configuration;
    }

    @Override
    public void initViews() {
        shouldSupportLiveCheck = SPUtil.getInstance().getValue(SPUtil.SUPPORT_LIVECHECK, false);
        shouldSupportOpenFillLight = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_FILL_LIGHTS, false);
        shouldSupportOpenLight = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_LIGHTS, true);
        shouldSupportDisplayName = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_NAME, true);
        switchOpenlight.setActivated(shouldSupportOpenLight);
        switchLivecheck.setActivated(shouldSupportLiveCheck);
        switchDisplayName.setActivated(shouldSupportDisplayName);
        trySelectDoorTime = openDoorTime = SPUtil.getInstance().getValue(SPUtil.DOOR_OPEN_TIME, 5);
        tvDoorOpenTime.setText(StringUtil.CpStrIntPara(R.string.str_setting_door_open_time_data, openDoorTime));
        tvThresholdSize.setText(Constants.THRESH_DETECT_FACE + "");
        voiceSize=SPUtil.getInstance().getValue(SPUtil.DEFAULT_VOLUME_SIZE,30);
        tvVoiceSize.setText(voiceSize+"");
        showTopLightLayout();
        showFillTopLightLayout();


        rgFillLight.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_regular_opening://定时常开
                        shouldSupportOpenFillLight = true;
                        //缓存顶灯状态定时常开
                        SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_FILL_LIGHTS, true);
                        SPUtil.getInstance().setValue(SPUtil.SHOW_FILL_LIGHTS_STATUS, Constants.TIMING_ON);
                        showFillTopLightLayout();
                        break;
                    case R.id.rb_timing_identification_on://定时识别开
                        shouldSupportOpenFillLight = true;
                        //缓存顶灯状态定时识别开
                        SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_FILL_LIGHTS, true);
                        SPUtil.getInstance().setValue(SPUtil.SHOW_FILL_LIGHTS_STATUS, Constants.INDENTIFY_ON);
                        showFillTopLightLayout();
                        break;
                    case R.id.rb_long_guan://常关
                        shouldSupportOpenFillLight = false;
                        //缓存顶灯状态常关
                        SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_FILL_LIGHTS, false);
                        SPUtil.getInstance().setValue(SPUtil.SHOW_FILL_LIGHTS_STATUS, Constants.OFTEN_OFF);
                        showFillTopLightLayout();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @OnClick({R.id.img_back, R.id.tv_network, R.id.layout_door_open, R.id.layout_livecheck, R.id.layout_display_name,
            R.id.tv_threshold, R.id.layout_toplight, R.id.rl_light_starttime, R.id.rl_light_endtime,R.id.tv_voice,
            R.id.layout_light_endtime, R.id.layout_light_starttime})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Constants.initOpenTopLights();
                finish();
                break;
            case R.id.tv_network:
                gotoActivity(NetworkConfigActivity.class);
                break;
            case R.id.layout_door_open:
                showOpenDoorTimeDlg();
                break;
            case R.id.layout_livecheck:
                Constants.backToSplashPage = true;
                shouldSupportLiveCheck = !shouldSupportLiveCheck;
                switchLivecheck.setActivated(shouldSupportLiveCheck);
                SPUtil.getInstance().setValue(SPUtil.SUPPORT_LIVECHECK, shouldSupportLiveCheck);
                break;
            case R.id.tv_threshold:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog dialog = builder
                        .setView(R.layout.dialog_threshold)
                        .create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
                ThresholdSeekBar seekbar = dialog.getWindow().findViewById(R.id.seekbar);
                seekbar.setProgress(Constants.THRESH_DETECT_FACE);
                dialog.getWindow().findViewById(R.id.btn_threshold).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.i("voice threshold：" + seekbar.getmTitleText());
                        if (!TextUtils.isEmpty(seekbar.getmTitleText())) {
                            int thresholdInt = Integer.parseInt(seekbar.getmTitleText());
                            Constants.THRESH_DETECT_FACE = thresholdInt;
                            tvThresholdSize.setText(seekbar.getmTitleText());
                        }
                        dialog.dismiss(); //取消对话框
                    }
                });
                break;
            case R.id.tv_voice:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                final AlertDialog alertDialog= alertBuilder
                        .setView(R.layout.dialog_voice)
                        .create();
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);
                ThresholdSeekBar thresholdSeekBar = alertDialog.getWindow().findViewById(R.id.seekbar);
                thresholdSeekBar.setProgress(SPUtil.getInstance().getValue(SPUtil.DEFAULT_VOLUME_SIZE, 30));
                alertDialog.getWindow().findViewById(R.id.btn_threshold).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.i("face threshold：" + thresholdSeekBar.getmTitleText());
                        if (!TextUtils.isEmpty(thresholdSeekBar.getmTitleText())) {
                            int thresholdInt = Integer.parseInt(thresholdSeekBar.getmTitleText());
                            SPUtil.getInstance().setValue(SPUtil.DEFAULT_VOLUME_SIZE,thresholdInt);
                            tvVoiceSize.setText(thresholdSeekBar.getmTitleText());
                            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            CommonUtil.detectSound(mAudioManager);
                        }
                        alertDialog.dismiss(); //取消对话框
                    }
                });
                break;
            case R.id.layout_toplight:
                //设置顶灯打开时间
                shouldSupportOpenLight = !shouldSupportOpenLight;
                //缓存是否打开顶灯状态
                SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_LIGHTS, shouldSupportOpenLight);
                showTopLightLayout();
                break;
            case R.id.layout_display_name:
                shouldSupportDisplayName = !shouldSupportDisplayName;
                //缓存是否显示名字状态
                SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_NAME, shouldSupportDisplayName);
                switchDisplayName.setActivated(shouldSupportDisplayName);
                break;
            case R.id.layout_light_starttime:
                int startTime = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_LIGHTS_STARTTIME, 1080);
                TimePickerUtil.showTimerPickerDialog(this, startTime / 60, startTime % 60, new TimePickerUtil.OnTimerPickerListener() {
                    @Override
                    public void onConfirm(int hourOfDay, int minute) {
                        tvLightStartTime.setText(formatTimeStr(hourOfDay, minute));
                        SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_LIGHTS_STARTTIME, hourOfDay * 60 + minute);
                    }
                });
                break;
            case R.id.layout_light_endtime:
                int endTime = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_LIGHTS_ENDTIME, 480);
                TimePickerUtil.showTimerPickerDialog(this, endTime / 60, endTime % 60, new TimePickerUtil.OnTimerPickerListener() {
                    @Override
                    public void onConfirm(int hourOfDay, int minute) {
                        tvLightEndTime.setText(formatTimeStr(hourOfDay, minute));
                        SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_LIGHTS_ENDTIME, hourOfDay * 60 + minute);
                    }
                });
                break;
            case R.id.rl_light_starttime:
                int lightstartTime = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_LIGHTS_STARTTIME, 1080);
                TimePickerUtil.showTimerPickerDialog(this, lightstartTime / 60, lightstartTime % 60, new TimePickerUtil.OnTimerPickerListener() {
                    @Override
                    public void onConfirm(int hourOfDay, int minute) {
                        tvNewLightStartTime.setText(formatTimeStr(hourOfDay, minute));
                        SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_LIGHTS_STARTTIME, hourOfDay * 60 + minute);
                    }
                });
                break;
            case R.id.rl_light_endtime:
                int lightendTime = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_LIGHTS_ENDTIME, 360);
                TimePickerUtil.showTimerPickerDialog(this, lightendTime / 60, lightendTime % 60, new TimePickerUtil.OnTimerPickerListener() {
                    @Override
                    public void onConfirm(int hourOfDay, int minute) {
                        tvNewLightEndTime.setText(formatTimeStr(hourOfDay, minute));
                        SPUtil.getInstance().setValue(SPUtil.SUPPORT_SHOW_LIGHTS_ENDTIME, hourOfDay * 60 + minute);
                    }
                });
                break;
            default:
                break;
        }
    }

    private void gotoActivity(Class<?> activityClassName) {
        startActivity(new Intent(ConfigurationActivity.this,
                activityClassName));
    }

    private void showOpenDoorTimeDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_seekbar, null);
        doorOpenTexts.clear();
        for (int textIdx = 0; textIdx < DoorOpenTimeText.length; textIdx++) {
            TextView textView = (TextView) view.findViewById(DoorOpenTimeText[textIdx]);
            textView.setOnClickListener(new TimeTextClickListener(textIdx + 2));
            doorOpenTexts.add(textView);
        }

        doorOpenTexts.get(openDoorTime - 2).setActivated(true);
        builder.setView(view)
                .setTitle(R.string.str_setting_door_open_time_dlgtitle)
                .setCancelable(false)
                .setPositiveButton(R.string.str_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        openDoorTime = trySelectDoorTime;
                        tvDoorOpenTime.setText(StringUtil.CpStrIntPara(R.string.str_setting_door_open_time_data, openDoorTime));
                    }
                })
                .setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        trySelectDoorTime = openDoorTime;
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * 显示设置开始时间和结束时间的ui
     */
    private void showTopLightLayout() {
        switchOpenlight.setActivated(shouldSupportOpenLight);
        if (shouldSupportOpenLight) {
            //显示布局
            layoutLightStarttime.setVisibility(View.VISIBLE);
            layoutLightEndtime.setVisibility(View.VISIBLE);
            tvLightStartTime.setText(formatTimeStr(Constants.dayOpenLightMinuteStart));
            tvLightEndTime.setText(formatTimeStr(Constants.dayOpenLightMinuteEnd));
        } else {
            //隐藏布局
            layoutLightStarttime.setVisibility(View.GONE);
            layoutLightEndtime.setVisibility(View.GONE);
        }
    }

    /**
     * 显示设置补光灯开始时间和结束时间的ui
     */
    private void showFillTopLightLayout() {
        fillLightStatus = SPUtil.getInstance().getValue(SPUtil.SHOW_FILL_LIGHTS_STATUS, Constants.OFTEN_OFF);
        switch (fillLightStatus) {
            case Constants.TIMING_ON:
                rbRegularOpening.setChecked(true);
                rbTimingIdentificationOn.setChecked(false);
                rbLongGuan.setChecked(false);
                break;
            case Constants.INDENTIFY_ON:
                rbTimingIdentificationOn.setChecked(true);
                rbRegularOpening.setChecked(false);
                rbLongGuan.setChecked(false);
                break;
            case Constants.OFTEN_OFF:
                rbLongGuan.setChecked(true);
                rbTimingIdentificationOn.setChecked(false);
                rbRegularOpening.setChecked(false);
                break;
            default:
                break;
        }
        if (shouldSupportOpenFillLight) {
            rlLightStartTime.setVisibility(View.VISIBLE);
            rlLightEndTime.setVisibility(View.VISIBLE);
            tvNewLightStartTime.setText(formatTimeStr(Constants.dayOpenLightMinuteStart));
            tvNewLightEndTime.setText(formatTimeStr(Constants.dayOpenLightMinuteEnd));
        } else {
            rlLightStartTime.setVisibility(View.GONE);
            rlLightEndTime.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    private class TimeTextClickListener implements View.OnClickListener {
        private int timeIdx = 0;

        public TimeTextClickListener(int idx) {
            timeIdx = idx;
        }

        @Override
        public void onClick(View view) {
            for (TextView singleView : doorOpenTexts) {
                singleView.setActivated(false);
            }

            view.setActivated(true);
            trySelectDoorTime = timeIdx;
        }
    }

    private String formatTimeStr(int timeMinute) {
        return String.format("%1$02d:%2$02d", timeMinute / 60, timeMinute % 60);
    }

    private String formatTimeStr(int hour, int minute) {
        return String.format("%1$02d:%2$02d", hour, minute);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SPUtil.getInstance().setValue(SPUtil.DOOR_OPEN_TIME, openDoorTime);
    }


}
