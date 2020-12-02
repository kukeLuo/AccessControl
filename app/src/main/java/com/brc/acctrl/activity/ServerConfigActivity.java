package com.brc.acctrl.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.mqtt.HttpConst;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.utils.StringUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ServerConfigActivity extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.tv_content_release)
    TextView tvContentRelease;
    @BindView(R.id.tv_content_sit)
    TextView tvContentSit;


    public final static String SP_APP_HOST_TYPE = "SP_APP_HOST_TYPE"; // 1-release 0-sit
    @BindView(R.id.tv_content_gateway)
    TextView tvContentGateway;
    private int hostType = 1;
    private int selectType = 1;
    private int gatewayType = 1;

    @Override
    public int getLayoutId() {
        return R.layout.activity_server_config;
    }

    @Override
    public void initViews() {
        selectType = hostType =gatewayType= SPUtil.getInstance().getValue(SP_APP_HOST_TYPE, 1);
        tvContentRelease.setText(StringUtil.CpStrStr2Para(R.string.str_server_config_content,
                HttpConst.BASE_URL_PRODUCT, HttpConst.BASE_MQTT_PRODUCT));
        tvContentSit.setText(StringUtil.CpStrStr2Para(R.string.str_server_config_content,
                HttpConst.BASE_URL_SIT, HttpConst.BASE_MQTT_SIT));

        tvContentGateway.setText(StringUtil.CpStrStr2Para(R.string.str_server_config_content,
                HttpConst.BASE_URL_GATEWAY, HttpConst.BASE_MQTT_GATEWAY));

        resetTextMode();
    }

    private void resetTextMode() {
        tvContentRelease.setActivated(selectType == 1);
        tvContentSit.setActivated(selectType == 0);
        tvContentGateway.setActivated(selectType == 2);
    }

    @OnClick({R.id.img_back, R.id.tv_save, R.id.layout_release, R.id.layout_sit, R.id.layout_gateway})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_save:
                if (selectType != hostType) {
                    RetrofitConfig.resetRetrofit();
                    SPUtil.getInstance().setValue(SP_APP_HOST_TYPE, selectType);
//                    SPUtil.getInstance().setValue(SPUtil.HAS_CONFIG_SERVER, true);
                    Constants.backToSplashPage = true;
                    finish();
                }
                break;
            case R.id.layout_release:
                selectType = 1;
                resetTextMode();
                break;
            case R.id.layout_sit:
                selectType = 0;
                resetTextMode();
                break;
            case R.id.layout_gateway:
                selectType = 2;
                resetTextMode();
                break;
        }
    }

}
