package com.brc.acctrl.activity;

import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.mqtt.MqttService;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingFunctionActivity extends BaseActivity {
    @BindView(R.id.tv_mode)
    TextView tvMode;
    @BindView(R.id.tv_chg_mode)
    TextView tvChgMode;
    @BindView(R.id.tv_room_title)
    TextView tvRoomTitle;
    @BindView(R.id.tv_room_desc)
    TextView tvRoomDesc;
    @BindView(R.id.tv_room_desc_more)
    TextView tvRoomDescMore;
    @BindView(R.id.layout_title)
    RelativeLayout layoutTitle;
    @BindView(R.id.tv_user_list)
    TextView tvUserList;
    @BindView(R.id.tv_enter_record)
    TextView tvEnterRecord;
    @BindView(R.id.tv_setting)
    TextView tvSetting;
    @BindView(R.id.tv_enter)
    TextView tvEnter;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_error_msg)
    TextView tvErrorMsg;

    private String[] arrayTitles;
    private int mode, oriMode;

    private final static int REQUEST_MODE = 1;
    private final static int REQUEST_MODIFY_INFO = 2;

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting_function;
    }

    @Override
    public void initViews() {
        arrayTitles = getResources().getStringArray(R.array.mode_machine);
        oriMode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, 0);
        setModeStyle();
        setRoomTitleDesc();
    }

    @OnClick({R.id.tv_chg_mode, R.id.layout_title, R.id.tv_user_list,
            R.id.tv_enter_record, R.id.tv_check_opendoor,R.id.tv_configuration,
            R.id.tv_setting, R.id.tv_enter, R.id.tv_cancel,R.id.tv_identification_log})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_chg_mode:
                gotoActivityResult(SettingModeActivity.class, REQUEST_MODE);
                break;
            case R.id.layout_title:
                gotoActivityResult(SettingBaseInfoActivity.class,
                        REQUEST_MODIFY_INFO);
                break;
            case R.id.tv_user_list:
                gotoActivity(SettingUserListActivity.class);
                break;
            case R.id.tv_enter_record:
                gotoActivity(RecordHistoryActivity.class);
                break;
            case R.id.tv_setting:
                gotoActivity(SettingActivity.class);
                break;
            case R.id.tv_identification_log:
                gotoActivity(IdentificationLogActivity.class);
                break;
            case R.id.tv_configuration:
                gotoActivity(ConfigurationActivity.class);
                break;
            case R.id.tv_enter:
                checkSave();
                break;
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_check_opendoor:
                GoldenEyesUtils.setGoldenEyesAutoOpenCloseDoor();
                break;
        }
    }

    private void checkSave() {
        if (!SPUtil.getInstance().getValue(SPUtil.HAS_CONFIG_NAME, false)) {
            tvErrorMsg.setVisibility(View.VISIBLE);
            tvErrorMsg.setText(R.string.str_err_device_name);
            return;
        }

//        if (!SPUtil.getInstance().getValue(SPUtil.HAS_CONFIG_SERVER, false)) {
//            tvErrorMsg.setVisibility(View.VISIBLE);
//            tvErrorMsg.setText(R.string.str_err_device_server);
//            return;
//        }

        if (Constants.backToSplashPage || oriMode != mode) {
            try {
                Intent intent = new Intent(getApplicationContext(), MqttService.class);
                stopService(intent);
            } catch (Exception e) {

            }

            EventBus.getDefault().post(new RefreshEvents.FinishPageEvent());
            startActivity(new Intent(SettingFunctionActivity.this,
                    SplashActivity.class));

            Constants.backToSplashPage = false;
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        tvErrorMsg.setVisibility(View.GONE);
    }

    private void gotoActivity(Class<?> activityClassName) {
        startActivity(new Intent(SettingFunctionActivity.this,
                activityClassName));
    }

    private void gotoActivityResult(Class<?> activityClassName,
                                    int requestCode) {
        Intent intent = new Intent(SettingFunctionActivity.this,
                activityClassName);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_MODE) {
                setModeStyle();
            } else if (requestCode == REQUEST_MODIFY_INFO) {
                setRoomTitleDesc();
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setModeStyle() {
        mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, 0);
        tvMode.setText(arrayTitles[mode]);
    }

    private void setRoomTitleDesc() {
        String modeTitle =
                SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_TITLE
                        , getString(R.string.str_mode_default_title));
        tvRoomTitle.setText(modeTitle);
        String modeDesc =
                SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_DESC
                        , getString(R.string.str_mode_default_desc));
        tvRoomDesc.setText(modeDesc);
        String modeDescMore =
                SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_INFO_DESC_MORE
                        , getString(R.string.str_mode_default_descmore));
        tvRoomDescMore.setText(modeDescMore);
    }
}
