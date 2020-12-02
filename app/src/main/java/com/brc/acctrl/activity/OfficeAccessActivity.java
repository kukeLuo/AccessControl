package com.brc.acctrl.activity;

import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.brc.acctrl.R;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.glide.GlideLoadUtil;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.view.TexturePreviewView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

public class OfficeAccessActivity extends BaseRGBCameraActivity {
    @BindView(R.id.preview_view)
    TexturePreviewView previewView;
    @BindView(R.id.texture_view)
    TextureView textureView;
    @BindView(R.id.videoview)
    VideoView videoView;
    @BindView(R.id.view_btm_mask)
    View viewBtmMask;
    @BindView(R.id.img_user_avatar)
    ImageView imgUserAvatar;
    @BindView(R.id.tv_scan_suc)
    TextView tvScanSuc;
    @BindView(R.id.layout_scan_suc)
    LinearLayout layoutScanSuc;
    @BindView(R.id.layout_scan_err)
    LinearLayout layoutScanErr;
    @BindView(R.id.tv_enter_suc_type)
    TextView tvEnterSucType;
    @BindView(R.id.layout_pwd_card_suc)
    LinearLayout layoutPwdCardSuc;
    @BindView(R.id.layout_camera)
    RelativeLayout layoutCamera;
    @BindView(R.id.tv_err)
    TextView tvErr;
    @BindView(R.id.tv_datetime)
    TextView tvDatetime;
    @BindView(R.id.tv_info_title)
    TextView tvInfoTitle;
    @BindView(R.id.tv_info_desc)
    TextView tvInfoDesc;
    @BindView(R.id.layout_card)
    LinearLayout layoutCard;
    @BindView(R.id.img_setting)
    ImageView imgSetting;
    @BindView(R.id.img_cloud)
    ImageView imgCloud;
    @BindView(R.id.tv_temperature)
    TextView tvTemperature;
    @BindView(R.id.tv_pm_value)
    TextView tvPmValue;
    @BindView(R.id.tv_status_pm)
    TextView tvStatusPm;
    @BindView(R.id.layout_btm_boss)
    RelativeLayout layoutBtmBoss;
    @BindView(R.id.tv_info_descmore)
    TextView tvInfoDescmore;
    @BindView(R.id.img_pm25_status)
    ImageView imgPm25Status;
    @BindView(R.id.layout_scan_door_open)
    LinearLayout layoutScanDoorOpen;

    @Override
    public int getLayoutId() {
        int mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
        if (mode == SPUtil.ACCESS_TYPE_BOSS) {
            return R.layout.activity_office;
        } else {
            return R.layout.activity_office_door;
        }
    }

    @Override
    public void initViews() {
        int mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
        if (mode == SPUtil.ACCESS_TYPE_BOSS) {
            tvInfoTitle.setGravity(Gravity.LEFT);
            tvInfoDesc.setGravity(Gravity.LEFT);
            layoutBtmBoss.setVisibility(View.VISIBLE);
            tvInfoDescmore.setVisibility(View.GONE);

            fetchWeather();
        } else if (mode == SPUtil.ACCESS_TYPE_OFFICE) {
            tvInfoTitle.setGravity(Gravity.CENTER);
            tvInfoDesc.setGravity(Gravity.CENTER);
            layoutBtmBoss.setVisibility(View.GONE);
//            layoutCard.setVisibility(View.GONE);

            shouldShowDescMore();
        }
        viewBtmMask.setVisibility(View.GONE);
        loadVideo(videoView);
        initCameraPreviewLayout(previewView, textureView);
        initSDK();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING) == SPUtil.ACCESS_TYPE_OFFICE) {
            shouldShowDescMore();
        }
    }

    @Override
    public TextView fetchTimeText() {
        return tvDatetime;
    }

    @OnClick({R.id.img_setting, R.id.tv_click_pwd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_setting:
                checkSettingAction();
                break;
            case R.id.tv_click_pwd:
                gotoEnterPWDActivity();
                break;
        }
    }


    @Override
    public int rawVideoResource() {
        return R.raw.office_loading_big;
    }

    @Override
    public View getInputSucView() {
        return layoutPwdCardSuc;
    }

    @Override
    public TextView getTextSucView() {
        return tvEnterSucType;
    }

    @Override
    public TextView getInfoDescMoreView() {
        return tvInfoDescmore;
    }

    @Override
    public TextView getInfoDescView() {
        return tvInfoDesc;
    }

    @Override
    public TextView getInfoTitleView() {
        return tvInfoTitle;
    }

    @Override
    public View getScanSucView() {
        return layoutScanSuc;
    }

    @Override
    public View getScanErrView() {
        return layoutScanErr;
    }

    @Override
    public ImageView getImageView() {
        int mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
        if (mode == SPUtil.ACCESS_TYPE_BOSS) {
            return imgUserAvatar;
        } else {
            return null;
        }
    }

    @Override
    public TextView getTextScanSucView() {
        return tvScanSuc;
    }

    @Override
    public TextView getErrTextView() {
        return tvErr;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvLightEvent(RefreshEvents.RefreshLightEvent event) {
        tvInfoTitle.setText(event.getLightValue() + "");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvWeatherEvent(RefreshEvents.WeatherEvent event) {
        GlideLoadUtil.getInstance().load(this, event.getWeather().iconUrl, imgCloud);
        tvTemperature.setText(event.getWeather().weatherTemp + "     " + event.getWeather().weatherDesc);
        tvPmValue.setText(event.getWeather().pm25 + "");
        if (event.getWeather().pm25 < 50) {
            imgPm25Status.setImageResource(R.drawable.icon_pm_good);
            tvStatusPm.setText(R.string.str_pm_good);
        } else if (event.getWeather().pm25 < 100) {
            imgPm25Status.setImageResource(R.drawable.icon_pm_normal);
            tvStatusPm.setText(R.string.str_pm_normal);
        } else {
            imgPm25Status.setImageResource(R.drawable.icon_pm_bad);
            tvStatusPm.setText(R.string.str_pm_bad);
        }
    }

    @Override
    public void showAlwaysOpenDoorUI() {
        layoutScanDoorOpen.setVisibility(isAlwaysOpenDoor ? View.VISIBLE : View.GONE);
    }
}
