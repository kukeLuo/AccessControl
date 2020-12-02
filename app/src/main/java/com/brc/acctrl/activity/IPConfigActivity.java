package com.brc.acctrl.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.listener.IPLayoutClickListener;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.view.CommonEditIPView;

import butterknife.BindView;
import butterknife.OnClick;

public class IPConfigActivity extends BaseActivity implements IPLayoutClickListener {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.view_ip_dynamic)
    CommonEditIPView viewIpDynamic;
    @BindView(R.id.view_ip_static)
    CommonEditIPView viewIpStatic;
    @BindView(R.id.view_ip_config)
    CommonEditIPView viewIpConfig;
    @BindView(R.id.view_ip_gateway)
    CommonEditIPView viewIpGateway;
    @BindView(R.id.view_ip_mask)
    CommonEditIPView viewIpMask;

    // true: dynamic false:static
    private static final String SP_IP_TYPE = "SP_IP_TYPE";
    private static final String SP_IP_STATIC = "SP_IP_STATIC";
    private static final String SP_IP_STATIC_GATEWAY = "SP_IP_STATIC_GATEWAY";
    private static final String SP_IP_STATIC_MASK = "SP_IP_STATIC_MASK";

    boolean isDynamicType = false;

    @Override
    public int getLayoutId() {
        return R.layout.activity_ip_config;
    }

    @Override
    public void initViews() {
        isDynamicType = SPUtil.getInstance().getValue(SP_IP_TYPE, true);
        String curNetworkIP = NetworkUtil.getEthernetIp();
        viewIpDynamic.setIPContent(curNetworkIP);
        viewIpDynamic.setEnable(false);
        if (isDynamicType) {
            activateIPView(true);
            viewIpConfig.setIPContent(null);
            viewIpGateway.setIPContent(null);
            viewIpMask.setIPContent(null);
        } else {
            activateIPView(false);
            String strStaticIP = SPUtil.getInstance().getValue(SP_IP_STATIC,
                    null);
            String strStaticIPGW =
                    SPUtil.getInstance().getValue(SP_IP_STATIC_GATEWAY, null);
            String strStaticIPMask =
                    SPUtil.getInstance().getValue(SP_IP_STATIC_MASK, null);
            viewIpConfig.setIPContent(strStaticIP);
            viewIpGateway.setIPContent(strStaticIPGW);
            viewIpMask.setIPContent(strStaticIPMask);
        }

        viewIpDynamic.setClickListener(this);
        viewIpStatic.setClickListener(this);
        viewIpConfig.setClickListener(this);
        viewIpGateway.setClickListener(this);
        viewIpMask.setClickListener(this);
    }

    @OnClick({R.id.img_back, R.id.tv_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_save:
                if (isDynamicType) {
                    SPUtil.getInstance().setValue(SP_IP_TYPE, true);
                    SPUtil.getInstance().remove(SP_IP_STATIC);
                    SPUtil.getInstance().remove(SP_IP_STATIC_GATEWAY);
                    SPUtil.getInstance().remove(SP_IP_STATIC_MASK);
                    finish();
                } else {
                    String ipAddr = viewIpConfig.getInputIP();
                    if (TextUtils.isEmpty(ipAddr)) {
                        return;
                    }
                    String gatewayAddr = viewIpConfig.getInputIP();
                    if (TextUtils.isEmpty(gatewayAddr)) {
                        return;
                    }
                    String maskAddr = viewIpConfig.getInputIP();
                    if (TextUtils.isEmpty(maskAddr)) {
                        return;
                    }

                    checkInputAddrIsValid(ipAddr, gatewayAddr, maskAddr);
                    break;
                }
            default:
                break;
        }
    }

    private void checkInputAddrIsValid(String ipAddr, String
            gatewayAddr, String maskAddr) {

        // check set ip
        SPUtil.getInstance().setValue(SP_IP_TYPE, false);
        SPUtil.getInstance().setValue(SP_IP_STATIC, ipAddr);
        SPUtil.getInstance().setValue(SP_IP_STATIC_GATEWAY, gatewayAddr);
        SPUtil.getInstance().setValue(SP_IP_STATIC_MASK, maskAddr);
    }

    @Override
    public void onLayoutClickListener(View view) {
        if (view == viewIpDynamic) {
            isDynamicType = true;
            activateIPDynamic();
        } else {
            isDynamicType = false;
            activateIPStatic();
        }
    }

    private void activateIPStatic() {
        activateIPView(false);
    }

    private void activateIPDynamic() {
        activateIPView(true);
    }

    private void activateIPView(boolean dynamic) {
        viewIpDynamic.setActivate(dynamic);
        viewIpStatic.setActivate(!dynamic);
        viewIpConfig.setActivate(!dynamic);
        viewIpGateway.setActivate(!dynamic);
        viewIpMask.setActivate(!dynamic);
    }
}
