package com.brc.acctrl.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.brc.acctrl.R;

import butterknife.BindView;
import butterknife.OnClick;

public class NetworkConfigActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_ip)
    TextView tvIp;
    @BindView(R.id.tv_server)
    TextView tvServer;

    @Override
    public int getLayoutId() {
        return R.layout.activity_network_config;
    }

    @Override
    public void initViews() {

    }

    @OnClick({R.id.img_back, R.id.tv_ip, R.id.tv_server})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_ip:
                Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
//                startActivity(new Intent(NetworkConfigActivity.this, IPConfigActivity.class));
                break;
            case R.id.tv_server:
                startActivity(new Intent(NetworkConfigActivity.this, ServerConfigActivity.class));
                break;
        }
    }
}
