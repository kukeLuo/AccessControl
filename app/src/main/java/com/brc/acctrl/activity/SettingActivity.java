package com.brc.acctrl.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.db.UserDatabase;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.utils.AccessFailUtil;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.FaceSDKUtil;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;
import com.brc.acctrl.utils.TimePickerUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_about)
    TextView tvAbout;
    @BindView(R.id.tv_chg_pwd)
    TextView tvChgPwd;
    @BindView(R.id.tv_update)
    TextView tvUpdate;
    @BindView(R.id.switch_reboot)
    ImageView switchReboot;
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
    private boolean shouldSupportRebootTimely = true;
    private Handler rebootHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
//            GoldenEyesUtils.reboot();
            CommonUtil.reboot();
        }
    };


    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void initViews() {


        shouldSupportRebootTimely = SPUtil.getInstance().getValue(SPUtil.SUPPORT_REBOOT_TIMELY, true);
        switchReboot.setActivated(shouldSupportRebootTimely);




    }




    @OnClick({R.id.img_back, R.id.tv_about,  R.id.tv_chg_pwd,
            R.id.tv_update, R.id.tv_reboot,  R.id.tv_reset_face, R.id.tv_upload_fail, R.id.layout_time_reboot
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Constants.initOpenTopLights();
                finish();
                break;
            case R.id.tv_about:
                gotoActivity(AboutActivity.class);
                break;
            case R.id.tv_chg_pwd:
                Intent chgIntent = new Intent(SettingActivity.this, ChgPwdActivity.class);
                chgIntent.putExtra(ChgPwdActivity.CHANGE_TYPE, ChgPwdActivity.SETTING_TYPE);
                startActivity(chgIntent);
                break;
            case R.id.tv_update:
                gotoActivity(UpdateVersionActivity.class);
                break;
            case R.id.tv_reboot:
                // close watch dog, show dialog
                CommonUtil.showToast(this, R.string.str_reboot_after_3s);
                GoldenEyesUtils.setGoldenEyesWatchdog(GoldenEyesUtils.WACTHDOG_OPEN);
                rebootHandler.sendEmptyMessageDelayed(0, 3000L);
                break;
            case R.id.layout_time_reboot:
                shouldSupportRebootTimely = !shouldSupportRebootTimely;
                switchReboot.setActivated(shouldSupportRebootTimely);
                SPUtil.getInstance().setValue(SPUtil.SUPPORT_REBOOT_TIMELY, shouldSupportRebootTimely);
                break;

            case R.id.tv_reset_face:
                showDeleteAllDlg();
                break;
            case R.id.tv_upload_fail:
                Calendar dayStartCalendar = Calendar.getInstance();
                AccessFailUtil.checkUnuploadRecord(this,dayStartCalendar.getTimeInMillis());
                break;
            default:
                break;
        }
    }

    private void showDeleteAllDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.str_setting_delete_all);
        builder.setMessage(R.string.str_setting_delete_content);
        builder.setPositiveButton(R.string.str_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UserDatabase.getInstance().getAccessUserDao().deleteAll();
                        FaceSDKUtil.getInstance().deleteAllJpgFiles();
//                        RecordDatabase.getInstance().getAccessHistoryDao().deleteAll();
//                        MeetingDatabase.getInstance().getMeetingDao().deleteAll();
                        EventBus.getDefault().post(new RefreshEvents.RefreshFaceEvent(true));
                    }
                }).start();
            }
        });
        builder.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void gotoActivity(Class<?> activityClassName) {
        startActivity(new Intent(SettingActivity.this,
                activityClassName));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
